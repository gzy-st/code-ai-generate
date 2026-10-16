package com.guozy.codeaigenerate.controller;

import com.guozy.codeaigenerate.constant.AppConstant;
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

/**
 * 部署资源控制器
 * 服务已部署应用的静态资源（tmp/code_deploy 目录）
 * 访问格式：http://localhost:8125/api/deploy/{deployKey}/index.html
 */
@Slf4j
@RestController
@RequestMapping("/deploy")
public class DeployResourceController {

    private static final String DEPLOY_ROOT_DIR = AppConstant.CODE_DEPLOY_ROOT_DIR;

    @GetMapping("/{deployKey}/{*filePath}")
    public ResponseEntity<Resource> serveDeployResource(
            @PathVariable String deployKey,
            @PathVariable(required = false) String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                filePath = "index.html";
            } else if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
                if (filePath.isEmpty()) {
                    filePath = "index.html";
                }
            }
            String fullPath = DEPLOY_ROOT_DIR + File.separator + deployKey + File.separator + filePath;
            File file = new File(fullPath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, getContentType(fullPath))
                    .body(resource);
        } catch (Exception e) {
            log.error("部署资源服务异常, deployKey={}", deployKey, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{deployKey}")
    public ResponseEntity<Resource> serveDeployIndex(@PathVariable String deployKey) {
        return serveDeployResource(deployKey, "index.html");
    }

    private String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".json")) return "application/json; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".svg")) return "image/svg+xml";
        if (filePath.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}