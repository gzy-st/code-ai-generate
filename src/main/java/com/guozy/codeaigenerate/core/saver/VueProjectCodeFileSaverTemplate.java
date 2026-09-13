package com.guozy.codeaigenerate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.guozy.codeaigenerate.ai.model.VueProjectCodeResult;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.util.Map;

/**
 * Vue 项目代码保存模板
 */
public class VueProjectCodeFileSaverTemplate extends CodeFileSaverTemplate<VueProjectCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.VUE_PROJECT;
    }

    @Override
    protected void saveFiles(VueProjectCodeResult result, String baseDirPath) {
        for (Map.Entry<String, String> entry : result.getFiles().entrySet()) {
            String relativePath = entry.getKey();
            String content = entry.getValue();
            if (StrUtil.isBlank(relativePath) || content == null) {
                continue;
            }
            // 处理路径分隔符
            relativePath = relativePath.replace("/", File.separator).replace("\\", File.separator);
            writeToFile(baseDirPath, relativePath, content);
        }
    }

    @Override
    protected void validateInput(VueProjectCodeResult result) {
        super.validateInput(result);
        if (result.getFiles() == null || result.getFiles().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Vue 项目文件内容不能为空");
        }
        // 必须包含 package.json 和 vite.config.ts
        if (!result.getFiles().containsKey("package.json")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缺少 package.json");
        }
        if (!result.getFiles().containsKey("vite.config.ts") && !result.getFiles().containsKey("vite.config.js")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "缺少 vite.config.ts/js");
        }
    }
}