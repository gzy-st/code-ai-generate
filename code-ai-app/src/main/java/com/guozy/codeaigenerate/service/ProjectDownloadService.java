package com.guozy.codeaigenerate.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ProjectDownloadService {
    /**
     * Download the project as a zip file.
     *
     * @param projectPath The path of the project to be downloaded.
     * @param downloadFileName The name of the downloaded file.
     * @param response The HTTP response object.
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
