package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
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

}
