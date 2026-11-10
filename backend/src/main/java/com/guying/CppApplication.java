package com.guying;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("com.guying.mapper")
// 排除掉不兼容 Spring Boot 4 路径的 MyBatis 相关自动配置类
@SpringBootApplication(excludeName = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration",
        "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
public class CppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CppApplication.class, args);
    }

}