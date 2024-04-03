package iljin.framework.ebid.etc.util.common.excel.utils;

import com.opencsv.CSVWriter;
import iljin.framework.ebid.etc.util.CommonUtils;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBiInfoDetailList;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteBidPresentList;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ConcreteCompanyBidPerformance;
import iljin.framework.ebid.etc.util.common.excel.concreteExcelGenerator.ExcelBiddingDetail;
import iljin.framework.ebid.etc.util.common.excel.dto.BidDetailListDto;
import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import iljin.framework.ebid.etc.util.common.excel.dto.BiddingDetailExcelDto;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.service.ExcelService;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class ExcelUtils implements ExcelSupport {

    private static final int MAX_ROW = 1000;

    @Autowired
    private ExcelRepository excelRepository;
    @Autowired
    private ConcreteCompanyBidPerformance concreteCompanyBidPerformance;
    @Autowired
    private ConcreteBiInfoDetailList concreteBiInfoDetailList;
    @Autowired
    private ExcelBiddingDetail excelBiddingDetail;
    @Autowired
    private ConcreteBidPresentList concreteBidPresentList;


    /**
     * CSV 다운로드
     */
    @Override
    public void downLoadCsv(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        try {
            log.info("----------------CSV Write Start----------------");
            response.setContentType("text/csv; charset=UTF-8"); // Set the character encoding
            fileName = URLEncoder.encode("회원정보.csv", "UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName + "\"");

            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(),
                    StandardCharsets.UTF_8);
            writer.write("\uFEFF");
            CSVWriter csvWriter = new CSVWriter(writer);

            csvWriter.writeAll(listMemberString(clazz, data));
            csvWriter.close();
            writer.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 다운로드
     */
    @Override
    public void downLoadExcel(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            int listSize = data.size();
            int start = 0;


            getWorkBook(clazz, workbook, start, findHeaderNames(clazz), data, listSize);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");



            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 다운로드 Pagination 적용.
     */
    @Override
    public void downLoadExcelPaging(Class<?> clazz, List<?> data, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            int listSize = data.size();
            int start = 0;

            getWorkBookPaging(clazz, workbook, start, findHeaderNames(clazz), data, listSize);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void downLoadExcelCompanyBidPerformance(Class<?> clazz, Map<String, Object> param, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteCompanyBidPerformance.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 다운로드 Pagination 적용.
     */

    public void downLoadExcelBiddingDetail(Class<?> clazz, Map<String, Object> param, String fileName, @NotNull HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            excelBiddingDetail.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void downLoadExcelBiddingStatus(Class<?> clazz, Map<String, Object> param, String fileName, @NotNull HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteBidPresentList.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), param);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Excel 다운로드 Pagination 적용.
     */

    public void downLoadExcelBiInfoDetailList(Class<?> clazz, Map<String, Object> params, String fileName, HttpServletResponse response) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            concreteBiInfoDetailList.getWorkBookPaging(clazz, workbook, findHeaderNames(clazz), params);

            response.setCharacterEncoding("UTF-8");  // UTF-8 설정 추가
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            workbook.close();

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            log.error("Excel Download Error Message = {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }














    private SXSSFWorkbook getWorkBook(Class<?> clazz, SXSSFWorkbook workbook, int rowIdx, List<String> headerNames, List<?> data, int maxSize) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet4"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(10); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;

        row = sheet.createRow(0);

        //케이스에 따른 엑셀 헤더 생성.
        if("BidHistoryMatExcelDto".equals(clazz.getSimpleName())) {
            createHeadersBidHisMat(workbook, row, cell, headerNames);
        } else if("BidHistoryExcelDto".equals(clazz.getSimpleName())) {
            createHeadersBidHis(workbook, row, cell, headerNames);

        } else {
            createHeaders(workbook, row, cell, headerNames);
        }

        //엑셀 내용 생성
        createBody(clazz, workbook, data, sheet, row, cell, rowIdx);

        // 주기적인 flush 진행
        ((SXSSFSheet) sheet).flushRows(maxSize);

        return workbook;
    }


    //createBody 부분에 Paging처리.
    private SXSSFWorkbook getWorkBookPaging(Class<?> clazz, SXSSFWorkbook workbook, int rowIdx, List<String> headerNames, List<?> data, int maxSize) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        boolean commonHeaders = false;

        row = sheet.createRow(0);


        //Excel Header를 따로 지정해야 할 떄 사용.
        if("BidHistoryMatExcelDto".equals(clazz.getSimpleName())) {             //입찰이력 롯데머트리얼즈
           createHeadersBidHisMat(workbook, row, cell, headerNames);
        } else if("BidHistoryExcelDto".equals(clazz.getSimpleName())) {         //입찰이력
            createHeadersBidHis(workbook, row, cell, headerNames);
        } else if("BiddingStatusDto".equals(clazz.getSimpleName())) {           //입찰현황
            createHeadersBiddingStatus(workbook, row, cell, headerNames);
        } else if("BiddingDetailExcelDto".equals(clazz.getSimpleName())) {           //입찰상세내역
            createHeadersBidHis(workbook, row, cell, headerNames);              //입찰이력과 헤더가 같음.
        } else {
            createHeaders(workbook, row, cell, headerNames);
            commonHeaders = true;
        }

        int listSize = data.size();
        int start = 0;


        /*
         * 인스턴스 변수 MAX_ROW 상수로 선언되어있음.
         */
        for (int i = 0; i < listSize; i += MAX_ROW) {
            int nextPage = Math.min(listSize, start + MAX_ROW);
            List<?> list = new ArrayList<>(data.subList(start, nextPage));

            createBodyPaging(clazz, workbook, list, sheet, row, cell, start , commonHeaders);

            list.clear();
            start += MAX_ROW;

            // 주기적인 flush 진행
            ((SXSSFSheet) sheet).flushRows(MAX_ROW);
        }




        return workbook;
    }

    //createBody 부분에 Paging처리.
    private SXSSFWorkbook getWorkBookPagingV2(Class<?> clazz, SXSSFWorkbook workbook, List<String> headerNames, Map<String, Object> param) throws IllegalAccessException, IOException {
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(12); // 디폴트 너비 설정

        Row row = null;
        Cell cell = null;
        boolean commonHeaders = false;

        row = sheet.createRow(0);


        //Excel Header를 따로 지정해야 할 떄 사용.
        if("BidHistoryMatExcelDto".equals(clazz.getSimpleName())) {             //입찰이력 롯데머트리얼즈
            createHeadersBidHisMat(workbook, row, cell, headerNames);
        } else if("BidHistoryExcelDto".equals(clazz.getSimpleName())) {         //입찰이력
            createHeadersBidHis(workbook, row, cell, headerNames);
        } else if("BiddingStatusDto".equals(clazz.getSimpleName())) {           //입찰현황
            createHeadersBiddingStatus(workbook, row, cell, headerNames);
        } else if("BiddingDetailExcelDto".equals(clazz.getSimpleName())) {           //입찰상세내역
            createHeadersBidHis(workbook, row, cell, headerNames);              //입찰이력과 헤더가 같음.
        } else {
            createHeaders(workbook, row, cell, headerNames);
            commonHeaders = true;
        }


        List<BidDetailListDto> data = new ArrayList<>();

        int offset = 0;
        int limit = 5000;
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
            int listSize = excelData.size();
            int start = offset;


            createBodyPaging(clazz, workbook, excelData, sheet, row, cell, start , commonHeaders);

/*            *//*
             * 인스턴스 변수 MAX_ROW 상수로 선언되어있음.
             *//*
            for (int i = 0; i < listSize; i += MAX_ROW) {
                int nextPage = Math.min(listSize, start + MAX_ROW);
                List<?> list = new ArrayList<>(excelData.subList(start, nextPage));

                createBodyPaging(clazz, workbook, list, sheet, row, cell, start , commonHeaders);

                list.clear();
                start += MAX_ROW;

                // 주기적인 flush 진행
                ((SXSSFSheet) sheet).flushRows(MAX_ROW);
            }*/





            offset += limit;
            cnt++;

            excelData.clear();
            ((SXSSFSheet) sheet).flushRows(MAX_ROW);

            System.out.println("-----------------------bidDetailCnt의 개수" + bidDetailCnt);
            System.out.println("-----------------------bidDetailCnt2의 개수" + bidDetailCnt2);

            System.out.println("------------------------쿼리 실행 개수" + cnt);
        }








        return workbook;
    }











    private void createHeaders(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {
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

    //통계>회사별입찰실적에서 사용할 헤더값.
    private void createHeadersBiddingStatus(SXSSFWorkbook workbook, Row row, Cell cell, List<String> headerNames) {

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

        headerNames.clear();


        CellRangeAddress mergedRegion = null;


        mergedRegion = new CellRangeAddress(0, 1, 0, 0);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 1, 2);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 3, 4);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 0, 5, 7);
        row.getSheet().addMergedRegion(mergedRegion);


        mergedRegion = new CellRangeAddress(0, 1, 8, 8);
        row.getSheet().addMergedRegion(mergedRegion);

        mergedRegion = new CellRangeAddress(0, 1, 9, 9);
        row.getSheet().addMergedRegion(mergedRegion);

        for (int i = 0; i < 10; i++) {
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


            if(i == 0) {
                cell.setCellValue("회사명");
            }

            if(i == 1) {
                cell.setCellValue("입찰계획");
                cell2.setCellValue("건수");
            }

            if(i == 2) {
                cell2.setCellValue("예산금액");
            }

            if(i == 3) {
                cell.setCellValue("입찰진행");
                cell2.setCellValue("건수");
            }

            if(i == 4) {
                cell.setCellValue("입찰진행");
                cell2.setCellValue("예산금액");
            }

            if(i == 5) {
                cell.setCellValue("입찰완료(유찰제외)");
                cell2.setCellValue("건수");
            }

            if(i == 6) {
                cell2.setCellValue("낙찰금액");
            }

            if(i == 7) {
                cell2.setCellValue("업체수/건수");
            }

            if(i == 8) {
                cell.setCellValue("등록업체수");
            }

            if(i == 9) {
                cell.setCellValue("기타");
            }
        }
    }


    private void createBody(Class<?> clazz, SXSSFWorkbook workbook,List<?> data, Sheet sheet, Row row, Cell cell, int rowNo) throws IllegalAccessException, IOException {
        int startRow = 1;

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

                // 주기적인 flush 진행
                if (rowNo % MAX_ROW == 0) {
                    ((SXSSFSheet) sheet).flushRows(MAX_ROW);
                }

            }
        }
    }
    private void createBodyPaging(Class<?> clazz, SXSSFWorkbook workbook,List<?> data, Sheet sheet,
                                  Row row, Cell cell, int rowNo, boolean commonheaders) throws IllegalAccessException, IOException {
        int startRow = rowNo + 1;

        //헤더가 공통이면
        if(commonheaders) {
            startRow = rowNo;
        }

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


    /**
     * 엑셀의 헤더 명칭을 찾는 로직
     */
    private List<String> findHeaderNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExcelColumnName.class))
                .map(field -> field.getAnnotation(ExcelColumnName.class).name())
                .collect(Collectors.toList());
    }

    /**
     * 엑셀의 데이터의 값을 추출하는 메서드
     */
    private List<Object> findFieldValue(Class<?> clazz, Object obj) throws IllegalAccessException {
        List<Object> result = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            result.add(field.get(obj));
        }
        return result;
    }

    private void setCellValueByType(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellType(CellType.STRING);
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellType(CellType.NUMERIC);
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            DateTimeFormatter dateFormatter = null;
            cell.setCellValue(((LocalDate) value).format(dateFormatter));
        }  else {
            // 기타 데이터 타입에 대한 처리
            cell.setCellValue((String) value);
        }
    }

    /**
     * CSV에 사용하는 List
     */
    public List<String[]> listMemberString(Class<?> clazz, List<?> data) throws IllegalAccessException {

        List<String[]> listStrings = new ArrayList<>();

        //헤더 값 추출
        List<String> headerNames = findHeaderNames(clazz);

        // 헤더 이름을 리스트에 추가합니다.
        listStrings.add(headerNames.toArray(new String[0]));

        //데이터 값 추출
        for (Object o : data) {
            List<Object> fields = findFieldValue(clazz, o);
            String[] rowData = new String[fields.size()];
            for (int i = 0, fieldSize = fields.size(); i < fieldSize; i++) {

                rowData[i] = (fields.get(i) != null) ? fields.get(i).toString() : null;
            }
            listStrings.add(rowData);
        }
        return listStrings;
    }

    //전자입찰>입찰계획>입찰계획상세 Excel DownLoad
    public void createBidProgressDetailExcel(BidProgressResponseDto param, HttpServletResponse response) throws IOException {
        InputStream fis = getClass().getResourceAsStream("/menual/bidProgressDetail.xlsx");
        XSSFWorkbook xssfWorkBook = new XSSFWorkbook(fis);
        XSSFSheet xssfSheet = xssfWorkBook.getSheetAt(0);

        String fileName = param.getFileName();
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        Map<String, Object> result = param.getResult();                     //엑셀 모든 컬럼
        List<Map<String, Object>> custContent = param.getCustContent();     //입찰참가업체 컬럼
        List<Map<String, Object>> tableContent = param.getTableContent();   //직접등록시 내역사항에 들어가는 세부사항 컬럼
        List<Map<String, Object>> fileContent = param.getFileContent();     //파일등록시 내역사항에 들어가는 파일리스트 컬럼

        /*
         *  입찰참가 업체 컬럼 생성
         */
        StringBuilder custNameBuilder = new StringBuilder();
        int index = 0;

        for(Map<String, Object> map : custContent) {
            custNameBuilder.append(map.get("custName"));
            // 다음 요소가 있으면 쉼표 추가
            if (++index < custContent.size()) {
                custNameBuilder.append(", ");
            }
        }

        /*
         *  Flag : 직접등록, 파일등록
         *  직접등록시 내역사항에 세부사항   들어감
         *  파일등록시 내역사항에 파일리스트 들어감
         */
        String insModeFlag = String.valueOf(result.get("insMode"));
        StringBuilder insModeContentBuilder = new StringBuilder();

        //단가에 통화 포멧팅
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if("직접입력".equals(insModeFlag)) {
            //직접등록시 내역사항에 들어가는 세부사항
            for(Map<String, Object> map : tableContent) {
                insModeContentBuilder.append(map.get("name"));
                insModeContentBuilder.append("     ");
                insModeContentBuilder.append(map.get("ssize"));
                insModeContentBuilder.append("     ");
                insModeContentBuilder.append(map.get("orderQty"));
                insModeContentBuilder.append(map.get("unitcode"));
                insModeContentBuilder.append("     ");

                //단가에 통화 포멧팅
                String orderUc = decimalFormat.format(map.get("orderUc"));
                insModeContentBuilder.append("\\");
                insModeContentBuilder.append(orderUc);
                insModeContentBuilder.append("\n");
            }
        } else {
            //파일등록시 내역사항에 들어가는 파일리스트
            index = 0;
            for(Map<String, Object> map : fileContent) {
                insModeContentBuilder.append(map.get("fileNm"));
                // 다음 요소가 있으면 공백 추가
                if (++index < fileContent.size()) {
                    insModeContentBuilder.append("\n");
                }
            }
        }

        /*
         ** 입찰 기본정보
         */
        String biNo         = CommonUtils.getString(result.get("biNo"), "");             //입찰번호
        String biName       = CommonUtils.getString(result.get("biName"), "");           //입찰명
        String itemName     = CommonUtils.getString(result.get("itemName"), "");         //품목
        String biMode       = CommonUtils.getString(result.get("biMode"), "");           //입찰방식
        String bidJoinSpec  = CommonUtils.getString(result.get("bidJoinSpec"), "");      //입찰참가자격
        String specialCond  = CommonUtils.getString(result.get("specialCond"), "");      //특수조건
        String spotDate     = CommonUtils.getString(result.get("spotDate"), "");         //현장설명일시
        String spotArea     = CommonUtils.getString(result.get("spotArea"), "");         //현장설명장소
        String succDeciMeth = CommonUtils.getString(result.get("succDeciMeth"), "");     //낙찰자결정방법
        String custName     = CommonUtils.getString(custNameBuilder, "");                //입찰참가업체
        String amtBasis     = CommonUtils.getString(result.get("amtBasis"), "");         //금액기준
        String payCond      = CommonUtils.getString(result.get("payCond"), "");          //결제조건
        String bdAmt        = CommonUtils.getString(result.get("bdAmt"), "");            //예산금액
        String cuser        = CommonUtils.getString(result.get("cuser"), "");            //입찰담당자

        /*
         ** 입찰 공고 추가등록 사항
         */
        String estStartDate    = CommonUtils.getString(result.get("estStartDate"), "");  //제출 시작일시
        String estCloseDate    = CommonUtils.getString(result.get("estCloseDate"), "");  //제출 마감일시
        String estOpener       = CommonUtils.getString(result.get("estOpener"), "");     //개찰자
        String gongoId         = CommonUtils.getString(result.get("gongoId"), "");       //입찰 공고자
        String estBidder       = CommonUtils.getString(result.get("estBidder"), "");     //낙찰자
        String openAtt1        = CommonUtils.getString(result.get("openAtt1"), "");      //입회자1
        String openAtt2        = CommonUtils.getString(result.get("openAtt2"), "");      //입회자2
        String supplyCond      = CommonUtils.getString(result.get("supplyCond"), "");    //납품 조건
        String insMode         = CommonUtils.getString(result.get("insMode"), "");       //내역 방식
        String insModeContent  = CommonUtils.getString(insModeContentBuilder, "");       //내역사항

        /*
        ** 입찰 기본정보
         */
        xssfSheet.getRow(3).getCell(1).setCellValue(biNo);          //입찰번호
        xssfSheet.getRow(4).getCell(1).setCellValue(biName);        //입찰명
        xssfSheet.getRow(5).getCell(1).setCellValue(itemName);      //품목
        xssfSheet.getRow(6).getCell(1).setCellValue(biMode);        //입찰방식
        xssfSheet.getRow(7).getCell(1).setCellValue(bidJoinSpec);   //입찰참가자격
        xssfSheet.getRow(8).getCell(1).setCellValue(specialCond);   //특수조건
        xssfSheet.getRow(9).getCell(1).setCellValue(spotDate);      //현장설명일시
        xssfSheet.getRow(10).getCell(1).setCellValue(spotArea);     //현장설명장소
        xssfSheet.getRow(11).getCell(1).setCellValue(succDeciMeth); //낙찰자결정방법
        xssfSheet.getRow(12).getCell(1).setCellValue(custName);     //입찰참가업체
        xssfSheet.getRow(13).getCell(1).setCellValue(amtBasis);     //금액기준
        xssfSheet.getRow(14).getCell(1).setCellValue(payCond);      //결제조건
        xssfSheet.getRow(15).getCell(1).setCellValue(bdAmt);        //예산금액
        xssfSheet.getRow(16).getCell(1).setCellValue(cuser);        //입찰담당자
       /*
       ** 입찰 공고 추가등록 사항
        */
        xssfSheet.getRow(19).getCell(1).setCellValue(estStartDate);  //제출시작일시
        xssfSheet.getRow(20).getCell(1).setCellValue(estCloseDate);  //제출마감일시
        xssfSheet.getRow(21).getCell(1).setCellValue(estOpener);     //개찰자
        xssfSheet.getRow(22).getCell(1).setCellValue(gongoId);       //입찰공고자
        xssfSheet.getRow(23).getCell(1).setCellValue(estBidder);     //낙찰자
        xssfSheet.getRow(24).getCell(1).setCellValue(openAtt1);      //입회자1
        xssfSheet.getRow(25).getCell(1).setCellValue(openAtt2);      //입회자2
        xssfSheet.getRow(26).getCell(1).setCellValue(supplyCond);    //납품조건
        xssfSheet.getRow(27).getCell(1).setCellValue(insMode);       //내역방식
        xssfSheet.getRow(28).getCell(1).setCellValue(insModeContent);//내역사항

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        ServletOutputStream outputStream = response.getOutputStream();
        xssfWorkBook.write(outputStream);
        xssfWorkBook.close();
        outputStream.flush();
        outputStream.close();
    }
}





