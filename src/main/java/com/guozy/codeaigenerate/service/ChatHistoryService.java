package com.guozy.codeaigenerate.service;

import com.guozy.codeaigenerate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.guozy.codeaigenerate.model.entity.ChatHistory;
import com.guozy.codeaigenerate.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 加载历史对话到记忆中
     * @param appId 应用ID
     * @param chatMemory 记忆对象
     * @param maxCount 最大加载条数
     * @return 加载的条数
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 添加聊天消息
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用ID删除聊天消息
     * @param appId
     * @return
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 根据应用ID分页获取聊天消息
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser);

    /**
     * 统计应用的对话轮次
     *
     * @param appId 应用ID
     * @return 对话轮次
     */
    long countChatRoundByAppId(Long appId);

    /**
     * 导出应用对话历史为 Markdown
     *
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return Markdown 文件字节数组
     */
    byte[] exportChatHistoryToMarkdown(Long appId, User loginUser);

    /**
     * 智能总结对话历史（满足条件时异步执行）
     *
     * @param appId 应用ID
     */
    void trySummarizeIfNeeded(Long appId);
}