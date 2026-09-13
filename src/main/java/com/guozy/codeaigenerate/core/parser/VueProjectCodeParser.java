package com.guozy.codeaigenerate.core.parser;

import com.guozy.codeaigenerate.ai.model.VueProjectCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vue 项目代码解析器
 * 从 AI 输出的 markdown 中提取文件路径和文件内容
 *
 * 支持的格式示例：
 * ### File: package.json
 * ```json
 * { ... }
 * ```
 */
public class VueProjectCodeParser implements CodeParser<VueProjectCodeResult> {

    /**
     * 匹配文件路径和内容的正则表达式
     * 匹配包含 "File:" 标记的行，以及紧随其后的 markdown 代码块
     */
    private static final Pattern FILE_PATTERN = Pattern.compile(
            "(?m)^[^\\n]*File:\\s*([^\\n]+)\\s*$\\s*```[\\w]*\\s*\\n([\\s\\S]*?)```",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public VueProjectCodeResult parseCode(String codeContent) {
        VueProjectCodeResult result = new VueProjectCodeResult();
        if (codeContent == null || codeContent.isBlank()) {
            return result;
        }

        Matcher matcher = FILE_PATTERN.matcher(codeContent);
        while (matcher.find()) {
            String relativePath = matcher.group(1).trim();
            String fileContent = matcher.group(2);
            // 去除首尾空白但保留代码缩进
            result.addFile(relativePath, fileContent);
        }

        return result;
    }
}