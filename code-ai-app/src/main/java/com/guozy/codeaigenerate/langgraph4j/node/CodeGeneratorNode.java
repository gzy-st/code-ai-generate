package com.guozy.codeaigenerate.langgraph4j.node;

import com.guozy.codeaigenerate.constant.AppConstant;
import com.guozy.codeaigenerate.core.AiCodeGeneratorFacade;
import com.guozy.codeaigenerate.langgraph4j.model.QualityResult;
import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;
import com.guozy.codeaigenerate.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码生成节点：调用 AI 生成代码并保存到本地
 */
@Slf4j
public class CodeGeneratorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码生成");
            String userMessage = buildUserMessage(context);
            CodeGenTypeEnum generationType = context.getGenerationType();
            AiCodeGeneratorFacade codeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            log.info("开始生成代码，类型: {}", generationType.getValue());
            // 工作流演示使用固定 appId=0
            Long appId = 0L;
            Flux<String> codeStream = codeGeneratorFacade.generateAndSaveCodeStream(userMessage, generationType, appId);
            codeStream.blockLast(Duration.ofMinutes(10));
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, generationType.getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(generatedCodeDir);
            return WorkflowContext.saveContext(context);
        });
    }

    private static String buildUserMessage(WorkflowContext context) {
        String userMessage = context.getEnhancedPrompt();
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            userMessage = buildErrorFixPrompt(qualityResult);
        }
        return userMessage;
    }

    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null && Boolean.FALSE.equals(qualityResult.getIsValid())
                && qualityResult.getErrors() != null && !qualityResult.getErrors().isEmpty();
    }

    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        qualityResult.getErrors().forEach(error -> errorInfo.append("- ").append(error).append("\n"));
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(s -> errorInfo.append("- ").append(s).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}