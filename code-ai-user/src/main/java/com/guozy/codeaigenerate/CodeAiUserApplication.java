package com.guozy.codeaigenerate;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.guozy.codeaigenerate")
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.guozy.codeaigenerate.mapper")
public class CodeAiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAiUserApplication.class, args);
    }
}