package com.guozy.codeaigenerate.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.guozy.codeaigenerate.ai.ChatHistorySummarizer;
import com.guozy.codeaigenerate.common.ThrowUtils;
import com.guozy.codeaigenerate.constant.UserConstant;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.model.dto.chathistory.ChatHistoryQueryRequest;
import com.guozy.codeaigenerate.model.entity.App;
import com.guozy.codeaigenerate.model.entity.ChatHistorySummary;
import com.guozy.codeaigenerate.model.entity.User;
import com.guozy.codeaigenerate.model.enums.ChatHistoryMessageTypeEnum;
import com.guozy.codeaigenerate.service.AppService;
import com.guozy.codeaigenerate.service.ChatHistorySummaryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.guozy.codeaigenerate.model.entity.ChatHistory;
import com.guozy.codeaigenerate.mapper.ChatHistoryMapper;
import com.guozy.codeaigenerate.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    /**
     * 触发智能总结的消息阈值
     */
    private static final int SUMMARY_THRESHOLD = 30;

    /**
     * 始终保留的最近原始消息条数
     */
    private static final int RECENT_MESSAGE_COUNT = 20;

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private ChatHistorySummaryService chatHistorySummaryService;

    @Resource
    private ChatHistorySummarizer chatHistorySummarizer;

    /**
     * 时间格式化
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 如果记忆已存在，跳过加载，避免重复
            if (!chatMemory.messages().isEmpty()) {
                return 0;
            }
            int loadedCount = 0;
            // 1. 先加载智能摘要，作为 SystemMessage 放在记忆开头
            ChatHistorySummary latestSummary = chatHistorySummaryService.getLatestByAppId(appId);
            if (latestSummary != null && StrUtil.isNotBlank(latestSummary.getSummaryContent())) {
                chatMemory.add(SystemMessage.from(latestSummary.getSummaryContent()));
                loadedCount++;
            }
            // 2. 加载最近原始消息（offset 1 跳过当前用户最新消息，防止 LangChain4j 重复添加）
            int limit = Math.max(1, Math.min(maxCount, RECENT_MESSAGE_COUNT));
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, limit);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isNotEmpty(historyList)) {
                // 反转数组，确保按时间正序（老的在前，新的在后）
                historyList = historyList.reversed();
                // 按时间顺序添加到记忆中
                for (ChatHistory history : historyList) {
                    if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                        chatMemory.add(UserMessage.from(history.getMessage()));
                        loadedCount++;
                    } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                        chatMemory.add(AiMessage.from(history.getMessage()));
                        loadedCount++;
                    }
                }
            }
            log.info("成功为 appId: {} 加载记忆，摘要: {}, 原始消息: {}",
                    appId,
                    latestSummary != null ? "有" : "无",
                    loadedCount - (latestSummary != null ? 1 : 0));
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

    /**
     * 添加聊天消息
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    /**
     * 根据应用ID删除聊天消息
     * @param appId
     * @return
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }
    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 根据应用ID分页获取聊天消息
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public long countChatRoundByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            return 0L;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .eq(ChatHistory::getMessageType, ChatHistoryMessageTypeEnum.USER.getValue());
        return this.count(queryWrapper);
    }

    @Override
    public byte[] exportChatHistoryToMarkdown(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权导出该应用的对话历史");
        // 查询该应用的全部对话历史，按时间正序排列
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, true);
        List<ChatHistory> historyList = this.list(queryWrapper);
        // 生成 Markdown
        StringBuilder markdown = new StringBuilder();
        String appName = StrUtil.isNotBlank(app.getAppName()) ? app.getAppName() : "未命名应用";
        markdown.append("# ").append(appName).append(" 对话记录\n\n");
        if (CollUtil.isEmpty(historyList)) {
            markdown.append("> 暂无对话记录\n");
            return markdown.toString().getBytes(StandardCharsets.UTF_8);
        }
        int round = 0;
        for (int i = 0; i < historyList.size(); i++) {
            ChatHistory history = historyList.get(i);
            String role;
            if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                role = "用户";
                round++;
            } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                role = "AI";
            } else {
                role = "未知";
            }
            String time = history.getCreateTime() != null
                    ? history.getCreateTime().format(DATE_TIME_FORMATTER)
                    : "";
            markdown.append("## 第 ").append(round).append(" 轮 - ").append(role).append(" ").append(time).append("\n\n");
            markdown.append(history.getMessage()).append("\n\n");
        }
        return markdown.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Async
    public void trySummarizeIfNeeded(Long appId) {
        if (appId == null || appId <= 0) {
            return;
        }
        try {
            // 统计当前总消息数
            long totalMessageCount = this.count(QueryWrapper.create().eq(ChatHistory::getAppId, appId));
            if (totalMessageCount <= SUMMARY_THRESHOLD) {
                return;
            }
            // 如果已有摘要且新增消息数未达阈值，跳过
            ChatHistorySummary latestSummary = chatHistorySummaryService.getLatestByAppId(appId);
            if (latestSummary != null && totalMessageCount - latestSummary.getMessageCount() < SUMMARY_THRESHOLD) {
                return;
            }
            // 查询全部历史，按时间正序排列
            List<ChatHistory> allHistory = this.list(
                    QueryWrapper.create()
                            .eq(ChatHistory::getAppId, appId)
                            .orderBy(ChatHistory::getCreateTime, true)
            );
            if (CollUtil.isEmpty(allHistory) || allHistory.size() <= RECENT_MESSAGE_COUNT) {
                return;
            }
            // 需要被摘要的消息：除最近 RECENT_MESSAGE_COUNT 条外的所有历史
            List<ChatHistory> toSummarize = allHistory.subList(0, allHistory.size() - RECENT_MESSAGE_COUNT);
            String summaryContent = chatHistorySummarizer.summarize(toSummarize);
            if (StrUtil.isBlank(summaryContent)) {
                log.warn("应用 {} 的摘要生成结果为空，跳过保存", appId);
                return;
            }
            ChatHistorySummary summary = ChatHistorySummary.builder()
                    .appId(appId)
                    .summaryContent(summaryContent)
                    .messageCount(toSummarize.size())
                    .startTime(toSummarize.get(0).getCreateTime())
                    .endTime(toSummarize.get(toSummarize.size() - 1).getCreateTime())
                    .build();
            boolean saved = chatHistorySummaryService.save(summary);
            if (saved) {
                log.info("应用 {} 完成智能摘要，覆盖 {} 条历史消息", appId, toSummarize.size());
            }
        } catch (Exception e) {
            log.error("智能总结对话历史失败，appId: {}", appId, e);
        }
    }

}