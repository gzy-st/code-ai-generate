package com.guozy.codeaigenerate.langgraph4j.state;

import com.guozy.codeaigenerate.langgraph4j.model.QualityResult;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前执行步骤 */
    private String currentStep;

    /** 用户原始输入的提示词 */
    private String originalPrompt;

    /** 增强后的提示词 */
    private String enhancedPrompt;

    /** 代码生成类型 */
    private CodeGenTypeEnum generationType;

    /** 生成的代码目录 */
    private String generatedCodeDir;

    /** 构建成功的目录 */
    private String buildResultDir;

    /** 质量检查结果 */
    private QualityResult qualityResult;

    /** 错误信息 */
    private String errorMessage;

    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}