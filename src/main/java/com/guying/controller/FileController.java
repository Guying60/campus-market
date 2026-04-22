package com.guying.controller;

import com.guying.pojo.Result;
import com.guying.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;


    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String suffix = Objects.requireNonNull(file.getOriginalFilename()).
                substring(file.getOriginalFilename().lastIndexOf("."));
        if (!".jpg".equals(suffix) && !".AVI".equals(suffix)  && !".MKV".equals(suffix) && !".MOV".equals(suffix)) {
            log.error("文件格式错误，实际后缀为: " + suffix);
            return Result.error("文件格式错误");
        }
        try {
            String url = aliyunOSSOperator.uploadFile(file.getInputStream(), file.getOriginalFilename());
            log.info("上传成功，访问路径: " + url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传失败");
            return Result.error("上传失败");
        }
    }
}
