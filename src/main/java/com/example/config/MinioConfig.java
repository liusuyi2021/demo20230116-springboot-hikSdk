package com.example.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @ClassName: MinioConfig
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月28日 14:28
 * @Version: 1.0
 **/

@Data
@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;
    @Value("${minio.accessKey}")
    private String minioAccessKey;
    @Value("${minio.secretKey}")
    private String minioSecretKey;

    /**
     * 注入minio 客户端
     *
     * @return
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .build();
    }
}
