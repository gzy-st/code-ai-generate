package com.guozy.codeaigenerate.ai.monitor;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * AI 模型监听器：挂载到 StreamingChatModel，采集请求/响应/错误/Token/耗时指标
 */
@Component
public class AiModelMonitorListener implements ChatModelListener {

    private static final String REQUEST_START_TIME_KEY = "request_start_time";
    private static final String MONITOR_CONTEXT_KEY = "monitor_context";

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestContext.attributes().put(REQUEST_START_TIME_KEY, Instant.now());
        MonitorContext monitorContext = MonitorContextHolder.getContext();
        if (monitorContext == null) {
            monitorContext = MonitorContext.builder().userId("unknown").appId("unknown").build();
        }
        requestContext.attributes().put(MONITOR_CONTEXT_KEY, monitorContext);
        String modelName = requestContext.chatRequest().modelName();
        aiModelMetricsCollector.recordRequest(monitorContext.getUserId(), monitorContext.getAppId(), modelName, "started");
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Map<Object, Object> attributes = responseContext.attributes();
        MonitorContext context = (MonitorContext) attributes.get(MONITOR_CONTEXT_KEY);
        if (context == null) return;
        String modelName = responseContext.chatResponse().modelName();
        aiModelMetricsCollector.recordRequest(context.getUserId(), context.getAppId(), modelName, "success");
        recordResponseTime(attributes, context.getUserId(), context.getAppId(), modelName);
        recordTokenUsage(responseContext, context.getUserId(), context.getAppId(), modelName);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        MonitorContext context = MonitorContextHolder.getContext();
        if (context == null) {
            context = MonitorContext.builder().userId("unknown").appId("unknown").build();
        }
        String modelName = errorContext.chatRequest().modelName();
        String errorMessage = errorContext.error().getMessage();
        aiModelMetricsCollector.recordRequest(context.getUserId(), context.getAppId(), modelName, "error");
        aiModelMetricsCollector.recordError(context.getUserId(), context.getAppId(), modelName, errorMessage);
        recordResponseTime(errorContext.attributes(), context.getUserId(), context.getAppId(), modelName);
    }

    private void recordResponseTime(Map<Object, Object> attributes, String userId, String appId, String modelName) {
        Instant startTime = (Instant) attributes.get(REQUEST_START_TIME_KEY);
        if (startTime != null) {
            aiModelMetricsCollector.recordResponseTime(userId, appId, modelName, Duration.between(startTime, Instant.now()));
        }
    }

    private void recordTokenUsage(ChatModelResponseContext responseContext, String userId, String appId, String modelName) {
        TokenUsage tokenUsage = responseContext.chatResponse().metadata().tokenUsage();
        if (tokenUsage != null) {
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "input", tokenUsage.inputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "output", tokenUsage.outputTokenCount());
            aiModelMetricsCollector.recordTokenUsage(userId, appId, modelName, "total", tokenUsage.totalTokenCount());
        }
    }
}