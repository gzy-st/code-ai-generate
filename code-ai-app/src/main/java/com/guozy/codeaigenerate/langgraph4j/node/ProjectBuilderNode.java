package com.guozy.codeaigenerate.langgraph4j.node;

import com.guozy.codeaigenerate.core.builder.VueProjectBuilder;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.langgraph4j.state.WorkflowContext;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;
import com.guozy.codeaigenerate.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 项目构建节点：Vue 项目类型执行 npm install + npm run build
 */
@Slf4j
public class ProjectBuilderNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 项目构建");
            String generatedCodeDir = context.getGeneratedCodeDir();
            CodeGenTypeEnum generationType = context.getGenerationType();
            String buildResultDir;
            try {
                VueProjectBuilder vueBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                boolean buildSuccess = vueBuilder.buildProject(generatedCodeDir);
                if (buildSuccess) {
                    buildResultDir = generatedCodeDir + File.separator + "dist";
                    log.info("Vue 项目构建成功，dist 目录: {}", buildResultDir);
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败");
                }
            } catch (Exception e) {
                log.error("Vue 项目构建异常: {}", e.getMessage(), e);
                buildResultDir = generatedCodeDir;
            }
            context.setCurrentStep("项目构建");
            context.setBuildResultDir(buildResultDir);
            return WorkflowContext.saveContext(context);
        });
    }
}