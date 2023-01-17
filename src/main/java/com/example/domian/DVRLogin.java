package com.example.domian;

/**
 * @ClassName: DVRLogin
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月17日 9:19
 * @Version: 1.0
 **/
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录相机对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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

