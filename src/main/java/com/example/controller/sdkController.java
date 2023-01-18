package com.example.controller;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.service.hikSdkClinetImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: sdkController
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 12:04
 * @Version: 1.0
 **/
@Controller
public class sdkController {
    @Resource
    private hikSdkClinetImpl sdk;

    private static Integer UserId;

    @PostConstruct
    private void initHCNetSDK() {
        sdk.initHCNetSDK();
    }

    @RequestMapping("/")
    String index() {
        return "test";
    }

    @RequestMapping("/login")
    private @ResponseBody
    String loginIndex() {
        DVRLogin DVR = new DVRLogin();
        DVR.setIp("112.98.126.2");
        //DVR.setPort((short) 2018);
        DVR.setUserName("admin");
        DVR.setPassword("ardkj12345");
        DVR.setPort((short) 2148);

//        DVR.setIp("192.168.1.104");
//        DVR.setPort((short) 8000);
//        DVR.setUserName("admin");
//        DVR.setPassword("xzx12345");
        UserId = sdk.login(DVR);
        System.out.println(UserId);
        return UserId.toString();
    }

    @RequestMapping("/up")
    private @ResponseBody
    String up(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlUp(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlUp(UserId, channelNum, speed, false);
        return "up";
    }

    @RequestMapping("/down")
    private @ResponseBody
    String down(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlDown(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlDown(UserId, channelNum, speed, false);
        return "down";
    }

    @RequestMapping("/left")
    private @ResponseBody
    String left(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlLeft(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlLeft(UserId, channelNum, speed, false);
        return "left";
    }

    @RequestMapping("/right")
    private @ResponseBody
    String right(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlRight(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlRight(UserId, channelNum, speed, false);
        return "right";
    }

    @RequestMapping("/controlZoomIn")
    private @ResponseBody
    String controlZoomIn(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlZoomIn(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlZoomIn(UserId, channelNum, speed, false);
        return "controlZoomIn";
    }

    @RequestMapping("/controlZoomOut")
    private @ResponseBody
    String controlZoomOut(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlZoomOut(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlZoomOut(UserId, channelNum, speed, false);
        return "controlZoomOut";
    }

    @RequestMapping("/controlFocusNear")
    private @ResponseBody
    String controlFocusNear(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlFocusNear(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlFocusNear(UserId, channelNum, speed, false);
        return "controlFocusNear";
    }

    @RequestMapping("/controlFocusFar")
    private @ResponseBody
    String controlFocusFar(Integer channelNum, Integer speed) throws InterruptedException {
        sdk.controlFocusFar(UserId, channelNum, speed, true);
        Thread.sleep(200);
        sdk.controlFocusFar(UserId, channelNum, speed, false);
        return "controlZoomOut";
    }

    @RequestMapping("/gotoPreset")
    private @ResponseBody
    String gotoPreset(Integer channelNum, Integer presetIndex) {
        sdk.gotoPreset(UserId, channelNum, presetIndex);
        return "转到预置点" + presetIndex + "成功！";
    }

    @RequestMapping("/setPreset")
    private @ResponseBody
    String setPreset(Integer channelNum, Integer presetIndex) {
        sdk.setPreset(UserId, channelNum, presetIndex);
        return "设置预置点" + presetIndex + "成功！";
    }

    @RequestMapping("/getPTZ")
    private @ResponseBody
    PTZ GetPTZ(Integer channelNum) {
        PTZ ptz = sdk.getPtz(UserId, channelNum);
        Map<String, String> map = new HashMap<>();
        map.put("p", ptz.getWPanPos());
        map.put("t", ptz.getWTiltPos());
        map.put("z", ptz.getWZoomPos());
        return ptz;
    }

    @RequestMapping("/setPTZ")
    private @ResponseBody
    String SetPTZ(Integer channelNum, String p, String t, String z) {
        PTZ ptz = new PTZ();
        ptz.setWPanPos(p);
        ptz.setWTiltPos(t);
        ptz.setWZoomPos(z);
        sdk.setPtz(UserId, channelNum, ptz);
        return "设置ptz成功！";
    }

    @RequestMapping("/enableWiperPwron")
    private @ResponseBody
    String controlWiperPwron(Integer channelNum, Integer speed) {
        sdk.controlWiperPwron(UserId, channelNum, speed, true);
        return "开启雨刷成功！";
    }

    @RequestMapping("/enableDefogcfg")
    private @ResponseBody
    String EnableDefogcfg(Integer channelNum) {
        sdk.controlDefogcfg(UserId, channelNum, true);
        return "开启透雾成功！";
    }

    @RequestMapping("/disableDefogcfg")
    private @ResponseBody
    String DisableDefogcfg(Integer channelNum) {
        sdk.controlDefogcfg(UserId, channelNum, false);
        return "关闭透雾成功！";
    }

    @RequestMapping("/enableInfrarecfg")
    private @ResponseBody
    String enableInfrarecfg(Integer channelNum) {
        sdk.controlInfrarecfg(UserId, channelNum, true);
        return "开启红外成功！";
    }
    @RequestMapping("/disableInfrarecfg")
    private @ResponseBody
    String disableInfrarecfg(Integer channelNum) {
        sdk.controlInfrarecfg(UserId, channelNum, false);
        return "关闭红外成功！";
    }
    @RequestMapping("/enableFocusMode")
    private @ResponseBody
    String enableFocusMode(Integer channelNum) {
        sdk.controlFocusMode(UserId, channelNum, true);
        return "开启手动聚焦成功！";
    }
    @RequestMapping("/disableFocusMode")
    private @ResponseBody
    String disableFocusMode(Integer channelNum) {
        sdk.controlFocusMode(UserId, channelNum, false);
        return "开启自动聚焦成功！";
    }
    @RequestMapping("/enableHeateRpwron")
    private @ResponseBody
    String enableHeateRpwron(Integer channelNum) {
        sdk.controlHeateRpwron(UserId, channelNum,true);
        return "开启云台加热成功！";
    }
    @RequestMapping("/disableHeateRpwron")
    private @ResponseBody
    String disableHeateRpwron(Integer channelNum) {
        sdk.controlHeateRpwron(UserId, channelNum,false);
        return "关闭云台加热成功！";
    }

    @RequestMapping("/captureJPEGPicture")
    private @ResponseBody
    String captureJPEGPicture(HttpServletResponse response) {
        sdk.captureJPEGPicture(UserId, response);
        return "图片上传成功";
    }

    @RequestMapping("/captureJPEGPicture1")
    private String captureJPEGPicture1(Integer channelNum, String imagePath) {
        sdk.picCutCate(UserId, channelNum, imagePath);
        return "图片保存成功";
    }

}
