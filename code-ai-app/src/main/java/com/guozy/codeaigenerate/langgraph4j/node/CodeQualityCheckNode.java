package com.guozy.codeaigenerate.langgraph4j.node;

import com.guozy.codeaigenerate.langgraph4j.model.QualityResult;
import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 代码质量检查节点
 * 校验生成目录是否存在且包含文件；不通过则回流到 code_generator 重新生成。
 */
@Slf4j
public class CodeQualityCheckNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 代码质量检查");
            String generatedCodeDir = context.getGeneratedCodeDir();
            List<String> errors = new ArrayList<>();
            List<String> suggestions = new ArrayList<>();
            boolean valid = true;

            if (generatedCodeDir == null || generatedCodeDir.isBlank()) {
                valid = false;
                errors.add("生成的代码目录为空");
                suggestions.add("请检查代码生成节点是否正常执行");
            } else {
                File dir = new File(generatedCodeDir);
                if (!dir.exists() || !dir.isDirectory()) {
                    valid = false;
                    errors.add("代码目录不存在: " + generatedCodeDir);
                    suggestions.add("请确认 AI 已成功生成并保存代码");
                } else {
                    File[] files = dir.listFiles();
                    if (files == null || files.length == 0) {
                        valid = false;
                        errors.add("代码目录为空，未生成任何文件");
                        suggestions.add("请检查提示词是否清晰，或重试生成");
                    }
                }
            }

            QualityResult result = QualityResult.builder()
                    .isValid(valid)
                    .errors(errors)
                    .suggestions(suggestions)
                    .build();
            context.setQualityResult(result);
            context.setCurrentStep("代码质量检查");
            log.info("代码质量检查结果: isValid={}, errors={}", valid, errors);
            return WorkflowContext.saveContext(context);
        });
    }
}