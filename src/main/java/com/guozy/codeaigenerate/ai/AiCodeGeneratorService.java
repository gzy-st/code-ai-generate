package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/singlefile.text")
    HtmlCodeResult generateCodeWithSingleFile(String userMessage);
    /**
     * 生成多文件
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/morefile.text")
    MultiFileCodeResult generateCodeWithMultipleFiles(String userMessage);
}
