package com.guying.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class AliyunOSSOperator {



    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public String uploadFile(InputStream inputStream, String originalFileName) {

        // 1. 检查 Key 是否存在 (防止你配置没读进去)
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            throw new RuntimeException("错误：未读取到 AccessKeyId，请检查宝塔启动命令或配置文件！");
        }

        // 2. 创建 OSSClient 实例 (最简单的标准写法)
        // 只要把 ID 和 Secret 传进去，它自己就会通过验证，不需要搞复杂的 EnvironmentVariableCredentialsProvider
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 3. 生成文件名逻辑
            String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String fileName = UUID.randomUUID().toString().replace("-", "");
            String suffix = "";
            if(originalFileName != null && originalFileName.contains(".")) {
                suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String objectName = folder + "/" + fileName + suffix;

            // 4. 上传
            ossClient.putObject(bucketName, objectName, inputStream);

            // 5. 返回 URL
            String cleanEndpoint = endpoint.replace("https://", "").replace("http://", "");
            String fileUrl = "https://" + bucketName + "." + cleanEndpoint + "/" + objectName;

            return fileUrl;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("OSS上传失败: " + e.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}