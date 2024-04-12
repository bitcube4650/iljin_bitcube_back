package iljin.framework.ebid.etc.util.common.excel.utils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface ExcelSupport {
    void downLoadExcel(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response);

    void downLoadCsv(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) throws IOException, IllegalAccessException;
}
