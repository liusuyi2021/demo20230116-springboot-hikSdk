package com.example.service;

import com.example.domian.DVRLogin;
import com.example.util.WaterMarkUtil;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.service.HCNetSDK.GOTO_PRESET;
import static com.example.service.HCNetSDK.SET_PRESET;

/**
 * @ClassName: initSdk
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 11:25
 * @Version: 1.0
 **/
@Slf4j
@Service
public class sdkClinetImpl implements sdkClinet {

   private static HCNetSDK hCNetSDK;

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
     *@描述 控制上-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:14
     *@修改人和其它信息
     */
    @Override
    public boolean startUp(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_UP, 0, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }

    /**
     *@描述 控制上-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:17
     *@修改人和其它信息
     */
    @Override
    public boolean endUp(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_UP, 1, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }

    /**
     *@描述 控制下-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:18
     *@修改人和其它信息
     */
    public boolean startDown(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_DOWN, 0, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }

    /**
     *@描述 控制下-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:19
     *@修改人和其它信息
     */
    public boolean endDown(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.TILT_DOWN, 1, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }
    /**
     *@描述 控制左转-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startLeft(Integer userId, Integer channelNum, Integer speed) {

        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_LEFT, 0, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }
    /**
     *@描述 控制左转-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endLeft(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_LEFT, 1, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }
    /**
     *@描述 控制右转-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startRight(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_RIGHT, 0, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }
    /**
     *@描述 控制右转-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endRight(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(userId, channelNum, HCNetSDK.PAN_RIGHT, 1, speed);
        if (!bool) {
            int code = hCNetSDK.NET_DVR_GetLastError();
            System.out.println("控制失败,请稍后重试"+code);
        }
        return bool;
    }
    /**
     *@描述 控制焦距变大-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startZoomIn(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.ZOOM_IN, 0);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦距变大-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endZoomIn(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.ZOOM_IN, 1);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦距变小-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startZoomOut(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.ZOOM_OUT, 0);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦距变小-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endZoomOut(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.ZOOM_OUT, 1);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦点前调-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startFocusNear(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.FOCUS_NEAR, 0);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦点前调-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endFocusNear(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.FOCUS_NEAR, 1);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦点后调-开始
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean startFocusFar(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.FOCUS_FAR, 0);
        if (!bool) {
            System.out.println("控制失败,请稍后重试");
        }
        return bool;
    }
    /**
     *@描述 控制焦点后调-结束
     *@参数 [userId, channelNum, speed]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:23
     *@修改人和其它信息
     */
    @Override
    public boolean endFocusFar(Integer userId, Integer channelNum, Integer speed) {
        boolean bool = hCNetSDK.NET_DVR_PTZControl_Other(userId, 1, HCNetSDK.FOCUS_FAR, 1);
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
     * 截图
     *
     * @param userId
     */
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
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".jpeg", "UTF-8"));

            outputStream = response.getOutputStream();
           // LoginUser loginUser = LoginContext.me().getLoginUser();
            WaterMarkUtil.markImageByIO("",in,outputStream,null,"jpeg");
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

//    private void picCutCate(Integer lUserID, Integer chanLong, String imgPath) {
//        //图片质量
//        HCNetSDK.NET_DVR_JPEGPARA jpeg = new HCNetSDK.NET_DVR_JPEGPARA();
//        //设置图片分辨率
//        jpeg.wPicSize = 0;
//        //设置图片质量
//        jpeg.wPicQuality = 0;
//        IntByReference a = new IntByReference();
//        //设置图片大小
//        ByteBuffer jpegBuffer = ByteBuffer.allocate(1024 * 1024);
//        File file = new File(imgPath);
//        // 抓图到内存，单帧数据捕获并保存成JPEG存放在指定的内存空间中
//        log.info("-----------这里开始封装 NET_DVR_CaptureJPEGPicture_NEW---------");
//        boolean is = hCNetSDK.NET_DVR_CaptureJPEGPicture_NEW(lUserID, chanLong, jpeg, jpegBuffer, 1024 * 1024, a);
//        log.info("-----------这里开始图片存入内存----------" + is);
//        if (is) {
//            /**
//             * 该方式使用内存获取 但是读取有问题无法预览
//             * linux下 可能有问题
//             * */
//            log.info("hksdk(抓图)-结果状态值(0表示成功):" + hCNetSDK.NET_DVR_GetLastError());
//            byte[] array = jpegBuffer.array();
//
//            //存储到本地
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
//        } else {
//            log.info("hksdk(抓图)-抓取失败,错误码:" + hCNetSDK.NET_DVR_GetLastError());
//        }
//    }

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
     *@描述 获取ptz信息
     *@参数 [userId, channelNum]
     *@返回值 boolean
     *@创建人 刘苏义
     *@创建时间 2023/1/17 16:36
     *@修改人和其它信息
     */
    @Override
    public boolean getPtz(Integer userId, Integer channelNum) {

        HCNetSDK.NET_DVR_PTZPOS m_ptzPosCurrent = new HCNetSDK.NET_DVR_PTZPOS();
        Pointer pioint = m_ptzPosCurrent.getPointer();
        IntByReference ibrBytesReturned = new IntByReference(0);
        boolean bool = hCNetSDK.NET_DVR_GetDVRConfig(userId, HCNetSDK.NET_DVR_GET_PTZPOS, channelNum, pioint, m_ptzPosCurrent.size(), ibrBytesReturned);
        if (bool) {
            m_ptzPosCurrent.read();
            DecimalFormat df = new DecimalFormat("0.0");//设置保留位数
            //16进制转Integer后除10，保留小数点1位
            //实际显示的PTZ值是获取到的十六进制值的十分之一，
            //如获取的水平参数P的值是0x1750，实际显示的P值为175度；
            //获取到的垂直参数T的值是0x0789，实际显示的T值为78.9度；
            //获取到的变倍参数Z的值是0x1100，实际显示的Z值为110倍。
            System.out.println("PTZ垂直参数为: " + df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wTiltPos)) / 10));
            System.out.println("PTZ水平参数为: " + df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wPanPos)) / 10));
            System.out.println("PTZ变倍参数为: " + df.format((float) Integer.parseInt(Integer.toHexString(m_ptzPosCurrent.wZoomPos)) / 10));
        }
        return  bool;
    }

}
