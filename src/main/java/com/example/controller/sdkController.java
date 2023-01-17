package com.example.controller;

import com.example.domian.DVRLogin;
import com.example.service.sdkClinetImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName: sdkController
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 12:04
 * @Version: 1.0
 **/
@RestController
public class sdkController {
    @Resource
    private sdkClinetImpl sdk;

    private static Integer UserId;

    @PostConstruct
    private void initHCNetSDK()
    {
        sdk.initHCNetSDK();
    }
    @RequestMapping("/login")
    private String loginIndex() {
        DVRLogin DVR = new DVRLogin();
//        DVR.setIp("112.98.126.2");
//        DVR.setPort((short) 28000);
//        DVR.setUserName("admin");
//        DVR.setPassword("ardkj12345");
        DVR.setIp("192.168.1.104");
        DVR.setPort((short) 8000);
        DVR.setUserName("admin");
        DVR.setPassword("xzx12345");
        UserId = sdk.login(DVR);
        System.out.println(UserId);
        return UserId.toString();
    }

    @RequestMapping("/up")
    private String up(Integer channelNum,Integer speed)
    {
        sdk.startUp(UserId,channelNum,speed);
        sdk.endUp(UserId,channelNum,speed);
        return "up";
    }
    @RequestMapping("/down")
    private String down(Integer channelNum,Integer speed)
    {
        sdk.startDown(UserId,channelNum,speed);
        sdk.endDown(UserId,channelNum,speed);
        return "down";
    }
    @RequestMapping("/gotoPreset")
    private String gotoPreset(Integer channelNum,Integer presetIndex)
    {
        sdk.gotoPreset(UserId,channelNum,presetIndex);
        return "转到预置点"+presetIndex+"成功！";
    }
    @RequestMapping("/setPreset")
    private String setPreset(Integer channelNum,Integer presetIndex)
    {
        sdk.setPreset(UserId,channelNum,presetIndex);
        return "设置预置点"+presetIndex+"成功！";
    }
    @RequestMapping("/GetDVRConfig")
    private String GetDVRConfig(Integer channelNum)
    {
        sdk.getPtz(UserId,channelNum);
        return "获取相机配置信息成功！";
    }
    @RequestMapping("/captureJPEGPicture")
    private HttpServletResponse captureJPEGPicture(HttpServletResponse response)
    {
        sdk.captureJPEGPicture(UserId,response);
        return response;
    }
    @RequestMapping("/captureJPEGPicture1")
    private String captureJPEGPicture1(Integer channelNum,String imagePath)
    {
        sdk.picCutCate(UserId,channelNum,imagePath);
        return "图片保存成功";
    }
}
