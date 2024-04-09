package iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.dto.*;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelSupportV2;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ConcreteBidCompleteList extends ExcelSupportV2 {
    @Autowired
    ExcelRepository excelRepository;

    @Autowired
    TCoUserRepository tCoUserRepository;

    @Override
    public SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        boolean commonHeaders = false;

        row = sheet.createRow(0);

        if("BidHistoryMatExcelDto".equals(clazz.getSimpleName())) {             //입찰이력 롯데머트리얼즈
            createHeadersBidHisMat(workbook, row, cell, headerNames);
        } else {
            createHeadersBidHis(workbook, row, cell, headerNames);
        }

        //DATA 가공.
        int offset = 0;
        int limit = 5000;
        int cnt = 0;

        int complateBidListCnt = excelRepository.findComplateBidListCnt(param);

        //로그인 유저가 롯데 에너지 머트리얼즈인지 확인
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();

        while(offset < complateBidListCnt) {
            List<BidCompleteDto> data = new ArrayList<>();

            //롯데 에너지 머트리얼즈 코드 값 '02'로 구분
            if("02".equals(userInterrelatedCustCode)) {
                List<BidHistoryMatExcelDto> excelData = new ArrayList<>();
                String tmpBiNo = "";
                data = excelRepository.findComplateBidList(param, offset, limit);

                for(int i = 0; i < data.size(); i++) {
                    if(!tmpBiNo.equals(data.get(i).getBiNo())) {
                        BidHistoryMatExcelDto bidHistoryMatExcelDto = new BidHistoryMatExcelDto();
                        bidHistoryMatExcelDto.setBiNo(data.get(i).getBiNo());
                        bidHistoryMatExcelDto.setMatDept(data.get(i).getMatDept());
                        bidHistoryMatExcelDto.setMatProc(data.get(i).getMatProc());
                        bidHistoryMatExcelDto.setMatCls(data.get(i).getMatCls());
                        bidHistoryMatExcelDto.setMatFactory(data.get(i).getMatFactory());
                        bidHistoryMatExcelDto.setMatFactoryLine(data.get(i).getMatFactoryLine());
                        bidHistoryMatExcelDto.setMatFactoryCnt(data.get(i).getMatFactoryCnt());
                        bidHistoryMatExcelDto.setBiName(data.get(i).getBiName());
                        bidHistoryMatExcelDto.setBdAmt(data.get(i).getBdAmt());
                        bidHistoryMatExcelDto.setSuccAmt(data.get(i).getSuccAmt());
                        bidHistoryMatExcelDto.setCustName(data.get(i).getCustName());
                        bidHistoryMatExcelDto.setEstStartDate(data.get(i).getEstStartDate());
                        bidHistoryMatExcelDto.setEstCloseDate(data.get(i).getEstCloseDate());
                        bidHistoryMatExcelDto.setUserName(data.get(i).getUserName());
                        bidHistoryMatExcelDto.setCustName2(data.get(i).getCustName2());
                        bidHistoryMatExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                        bidHistoryMatExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                        excelData.add(bidHistoryMatExcelDto);

                        tmpBiNo = data.get(i).getBiNo();
                    } else {
                        BidHistoryMatExcelDto bidHistoryMatExcelDto = new BidHistoryMatExcelDto();
                        bidHistoryMatExcelDto.setBiNo("");
                        bidHistoryMatExcelDto.setMatDept("");
                        bidHistoryMatExcelDto.setMatProc("");
                        bidHistoryMatExcelDto.setMatCls("");
                        bidHistoryMatExcelDto.setMatFactory("");
                        bidHistoryMatExcelDto.setMatFactoryLine("");
                        bidHistoryMatExcelDto.setMatFactoryCnt("");
                        bidHistoryMatExcelDto.setBiName("");
                        bidHistoryMatExcelDto.setBdAmt(null);
                        bidHistoryMatExcelDto.setSuccAmt(null);
                        bidHistoryMatExcelDto.setCustName("");
                        bidHistoryMatExcelDto.setEstStartDate("");
                        bidHistoryMatExcelDto.setEstCloseDate("");
                        bidHistoryMatExcelDto.setUserName("");
                        bidHistoryMatExcelDto.setCustName2(data.get(i).getCustName2());
                        bidHistoryMatExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                        bidHistoryMatExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                        excelData.add(bidHistoryMatExcelDto);

                        tmpBiNo = data.get(i).getBiNo();
                    }
                }
                int start = offset;

                createbody(clazz, workbook, excelData, sheet, row, cell, start);

                offset += limit;
                cnt++;

                excelData.clear();
                ((SXSSFSheet) sheet).flushRows(limit);

            } else {
                //롯데머트리얼즈 제외
                data = excelRepository.findComplateBidList(param, offset, limit);
                List<BidHistoryExcelDto> excelData = new ArrayList<>();

                String tmpBiNo = "";

                for(int i = 0; i < data.size(); i++) {
                    if(!tmpBiNo.equals(data.get(i).getBiNo())) {
                        BidHistoryExcelDto bidHistoryExcelDto = new BidHistoryExcelDto();
                        bidHistoryExcelDto.setBiName(data.get(i).getBiName());
                        bidHistoryExcelDto.setBiNo(data.get(i).getBiNo());
                        bidHistoryExcelDto.setBdAmt(data.get(i).getBdAmt());
                        bidHistoryExcelDto.setSuccAmt(data.get(i).getSuccAmt());
                        bidHistoryExcelDto.setCustName(data.get(i).getCustName());
                        bidHistoryExcelDto.setEstStartDate(data.get(i).getEstStartDate());
                        bidHistoryExcelDto.setEstCloseDate(data.get(i).getEstCloseDate());
                        bidHistoryExcelDto.setUserName(data.get(i).getUserName());
                        bidHistoryExcelDto.setCustName2(data.get(i).getCustName());
                        bidHistoryExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                        bidHistoryExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                        excelData.add(bidHistoryExcelDto);

                        tmpBiNo = data.get(i).getBiNo();
                    } else {
                        BidHistoryExcelDto bidHistoryExcelDto = new BidHistoryExcelDto();
                        bidHistoryExcelDto.setBiName("");
                        bidHistoryExcelDto.setBiNo("");
                        bidHistoryExcelDto.setBdAmt(null);
                        bidHistoryExcelDto.setSuccAmt(null);
                        bidHistoryExcelDto.setCustName("");
                        bidHistoryExcelDto.setEstStartDate("");
                        bidHistoryExcelDto.setEstCloseDate("");
                        bidHistoryExcelDto.setUserName("");
                        bidHistoryExcelDto.setCustName2(data.get(i).getCustName());
                        bidHistoryExcelDto.setEsmtAmt(data.get(i).getEsmtAmt());
                        bidHistoryExcelDto.setSubmitDate(data.get(i).getSubmitDate());
                        excelData.add(bidHistoryExcelDto);

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
        }
        return workbook;

    }

    @Override
    public void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {

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

    private void createHeadersBidHisMat(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {

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

        //배경 설정
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
            if (i <= 13) {
                CellRangeAddress mergedRegion = new CellRangeAddress(0, 1, i, i);
                row.getSheet().addMergedRegion(mergedRegion);

            }
            // 8열부터는 0행만 병합
            else if (i == 14) {
                CellRangeAddress mergedRegion = new CellRangeAddress(0, 0, i, i + 2);
                row.getSheet().addMergedRegion(mergedRegion);

                cell.setCellValue("투찰정보");
                cell2.setCellStyle(headerCellStyle);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(headerNames.get(i));
            }

            if(i >= 15) {
                cell2.setCellStyle(headerCellStyle);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(headerNames.get(i));
            }

            if(i != 14 ) {
                cell.setCellValue(headerNames.get(i));
            }
        }
    }



    private void createHeadersBidHis(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {

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


}
