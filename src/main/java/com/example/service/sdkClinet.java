package com.example.service;

import com.example.domian.DVRLogin;

/**
 * @ClassName: sdkClinet
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 15:59
 * @Version: 1.0
 **/
public interface sdkClinet {
    void initHCNetSDK();
    Integer login(DVRLogin dvrLogin);
    Boolean isOnLine(Integer userId);
    boolean startUp(Integer userId, Integer channelNum, Integer speed);
    boolean endUp(Integer userId, Integer channelNum, Integer speed);
    boolean startDown(Integer userId, Integer channelNum, Integer speed);
    boolean endDown(Integer userId, Integer channelNum, Integer speed);
    boolean startLeft(Integer userId, Integer channelNum, Integer speed);
    boolean endLeft(Integer userId, Integer channelNum, Integer speed);
    boolean startRight(Integer userId, Integer channelNum, Integer speed);
    boolean endRight(Integer userId, Integer channelNum, Integer speed);
    boolean startZoomIn(Integer userId, Integer channelNum, Integer speed);
    boolean endZoomIn(Integer userId, Integer channelNum, Integer speed);
    boolean startZoomOut(Integer userId, Integer channelNum, Integer speed);
    boolean endZoomOut(Integer userId, Integer channelNum, Integer speed);
    boolean startFocusNear(Integer userId, Integer channelNum, Integer speed);
    boolean endFocusNear(Integer userId, Integer channelNum, Integer speed);
    boolean startFocusFar(Integer userId, Integer channelNum, Integer speed);
    boolean endFocusFar(Integer userId, Integer channelNum, Integer speed);
    boolean setPreset(Integer userId, Integer channelNum, Integer PresetIndex);
    boolean gotoPreset(Integer userId, Integer channelNum, Integer PresetIndex);
    boolean getPtz(Integer userId, Integer channelNum);
}
