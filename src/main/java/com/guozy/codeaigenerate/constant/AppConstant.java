package com.guozy.codeaigenerate.constant;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface AppConstant {

    /**
     * 精选应用的优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 项目根目录
     * 优先读取系统属性 project.root，否则从 user.dir 向上查找 pom.xml 定位项目根目录
     */
    String PROJECT_ROOT_DIR = resolveProjectRoot();

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = PROJECT_ROOT_DIR + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = PROJECT_ROOT_DIR + "/tmp/code_deploy";

    /**
     * 应用部署域名（Nginx 静态资源服务器地址）
     */
    String CODE_DEPLOY_HOST = "http://localhost:8188";

    /**
     * 解析项目根目录
     */
    static String resolveProjectRoot() {
        String explicitRoot = System.getProperty("project.root");
        if (explicitRoot != null && !explicitRoot.isBlank()) {
            return explicitRoot.replace("\\", "/");
        }
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        // 如果当前目录是 target，先回到项目根目录
        if (current.getFileName().toString().equalsIgnoreCase("target")) {
            current = current.getParent();
        }
        // 向上查找 pom.xml 或 src 目录作为项目根目录标识
        Path search = current;
        while (search != null) {
            if (search.resolve("pom.xml").toFile().exists()
                    || search.resolve("src").toFile().exists()) {
                return search.toString().replace("\\", "/");
            }
            search = search.getParent();
        }
        return current.toString().replace("\\", "/");
    }

}
