package com.guozy.codeaigenerate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class CodeAiGenerateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAiGenerateApplication.class, args);
    }

}
