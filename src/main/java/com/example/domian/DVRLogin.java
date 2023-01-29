package com.example.domian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @ClassName: DVRLogin
 * @Description: 登录实体
 * @Author: Administrator
 * @Date: 2023年01月17日 9:19
 * @Version: 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "login")
@PropertySource(value = "classpath:login.properties")
public class DVRLogin {
    /**
     * 相机ip地址
     */
    private String ip;
    /**
     * 相机端口号
     */
    private short port;
    /**
     * 相机用户名
     */
    private String userName;
    /**
     * 相机密码
     */
    private String password;
}

