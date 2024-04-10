package iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator;

import iljin.framework.ebid.etc.statistics.dto.BiInfoDetailDto;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.dto.BiInfoDetailExcelDto;
import iljin.framework.ebid.etc.util.common.excel.dto.CompanyBidPerformanceExcelDto;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelSupportV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ConcreteCompanyBidPerformance extends ExcelSupportV2 {

    @Autowired
    ExcelRepository excelRepository;


    @Override
    public SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        row = sheet.createRow(0);


        //엑셀 헤더 작성
        createHeaders(workbook, row, cell, headerNames);

        //엑셀 바디 작성
        List<BiInfoDto> data = new ArrayList<>();

        int offset = 0;
        int limit = 1000;


        int biInfoListCnt = excelRepository.findBiInfoListCnt(param);

        while(offset < biInfoListCnt) {
            data = excelRepository.findBiInfoList(param, offset, limit);
            List<CompanyBidPerformanceExcelDto> excelData = new ArrayList<>();

            for(int i = 0; i < data.size(); i++) {
                CompanyBidPerformanceExcelDto companyBidPerformanceExcelDto = new CompanyBidPerformanceExcelDto();

                String interrelatedNm = CommonUtils.getString(data.get(i).getInterrelatedNm(), "");
                String cnt = CommonUtils.getString(data.get(i).getCnt(), "");
                BigDecimal bdAnt = data.get(i).getBdAnt();
                BigDecimal succAmt = data.get(i).getSuccAmt();
                BigDecimal mamt = data.get(i).getMamt();

                companyBidPerformanceExcelDto.setInterrelatedNm(interrelatedNm);
                companyBidPerformanceExcelDto.setCnt(CommonUtils.getFormatNumber(cnt));
                companyBidPerformanceExcelDto.setBdAnt(bdAnt);
                companyBidPerformanceExcelDto.setSuccAmt(succAmt);
                companyBidPerformanceExcelDto.setMamt(mamt);

                excelData.add(companyBidPerformanceExcelDto);
            }

            int start = offset;

            createbody(clazz, workbook, excelData, sheet, row, cell, start);

            offset += limit;


            excelData.clear();
            ((SXSSFSheet) sheet).flushRows(limit);
        }
        return workbook;
    }

    @Override
    public void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {
        /**
         * header font style
         */
        Font headerFont = workbook.createFont();
        //headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        //font.setColor((short) 255);

        /**
         * header cell style
         */
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);       // 가로 가운데 정렬
        headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로 가운데 정렬
        headerCellStyle.setFont(headerFont);

        //테두리 설정
        headerCellStyle.setBorderLeft(BorderStyle.MEDIUM);
        headerCellStyle.setBorderRight(BorderStyle.MEDIUM);
        headerCellStyle.setBorderTop(BorderStyle.MEDIUM);
        headerCellStyle.setBorderBottom(BorderStyle.MEDIUM);

        //배경 설정
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER );
        headerCellStyle.setFont(headerFont);


        for (int i = 0, size = headerNames.size(); i < size; i++) {
            cell = row.createCell(i);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

            cell.setCellStyle(headerCellStyle);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(headerNames.get(i));
        }
    }

    @Override
    public void createbody(Class<?> clazz, SXSSFWorkbook workbook, List<?> data, Sheet sheet, Row row, Cell cell, int rowNo) throws IllegalAccessException {
        int startRow = rowNo;
        /**
         * body font style
         */
        Font bodyFont = workbook.createFont();
        bodyFont.setBold(false);
        bodyFont.setFontHeightInPoints((short) 10);
        //font.setColor((short) 255);

        /**
         * body cell style
         */
        CellStyle bodyCellStyle = workbook.createCellStyle();
        bodyCellStyle.setAlignment(HorizontalAlignment.CENTER);       // 가로 가운데 정렬
        bodyCellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 세로 가운데 정렬
        bodyCellStyle.setFont(bodyFont);

        // 통화 서식 생성
        short currencyFormat = workbook.createDataFormat().getFormat("#,##0.00");

        for (Object o : data) {
            List<Object> fields = findFieldValue(clazz, o);
            row = sheet.createRow(++startRow);
            for (int i = 0, fieldSize = fields.size(); i < fieldSize; i++) {
                cell = row.createCell(i);

                bodyCellStyle.setBorderBottom(BorderStyle.THIN);
                bodyCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                bodyCellStyle.setBorderLeft(BorderStyle.THIN);
                bodyCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                bodyCellStyle.setBorderRight(BorderStyle.THIN);
                bodyCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                bodyCellStyle.setBorderTop(BorderStyle.THIN);
                bodyCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());



                cell.setCellStyle(bodyCellStyle);

                Object fieldValue = fields.get(i);
                if (fieldValue instanceof BigDecimal) {
                    // BigDecimal 값을 통화 형식으로 변환하여 셀에 설정
                    BigDecimal decimalValue = (BigDecimal) fieldValue;
                    String currencyValue = NumberFormat.getCurrencyInstance().format(decimalValue.doubleValue());
                    cell.setCellValue(currencyValue);
                } else {
                    // 다른 형식의 데이터는 그대로 셀에 설정
                    cell.setCellType(CellType.STRING);
                    cell.setCellValue(CommonUtils.getString(fieldValue, " "));
                }
            }
        }
    }

    @Override
    protected List<String> findHeaderNames(Class<?> clazz) {
        return super.findHeaderNames(clazz);
    }

    @Override
    protected List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
        return super.findFieldValue(clazz, obj);
    }
}
