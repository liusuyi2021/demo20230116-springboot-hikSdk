package com.example.service;

import static java.lang.Math.*;

/**
 * @ClassName Service3D
 * @Description:
 * @Author 刘苏义
 * @Date 2023/1/21 0:38
 * @Version 1.0
 */

public class Service3D {
    public static void main(String[] args) {
        location(100.0, 116.40973, 39.918421, 116.404125, 39.915046);
    }

    public static void location(Double initAngle, Double target_x, Double target_y, Double camera_x, Double camera_y) {

        //定义球机云台转动角度变量
        double angle1, angle2 = 0;
        angle2 += initAngle;
        // 计算球机云台转动的角度
        angle1 = Math.atan2(target_y - camera_y, target_x - camera_x) * 180 / Math.PI;  // 相对于Y正半轴旋转的夹角
        if (angle1 >= 0) {
            angle2 += (360.0 - angle1);
        } else {
            angle2 += -angle1;
        }
        if (angle2 > 360) {
            angle2 = angle2 % 360;
        }
        // 输出球机云台需要旋转的夹角
        System.out.println("球机需要旋转的逆时针夹角为：" + angle1 + "°");
        System.out.println("球机需要旋转的顺时针夹角为：" + angle2 + "°");
    }
}
