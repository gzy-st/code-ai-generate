package com.guozy.codeaigenerate.config;

import com.guozy.codeaigenerate.ai.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ReasoningStreamingChatModelConfig.ChatModelProperties.class)
public class ReasoningStreamingChatModelConfig {

    /**
     * Vue 项目生成专用流式模型配置属性
     */
    @Data
    @ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
    public static class ChatModelProperties {
        private String baseUrl;
        private String apiKey;
    }

    /**
     * Vue 项目生成专用流式模型
     * <p>
     * Vue 项目生成依赖 FileWriteTool 工具调用写入文件，
     * 因此必须使用支持 function calling 的模型。
     * 挂载 AiModelMonitorListener 采集请求/响应/Token/耗时指标。
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel(ChatModelProperties properties,
                                                          ObjectProvider<AiModelMonitorListener> listenerProvider) {
        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = OpenAiStreamingChatModel.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true);
        AiModelMonitorListener listener = listenerProvider.getIfAvailable();
        if (listener != null) {
            builder.listeners(java.util.List.of(listener));
        }
        return builder.build();
    }
}