package com.guozy.codeaigenerate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.guozy.codeaigenerate.ai.model.MultiFileCodeResult;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;

/**
 * MultiFileCodeFileSaverTemplate
 *
 * @author guozy
 * @createDate 2026/8/18 15:44
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "index.css", result.getCssCode());
        writeToFile(baseDirPath, "index.js", result.getJsCode());
    }
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有 HTML 代码，CSS 和 JS 可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}