package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码
     * @param appId 应用ID
     * @param userMessage 用户消息
     * @return HTML代码结果
     */
    @SystemMessage(fromResource = "prompt/singlefile.txt")
    HtmlCodeResult generateCodeWithSingleFile(@MemoryId Long appId, String userMessage);

    /**
     * 生成多文件
     * @param appId 应用ID
     * @param userMessage 用户消息
     * @return 多文件代码结果
     */
    @SystemMessage(fromResource = "prompt/morefile.txt")
    MultiFileCodeResult generateCodeWithMultipleFiles(@MemoryId Long appId, String userMessage);

    /**
     * 生成 HTML 代码（流式）
     * @param appId 应用ID
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/singlefile.txt")
    Flux<String> generateHtmlCodeStream(@MemoryId Long appId, String userMessage);

    /**
     * 生成多文件代码（流式）
     * @param appId 应用ID
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/morefile.txt")
    Flux<String> generateMultiFileCodeStream(@MemoryId Long appId, String userMessage);

}