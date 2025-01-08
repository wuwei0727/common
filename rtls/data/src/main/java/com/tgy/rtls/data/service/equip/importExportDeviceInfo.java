package com.tgy.rtls.data.service.equip;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface importExportDeviceInfo {
    /**
     * @param map
     * @param response
     * @throws IOException
     */
    void exportDetector(String map, HttpServletResponse response) throws IOException;

}
