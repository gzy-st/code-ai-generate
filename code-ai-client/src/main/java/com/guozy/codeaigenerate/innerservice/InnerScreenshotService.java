package com.guozy.codeaigenerate.innerservice;

/**
 * 内部截图服务（Dubbo 接口）
 */
public interface InnerScreenshotService {

    /**
     * 对指定网页生成截图并上传到 COS，返回可访问的 URL
     *
     * @param webUrl 网页地址
     * @return 截图的 COS URL
     */
    String generateAndUploadScreenshot(String webUrl);
}