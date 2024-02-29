package iljin.framework.core.excel.service;

import iljin.framework.core.excel.repository.ExcelRepository;
import iljin.framework.core.excel.utils.ExcelUtils;
import iljin.framework.core.excel.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExcelService {
    private final ExcelRepository excelRepository;
    private final ExcelUtils excelUtils;

    private static final int MAX_ROW = 15;

    /*
     * 공통 엑셀 다운로드.
     * 현재 FileEntity로 하는 예시가 들어가 있음.
     */
    public void downLoadCommon(HttpServletResponse response) {

        // 데이터 조회.
        List<FileEntity> result = excelRepository.findAll();

        //10000건 이상 CSV, 10000건 미만 Excel
        if(result.size() >= MAX_ROW) {
            downLoadCsv(FileEntity.class, result, "downLoad", response);
        } else {
            downLoadExcel(FileEntity.class, result, "donLoad", response);
        }
    }

    /**
     * 공통 CSV 다운로드 10000건 이상
     */
    private void downLoadCsv(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        excelUtils.downLoadCsv(clazz, data, fileName, response);
    }

    /**
     * 공통 엑셀 다운로드 10000건 미만
     */
    private void downLoadExcel(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        excelUtils.downLoadExcel(clazz, data, fileName, response);
    }
}
