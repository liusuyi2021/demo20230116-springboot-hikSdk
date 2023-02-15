package com.example.controller;

import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.service.hikSdkClinetImpl;
import com.example.util.CommonResult;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j(topic = "hiksdk")
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
    CommonResult<String> loginIndex() {
        UserId = sdk.login(login);
        log.info("相机登录成功：" + UserId);
        return CommonResult.success("相机登录成功：" + UserId);
    }

    @RequestMapping("/up")
    private @ResponseBody
    CommonResult<String> up(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlUp(UserId, channelNum, speed, enable);
        return CommonResult.success("up");
    }
    @RequestMapping("/down")
    private @ResponseBody
    CommonResult<String> down(Integer channelNum, Integer speed,boolean enable) {
        sdk.controlDown(UserId, channelNum, speed, enable);
        return CommonResult.success("down");
    }

    @RequestMapping("/left")
    private @ResponseBody
    CommonResult<String> left(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlLeft(UserId, channelNum, speed, enable);
        return CommonResult.success("left");
    }

    @RequestMapping("/right")
    private @ResponseBody
    CommonResult<String> right(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlRight(UserId, channelNum, speed, enable);
        return CommonResult.success("right");
    }

    @RequestMapping("/controlZoomIn")
    private @ResponseBody
    CommonResult<String> controlZoomIn(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlZoomIn(UserId, channelNum, speed, enable);
        return CommonResult.success("controlZoomIn");
    }

    @RequestMapping("/ZoomOut")
    private @ResponseBody
    CommonResult<String> controlZoomOut(Integer channelNum, Integer speed,boolean enable) {
        sdk.controlZoomOut(UserId, channelNum, speed, enable);
        return CommonResult.success("ZoomOut");
    }

    @RequestMapping("/controlFocusNear")
    private @ResponseBody
    CommonResult<String> controlFocusNear(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlFocusNear(UserId, channelNum, speed, enable);
        return CommonResult.success("FocusNear");
    }

    @RequestMapping("/controlFocusFar")
    private @ResponseBody
    CommonResult<String> controlFocusFar(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlFocusFar(UserId, channelNum, speed, enable);
        return CommonResult.success("FocusFar");
    }

    @RequestMapping("/gotoPreset")
    private @ResponseBody
    CommonResult<String> gotoPreset(Integer channelNum, Integer presetIndex) {
        boolean b = sdk.gotoPreset(UserId, channelNum, presetIndex);
        if (b) {
            return CommonResult.success("转到预置点" + presetIndex + "成功！");
        } else {
            return CommonResult.success("转到预置点" + presetIndex + "失败！");
        }
    }

    @RequestMapping("/setPreset")
    private @ResponseBody
    CommonResult<String> setPreset(Integer channelNum, Integer presetIndex) {
        boolean b = sdk.setPreset(UserId, channelNum, presetIndex);
        if (b) {
            return CommonResult.success("设置预置点" + presetIndex + "成功！");
        } else {
            return CommonResult.success("设置预置点" + presetIndex + "失败！");
        }
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
