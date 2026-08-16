package com.guozy.codeaigenerate;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
@MapperScan("com.guozy.codeaigenerate.mapper")
public class CodeAiGenerateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAiGenerateApplication.class, args);
    }

}
