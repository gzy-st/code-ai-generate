package com.guozy.codeaigenerate.controller;

import com.guozy.codeaigenerate.common.BaseResponse;
import com.guozy.codeaigenerate.common.ResultUtils;
import com.guozy.codeaigenerate.langgraph4j.CodeGenWorkflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * LangGraph4j 代码生成工作流接口
 */
@Tag(name = "工作流", description = "LangGraph4j 代码生成工作流")
@RestController
@RequestMapping("/api/workflow")
@Slf4j
public class WorkflowSseController {

    @Resource
    private CodeGenWorkflow codeGenWorkflow;

    /**
     * 执行工作流（SSE 流式输出）
     */
    @Operation(summary = "执行代码生成工作流（SSE）")
    @GetMapping(value = "/execute-sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter executeWorkflowSse(@RequestParam String prompt) {
        log.info("执行工作流 SSE, prompt={}", prompt);
        return codeGenWorkflow.executeWorkflowWithSse(prompt);
    }

    /**
     * 执行工作流（Flux 流式输出，返回 text/event-stream）
     */
    @Operation(summary = "执行代码生成工作流（Flux）")
    @GetMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowFlux(@RequestParam String prompt) {
        log.info("执行工作流 Flux, prompt={}", prompt);
        return codeGenWorkflow.executeWorkflowWithFlux(prompt);
    }

    /**
     * 执行工作流（POST body）
     */
    @Operation(summary = "执行代码生成工作流（POST）")
    @PostMapping(value = "/execute-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> executeWorkflowFluxPost(@RequestBody Map<String, String> body) {
        String prompt = body.getOrDefault("prompt", "");
        return codeGenWorkflow.executeWorkflowWithFlux(prompt);
    }
}