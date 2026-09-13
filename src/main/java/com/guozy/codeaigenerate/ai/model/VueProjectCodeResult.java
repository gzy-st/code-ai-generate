package com.guozy.codeaigenerate.ai.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Vue 项目代码生成结果
 * 以文件相对路径为 key，文件内容为 value
 */
@Data
public class VueProjectCodeResult {

    /**
     * 文件映射：key 为相对路径（如 package.json），value 为文件内容
     */
    private Map<String, String> files = new HashMap<>();

    /**
     * 添加文件
     *
     * @param relativePath 相对路径
     * @param content      文件内容
     */
    public void addFile(String relativePath, String content) {
        if (relativePath == null || content == null) {
            return;
        }
        files.put(relativePath.trim(), content);
    }
}