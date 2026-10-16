package com.guozy.codeaigenerate.controller;

import com.guozy.codeaigenerate.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@Slf4j
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * 调试端点：确认控制器已注册
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info("StaticResourceController ping 被调用, PREVIEW_ROOT_DIR={}", PREVIEW_ROOT_DIR);
        return ResponseEntity.ok("StaticResourceController OK, root=" + PREVIEW_ROOT_DIR);
    }

    /**
     * 提供静态资源访问
     * 访问格式：http://localhost:8125/api/static/{deployKey}/{filePath}
     */
    @GetMapping("/{deployKey}/{*filePath}")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            @PathVariable(required = false) String filePath,
            HttpServletRequest request) {
        try {
            log.info("静态资源请求: deployKey={}, filePath={}", deployKey, filePath);
            // 默认返回 index.html
            if (filePath == null) {
                filePath = "index.html";
            } else if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            if (filePath.isEmpty()) {
                filePath = "index.html";
            }
            // 构建文件路径
            String fullPath = PREVIEW_ROOT_DIR + File.separator + deployKey + File.separator + filePath;
            File file = new File(fullPath);
            log.info("查找文件: {}", fullPath);
            if (!file.exists()) {
                log.warn("文件不存在: {}", fullPath);
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(fullPath))
                    .body(resource);
        } catch (Exception e) {
            log.error("静态资源服务异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根路径访问重定向到 index.html
     */
    @GetMapping("/{deployKey}")
    public ResponseEntity<Resource> serveIndex(@PathVariable String deployKey) {
        return serveStaticResource(deployKey, "index.html", null);
    }

    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
