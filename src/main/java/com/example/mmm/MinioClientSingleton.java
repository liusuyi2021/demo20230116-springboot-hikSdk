package com.example.mmm;

import io.minio.MinioClient;

/**
 * @ClassName MinioClientSingleton
 * @Description:
 * @Author 刘苏义
 * @Date 2023/2/16 21:45
 * @Version 1.0
 */

public class MinioClientSingleton {

    private static String endpoint="http://192.168.1.7:9001";
    private static String minioAccessKey="admin";
    private static String minioSecretKey="xzx12345";

    private volatile static MinioClient minioClient;

    /**
     * 获取minio客户端实例
     *
     * @return {@link MinioClient}
     */
    public static MinioClient getMinioClient() {
        if (minioClient == null) {
            synchronized (MinioClientSingleton.class) {
                if (minioClient == null) {
                    minioClient = MinioClient.builder()
                            .endpoint(endpoint)
                            .credentials(minioAccessKey, minioSecretKey)
                            .build();
                }
            }
        }
        return minioClient;
    }

}
