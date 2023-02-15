package com.example.service;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.domian.recordInfo;
import com.example.util.WaterMarkUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.service.HCNetSDK.*;

/**
 * @ClassName: initSdk
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 11:25
 * @Version: 1.0
 **/
@Slf4j(topic = "hiksdk")
@Service
public class hikSdkClinetImpl implements hikSdkClinet {
    @Resource
    minioService minio;
    @Resource
    DVRLogin dvrLogin;

    private static HCNetSDK hCNetSDK;
    // 报警回调函数实现
    public static HCNetSDK.FMSGCallBack_V31 fMSFCallBack_V31;
    private static Map<Integer, recordInfo> user_real_Map = new HashMap<>();

    /**
     * @描述 初始化sdk
     * @参数 []
     * @返回值 void
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:13
     * @修改人和其它信息
     */
    @Override

    public void initHCNetSDK() {
        try {
            String WIN_PATH = System.getProperty("user.dir") + File.separator + "lib" + File.separator + "HCNetSDK.dll";
            String LINUX_PATH = System.getProperty("user.dir") + File.separator + "hcnet" + File.separator + "libhcnetsdk.so";
            if (Platform.isWindows()) {
                hCNetSDK = (HCNetSDK) Native.loadLibrary(WIN_PATH, HCNetSDK.class);
                if (Platform.isLinux()) {
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(LINUX_PATH, HCNetSDK.class);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * @描述 注册登录
     * @参数 [dvrLogin]
     * @返回值 java.lang.Integer
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:12
     * @修改人和其它信息
     */
    @Override
    public Integer login(DVRLogin dvrLogin) {

        // 初始化
        if (!hCNetSDK.NET_DVR_Init()) {
            log.error("SDK初始化失败");
        }
        String m_sDeviceIP = dvrLogin.getIp();
        String m_sUsername = dvrLogin.getUserName();
        String m_sPassword = dvrLogin.getPassword();
        short m_sPort = dvrLogin.getPort();
        //设置连接时间与重连时间
        hCNetSDK.NET_DVR_SetConnectTime(2000, 1);
        hCNetSDK.NET_DVR_SetReconnect(100000, true);
        //设备信息, 输出参数
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        // 注册设备-登录参数，包括设备地址、登录用户、密码等
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());
        m_strLoginInfo.wPort = m_sPort;
        //是否异步登录：0- 否，1- 是  windowsSDK里是true和false
        m_strLoginInfo.bUseAsynLogin = false;
        m_strLoginInfo.write();
        int lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID < 0) {
            //释放SDK资源
            hCNetSDK.NET_DVR_Cleanup();
            log.error("登录失败");
        }
        // 设置报警回调函数，建立报警上传通道（启用布防）
        int lAlarmHandle = setupAlarmChan(lUserID, -1);
        return lUserID;
    }

    /**
     * @描述 控制上
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:14
     * @修改人和其它信息
     */
    @Override
    public boolean controlUp(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_UP, dwStop, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试" + code);
        }
        return bool;
    }


    /**
     * @描述 控制下
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:18
     * @修改人和其它信息
     */
    public boolean controlDown(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_DOWN, dwStop, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试" + code);
        }
        return bool;
    }


