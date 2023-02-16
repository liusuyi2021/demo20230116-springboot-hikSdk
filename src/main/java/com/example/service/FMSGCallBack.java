package com.example.service;

import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;


/**
 * @ClassName: FMSGCallBack
 * @Description:
 * @Author: Administrator
 * @Date: 2023年02月15日 12:16
 * @Version: 1.0
 **/
@Slf4j(topic = "hiksdk")
public class FMSGCallBack implements HCNetSDK.FMSGCallBack_V31 {

    /**
     * 报警信息回调函数
     *
     * @param lCommand   上传消息类型
     * @param pAlarmer   报警设备信息
     * @param pAlarmInfo 报警信息
     * @param dwBufLen   报警信息缓存大小
     * @param pUser      用户数据
     */
    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        AlarmDataParse.alarmDataHandle( lCommand, pAlarmer,  pAlarmInfo,  dwBufLen,  pUser);
        return true;
    }
}
