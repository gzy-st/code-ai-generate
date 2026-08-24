package com.guozy.codeaigenerate.ai;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.guozy.codeaigenerate.model.entity.ChatHistory;
import com.guozy.codeaigenerate.model.enums.ChatHistoryMessageTypeEnum;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 对话历史 AI 总结器
 * <p>
 * 当对话轮次过多时，利用 AI 将早期对话压缩成摘要，
 * 在保留关键需求和代码上下文的同时节省 Token。
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
@Slf4j
@Component
public class ChatHistorySummarizer {

    @Resource
    private ChatModel chatModel;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成对话历史摘要
     *
     * @param histories 按时间正序排列的对话历史
     * @return 摘要内容，生成失败返回 null
     */
    public String summarize(List<ChatHistory> histories) {
        if (CollUtil.isEmpty(histories)) {
            return null;
        }
        try {
            StringBuilder historyText = new StringBuilder();
            for (ChatHistory history : histories) {
                String role = ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType()) ? "用户" : "AI";
                String time = history.getCreateTime() != null ? history.getCreateTime().format(DATE_TIME_FORMATTER) : "";
                historyText.append("[").append(time).append("] ").append(role).append("：\n")
                        .append(history.getMessage()).append("\n\n");
            }
            UserMessage prompt = UserMessage.from(
                    "请对以下对话历史进行高度概括，保留关键信息用于后续代码生成上下文：\n" +
                            "1. 用户的核心需求和目标；\n" +
                            "2. 已确认或已实现的功能、样式、交互细节；\n" +
                            "3. 尚未完成或待后续优化的点；\n" +
                            "4. 与代码相关的关键约束（如技术栈、页面数量、配色等）。\n\n" +
                            "请控制总结在 800 字以内，不要包含寒暄内容，只输出可用于 AI 理解的上下文摘要。\n\n" +
                            "对话历史：\n" + historyText
            );
            ChatResponse response = chatModel.chat(prompt);
            String summary = response.aiMessage().text();
            if (StrUtil.isBlank(summary)) {
                log.warn("AI 返回空摘要");
                return null;
            }
            return summary;
        } catch (Exception e) {
            log.error("生成对话历史摘要失败", e);
            return null;
        }
    }
}