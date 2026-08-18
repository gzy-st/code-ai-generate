package com.guozy.codeaigenerate.core.saver;

import cn.hutool.core.util.StrUtil;
import com.guozy.codeaigenerate.ai.model.HtmlCodeResult;
import com.guozy.codeaigenerate.exception.BusinessException;
import com.guozy.codeaigenerate.exception.ErrorCode;
import com.guozy.codeaigenerate.model.enums.CodeGenTypeEnum;


/**
 * HtmlCodeFileSaverTemplate
 *
 * @author guozy
 * @createDate 2026/8/18 15:41
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}