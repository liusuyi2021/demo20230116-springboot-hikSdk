package com.example.service;

import com.example.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * @ClassName: minioServiceImpl
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月28日 13:18
 * @Version: 1.0
 **/
@Service
@Slf4j
public class minioServiceImpl implements minioService {
    @Resource
    private MinioUtil minioUtil;

    @Override
    public String uploadFile(String BucketName, String ObjectName, InputStream stream, String contextType) {
        String url = "";
        try {
            boolean b = minioUtil.putObject(BucketName, ObjectName, stream, stream.available(), contextType);
            if (b) {
                url = minioUtil.presignedGetObject(BucketName, ObjectName, 5);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
        return url;
    }
}
