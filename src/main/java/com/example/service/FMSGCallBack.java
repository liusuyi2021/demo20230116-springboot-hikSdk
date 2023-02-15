package com.example.service;

import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

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
     * 处理返回的信息 字节转字符串
     *
     * @param bytes
     * @return
     */
    private static String byteToString(byte[] bytes) {
        String[] strings = new String(bytes).split("\0", 2);
        StringBuilder sb = new StringBuilder();
        if (strings != null && strings.length > 0) {
            for (int i = 0; i < strings.length - 1; i++) {
                sb.append(strings[i]);
            }
        }
        return sb.toString();
    }

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
        //报警类型
        String sAlarmType = "lCommand=0x" + Integer.toHexString(lCommand);
        //报警时间
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //序列号
        String sSerialNumber = byteToString(pAlarmer.sSerialNumber);
        //用户id
        String lUserID = new String(String.valueOf(pAlarmer.lUserID));
        //ip地址
        String sDeviceIP = byteToString(pAlarmer.sDeviceIP);
        //lCommand是传的报警类型
        switch (lCommand) {
            //异常行为识别信息
            case HCNetSDK.COMM_ALARM_RULE:
                HCNetSDK.NET_VCA_RULE_ALARM netVcaRuleAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                netVcaRuleAlarm.write();
                Pointer pFaceSnapInfo = netVcaRuleAlarm.getPointer();
                // 写入传入数据
                pFaceSnapInfo.write(0, pAlarmInfo.getByteArray(0, netVcaRuleAlarm.size()), 0, netVcaRuleAlarm.size());
                netVcaRuleAlarm.read();
                int wEventTypeEx = netVcaRuleAlarm.struRuleInfo.wEventTypeEx;
                short byPicTransType = netVcaRuleAlarm.byPicTransType;
                switch (wEventTypeEx) {
                    case HCNetSDK.VCA_RULE_EVENT_TYPE_EX.ENUM_VCA_EVENT_RETENTION:
                        log.info("滞留检测");
                        break;
                    case HCNetSDK.VCA_RULE_EVENT_TYPE_EX.ENUM_VCA_EVENT_LEAVE_POSITION:
                        log.info("人员离岗");
                        log.info("图片传输方式："+byPicTransType);
                        Pointer pImage = netVcaRuleAlarm.pImage;
                        netVcaRuleAlarm.read();
                        log.info("图片传输方式："+byPicTransType);
                        break;
                }
                //报警类型
                log.info("sAlarmType：======{}", sAlarmType);
                //设备序列号
                log.info("sSerialNumber：======{}", sSerialNumber);
                //用户id
                log.info("lUserID：======{}", lUserID);
                //设备ip
                log.info("sDeviceIP：======{}", sDeviceIP);
                //当前时间
                log.info("date：======{}", date);
                break;
            default:
                log.info("其他报警信息=========={" + sAlarmType + "}");
                break;
        }
        return true;
    }
}
