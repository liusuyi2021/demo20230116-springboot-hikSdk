package com.example.mmm;


import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * Minio客户端工具类
 */

public class MinioUtils {
    private static Log log = LogFactory.getLog("hiksdk");

    /**
     * 创建文件桶（建议租户ID为桶的名称）
     */
    public static boolean exitsBucket(String bucket) {
        boolean found = false;
        try {
            found = MinioClientSingleton.getMinioClient().bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        } catch (Exception e) {
            log.error("create bucket is error", e);
        }
        return found;
    }

    public static String putObjectLocalFile(String bucket, String filename, String fileFullPath) {
        try {
            boolean bucketExsit = exitsBucket(bucket);
            if (!bucketExsit) {
                //makeBucketPolicy(bucket);
                log.error(bucket + "-不存在");
                throw new RuntimeException(bucket + "-不存在");
            }

            MinioClientSingleton.getMinioClient()
                    .uploadObject(
                            UploadObjectArgs.builder().bucket(bucket).object(filename).filename(fileFullPath).build()
                    );
            return MinioClientSingleton.getMinioClient().getObjectUrl(bucket, filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 自动创建桶并存储文件
     *
     * @param inputStream
     * @param fileName
     * @param bucket
     * @param fileSize
     * @return
     */
    public static String putObjectStream(InputStream inputStream, String fileName, String bucket, Long fileSize) {

        try {
            boolean bucketExsit = exitsBucket(bucket);
            if (bucketExsit) {
                //makeBucketPolicy(bucket);
                log.error(bucket + "-不存在");
            }
            MinioClientSingleton.getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket).object(fileName).stream(inputStream, fileSize, -1).build());
            inputStream.close();
            return MinioClientSingleton.getMinioClient().getObjectUrl(bucket, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bucket   桶名称
     * @param path     文件夹路径 [doc/]
     * @param file     要上传的文件
     * @param fileName 自定义文件名
     * @return
     */
    public static String putObject(String bucket, String path, MultipartFile file, String fileName) throws Exception {
        if (!exitsBucket(bucket)) {
            //makeBucketPolicy(bucket);
            log.error(bucket + "-不存在");
        }
        InputStream inputStream = null;
        try {
            MinioClientSingleton.getMinioClient().putObject(
                    PutObjectArgs.builder().bucket(bucket).object(path).stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
            inputStream = file.getInputStream();
            if (StringUtils.isEmpty(fileName)) {
                fileName = file.getOriginalFilename();
            }

            InputStream in = file.getInputStream();
            PutObjectOptions options = new PutObjectOptions(in.available(), -1);
            options.setContentType(file.getContentType());
            String objectName = path + System.currentTimeMillis() + "_" + fileName; // 生成时间戳防止重名
            MinioClientSingleton.getMinioClient().putObject(bucket, objectName, in, options);
            file.getInputStream().close();
            in.close();
            return MinioClientSingleton.getMinioClient().presignedGetObject(bucket, objectName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return null;
    }

    /**
     * 自动创建桶并存储文件
     *
     * @return
     */
    public static String putObjectStream(MultipartFile file, String bucket) throws Exception {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            System.out.println("上传minio文件服务器错误，上传文件为空");
        }

        boolean exsit = exitsBucket(bucket);
        if (!exsit) {
            //makeBucketPolicy(bucket);
            log.error(bucket + "-不存在");
            System.out.println("minio文件服务器：" + bucket + " 桶不存在");
        }
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 时间戳_随机数.后缀名
        assert originalFilename != null;
        long now = System.currentTimeMillis() / 1000;
        String fileName = DateUtils.parseDateToStr("yyyyMMdd", new Date())+"_"+ now + "_" + new Random().nextInt(1000) +
                originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileHeader = null;
        try {
            InputStream inputStream = file.getInputStream();
            //boolean bucketExsit = exitsBucket(bucket);
            //if (bucketExsit) {
            //    MinioClientSingleton.getMinioClient().makeBucket(MakeBucketArgs.builder()
            //            .bucket(bucket)
            //            .build());
            //}

            MinioClientSingleton.getMinioClient().putObject(
                    PutObjectArgs.builder().bucket(bucket).object(fileName).stream(
                                    inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            //MinioClientSingleton.getMinioClient().putObject(
            //        PutObjectArgs.builder()
            //                .bucket(bucket).object(file.getOriginalFilename()).stream(inputStream, inputStream.available(), -1).build());
            inputStream.close();
            return MinioClientSingleton.getMinioClient().getObjectUrl(bucket, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //private static void makeBucketPolicy(String bucket) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidBucketNameException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, RegionConflictException, ServerException, XmlParserException {
    //    String policyJsonBuilder = "{\n" +
    //            "    "Statement": [\n" +
    //            "        {\n" +
    //            "            "Action": [\n" +
    //            "                "s3:GetBucketLocation",\n" +
    //            "                "s3:ListBucket"\n" +
    //            "            ],\n" +
    //            "            "Effect": "Allow",\n" +
    //            "            "Principal": "*",\n" +
    //            "            "Resource": "arn:aws:s3:::" + bucket + ""\n" +
    //            "        },\n" +
    //            "        {\n" +
    //            "            "Action": "s3:GetObject",\n" +
    //            "            "Effect": "Allow",\n" +
    //            "            "Principal": "*",\n" +
    //            "            "Resource": "arn:aws:s3:::" + bucket + "/*"\n" +
    //            "        }\n" +
    //            "    ],\n" +
    //            "    "Version": "2012-10-17"\n" +
    //            "}\n";
    //    MinioClientSingleton.getMinioClient().makeBucket(MakeBucketArgs.builder()
    //            .bucket(bucket)
    //            .build());
    //    //noinspection deprecation
    //    MinioClientSingleton.getMinioClient().setBucketPolicy(bucket, policyJsonBuilder);
    //}

    /**
     * 查询所有桶文件
     *
     * @return
     */
    public static List<Bucket> getListBuckets() {
        try {
            return MinioClientSingleton.getMinioClient().listBuckets();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 删除文件
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     * @return boolean
     */
    public static boolean removeObject(String bucket, String objectName) {
        try {
            boolean exsit = exitsBucket(bucket);
            if (exsit) {
                // 从mybucket中删除myobject。removeobjectargs.builder().bucket(bucketname).object(objectname).build()
                MinioClientSingleton.getMinioClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
                return true;
            }
        } catch (Exception e) {
            log.error("removeObject", e);
        }
        return false;
    }

    /**
     * 批量删除文件
     *
     * @param bucket      桶名称
     * @param objectNames 对象名称
     * @return boolean
     */
    public static boolean removeObjects(String bucket, List<String> objectNames) {
        boolean exsit = exitsBucket(bucket);
        if (exsit) {
            try {
                List<DeleteObject> objects = new LinkedList<>();
                for (String str : objectNames) {
                    objects.add(new DeleteObject(str));
                }
                MinioClientSingleton.getMinioClient().removeObjects(RemoveObjectsArgs.builder().bucket(bucket).objects(objects).build());
                return true;
            } catch (Exception e) {
                log.error("removeObject", e);
            }
        }
        return false;
    }

    /**
     * 获取单个桶中的所有文件对象名称
     *
     * @param bucket 桶名称
     * @return {@link List}<{@link String}>
     */
    public static List<String> getBucketObjectName(String bucket) {
        boolean exsit = exitsBucket(bucket);
        if (exsit) {
            List<String> listObjetcName = new ArrayList<>();
            try {
                Iterable<Result<Item>> myObjects = MinioClientSingleton.getMinioClient().listObjects(ListObjectsArgs.builder().bucket(bucket).build());
                for (Result<Item> result : myObjects) {
                    Item item = result.get();
                    listObjetcName.add(item.objectName());
                }
                return listObjetcName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 以流的形式获取一个文件对象
     *
     * @param bucket     桶名称
     * @param objectName 对象名称
     * @return {@link InputStream}
     */
    public static InputStream getObjectInputStream(String bucket, String objectName) {
        boolean exsit = exitsBucket(bucket);
        if (exsit) {
            try {
                ObjectStat objectStat = MinioClientSingleton.getMinioClient().statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
                if (objectStat.length() > 0) {
                    // 获取objectName的输入流。
                    return MinioClientSingleton.getMinioClient().getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 删除一个桶
     *
     * @param bucket 桶名称
     */
    public static boolean removeBucket(String bucket) throws Exception {
        // 删除之前先检查`my-bucket`是否存在。
        boolean found = exitsBucket(bucket);
        if (found) {
            Iterable<Result<Item>> myObjects = MinioClientSingleton.getMinioClient().listObjects(ListObjectsArgs.builder().bucket(bucket).build());
            for (Result<Item> result : myObjects) {
                Item item = result.get();
                //有对象文件，则删除失败
                if (item.size() > 0) {
                    return false;
                }
            }
            // 删除`bucketName`存储桶，注意，只有存储桶为空时才能删除成功。
            MinioClientSingleton.getMinioClient().removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
            found = exitsBucket(bucket);
            return !found;
        }
        return false;
    }

    /**
     * 获取某个桶下某个对象的URL
     *
     * @param bucket     桶名称
     * @param objectName 对象名 (文件夹名 + 文件名)
     * @return
     */
    public static String getBucketObject(String bucket, String objectName) throws Exception {
        // 删除之前先检查`my-bucket`是否存在。
        boolean found = exitsBucket(bucket);
        if (found) {
            return MinioClientSingleton.getMinioClient().getObjectUrl(bucket, objectName);
        }
        return "";
    }

    /**
     * 根据文件路径得到预览文件绝对地址
     *
     * @param bucket     桶名称
     * @param objectName 对象名 (文件夹名+文件名)
     * @return
     */
    public String getPreviewFileUrl(String bucket, String objectName) {
        try {
            return MinioClientSingleton.getMinioClient().presignedGetObject(bucket, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void main(String[] args) {
        try {
            String bucket = "pic";
            String filename = System.currentTimeMillis() + "截图.png";
            String fileFullPath = "C:\\Users\\刘苏义\\Pictures\\Camera Roll\\刘苏义.jpg";

            String fileUrl = putObjectLocalFile(bucket, filename, fileFullPath);
            System.out.println(fileUrl);
        }catch (Exception EX)
        {
            log.error(EX.getMessage());
        }
    }
}