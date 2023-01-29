package com.example.service;

import java.io.InputStream;

/**
 * @ClassName: minioService
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月28日 13:18
 * @Version: 1.0
 **/
public interface minioService {
    String uploadFile(String BucketName, String ObjectName, InputStream stream, String contextType);
}
