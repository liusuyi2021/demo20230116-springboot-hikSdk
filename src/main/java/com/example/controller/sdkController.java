package com.example.controller;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.service.hikSdkClinetImpl;
import com.example.util.CommonResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    private static Integer UserId;

    @Resource
    private hikSdkClinetImpl sdk;
    @Resource
    DVRLogin login;

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
        UserId = sdk.login(login);
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
    CommonResult<PTZ> GetPTZ(Integer channelNum) {
        PTZ ptz = sdk.getPtz(UserId, channelNum);
        Map<String, String> map = new HashMap<>();
        map.put("p", ptz.getWPanPos());
        map.put("t", ptz.getWTiltPos());
        map.put("z", ptz.getWZoomPos());
        return CommonResult.success(ptz);
    }

    @RequestMapping("/setPTZ")
    private @ResponseBody
    CommonResult<PTZ> SetPTZ(Integer channelNum, String p, String t, String z) {
        PTZ ptz = new PTZ();
        ptz.setWPanPos(p);
        ptz.setWTiltPos(t);
        ptz.setWZoomPos(z);
        boolean b = sdk.setPtz(UserId, channelNum, ptz);
        if (b) {
            return CommonResult.success(ptz);
        } else {
            return CommonResult.failed("设置ptz失败！");
        }
    }

    @RequestMapping("/enableWiperPwron")
    private @ResponseBody
    CommonResult<String> controlWiperPwron(Integer channelNum, Integer speed) {
        boolean b = sdk.controlWiperPwron(UserId, channelNum, speed, true);
        if (b) {
            return CommonResult.success("开启雨刷成功！");
        } else {
            return CommonResult.failed("开启雨刷失败！");
        }
    }

    @RequestMapping("/enableDefogcfg")
    private @ResponseBody
    CommonResult<String> EnableDefogcfg(Integer channelNum) {
        boolean b = sdk.controlDefogcfg(UserId, channelNum, true);
        if (b) {
            return CommonResult.success("开启透雾成功！");
        } else {
            return CommonResult.failed("开启透雾失败！");
        }
    }

    @RequestMapping("/disableDefogcfg")
    private @ResponseBody
    CommonResult<String> DisableDefogcfg(Integer channelNum) {
        boolean b = sdk.controlDefogcfg(UserId, channelNum, false);
        if (b) {
            return CommonResult.success("关闭透雾成功！");
        } else {
            return CommonResult.failed("关闭透雾失败！");
        }
    }

    @RequestMapping("/enableInfrarecfg")
    private @ResponseBody
    CommonResult<String> enableInfrarecfg(Integer channelNum) {
        boolean b = sdk.controlInfrarecfg(UserId, channelNum, true);
        if (b) {
            return CommonResult.success("开启红外成功！");
        } else {
            return CommonResult.failed("开启红外失败！");
        }
    }

    @RequestMapping("/disableInfrarecfg")
    private @ResponseBody
    CommonResult<String> disableInfrarecfg(Integer channelNum) {
        boolean b = sdk.controlInfrarecfg(UserId, channelNum, false);
        if (b) {
            return CommonResult.success("关闭红外成功！");
        } else {
            return CommonResult.failed("关闭红外失败！");
        }
    }

    @RequestMapping("/enableFocusMode")
    private @ResponseBody
    CommonResult<String> enableFocusMode(Integer channelNum) {
        boolean b = sdk.controlFocusMode(UserId, channelNum, true);
        if (b) {
            return CommonResult.success("开启手动聚焦成功！");
        } else {
            return CommonResult.failed("开启手动聚焦失败！");
        }
    }

    @RequestMapping("/disableFocusMode")
    private @ResponseBody
    CommonResult<String> disableFocusMode(Integer channelNum) {
        boolean b = sdk.controlFocusMode(UserId, channelNum, false);
        if (b) {
            return CommonResult.success("开启自动聚焦成功！");
        } else {
            return CommonResult.failed("开启自动聚焦失败！");
        }
    }

    @RequestMapping("/enableHeateRpwron")
    private @ResponseBody
    CommonResult<String> enableHeateRpwron(Integer channelNum) {
        boolean b = sdk.controlPTHeateRpwron(UserId, channelNum, true);
        if (b) {
            return CommonResult.success("开启云台加热成功！");
        } else {
            return CommonResult.failed("开启云台加热失败！");
        }
    }

    @RequestMapping("/disableHeateRpwron")
    private @ResponseBody
    CommonResult<String> disableHeateRpwron(Integer channelNum) {
        boolean b = sdk.controlPTHeateRpwron(UserId, channelNum, false);
        if (b) {
            return CommonResult.success("关闭云台加热成功！");
        } else {
            return CommonResult.failed("关闭云台加热失败！");
        }
    }

    @RequestMapping("/enableCameraDeicing")
    private @ResponseBody
    CommonResult<String> enableCameraDeicing(Integer channelNum) {
        boolean b = sdk.controlCameraDeicing(UserId, channelNum, true);
        if (b) {
            return CommonResult.success("开启镜头加热成功！");
        } else {
            return CommonResult.failed("开启镜头加热失败！");
        }
    }

    @RequestMapping("/disableCameraDeicing")
    private @ResponseBody
    CommonResult<String> disableCameraDeicing(Integer channelNum) {
        boolean b = sdk.controlCameraDeicing(UserId, channelNum, false);
        if (b) {
            return CommonResult.success("关闭镜头加热成功！");
        } else {
            return CommonResult.failed("关闭镜头加热失败！");
        }
    }

    @RequestMapping("/captureJPEGPicture")
    private @ResponseBody
    String captureJPEGPicture(HttpServletResponse response) {
        sdk.captureJPEGPicture(UserId, response);
        return "图片上传成功";
    }

    @RequestMapping("/captureJPEGPicture1")
    private @ResponseBody
    CommonResult<String> captureJPEGPicture1(Integer channelNum) {
        String path = sdk.picCutCate(UserId, channelNum);
        return CommonResult.success(path);
    }

    @RequestMapping("/recordStart")
    private @ResponseBody
    CommonResult<String> recordStart(Integer channelNum) {
        String path = sdk.record(UserId, channelNum, true);
        return CommonResult.success("录像开始" + path);
    }

    @RequestMapping("/recordStop")
    private @ResponseBody
    CommonResult<String> recordStop(Integer channelNum) {
        String path = sdk.record(UserId, channelNum, false);
        return CommonResult.success("录像结束" + path);
    }
}
