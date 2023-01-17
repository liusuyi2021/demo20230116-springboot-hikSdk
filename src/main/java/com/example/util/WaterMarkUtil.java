package com.example.util;

/**
 * @ClassName WaterMarkUtil
 * @Description:
 * @Author 刘苏义
 * @Date 2023/1/17 20:20
 * @Version 1.0
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *   图片水印工具类
 *
 */
public class WaterMarkUtil {

    // 水印透明度
    private static final float alpha = 0.8f;
    // 水印横向位置
    private static int positionWidth = 100;
    // 水印纵向位置
    private static int positionHeight = 300;
    // 水印文字字体
    private static final Font font = new Font("宋体", Font.BOLD, 30);
    // 水印文字颜色
    private static final Color color = Color.white;

    /**
     * 给图片添加水印文字
     *
     * @param text       水印文字
     * @param srcImgPath 源图片路径
     * @param targetPath 目标图片路径
     */
    public static void markImage(String text, String srcImgPath, String targetPath) {
        markImage(text, srcImgPath, targetPath, null);
    }

    /**
     * 给图片添加水印文字、可设置水印文字的旋转角度
     *
     * @param text
     * @param srcImgPath
     * @param targetPath
     * @param degree
     */
    public static void markImage(String text, String srcImgPath, String targetPath, Integer degree) {

        OutputStream os = null;
        try {
            // 0、图片类型
            String type = srcImgPath.substring(srcImgPath.indexOf(".") + 1, srcImgPath.length());

            // 1、源图片
            Image srcImg = ImageIO.read(new File(srcImgPath));

            int imgWidth = srcImg.getWidth(null);
            int imgHeight = srcImg.getHeight(null);

            BufferedImage buffImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH), 0, 0, null);
            // 4、设置水印旋转
            if (null != degree) {
                g.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            }
            // 5、设置水印文字颜色
            g.setColor(color);
            // 6、设置水印文字Font
            g.setFont(font);
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 8、第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)
            positionWidth = 50;
            positionHeight = imgHeight - 30;
            g.drawString(text, positionWidth, positionHeight);
            // 9、释放资源
            g.dispose();
            // 10、生成图片
            os = new FileOutputStream(targetPath);
            // ImageIO.write(buffImg, "JPG", os);
            ImageIO.write(buffImg, type.toUpperCase(), os);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != os) {
                    os.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给图片添加水印文字、可设置水印文字的旋转角度
     *
     * @param text
     * @param inputStream
     * @param outputStream
     * @param degree
     * @param typeName
     */
    public static void markImageByIO(String text, InputStream inputStream, OutputStream outputStream,
                                     Integer degree, String typeName) {
        try {
            // 1、源图片
            Image srcImg = ImageIO.read(inputStream);

            int imgWidth = srcImg.getWidth(null);
            int imgHeight = srcImg.getHeight(null);
            BufferedImage buffImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);

            // 2、得到画笔对象
            Graphics2D g = buffImg.createGraphics();
            // 3、设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(srcImg.getScaledInstance(imgWidth, imgHeight, Image.SCALE_SMOOTH), 0, 0, null);
            // 4、设置水印旋转
            if (null != degree) {
                g.rotate(Math.toRadians(degree), (double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            }
            // 5、设置水印文字颜色
            g.setColor(color);
            // 6、设置水印文字Font
            g.setFont(font);
            // 7、设置水印文字透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
            // 8、第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y)

            g.drawString(text, positionWidth, positionHeight);
            // 9、释放资源
            g.dispose();
            // 10、生成图片
            ImageIO.write(buffImg, typeName.toUpperCase(), outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//    public static void main(String[] args) {
//        String srcImgPath = "D:\\testSmile.jpg";
//        String text = "JCccc";
//        // 给图片添加水印文字
//        markImage(text, srcImgPath, "D:\\testSmileWithMark.jpg");
//        // 给图片添加水印文字,水印文字旋转-45
//        markImage(text, srcImgPath, "D:\\testSmileWithMarkRotate.jpg", -45);
//        System.out.println("给图片添加水印文字完毕");
//    }


