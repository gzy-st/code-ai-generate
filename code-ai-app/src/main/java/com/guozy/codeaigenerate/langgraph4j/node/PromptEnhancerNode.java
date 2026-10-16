package com.guozy.codeaigenerate.langgraph4j.node;

import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 提示词增强节点
 * 当前直接使用原始提示词作为增强结果，后续可接入 AI 进行提示词优化。
 */
@Slf4j
public class PromptEnhancerNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 提示词增强");
            context.setEnhancedPrompt(context.getOriginalPrompt());
            context.setCurrentStep("提示词增强");
            return WorkflowContext.saveContext(context);
        });
    }
}