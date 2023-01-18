package com.example.service;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: sdkClinet
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 15:59
 * @Version: 1.0
 **/
public interface hikSdkClinet {
    void initHCNetSDK();
    Integer login(DVRLogin dvrLogin);
    Boolean isOnLine(Integer userId);
    //方向
    boolean controlUp(Integer userId, Integer channelNum, Integer speed,boolean enable);
    boolean controlDown(Integer userId, Integer channelNum, Integer speed, boolean enable);
    boolean controlLeft(Integer userId, Integer channelNum, Integer speed, boolean enable);
    boolean controlRight(Integer userId, Integer channelNum, Integer speed, boolean enable);
    //变倍
    boolean controlZoomIn(Integer userId, Integer channelNum, Integer speed, boolean enable);
    boolean controlZoomOut(Integer userId, Integer channelNum, Integer speed, boolean enable);
    //变焦
    boolean controlFocusNear(Integer userId, Integer channelNum, Integer speed, boolean enable);
    boolean controlFocusFar(Integer userId, Integer channelNum, Integer speed, boolean enable);
    //预置位
    boolean setPreset(Integer userId, Integer channelNum, Integer PresetIndex);
    boolean gotoPreset(Integer userId, Integer channelNum, Integer PresetIndex);
    //雨刷
    boolean controlWiperPwron(Integer userId, Integer channelNum, Integer speed,boolean enable);
    //透雾
    boolean controlDefogcfg(Integer userId, Integer channelNum,boolean enable);
    //红外?
    boolean controlInfrarecfg(Integer userId, Integer channelNum,boolean enable);
    //聚焦模式
    boolean controlFocusMode(Integer userId, Integer channelNum,boolean enable);
    //云台加热
    boolean controlHeateRpwron(Integer userId, Integer channelNum, boolean enable);
    //抓图-返给前端二进制流
    void captureJPEGPicture(Integer userId, HttpServletResponse response);
    //抓图-存本地
    void picCutCate(Integer userId, Integer channelNum, String imgPath);
    //ptz 三坐标
    PTZ getPtz(Integer userId, Integer channelNum);
    boolean setPtz(Integer userId, Integer channelNum,PTZ ptz);
}
