package com.guozy.codeaigenerate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 截图服务启动类
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.guozy.codeaigenerate")
@EnableScheduling
public class CodeAiScreenshotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAiScreenshotApplication.class, args);
    }
}