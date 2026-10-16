package com.guozy.codeaigenerate.langgraph4j;

import cn.hutool.json.JSONUtil;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.langgraph4j.model.QualityResult;
import com.guozy.codeaigenerate.langgraph4j.node.CodeGeneratorNode;
import com.guozy.codeaigenerate.langgraph4j.node.CodeQualityCheckNode;
import com.guozy.codeaigenerate.langgraph4j.node.ImageCollectorNode;
import com.guozy.codeaigenerate.langgraph4j.node.PromptEnhancerNode;
import com.guozy.codeaigenerate.langgraph4j.node.ProjectBuilderNode;
import com.guozy.codeaigenerate.langgraph4j.node.RouterNode;
import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 代码生成工作流
 * 节点：image_collector -> prompt_enhancer -> router -> code_generator -> code_quality_check -> project_builder
 * 质检条件边：build / skip_build / fail(回流 code_generator)
 */
@Slf4j
@Component
public class CodeGenWorkflow {

    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",
                                    "skip_build", END,
                                    "fail", "code_generator"
                            ))
                    .addEdge("project_builder", END)
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    /**
     * 执行工作流（Flux 流式输出）
     */
    public Flux<String> executeWorkflowWithFlux(String originalPrompt) {
        return Flux.create(sink -> Thread.startVirtualThread(() -> {
            try {
                CompiledGraph<MessagesState<String>> workflow = createWorkflow();
                WorkflowContext initialContext = WorkflowContext.builder()
                        .originalPrompt(originalPrompt)
                        .currentStep("初始化")
                        .build();
                sink.next(formatSseEvent("workflow_start", Map.of(
                        "message", "开始执行代码生成工作流",
                        "originalPrompt", originalPrompt
                )));
                GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                log.info("工作流图:\n{}", graph.content());

                int stepCounter = 1;
                for (NodeOutput<MessagesState<String>> step : workflow.stream(
                        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
                    WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                    if (currentContext != null) {
                        sink.next(formatSseEvent("step_completed", Map.of(
                                "stepNumber", stepCounter,
                                "currentStep", currentContext.getCurrentStep()
                        )));
                        log.info("第 {} 步完成: {}", stepCounter, currentContext.getCurrentStep());
                    }
                    stepCounter++;
                }
                sink.next(formatSseEvent("workflow_completed", Map.of(
                        "message", "代码生成工作流执行完成！"
                )));
                sink.complete();
            } catch (Exception e) {
                log.error("工作流执行失败: {}", e.getMessage(), e);
                sink.next(formatSseEvent("workflow_error", Map.of(
                        "error", e.getMessage(),
                        "message", "工作流执行失败"
                )));
                sink.error(e);
            }
        }));
    }

    /**
     * 执行工作流（SseEmitter 流式输出）
     */
    public SseEmitter executeWorkflowWithSse(String originalPrompt) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        Thread.startVirtualThread(() -> {
            try {
                CompiledGraph<MessagesState<String>> workflow = createWorkflow();
                WorkflowContext initialContext = WorkflowContext.builder()
                        .originalPrompt(originalPrompt)
                        .currentStep("初始化")
                        .build();
                sendSseEvent(emitter, "workflow_start", Map.of(
                        "message", "开始执行代码生成工作流",
                        "originalPrompt", originalPrompt
                ));
                int stepCounter = 1;
                for (NodeOutput<MessagesState<String>> step : workflow.stream(
                        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
                    WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                    if (currentContext != null) {
                        sendSseEvent(emitter, "step_completed", Map.of(
                                "stepNumber", stepCounter,
                                "currentStep", currentContext.getCurrentStep()
                        ));
                    }
                    stepCounter++;
                }
                sendSseEvent(emitter, "workflow_completed", Map.of(
                        "message", "代码生成工作流执行完成！"
                ));
                emitter.complete();
            } catch (Exception e) {
                log.error("工作流执行失败: {}", e.getMessage(), e);
                sendSseEvent(emitter, "workflow_error", Map.of(
                        "error", e.getMessage(),
                        "message", "工作流执行失败"
                ));
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    private String formatSseEvent(String eventType, Object data) {
        try {
            return "event: " + eventType + "\ndata: " + JSONUtil.toJsonStr(data) + "\n\n";
        } catch (Exception e) {
            return "event: error\ndata: {\"error\":\"格式化失败\"}\n\n";
        }
    }

    private void sendSseEvent(SseEmitter emitter, String eventType, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventType).data(data));
        } catch (IOException e) {
            log.error("发送 SSE 事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据质检结果决定下一步
     */
    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        if (qualityResult == null || Boolean.FALSE.equals(qualityResult.getIsValid())) {
            log.error("代码质检失败，重新生成代码");
            return "fail";
        }
        log.info("代码质检通过，继续后续流程");
        CodeGenTypeEnum generationType = context.getGenerationType();
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        return "build";
    }
}