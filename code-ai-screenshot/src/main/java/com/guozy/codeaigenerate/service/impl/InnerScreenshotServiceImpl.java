package com.guozy.codeaigenerate.service.impl;

import com.guozy.codeaigenerate.innerservice.InnerScreenshotService;
import com.guozy.codeaigenerate.service.ScreenshotService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 内部截图服务 Dubbo 实现
 */
@DubboService
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}