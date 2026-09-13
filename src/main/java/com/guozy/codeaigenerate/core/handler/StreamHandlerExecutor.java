package com.guozy.codeaigenerate.core.handler;

import com.guozy.codeaigenerate.model.entity.User;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;
import com.guozy.codeaigenerate.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 所有代码生成类型（HTML、MULTI_FILE、VUE_PROJECT）统一使用简单文本流处理，
 * AI 直接输出 markdown 代码块，后端统一解析保存。
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @param codeGenType        代码生成类型
     * @return 处理后的流
     */
    public Flux<String> doExecute(Flux<String> originFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser, CodeGenTypeEnum codeGenType) {
        // 三种模式现在都是纯文本 markdown 代码块流，统一用 SimpleTextStreamHandler
        return new SimpleTextStreamHandler().handle(originFlux, chatHistoryService, appId, loginUser);
    }
}