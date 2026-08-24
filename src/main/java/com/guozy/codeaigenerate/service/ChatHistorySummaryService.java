package com.guozy.codeaigenerate.service;

import com.guozy.codeaigenerate.model.entity.ChatHistorySummary;
import com.mybatisflex.core.service.IService;

/**
 * 对话历史摘要服务层
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
public interface ChatHistorySummaryService extends IService<ChatHistorySummary> {

    /**
     * 获取应用最新的对话摘要
     *
     * @param appId 应用ID
     * @return 最新的摘要，不存在返回 null
     */
    ChatHistorySummary getLatestByAppId(Long appId);
}