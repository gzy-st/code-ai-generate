package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/singlefile.txt")
    HtmlCodeResult generateCodeWithSingleFile(String userMessage);
    /**
     * 生成多文件
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/morefile.txt")
    MultiFileCodeResult generateCodeWithMultipleFiles(String userMessage);

    /**
     * 生成 HTML 代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/singlefile.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成多文件代码（流式）
     *
     * @param userMessage 用户消息
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/morefile.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 Vue 项目代码（流式）
     * <p>
     * 已不再使用 FileWriteTool 工具调用，改为由 AI 直接输出 markdown 代码块，
     * 后端解析并保存文件。因此返回 Flux<String> 纯文本流即可。
     *
     * @param appId        应用ID（作为 @MemoryId 实现对话记忆隔离）
     * @param userMessage  用户消息
     * @return 流式响应（markdown 格式的代码块）
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.txt")
    Flux<String> generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);

}