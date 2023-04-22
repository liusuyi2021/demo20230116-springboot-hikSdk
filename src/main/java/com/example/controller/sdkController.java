package com.example.controller;

import com.example.core.domain.AjaxResult;
import com.example.domian.DVRLogin;
import com.example.domian.PTZ;
import com.example.service.hikSdkClinetImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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


    @RequestMapping("/login")
    public String loginHtml(){
        return "login";
    }
    @RequestMapping("/index")
    public String index(){
        return "index";
    }
    @GetMapping("/loginCamera")
    private @ResponseBody
    AjaxResult loginIndex() {
        UserId = sdk.login(login);
        log.info("相机登录成功：" + UserId);
        return AjaxResult.success("相机登录成功：" + UserId);
    }

    @GetMapping("/up")
    private @ResponseBody
    AjaxResult up(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlUp(UserId, channelNum, speed, enable);
        return AjaxResult.success("up");
    }
    @GetMapping("/down")
    private @ResponseBody
    AjaxResult down(Integer channelNum, Integer speed,boolean enable) {
        sdk.controlDown(UserId, channelNum, speed, enable);
        return AjaxResult.success("down");
    }

    @GetMapping("/left")
    private @ResponseBody
    AjaxResult left(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlLeft(UserId, channelNum, speed, enable);
        return AjaxResult.success("left");
    }

    @GetMapping("/right")
    private @ResponseBody
    AjaxResult right(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlRight(UserId, channelNum, speed, enable);
        return AjaxResult.success("right");
    }

    @GetMapping("/controlZoomIn")
    private @ResponseBody
    AjaxResult controlZoomIn(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlZoomIn(UserId, channelNum, speed, enable);
        return AjaxResult.success("controlZoomIn");
    }

    @GetMapping("/controlZoomOut")
    private @ResponseBody
    AjaxResult controlZoomOut(Integer channelNum, Integer speed,boolean enable) {
        sdk.controlZoomOut(UserId, channelNum, speed, enable);
        return AjaxResult.success("ZoomOut");
    }

    @GetMapping("/controlFocusNear")
    private @ResponseBody
    AjaxResult controlFocusNear(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlFocusNear(UserId, channelNum, speed, enable);
        return AjaxResult.success("FocusNear");
    }

    @GetMapping("/controlFocusFar")
    private @ResponseBody
    AjaxResult controlFocusFar(Integer channelNum, Integer speed,boolean enable)  {
        sdk.controlFocusFar(UserId, channelNum, speed, enable);
        return AjaxResult.success("FocusFar");
    }

    @GetMapping("/gotoPreset")
    private @ResponseBody
    AjaxResult gotoPreset(Integer channelNum, Integer presetIndex) {
        boolean b = sdk.gotoPreset(UserId, channelNum, presetIndex);
        if (b) {
            return AjaxResult.success("转到预置点" + presetIndex + "成功！");
        } else {
            return AjaxResult.success("转到预置点" + presetIndex + "失败！");
        }
    }

    @GetMapping("/setPreset")
    private @ResponseBody
    AjaxResult setPreset(Integer channelNum, Integer presetIndex) {
        boolean b = sdk.setPreset(UserId, channelNum, presetIndex);
        if (b) {
            return AjaxResult.success("设置预置点" + presetIndex + "成功！");
        } else {
            return AjaxResult.success("设置预置点" + presetIndex + "失败！");
        }
    }

    @GetMapping("/getPTZ")
    private @ResponseBody
    AjaxResult GetPTZ(Integer channelNum) {
        PTZ ptz = sdk.getPtz(UserId, channelNum);
        Map<String, String> map = new HashMap<>();
        map.put("p", ptz.getWPanPos());
        map.put("t", ptz.getWTiltPos());
        map.put("z", ptz.getWZoomPos());
        return AjaxResult.success(ptz);
    }

    @GetMapping("/setPTZ")
    private @ResponseBody
    AjaxResult SetPTZ(Integer channelNum, String p, String t, String z) {
        PTZ ptz = new PTZ();
        ptz.setWPanPos(p);
        ptz.setWTiltPos(t);
        ptz.setWZoomPos(z);
        boolean b = sdk.setPtz(UserId, channelNum, ptz);
        if (b) {
            return AjaxResult.success(ptz);
        } else {
            return AjaxResult.error("设置ptz失败！");
        }
    }

    @GetMapping("/enableWiperPwron")
    private @ResponseBody
    AjaxResult controlWiperPwron(Integer channelNum, Integer speed) {
        boolean b = sdk.controlWiperPwron(UserId, channelNum, speed, true);
        if (b) {
            return AjaxResult.success("开启雨刷成功！");
        } else {
            return AjaxResult.error("开启雨刷失败！");
        }
    }

    @GetMapping("/enableDefogcfg")
    private @ResponseBody
    AjaxResult EnableDefogcfg(Integer channelNum) {
        boolean b = sdk.controlDefogcfg(UserId, channelNum, true);
        if (b) {
            return AjaxResult.success("开启透雾成功！");
        } else {
            return AjaxResult.error("开启透雾失败！");
        }
    }

    @GetMapping("/disableDefogcfg")
    private @ResponseBody
    AjaxResult DisableDefogcfg(Integer channelNum) {
        boolean b = sdk.controlDefogcfg(UserId, channelNum, false);
        if (b) {
            return AjaxResult.success("关闭透雾成功！");
        } else {
            return AjaxResult.error("关闭透雾失败！");
        }
    }

    @GetMapping("/enableInfrarecfg")
    private @ResponseBody
    AjaxResult enableInfrarecfg(Integer channelNum) {
        boolean b = sdk.controlInfrarecfg(UserId, channelNum, true);
        if (b) {
            return AjaxResult.success("开启红外成功！");
        } else {
            return AjaxResult.error("开启红外失败！");
        }
    }

    @GetMapping("/disableInfrarecfg")
    private @ResponseBody
    AjaxResult disableInfrarecfg(Integer channelNum) {
        boolean b = sdk.controlInfrarecfg(UserId, channelNum, false);
        if (b) {
            return AjaxResult.success("关闭红外成功！");
        } else {
            return AjaxResult.error("关闭红外失败！");
        }
    }

    @GetMapping("/enableFocusMode")
    private @ResponseBody
    AjaxResult enableFocusMode(Integer channelNum) {
        boolean b = sdk.controlFocusMode(UserId, channelNum, true);
        if (b) {
            return AjaxResult.success("开启手动聚焦成功！");
        } else {
            return AjaxResult.error("开启手动聚焦失败！");
        }
    }

    @GetMapping("/getFocusPos")
    private @ResponseBody
    AjaxResult getFocusPos(Integer channelNum) {
        Map<String, Object> Map = sdk.getFocusPos(UserId, channelNum);
        return AjaxResult.success("获取聚焦值",Map);
    }

    @GetMapping("/setFocusPos")
    private @ResponseBody
    AjaxResult setFocusPos(Integer channelNum, Integer dwFocusPos) {
        boolean b = sdk.setFocusPos(UserId, channelNum, dwFocusPos);
        if (b) {
            return AjaxResult.success("设置聚焦值成功！");
        } else {
            return AjaxResult.success("设置聚焦值失败！");
        }
    }
    @GetMapping("/disableFocusMode")
    private @ResponseBody
    AjaxResult disableFocusMode(Integer channelNum) {
        boolean b = sdk.controlFocusMode(UserId, channelNum, false);
        if (b) {
            return AjaxResult.success("开启自动聚焦成功！");
        } else {
            return AjaxResult.error("开启自动聚焦失败！");
        }
    }

    @GetMapping("/enableHeateRpwron")
    private @ResponseBody
    AjaxResult enableHeateRpwron(Integer channelNum) {
        boolean b = sdk.controlPTHeateRpwron(UserId, channelNum, true);
        if (b) {
            return AjaxResult.success("开启云台加热成功！");
        } else {
            return AjaxResult.error("开启云台加热失败！");
        }
    }

    @GetMapping("/disableHeateRpwron")
    private @ResponseBody
    AjaxResult disableHeateRpwron(Integer channelNum) {
        boolean b = sdk.controlPTHeateRpwron(UserId, channelNum, false);
        if (b) {
            return AjaxResult.success("关闭云台加热成功！");
        } else {
            return AjaxResult.error("关闭云台加热失败！");
        }
    }

    @GetMapping("/enableCameraDeicing")
    private @ResponseBody
    AjaxResult enableCameraDeicing(Integer channelNum) {
        boolean b = sdk.controlCameraDeicing(UserId, channelNum, true);
        if (b) {
            return AjaxResult.success("开启镜头加热成功！");
        } else {
            return AjaxResult.error("开启镜头加热失败！");
        }
    }

    @GetMapping("/disableCameraDeicing")
    private @ResponseBody
    AjaxResult disableCameraDeicing(Integer channelNum) {
        boolean b = sdk.controlCameraDeicing(UserId, channelNum, false);
        if (b) {
            return AjaxResult.success("关闭镜头加热成功！");
        } else {
            return AjaxResult.error("关闭镜头加热失败！");
        }
    }

    @GetMapping("/captureJPEGPicture")
    private @ResponseBody
    String captureJPEGPicture(HttpServletResponse response) {
        sdk.captureJPEGPicture(UserId, response);
        return "图片上传成功";
    }

    @GetMapping("/captureJPEGPicture1")
    private @ResponseBody
    AjaxResult captureJPEGPicture1(Integer channelNum) {
        String path = sdk.picCutCate(UserId, channelNum);
        return AjaxResult.success(path);
    }

    @GetMapping("/recordStart")
    private @ResponseBody
    AjaxResult recordStart(Integer channelNum) {
        String path = sdk.record(UserId, channelNum, true);
        return AjaxResult.success("录像开始" + path);
    }

    @GetMapping("/recordStop")
    private @ResponseBody
    AjaxResult recordStop(Integer channelNum) {
        String path = sdk.record(UserId, channelNum, false);
        return AjaxResult.success("录像结束" + path);
    }
    @GetMapping("/voiceStart")
    private @ResponseBody
    AjaxResult voiceStart(Integer channelNum) {
        boolean res = sdk.startVoiceCom(UserId,channelNum);
        return AjaxResult.success("语音对讲开始" + res);
    }
    @GetMapping("/voiceStop")
    private @ResponseBody
    AjaxResult voiceStop() {
        boolean res = sdk.stopVoiceCom();
        return AjaxResult.success("语音对讲结束" + res);
    }
    @GetMapping("/getZeroPTZ")
    private @ResponseBody
    AjaxResult GetZeroPTZ(Integer channelNum) {
        boolean zeroPtz = sdk.getZeroPtz(UserId, channelNum);
        return AjaxResult.success(zeroPtz);
    }

    @GetMapping("/setZeroPTZ")
    private @ResponseBody
    AjaxResult SetZeroPTZ(Integer channelNum) {
        boolean zeroPtz = sdk.setZeroPtz(UserId, channelNum);
        return AjaxResult.success(zeroPtz);
    }
}
