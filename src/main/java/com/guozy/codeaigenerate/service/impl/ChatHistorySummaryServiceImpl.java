package com.guozy.codeaigenerate.service.impl;

import com.guozy.codeaigenerate.mapper.ChatHistorySummaryMapper;
import com.guozy.codeaigenerate.model.entity.ChatHistorySummary;
import com.guozy.codeaigenerate.service.ChatHistorySummaryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 对话历史摘要服务实现
 *
 * @author <a href="https://github.com/gzy-st">程序员郭志勇</a>
 */
@Service
public class ChatHistorySummaryServiceImpl
        extends ServiceImpl<ChatHistorySummaryMapper, ChatHistorySummary>
        implements ChatHistorySummaryService {

    @Override
    public ChatHistorySummary getLatestByAppId(Long appId) {
        if (appId == null || appId <= 0) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistorySummary::getAppId, appId)
                .orderBy(ChatHistorySummary::getCreateTime, false)
                .limit(1);
        return this.getOne(queryWrapper);
    }
}