package com.guozy.codeaigenerate.langgraph4j.node;

import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片素材收集节点
 * 当前项目未集成图片素材 API，此处为占位节点（直接透传），后续可扩展。
 */
@Slf4j
public class ImageCollectorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片素材收集（占位，跳过）");
            context.setCurrentStep("图片素材收集");
            return WorkflowContext.saveContext(context);
        });
    }
}