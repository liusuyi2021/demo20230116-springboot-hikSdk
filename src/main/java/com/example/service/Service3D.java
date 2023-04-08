package com.example.service;

import javafx.geometry.Point3D;

import java.awt.geom.Point2D;

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
        // location(0.0, 125.160011,46.566609, 125.156884,46.566637);
        //  getZero(125.156876,46.566636,275.4,125.163748,46.562726,147.1);
//        Point3D p1 = new Point3D(125.163751,46.562712, 139.8);
//        Point3D p2 = new Point3D(125.17219245056302, 46.555842706802196, 268.3);
//        Point3D p3 = new Point3D(125.166523,46.565968, 89.5);
//        Point3D p4 = new Point3D(125.149176,46.569246, 283.7);
//        getIntersection(p1, p2, p3, p4);
       // getDestinationPoint(125.163751, 46.562712, 139.8);
        isZeroAdjust(288.9,321.5,339.0,339.0);
    }

    public static void location(Double initAngle, Double target_x, Double target_y, Double camera_x, Double camera_y) {

        //定义球机云台转动角度变量
        double angle1 = 0;
        // 计算球机云台转动的角度
        angle1 = Math.atan2(target_x - camera_x, target_y - camera_y) * 180 / Math.PI;  // 相对于Y正半轴旋转的夹角
        if (angle1 < 0) {
            angle1 = 360 + angle1;
        }
        // 输出球机云台需要旋转的夹角
        System.out.println("球机需要旋转的顺时针夹角为：" + angle1 + "°");
    }

    static public void getZero(double x1, double y1, double p1, double x2, double y2, double p2) {
        // 输入点A的坐标和角度
        double ax = x1;
        double ay = y1;
        double alphaNorth = p1;
        double alpha = Math.toRadians(90 - alphaNorth);

        // 输入点B的坐标和角度
        double bx = x2;
        double by = y2;
        double betaNorth = p2;
        double beta = Math.toRadians(90 - betaNorth);

        // 求解两条射线的交点作为原点坐标
        double tanAlpha = Math.tan(alpha);
        double tanBeta = Math.tan(beta);
        double x = (by - ay - (bx - ax) * (tanBeta - tanAlpha)) / (tanAlpha - tanBeta);
        double y = ay + (x - ax) * tanAlpha;

        System.out.println("原点坐标为: (" + x + ", " + y + ")");

    }

    static void getIntersection(Point3D line1P1, Point3D line1P2, Point3D line2P1, Point3D line2P2) {
        double x1 = line1P1.getX();
        double y1 = line1P1.getY();
        double z1 = line1P1.getZ();
        double x2 = line1P2.getX();
        double y2 = line1P2.getY();
        double z2 = line1P2.getZ();

        double x3 = line2P1.getX();
        double y3 = line2P1.getY();
        double z3 = line2P1.getZ();
        double x4 = line2P2.getX();
        double y4 = line2P2.getY();
        double z4 = line2P2.getZ();

        double t1 = 0, t2 = 0;

        t2 = ((z3 - z1) / (z2 - z1) - (x3 - x1) / (x2 - x1)) / ((x4 - x3) / (x2 - x1) - (z4 - z3) / (z2 - z1));
        t1 = (z3 - z1 + (z4 - z3) * t2) / (z2 - z1);
        if (t1 <= 1 && t2 <= 1) {
            double x = x1 + (x2 - x1) * t1;
            double y = y1 + (y2 - y1) * t1;
            double z = z1 + (z2 - z1) * t1;

            System.out.println(x);
            System.out.println(y);
            System.out.println(z);
            getDestinationPoint(x, y, z);
        }

    }

    static void getDestinationPoint(Double lon, Double lat, Double heading) {
        Double radius = 6371e3; // 地球半径，单位是米
        Double distance = 1000.0; // 目标点距离，单位是米

        // 将经度和纬度转换为弧度
        Double lonRad = lon * Math.PI / 180;
        Double latRad = lat * Math.PI / 180;

        // 计算给定方向的弧度值
        Double headingRad = heading * Math.PI / 180;

        // 使用距离公式计算出目标点的新经纬度坐标
        Double newLatRad = Math.asin(Math.sin(latRad) * Math.cos(distance / radius) +
                Math.cos(latRad) * Math.sin(distance / radius) * Math.cos(headingRad));
        Double newLonRad = lonRad + Math.atan2(Math.sin(headingRad) * Math.sin(distance / radius) * Math.cos(latRad),
                Math.cos(distance / radius) - Math.sin(latRad) * Math.sin(newLatRad));

        // 将弧度值转换为度数，并返回新的经纬度坐标
        Double newLat = newLatRad * 180 / Math.PI;
        Double newLon = newLonRad * 180 / Math.PI;

        System.out.println("新点坐标为: (" + newLon + ", " + newLat + ")");
    }

    static void isZeroAdjust(double p0, double p1, double t0, double t1) {
        double p = p1 - p0;
        double dp = p >= 0 ? p : p + 360;
        double t = t1 - t0;
        double dt = t >= 0 ? t : t + 360;
        System.out.println("p:"+dp);
        System.out.println("t:"+dt);
    }
}
