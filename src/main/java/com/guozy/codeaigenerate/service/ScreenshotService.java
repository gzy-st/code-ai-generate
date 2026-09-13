package com.guozy.codeaigenerate.service;

/**
 * ScreenshotService
 *
 * @author guozy
 * @createDate 2026/9/12 16:18
 */
public interface ScreenshotService {
    /**
     * Generate and upload screenshot
     *
     * @param webUrl
     * @return
     */
    String generateAndUploadScreenshot(String webUrl);
}