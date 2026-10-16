package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;


    @Test
    void generateCodeWithSingleFile() {
        HtmlCodeResult result = aiCodeGeneratorService.generateCodeWithSingleFile("做个郭志勇的博客，不超过20行");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateCodeWithMultipleFiles() {
        MultiFileCodeResult result = aiCodeGeneratorService.generateCodeWithMultipleFiles("做个郭志勇的留言板，不超过50行");
        Assertions.assertNotNull(result);

    }
/*    @Test
    void testChatMemory() {
        HtmlCodeResult result = aiCodeGeneratorService.generateCodeWithSingleFile(1, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateCodeWithSingleFile(1, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateCodeWithSingleFile(2, "做个程序员鱼皮的工具网站，总代码量不超过 20 行");
        Assertions.assertNotNull(result);
        result = aiCodeGeneratorService.generateCodeWithSingleFile(2, "不要生成网站，告诉我你刚刚做了什么？");
        Assertions.assertNotNull(result);
    }*/

}