    /**
     * @描述 控制左转
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlLeft(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_LEFT, dwStop, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试" + code);
        }
        return bool;
    }


    /**
     * @描述 控制右转
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlRight(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_RIGHT, dwStop, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试" + code);
        }
        return bool;
    }

    /**
     * @描述 控制焦距变大
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlZoomIn(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HCNetSDK.ZOOM_IN, dwStop);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }

    /**
     * @描述 控制焦距变小
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlZoomOut(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HCNetSDK.ZOOM_OUT, dwStop);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }


    /**
     * @描述 控制焦点前调
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlFocusNear(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HCNetSDK.FOCUS_NEAR, dwStop);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }


    /**
     * @描述 控制焦点后调
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:23
     * @修改人和其它信息
     */
    @Override
    public boolean controlFocusFar(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HCNetSDK.FOCUS_FAR, dwStop);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }


    /**
     * @描述 接通雨刷开关
     * @参数 [userId, channelNum, speed]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 8:40
     * @修改人和其它信息
     */
    @Override
    public boolean controlWiperPwron(Integer userId, Integer channelNum, Integer speed, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HCNetSDK.WIPER_PWRON, dwStop);
        if (!bool) {
            log.error("控制失败,请稍后重试");
        }
        return bool;
    }


    /**
     * 是否在线
     *
     * @param userId
     */
    @Override
    public Boolean isOnLine(Integer userId) {
        boolean isOnLine = hCNetSDK.NET_DVR_RemoteControl(userId, HCNetSDK.NET_DVR_CHECK_USER_STATUS, null, 0);
        return isOnLine;
    }


    /**
     * 设置预置点
     *
     * @param userId
     */
    @Override
    public boolean setPreset(Integer userId, Integer channelNum, Integer PresetIndex) {
        boolean bool = hCNetSDK.NET_DVR_PTZPreset_Other(userId, channelNum, SET_PRESET, PresetIndex);
        if (!bool) {
            log.error("预置点设置失败!" + "登录ID：" + userId + "通道号：" + channelNum + "预置点号：" + PresetIndex);
        }
        return bool;
    }

    /**
     * 转到预置点
     *
     * @param userId
     */
    @Override
    public boolean gotoPreset(Integer userId, Integer channelNum, Integer PresetIndex) {
        boolean bool = hCNetSDK.NET_DVR_PTZPreset_Other(userId, channelNum, GOTO_PRESET, PresetIndex);
        if (!bool) {
            log.error("预置点设置失败!" + "登录ID：" + userId + "通道号：" + channelNum + "预置点号：" + PresetIndex);
        }
        return bool;
    }

    /**
     * @return
     * @描述 获取ptz信息
     * @参数 [userId, channelNum]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:36
     * @修改人和其它信息
     */
    @Override
    public PTZ getPtz(Integer userId, Integer channelNum) {

        HCNetSDK.NET_DVR_PTZPOS m_ptzPosCurrent = new HCNetSDK.NET_DVR_PTZPOS();
        Pointer pioint = m_ptzPosCurrent.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        PTZ ptz = new PTZ();
        boolean bool = hCNetSDK.NET_DVR_GetDVRConfig(userId, HCNetSDK.NET_DVR_GET_PTZPOS, channelNum, pioint, m_ptzPosCurrent.size(), ibrBytesReturned);
        if (bool) {
            m_ptzPosCurrent.read();
            DecimalFormat df = new DecimalFormat("0.0");//设置保留位数
            //16进制转Integer后除10，保留小数点1位
            //实际显示的PTZ值是获取到的十六进制值的十分之一，
            //如获取的水平参数P的值是0x1750，实际显示的P值为175度；
            //获取到的垂直参数T的值是0x0789，实际显示的T值为78.9度；
            //获取到的变倍参数Z的值是0x1100，实际显示的Z值为110倍。
            String p = df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wPanPos)) / 10);
            String t = df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wTiltPos)) / 10);
            String z = df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wZoomPos)) / 10);
            log.info("T垂直参数为: " + p + "P水平参数为: " + t + "Z变倍参数为: " + z);
            ptz.setWPanPos(p);
            ptz.setWTiltPos(t);
            ptz.setWZoomPos(z);
        }
        return ptz;
    }

    /**
     * @描述 设置ptz信息
     * @参数 [userId, channelNum]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/17 16:36
     * @修改人和其它信息
     */
    @Override
    public boolean setPtz(Integer userId, Integer channelNum, PTZ ptz) {
        HCNetSDK.NET_DVR_PTZPOS m_ptzPosCurrent = new HCNetSDK.NET_DVR_PTZPOS();
        m_ptzPosCurrent.wAction = 1;
        m_ptzPosCurrent.wPanPos = (short) (Integer.parseInt(ptz.getWPanPos(), 16));
        m_ptzPosCurrent.wTiltPos = (short) (Integer.parseInt(ptz.getWTiltPos(), 16));
        m_ptzPosCurrent.wZoomPos = (short) (Integer.parseInt(ptz.getWZoomPos(), 16));
        Pointer point = m_ptzPosCurrent.getPointer();
        m_ptzPosCurrent.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_PTZPOS, channelNum, point, m_ptzPosCurrent.size());
        if (!bool) {
            int i = hCNetSDK.NET_DVR_GetLastError();
            log.error("错误码：" + i);
        }
        return bool;
    }

    /**
     * @描述 透雾开关
     * @参数 [userId, channelNum, enable]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 13:07
     * @修改人和其它信息
     */
    @Override
    public boolean controlDefogcfg(Integer userId, Integer channelNum, boolean enable) {
        HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX struCameraParam = new HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX();
        Pointer point = struCameraParam.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(userId, NET_DVR_GET_CCDPARAMCFG_EX, channelNum, point, struCameraParam.size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            log.error("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struCameraParam.read();
        log.info("是否开启透雾：" + struCameraParam.struDefogCfg.byMode);

        NET_DVR_DEFOGCFG defogcfg = new NET_DVR_DEFOGCFG();
        if (enable) {
            defogcfg.byMode = 2;//0-不启用 1-自动模式 2-常开模式
            defogcfg.byLevel = 100;//取值范围0-100
        } else {
            defogcfg.byMode = 0;//0-不启用 1-自动模式 2-常开模式
        }
        struCameraParam.struDefogCfg = defogcfg;
        struCameraParam.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_CCDPARAMCFG_EX, channelNum, point, struCameraParam.size());
        if (!bool) {
            log.error("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        log.info("设置透雾成功");
        return bool;
    }

    /**
     * @描述 红外开关
     * @参数 [userId, channelNum, enable]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 13:07
     * @修改人和其它信息
     */
    @Override
    public boolean controlInfrarecfg(Integer userId, Integer channelNum, boolean enable) {

        HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX struDayNigh = new HCNetSDK.NET_DVR_CAMERAPARAMCFG_EX();
        Pointer point = struDayNigh.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(userId, NET_DVR_GET_CCDPARAMCFG_EX, channelNum, point, struDayNigh.size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            log.error("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struDayNigh.read();
        log.info("是否开启夜视：" + struDayNigh.struDayNight.byDayNightFilterType);

        HCNetSDK.NET_DVR_DAYNIGHT daynight = new HCNetSDK.NET_DVR_DAYNIGHT();
        if (enable) {
            daynight.byDayNightFilterType = 1;//夜晚

        } else {
            daynight.byDayNightFilterType = 0;//白天
        }
        daynight.bySwitchScheduleEnabled = 1;
        daynight.byDayNightFilterTime = 60;
        struDayNigh.struDayNight = daynight;
        struDayNigh.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_CCDPARAMCFG_EX, channelNum, point, struDayNigh.size());
        if (!bool) {
            log.error("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        log.info("设置夜视成功");
        return bool;
    }

    /**
     * @描述 聚焦开关
     * @参数 [userId, channelNum, enable]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 13:07
     * @修改人和其它信息
     */
    @Override
    public boolean controlFocusMode(Integer userId, Integer channelNum, boolean enable) {
        HCNetSDK.NET_DVR_FOCUSMODE_CFG struFocusMode = new HCNetSDK.NET_DVR_FOCUSMODE_CFG();
        Pointer point = struFocusMode.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(userId, NET_DVR_GET_FOCUSMODECFG, channelNum, point, struFocusMode.size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            System.out.println("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struFocusMode.read();
        System.out.println("是否开启手动聚焦：" + struFocusMode.byFocusMode);

        if (enable) {
            struFocusMode.byFocusMode = 1;//手动聚焦
        } else {
            struFocusMode.byFocusMode = 2;//自动聚焦
        }
        struFocusMode.byFocusDefinitionDisplay = 1;
        struFocusMode.byFocusSpeedLevel = 3;
        struFocusMode.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_FOCUSMODECFG, channelNum, point, struFocusMode.size());
        if (!bool) {
            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("设置成功");
        return bool;
    }

    /**
     * @描述 云台加热开关
     * @参数 [userId, channelNum, enable]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 13:07
     * @修改人和其它信息
     */
    @Override
    public boolean controlPTHeateRpwron(Integer userId, Integer channelNum, boolean enable) {
        Integer dwStop;
        if (enable) {
            dwStop = 0;//开启
        } else {
            dwStop = 1;//关闭
        }
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, channelNum, HEATER_PWRON, dwStop);
        if (!bool) {
            log.error("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        log.info("设置云台加热成功");
        return bool;
    }

    /**
     * @描述 镜头加热开关
     * @参数 [userId, channelNum, enable]
     * @返回值 boolean
     * @创建人 刘苏义
     * @创建时间 2023/1/18 13:07
     * @修改人和其它信息
     */
    @Override
    public boolean controlCameraDeicing(Integer userId, Integer channelNum, boolean enable) {
        HCNetSDK.NET_DVR_DEVSERVER_CFG struDeicing = new HCNetSDK.NET_DVR_DEVSERVER_CFG();
        Pointer point = struDeicing.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(userId, NET_DVR_GET_DEVSERVER_CFG, channelNum, point, struDeicing.size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            log.error("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struDeicing.read();
        log.info("是否开启除冰：" + struDeicing.byEnableDeicing);

        if (enable) {
            struDeicing.byEnableDeicing = 1;//开启
        } else {
            struDeicing.byEnableDeicing = 0;//关闭
        }
        struDeicing.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_DEVSERVER_CFG, channelNum, point, struDeicing.size());
        if (!bool) {
            log.error("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        log.info("设置镜头除冰成功");
        return bool;
    }

    /**
     * 截图 返给前端
     *
     * @param userId
     */
    @Override
    public void captureJPEGPicture(Integer userId, HttpServletResponse response) {
        HCNetSDK.NET_DVR_WORKSTATE_V30 devwork = new HCNetSDK.NET_DVR_WORKSTATE_V30();
        if (!hCNetSDK.NET_DVR_GetDVRWorkState_V30(userId, devwork)) {
            // 返回Boolean值，判断是否获取设备能力
            log.error("抓图失败，请稍后重试");
        }
        //图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        //设置图片分辨率
        jpeg.wPicSize = 0;
        //设置图片质量
        jpeg.wPicQuality = 0;
        IntByReference a = new IntByReference();
        //设置图片大小
        ByteBuffer jpegBuffer = ByteBuffer.allocate(1024 * 1024);
        // 抓图到内存，单帧数据捕获并保存成JPEG存放在指定的内存空间中
        boolean is = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(userId, 1, jpeg, jpegBuffer, 1024 * 1024, a);
        log.info("-----------这里开始图片存入内存----------" + is);

        ByteArrayInputStream in = new ByteArrayInputStream(jpegBuffer.array(), 0, a.getValue());
        OutputStream outputStream = null;
        try {
            //1、设置response 响应头 //设置页面不缓存,清空buffer
            response.reset();
            //字符编码
            response.setCharacterEncoding("UTF-8");
            //二进制传输数据
            response.setContentType("multipart/form-data");
            //设置响应头
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpeg", "UTF-8"));

            outputStream = response.getOutputStream();
            // LoginUser loginUser = LoginContext.me().getLoginUser();
            WaterMarkUtil.markImageByIO("", in, outputStream, null, "jpeg");
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("抓图失败，请稍后重试");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                log.error("抓图失败，请稍后重试");
            }
        }
        log.info("-----------处理完成截图数据----------");

    }

    /**
     * 截图 存服务器
     *
     * @param userId
     */
    public String picCutCate(Integer userId, Integer channelNum) {
        //图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        //设置图片分辨率
        jpeg.wPicSize = 0;
        //设置图片质量
        jpeg.wPicQuality = 0;
        IntByReference a = new IntByReference();
        //设置图片大小
        ByteBuffer jpegBuffer = ByteBuffer.allocate(1024 * 1024);

        // 抓图到内存，单帧数据捕获并保存成JPEG存放在指定的内存空间中
        log.info("-----------这里开始封装 NET_DVR_CaptureJPEGPicture_NEW---------");
        boolean is = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(userId, channelNum, jpeg, jpegBuffer, 1024 * 1024, a);
        log.info("-----------这里开始图片存入内存----------" + is);
        if (is) {
            /**
             * 该方式使用内存获取 但是读取有问题无法预览
             * linux下 可能有问题
             * */
            log.info("hksdk(抓图)-结果状态值(0表示成功):" + hCNetSDK.NET_DVR_GetLastError());
            byte[] array = jpegBuffer.array();

            //存储到minio
            String BucketName = "pic";
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String time = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String ObjectName = dvrLogin.getIp() + "/" + userId + "/" + time + "/" + uuid + ".jpeg";
            String ContentType = "image/JPEG";
            InputStream input = new ByteArrayInputStream(array);
            String url = minio.uploadFile(BucketName, ObjectName, input, ContentType);
            return url;
            //存储到本地
//            String path = "D:/pic/" + new Date().getTime() + "(" + userId + ")" + ".jpeg";
//            File file = new File(path);
//            if (!file.exists()) {
//                try {
//                    File fileParent = file.getParentFile();
//                    if (!fileParent.exists()) {
//                        fileParent.mkdirs();
//                    }
//                    file.createNewFile();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            BufferedOutputStream outputStream = null;
//            try {
//                outputStream = new BufferedOutputStream(new FileOutputStream(file));
//                outputStream.write(jpegBuffer.array(), 0, a.getValue());
//                outputStream.flush();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//                if (outputStream != null) {
//                    try {
//                        outputStream.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        } else {
            log.info("hksdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError());
            return "";
        }
        // return path;
    }

    /**
     * @描述 短时录像
     * @参数 [userId, channelNum, enable]
     * @返回值 void
     * @创建人 刘苏义
     * @创建时间 2023/1/20 11:18
     * @修改人和其它信息
     */
    @Override
    public String record(Integer userId, Integer channelNum, Boolean enable) {
        String path = "";
        //预览参数
        NET_DVR_PREVIEWINFO previewinfo = new NET_DVR_PREVIEWINFO();
        previewinfo.read();
        previewinfo.lChannel = channelNum;
        previewinfo.dwStreamType = 0;//码流类型：0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        previewinfo.dwLinkMode = 0;//连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        previewinfo.hPlayWnd = null;//播放窗口的句柄，为NULL表示不解码显示。
        previewinfo.bBlocked = 0;//0- 非阻塞取流，1- 阻塞取流
        previewinfo.byNPQMode = 0;//NPQ模式：0- 直连模式，1-过流媒体模式
        previewinfo.write();
        int lRealHandle;
        if (enable) {
            if (!user_real_Map.containsKey(userId)) {
                lRealHandle = hCNetSDK.NET_DVR_RealPlay_V40(userId, previewinfo, null, null);
                if (lRealHandle == -1) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("取流失败" + iErr);
                    return "";
                }
                log.info("取流成功");

                File file = new File("D:/record/temp.mp4");
                if (!file.exists()) {
                    try {
                        File fileParent = file.getParentFile();
                        if (!fileParent.exists()) {
                            fileParent.mkdirs();
                        }
                        file.createNewFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    path = file.getCanonicalPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recordInfo info = new recordInfo();
                info.setLRealHandle(lRealHandle);
                info.setRecordPath(path);
                user_real_Map.put(userId, info);
            }
            recordInfo info = user_real_Map.get(userId);
            if (!hCNetSDK.NET_DVR_SaveRealData_V30(info.getLRealHandle(), 1, info.getRecordPath())) {
                log.error("保存视频文件到文件夹失败 错误码为:  " + hCNetSDK.NET_DVR_GetLastError());
                return "";
            }
            log.info("录像开始");
            return info.getRecordPath();
        } else {
            recordInfo info = user_real_Map.get(userId);
            hCNetSDK.NET_DVR_StopRealPlay(info.getLRealHandle());
            log.info("录像停止");
            //存入minio
            String BucketName = "record";
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String time = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String ObjectName = dvrLogin.getIp() + "/" + userId + "/" + time + "/" + uuid + ".mp4";
            String ContentType = "video/MP4";
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(info.getRecordPath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String url = minio.uploadFile(BucketName, ObjectName, stream, ContentType);
            user_real_Map.remove(userId);
            return url;
        }
    }


    /**
     * 建立布防上传通道，用于传输数据
     *
     * @param lUserID      唯一标识符
     * @param lAlarmHandle 报警处理器
     */
    public int setupAlarmChan(int lUserID, int lAlarmHandle) {
        // 根据设备注册生成的lUserID建立布防的上传通道，即数据的上传通道
        if (lUserID == -1) {
            log.info("请先注册");
            return lUserID;
        }
        if (lAlarmHandle < 0) {
            // 设备尚未布防,需要先进行布防
            if (fMSFCallBack_V31 == null) {
                fMSFCallBack_V31 = new FMSGCallBack();
                Pointer pUser = null;
                if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                    log.info("设置回调函数失败!", hCNetSDK.NET_DVR_GetLastError());
                }
            }
            // 这里需要对设备进行相应的参数设置，不设置或设置错误都会导致设备注册失败
            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
            // 智能交通布防优先级：0 - 一等级（高），1 - 二等级（中），2 - 三等级（低）
            m_strAlarmInfo.byLevel = 1;
            // 智能交通报警信息上传类型：0 - 老报警信息（NET_DVR_PLATE_RESULT）, 1 - 新报警信息(NET_ITS_PLATE_RESULT)
            m_strAlarmInfo.byAlarmInfoType = 1;
            // 布防类型(仅针对门禁主机、人证设备)：0 - 客户端布防(会断网续传)，1 - 实时布防(只上传实时数据)
            m_strAlarmInfo.byDeployType = 1;
            // 抓拍，这个类型要设置为 0 ，最重要的一点设置
            m_strAlarmInfo.byFaceAlarmDetection =0;
            // 报警图片数据类型 123位 都是1 url传输
            m_strAlarmInfo.byAlarmTypeURL=7;
            m_strAlarmInfo.write();
            // 布防成功，返回布防成功的数据传输通道号
            lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
            if (lAlarmHandle == -1) {
                log.info("设备布防失败，错误码=========={}", hCNetSDK.NET_DVR_GetLastError());
                // 注销 释放sdk资源
                logout(lUserID);
                return lAlarmHandle;
            } else {
                log.info("设备布防成功");
                return lAlarmHandle;
            }
        }
        return lAlarmHandle;
    }
    /**
     * 注销
     *
     * @param lUserID 设备注册成功唯一标识符
     */
    public void logout(int lUserID) {
        // 注销
        hCNetSDK.NET_DVR_Logout(lUserID);
        // 释放sdk资源
        hCNetSDK.NET_DVR_Cleanup();
    }
}
