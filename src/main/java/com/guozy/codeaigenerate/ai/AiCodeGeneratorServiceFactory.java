package com.guozy.codeaigenerate.ai;

import com.guozy.codeaigenerate.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI代码生成服务工厂
 */
@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 默认提供一个 Bean
     * 使用 @MemoryId 按 appId 隔离对话记忆
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> {
                    MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                            .id(memoryId)
                            .chatMemoryStore(redisChatMemoryStore)
                            .maxMessages(50)
                            .build();
                    // 如果该应用的记忆为空，从数据库加载历史对话
                    if (chatMemory.messages().isEmpty()) {
                        try {
                            Long appId = Long.valueOf(memoryId.toString());
                            chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
                        } catch (NumberFormatException e) {
                            log.warn("MemoryId 不是有效的 appId: {}", memoryId);
                        }
                    }
                    return chatMemory;
                })
                .build();
    }

}