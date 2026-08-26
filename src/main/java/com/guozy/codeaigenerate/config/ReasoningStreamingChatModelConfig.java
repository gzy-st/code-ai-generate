package com.guozy.codeaigenerate.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * Vue 项目生成专用流式模型
     * <p>
     * Vue 项目生成依赖 {@link com.guozy.codeaigenerate.ai.tools.FileWriteTool} 工具调用写入文件，
     * 因此必须使用支持 function calling 的模型，否则 TokenStream 不会触发工具调用，
     * 文件无法写入磁盘。
     * <p>
     * <b>注意</b>：deepseek-reasoner 是推理模型，但 <b>不支持工具调用</b>，
     * 不能用于 Vue 项目生成场景。
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 测试环境使用 deepseek-chat：支持工具调用，价格便宜
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
        // 生产环境推荐使用支持 function calling 的模型，例如：
        // final String modelName = "gpt-4o";
        // final int maxTokens = 32768;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}