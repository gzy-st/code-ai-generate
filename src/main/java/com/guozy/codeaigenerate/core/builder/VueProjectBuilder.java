package com.guozy.codeaigenerate.core.builder;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * VueProjectBuilder
 *
 * @author guozy
 * @createDate 2026/8/26 16:48
 */
@Slf4j
@Component
public class VueProjectBuilder {

    /**
     * 匹配 import 语句中的 @/views/xxx.vue、@/components/xxx.vue
     * 以及 ../views/xxx.vue、../components/xxx.vue
     * 支持静态导入 import X from '...' 和动态导入 import('...')
     */
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
            "import\\s+(?:[^'\"]+\\s+from\\s+)?['\"]((?:@/views/[^'\"]+)|(?:@/components/[^'\"]+)|(?:\\.\\./views/[^'\"]+)|(?:\\.\\./components/[^'\"]+))['\"]"
    );

    /**
     * 异步构建项目（不阻塞主流程）
     *
     * @param projectPath 项目路径
     */
    public void buildProjectAsync(String projectPath) {
        // 在单独的线程中执行构建，避免阻塞主流程
        Thread.ofVirtual().name("vue-builder-" + System.currentTimeMillis()).start(() -> {
            try {
                buildProject(projectPath);
            } catch (Exception e) {
                log.error("异步构建 Vue 项目时发生异常: {}", e.getMessage(), e);
            }
        });
    }

    /**
     * 修复 AI 生成项目中缺失的引用文件
     */
    private void fixMissingReferencedFiles(File projectDir) {
        File routerFile = new File(projectDir, "src/router/index.ts");
        if (!routerFile.exists()) {
            return;
        }
        String routerContent = FileUtil.readString(routerFile, StandardCharsets.UTF_8);
        Set<String> referencedPaths = new HashSet<>();
        Matcher matcher = IMPORT_PATTERN.matcher(routerContent);
        while (matcher.find()) {
            referencedPaths.add(matcher.group(1));
        }
        if (referencedPaths.isEmpty()) {
            return;
        }
        log.info("检测到 router 中引用文件 {} 个", referencedPaths.size());
        List<String> createdFiles = new ArrayList<>();
        File srcDir = new File(projectDir, "src");
        for (String refPath : referencedPaths) {
            File targetFile = resolveRefPath(srcDir, routerFile, refPath);
            if (targetFile != null && !targetFile.exists()) {
                FileUtil.touch(targetFile);
                String stubContent = buildStubContent(targetFile.getName());
                FileUtil.writeString(stubContent, targetFile, StandardCharsets.UTF_8);
                createdFiles.add(targetFile.getAbsolutePath());
            }
        }
        if (!createdFiles.isEmpty()) {
            log.warn("AI 生成的 Vue 项目缺少以下引用文件，已自动创建占位文件: {}", createdFiles);
        }
    }

    private File resolveRefPath(File srcDir, File routerFile, String refPath) {
        if (refPath.startsWith("@/")) {
            return new File(srcDir, refPath.substring(2)).toPath().normalize().toFile();
        }
        // ../views/xxx 或 ../components/xxx，基于 router 文件目录解析
        return routerFile.toPath().getParent().resolve(refPath).normalize().toFile();
    }

    private String buildStubContent(String fileName) {
        if (fileName.endsWith(".vue")) {
            return "<script setup lang=\"ts\">\n</script>\n\n<template>\n  <div class=\"p-4\">\n    <h1 class=\"text-xl font-bold\">" + fileName.replace(".vue", "") + "</h1>\n    <p>该页面由系统自动生成占位。</p>\n  </div>\n</template>\n";
        }
        return "";
    }

    /**
     * 修复 AI 生成项目中 tsconfig.json 配置错误
     */
    private void fixTsConfig(File projectDir) {
        File tsConfig = new File(projectDir, "tsconfig.json");
        File tsConfigNode = new File(projectDir, "tsconfig.node.json");
        if (!tsConfig.exists()) {
            return;
        }
        try {
            String content = FileUtil.readString(tsConfig, StandardCharsets.UTF_8);
            // 移除 include 中的 vite.config.ts，避免 vue-tsc 报错
            String fixed = content.replace("\"vite.config.ts\"", "")
                    .replace(", ,", ",")
                    .replace(", ]", "]")
                    .replace("[ ,", "[")
                    .replace("[ ]", "[]");
            if (!fixed.equals(content)) {
                FileUtil.writeString(fixed, tsConfig, StandardCharsets.UTF_8);
                log.info("已修复 tsconfig.json，移除 vite.config.ts 引用");
            }
        } catch (Exception e) {
            log.warn("修复 tsconfig.json 失败: {}", e.getMessage());
        }
        if (tsConfigNode.exists()) {
            try {
                String content = FileUtil.readString(tsConfigNode, StandardCharsets.UTF_8);
                // tsconfig.node.json 用于 vite.config.ts，composite 项目不能禁用 emit
                String fixed = content.replace("\"noEmit\": true", "\"noEmit\": false");
                if (!fixed.equals(content)) {
                    FileUtil.writeString(fixed, tsConfigNode, StandardCharsets.UTF_8);
                    log.info("已修复 tsconfig.node.json，将 noEmit 设为 false");
                }
            } catch (Exception e) {
                log.warn("修复 tsconfig.node.json 失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 执行命令并捕获输出
     *
     * @param workingDir     工作目录
     * @param command        命令字符串
     * @param timeoutSeconds 超时时间（秒）
     * @return 是否执行成功
     */
    private boolean executeCommand(File workingDir, String command, int timeoutSeconds) {
        try {
            log.info("在目录 {} 中执行命令: {}", workingDir.getAbsolutePath(), command);
            Process process = RuntimeUtil.exec(
                    null,
                    workingDir,
                    command.split("\\s+") // 命令分割为数组
            );
            // 异步读取标准输出和错误输出
            StringBuilder output = new StringBuilder();
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception e) {
                    log.warn("读取命令输出失败: {}", e.getMessage());
                }
            });
            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception e) {
                    log.warn("读取命令错误输出失败: {}", e.getMessage());
                }
            });
            stdoutThread.start();
            stderrThread.start();
            // 等待进程完成，设置超时
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                log.error("命令执行超时（{}秒），强制终止进程", timeoutSeconds);
                process.destroyForcibly();
                return false;
            }
            stdoutThread.join(5000);
            stderrThread.join(5000);
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                log.info("命令执行成功: {}", command);
                return true;
            } else {
                log.error("命令执行失败，退出码: {}\n输出:\n{}", exitCode, output);
                return false;
            }
        } catch (Exception e) {
            log.error("执行命令失败: {}, 错误信息: {}", command, e.getMessage(), e);
            return false;
        }
    }
    /**
     * 执行 npm install 命令
     */
    private boolean executeNpmInstall(File projectDir) {
        log.info("执行 npm install...");
        String command = String.format("%s install", buildCommand("npm"));
        return executeCommand(projectDir, command, 300); // 5分钟超时
    }

    /**
     * 执行 npm run build 命令
     */
    private boolean executeNpmBuild(File projectDir) {
        log.info("执行 npm run build...");
        String command = String.format("%s run build", buildCommand("npm"));
        return executeCommand(projectDir, command, 180); // 3分钟超时
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
    private String buildCommand(String baseCommand) {
        if (isWindows()) {
            return baseCommand + ".cmd";
        }
        return baseCommand;
    }
    /**
     * 构建 Vue 项目
     *
     * @param projectPath 项目根目录路径
     * @return 是否构建成功
     */
    public boolean buildProject(String projectPath) {
        File projectDir = new File(projectPath);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.error("项目目录不存在: {}", projectPath);
            return false;
        }
        // 检查 package.json 是否存在
        File packageJson = new File(projectDir, "package.json");
        if (!packageJson.exists()) {
            log.error("package.json 文件不存在: {}", packageJson.getAbsolutePath());
            return false;
        }
        log.info("开始构建 Vue 项目: {}", projectPath);
        // 修复 AI 可能遗漏的引用文件
        fixMissingReferencedFiles(projectDir);
        // 修复 tsconfig 配置错误
        fixTsConfig(projectDir);
        // 执行 npm install
        if (!executeNpmInstall(projectDir)) {
            log.error("npm install 执行失败");
            return false;
        }
        // 执行 npm run build
        if (!executeNpmBuild(projectDir)) {
            log.error("npm run build 执行失败");
            return false;
        }
        // 验证 dist 目录是否生成
        File distDir = new File(projectDir, "dist");
        if (!distDir.exists()) {
            log.error("构建完成但 dist 目录未生成: {}", distDir.getAbsolutePath());
            return false;
        }
        log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        return true;
    }

}
