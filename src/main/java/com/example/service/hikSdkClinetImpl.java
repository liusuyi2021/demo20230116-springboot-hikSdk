package com.example.service;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.util.WaterMarkUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
@Slf4j
@Service
public class hikSdkClinetImpl implements hikSdkClinet {

    private static HCNetSDK hCNetSDK;

    private static Map<Integer, Integer> user_real_Map = new HashMap<>();

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
            System.out.println("控制失败,请稍后重试");
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
            System.out.println("预置点设置失败!" + "登录ID：" + userId + "通道号：" + channelNum + "预置点号：" + PresetIndex);
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
            System.out.println("预置点设置失败!" + "登录ID：" + userId + "通道号：" + channelNum + "预置点号：" + PresetIndex);
        }
        return bool;
    }

    /**
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
            System.out.println("T垂直参数为: " + p);
            System.out.println("P水平参数为: " + t);
            System.out.println("Z变倍参数为: " + z);
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
            System.out.println("错误码：" + i);
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
            System.out.println("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struCameraParam.read();
        System.out.println("是否开启透雾：" + struCameraParam.struDefogCfg.byMode);

        NET_DVR_DEFOGCFG defogcfg = new NET_DVR_DEFOGCFG();
        if (enable) {
            defogcfg.byMode = 2;//0-不启用 1-自动模式 2-常开模式
            defogcfg.byLevel = 100;//取值范围0-100
        } else {
            defogcfg.byMode = 0;//0-不启用 1-自动模式 2-常开模式
        }
        struCameraParam.struDefogCfg = defogcfg;
        //struCameraParam.struCorridorMode.byEnableCorridorMode = 1;
        struCameraParam.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_CCDPARAMCFG_EX, channelNum, point, struCameraParam.size());
        if (!bool) {
            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        // struCameraParam.read();
        System.out.println("设置成功");
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
            System.out.println("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struDayNigh.read();
        System.out.println("是否开启夜视：" + struDayNigh.struDayNight.byDayNightFilterType);

        HCNetSDK.NET_DVR_DAYNIGHT daynight = new HCNetSDK.NET_DVR_DAYNIGHT();
        if (enable) {
            daynight.byDayNightFilterType = 1;//夜晚

        } else {
            daynight.byDayNightFilterType = 0;//白天
        }
        daynight.bySwitchScheduleEnabled = 1;
        daynight.byDayNightFilterTime = 60;
        struDayNigh.struDayNight = daynight;
        // struDayNigh.struCorridorMode.byEnableCorridorMode = 1;
        struDayNigh.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_CCDPARAMCFG_EX, channelNum, point, struDayNigh.size());
        if (!bool) {
            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("设置成功");
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
    public boolean controlHeateRpwron(Integer userId, Integer channelNum, boolean enable) {
//        Integer dwStop;
//        if (enable) {
//            dwStop = 0;//开启
//        } else {
//            dwStop = 1;//关闭
//        }
//        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId,channelNum, HEATER_PWRON,dwStop,speed);
//        if (!bool) {
//            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
//        }
//        System.out.println("设置成功");
//        return bool;
        HCNetSDK.NET_DVR_DEVSERVER_CFG struDeicing = new HCNetSDK.NET_DVR_DEVSERVER_CFG();
        Pointer point = struDeicing.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean b_GetCameraParam = hCNetSDK.NET_DVR_GetDVRConfig(userId, NET_DVR_GET_DEVSERVER_CFG, channelNum, point, struDeicing.size(), ibrBytesReturned);
        if (!b_GetCameraParam) {
            System.out.println("获取前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        struDeicing.read();
        System.out.println("是否开启除冰：" + struDeicing.byEnableDeicing);

        if (enable) {
            struDeicing.byEnableDeicing = 1;//开启
        } else {
            struDeicing.byEnableDeicing = 0;//关闭
        }
        struDeicing.write();
        boolean bool = hCNetSDK.NET_DVR_SetDVRConfig(userId, NET_DVR_SET_DEVSERVER_CFG, channelNum, point, struDeicing.size());
        if (!bool) {
            System.out.println("设置前端参数失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }
        System.out.println("设置成功");
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
            System.out.println("抓图失败，请稍后重试");
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
            System.out.println("抓图失败，请稍后重试");
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                System.out.println("抓图失败，请稍后重试");
            }
        }
        log.info("-----------处理完成截图数据----------");

    }

    /**
     * 截图 存服务器
     *
     * @param userId
     */
    public void picCutCate(Integer userId, Integer channelNum, String imgPath) {
        //图片质量
        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
        //设置图片分辨率
        jpeg.wPicSize = 0;
        //设置图片质量
        jpeg.wPicQuality = 0;
        IntByReference a = new IntByReference();
        //设置图片大小
        ByteBuffer jpegBuffer = ByteBuffer.allocate(1024 * 1024);
        File file = new File(imgPath);
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

            //存储到本地
            BufferedOutputStream outputStream = null;
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
                outputStream.write(jpegBuffer.array(), 0, a.getValue());
                outputStream.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            log.info("hksdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError());
        }
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
    public void record(Integer userId, Integer channelNum, Boolean enable) {
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
        int lRealHandle = 0;
        if (enable) {
            if (!user_real_Map.containsKey(userId)) {
                lRealHandle = hCNetSDK.NET_DVR_RealPlay_V40(userId, previewinfo, null, null);
                if (lRealHandle == -1) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    System.out.println("取流失败" + iErr);
                    return;
                }
                System.out.println("取流成功");
                user_real_Map.put(userId,lRealHandle);
            }
            lRealHandle = user_real_Map.get(userId);

            File file = new File("D:/Download/" + new Date().getTime() + "(" + userId + ")" + ".mp4");
            String path = "";
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

            if (!hCNetSDK.NET_DVR_SaveRealData_V30(lRealHandle, 1, path)) {
                log.error("保存视频文件到文件夹失败 错误码为:  " + hCNetSDK.NET_DVR_GetLastError());
                return;
            }
            System.out.println("录像开始");
        } else {
            lRealHandle = user_real_Map.get(userId);
            hCNetSDK.NET_DVR_StopRealPlay(lRealHandle);
            user_real_Map.remove(userId);
            System.out.println("录像停止");
        }
    }
}
