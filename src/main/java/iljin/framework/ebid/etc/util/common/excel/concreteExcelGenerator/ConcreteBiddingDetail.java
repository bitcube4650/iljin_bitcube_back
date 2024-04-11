package iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator;

import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.dto.BidDetailListDto;
import iljin.framework.ebid.etc.util.common.excel.dto.BiddingDetailExcelDto;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelSupportV2;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
public class ConcreteBiddingDetail extends ExcelSupportV2 {

    @Autowired
    ExcelRepository excelRepository;

    @Override
    public SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        boolean commonHeaders = false;

        row = sheet.createRow(0);


        //입찰상세내역
        createHeaders(workbook, row, cell, headerNames);              //입찰이력과 헤더가 같음.

        //DATA 가공.
        List<BidDetailListDto> data = new ArrayList<>();

        int offset = 0;
        int limit = 1000;
        int cnt = 0;

        int bidDetailCnt = excelRepository.findBidDetailCnt(param);
        int bidDetailCnt2 = excelRepository.findBidDetailListCnt2(param);

        while(offset < bidDetailCnt2) {
            data = excelRepository.findBidDetailList2(param, offset, limit);
            List<BiddingDetailExcelDto> excelData = new ArrayList<>();

            String tmpBiNo = "";

            for(int i = 0; i < data.size(); i++) {
                if(!tmpBiNo.equals(data.get(i).getBiNo())) {
                    BiddingDetailExcelDto biddingDetailExcelDto = new BiddingDetailExcelDto();
                    biddingDetailExcelDto.setBiNo(data.get(i).getBiNo());
                    biddingDetailExcelDto.setBiName(data.get(i).getBiName());
                    biddingDetailExcelDto.setBdAmt(data.get(i).getBdAmt());
                    biddingDetailExcelDto.setSuccAmt(data.get(i).getSuccAmt());
                    biddingDetailExcelDto.setCustName(data.get(i).getCustName());
                    biddingDetailExcelDto.setEstStartDate(data.get(i).getEstStartDate());
                    biddingDetailExcelDto.setEstCloseDate(data.get(i).getEstCloseDate());
                    biddingDetailExcelDto.setUserName(data.get(i).getUserName());
                    biddingDetailExcelDto.setCustName2(data.get(i).getCustName2());
                    biddingDetailExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                    biddingDetailExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                    excelData.add(biddingDetailExcelDto);

                    tmpBiNo = data.get(i).getBiNo();
                } else {
                    BiddingDetailExcelDto biddingDetailExcelDto = new BiddingDetailExcelDto();
                    biddingDetailExcelDto.setBiNo("");
                    biddingDetailExcelDto.setBiName("");
                    biddingDetailExcelDto.setBdAmt(null);
                    biddingDetailExcelDto.setSuccAmt(null);
                    biddingDetailExcelDto.setCustName("");
                    biddingDetailExcelDto.setEstStartDate("");
                    biddingDetailExcelDto.setEstCloseDate("");
                    biddingDetailExcelDto.setUserName("");
                    biddingDetailExcelDto.setCustName2(data.get(i).getCustName2());
                    biddingDetailExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                    biddingDetailExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                    excelData.add(biddingDetailExcelDto);

                    tmpBiNo = data.get(i).getBiNo();
                }
            }

            int start = offset;

            createbody(clazz, workbook, excelData, sheet, row, cell, start);

            offset += limit;
            cnt++;

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
        headerFont.setFontHeightInPoints((short) 11);

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

        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER );
        headerCellStyle.setFont(headerFont);

        // 두 번째 행 생성 (1행)
        Row secondRow = row.getSheet().createRow(1);
        Cell cell2 = null;


        for (int i = 0, size = headerNames.size(); i < size; i++) {
            cell = row.createCell(i);
            cell2 = secondRow.createCell(i);

            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());

            cell.setCellStyle(headerCellStyle);
            cell2.setCellStyle(headerCellStyle);

            cell.setCellType(CellType.STRING);
            cell2.setCellType(CellType.STRING);

            // 7열까지는 0행과 1행 병합
            if (i <= 7) {
                CellRangeAddress mergedRegion = new CellRangeAddress(0, 1, i, i);
                row.getSheet().addMergedRegion(mergedRegion);

            }
            // 8열부터는 0행만 병합
            else if (i == 8) {
                CellRangeAddress mergedRegion = new CellRangeAddress(0, 0, i, i + 2);
                row.getSheet().addMergedRegion(mergedRegion);


                cell.setCellValue("투찰정보");


                cell2.setCellStyle(headerCellStyle);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(headerNames.get(i));
                // 1행은 병합하지 않고 그대로 나오도록 병합영역 삭제
            }

            if(i >= 9) {
                cell2.setCellStyle(headerCellStyle);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(headerNames.get(i));
            }

            // 첫 번째 행과 그 다음 행 병합 잘됨
            //  CellRangeAddress mergedRegion = new CellRangeAddress(0, 1, i, i);
            //  row.getSheet().addMergedRegion(mergedRegion);

/*          // 마지막 열과 그 다음 열 병합 (잘됨)
            if (i == size - 1) {
                 mergedRegion = new CellRangeAddress(0, 0, i - 1, i);
                row.getSheet().addMergedRegion(mergedRegion);
            }*/
            if(i != 8 ) {
                cell.setCellValue(headerNames.get(i));
            }
        }
    }

    @Override
    public void createbody(Class<?> clazz, SXSSFWorkbook workbook, List<?> data, Sheet sheet, Row row, Cell cell, int rowNo) throws IllegalAccessException {
        int startRow = rowNo + 1;
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
                    BigDecimal decimalValue = (BigDecimal) fieldValue;
                    bodyCellStyle.setDataFormat(cell.getSheet().getWorkbook().createDataFormat().getFormat("#,##0")); // 숫자 서식을 설정합니다.

                    // 셀에 값을 설정할 때 숫자 형식으로 설정합니다.
                    cell.setCellType(CellType.NUMERIC);

                    // 셀에 값을 설정합니다.
                    cell.setCellValue(decimalValue.doubleValue());

                    // 셀에 숫자 서식을 적용합니다.
                    cell.setCellStyle(bodyCellStyle);
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
