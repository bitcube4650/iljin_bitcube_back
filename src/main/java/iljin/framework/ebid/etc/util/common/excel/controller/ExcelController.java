package iljin.framework.ebid.etc.util.common.excel.controller;

import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import iljin.framework.ebid.etc.util.common.excel.service.ExcelService;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelUtils;
import iljin.framework.ebid.etc.util.common.excel.entity.FileEntity;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/excel")
@Slf4j
public class ExcelController {
    private final ExcelService excelService;
    private final ExcelRepository excelRepository;
    private final ExcelUtils excelUtils;

    /**
     *  MVC MODEL에 의거한 공통화 모듈.
     *
     */
   @PostMapping("/downLoadExcelCommon")
    public ResponseEntity excelDownLoadMVC2(HttpServletResponse response) {
        try {
            excelService.downloadCommonExcel(response);
        } catch(Exception e) {
            String msg = " ExcelDownLoad를 실패하였습니다.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage() + msg);
        }
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


    //전자입찰>입찰계획>입찰계획상세 Excel DownLoad
    @PostMapping("/bid/progressDetail/downLoad")
    public ResponseEntity downLoadExcelBidProgressDetail(HttpServletResponse response,
                                                         @RequestBody BidProgressResponseDto responseDto) throws IOException {
       excelService.downloadBidProgressExcel(responseDto, response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //전자입찰>입찰이력 Excel DownLoad
    @PostMapping("/bid/completeList/downLoad")
    public ResponseEntity downLoadExcelBidCompleteList(HttpServletResponse response,
                                                       @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadBidCompleteListExcel(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    ///////////////////////////////////////////////////테스트는 입찰이력으로..

    //통계>회사별 입찰실적 Excel DownLoad 테스트중.
    @PostMapping("/statistics/biInfoList/downLoad")
    public ResponseEntity downLoadExcelCompanyBidPerformance(HttpServletResponse response,
                                                         @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelCompanyBidPerformance(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //통계>입찰실적 상세내역 Excel DownLoad 개발해야함.
    @PostMapping("/bid/completeList/downLoadV3")
    public ResponseEntity downLoadExcelBidPerformanceDetail(HttpServletResponse response,
                                                         @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelBidPerformanceDetail(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //통계>입찰현황 개발 해야함. (데이터 조회해서 추가하면 됨..)
    @PostMapping("/statistics/bidPresentList/downLoad")
    public ResponseEntity biddingStatus(HttpServletResponse response,
                                                         @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelbiddingStatus(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //통계>입찰상세내역 Excel DownLoad
    @PostMapping("/statistics/bidDetailList/downLoad")
    public ResponseEntity downLoadExcelBiddingDetail(HttpServletResponse response,
                                                     @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelBiddingDetail(params, response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }




    /**
     * Service단 제외,
     * 간단하게 컨트롤러에서 처리하는 가이드 예제.
     */
    @PostMapping("/bid/completeList/downLoad2")
    public void excelDownLoad(HttpServletResponse response) throws IOException {

        //TEST DATA를 가져오기 위함.
        List<FileEntity> result = excelRepository.findAll();
        excelUtils.downLoadCsv(FileEntity.class, result, "downLoad", response);
        excelUtils.downLoadExcel(FileEntity.class, result, "downLoad", response);
    }


    /*
    ExcelDownLoad 되는 예제.
    */
    @PostMapping("/bid/completeList/downLoad3")
    public void downLoad2(HttpServletResponse response) throws IOException {
        /**
         * excel sheet 생성
         */
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1"); // 엑셀 sheet 이름
        sheet.setDefaultColumnWidth(28); // 디폴트 너비 설정

        /**
         * header font style
         */
        XSSFFont headerXSSFFont = (XSSFFont) workbook.createFont();


        /**
         * header cell style
         */
        XSSFCellStyle headerXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

        // 테두리 설정
        headerXssfCellStyle.setBorderLeft(BorderStyle.THIN);
        headerXssfCellStyle.setBorderRight(BorderStyle.THIN);
        headerXssfCellStyle.setBorderTop(BorderStyle.THIN);
        headerXssfCellStyle.setBorderBottom(BorderStyle.THIN);

        // 배경 설정

        headerXssfCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerXssfCellStyle.setFont(headerXSSFFont);

        /**
         * body cell style
         */
        XSSFCellStyle bodyXssfCellStyle = (XSSFCellStyle) workbook.createCellStyle();

        // 테두리 설정
        bodyXssfCellStyle.setBorderLeft(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderRight(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderTop(BorderStyle.THIN);
        bodyXssfCellStyle.setBorderBottom(BorderStyle.THIN);

        /**
         * header data
         */
        int rowCount = 0; // 데이터가 저장될 행
        String headerNames[] = new String[]{"첫번째 헤더", "두번째 헤더", "세번째 헤더"};

        Row headerRow = null;
        Cell headerCell = null;

        headerRow = sheet.createRow(rowCount++);
        for(int i=0; i<headerNames.length; i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headerNames[i]); // 데이터 추가
            headerCell.setCellStyle(headerXssfCellStyle); // 스타일 추가
        }

        /**
         * body data
         */
        String bodyDatass[][] = new String[][]{
                {"첫번째 행 첫번째 데이터", "첫번째 행 두번째 데이터", "첫번째 행 세번째 데이터"},
                {"두번째 행 첫번째 데이터", "두번째 행 두번째 데이터", "두번째 행 세번째 데이터"},
                {"세번째 행 첫번째 데이터", "세번째 행 두번째 데이터", "세번째 행 세번째 데이터"},
                {"네번째 행 첫번째 데이터", "네번째 행 두번째 데이터", "네번째 행 세번째 데이터"}
        };

        Row bodyRow = null;
        Cell bodyCell = null;

        for(String[] bodyDatas : bodyDatass) {
            bodyRow = sheet.createRow(rowCount++);

            for(int i=0; i<bodyDatas.length; i++) {
                bodyCell = bodyRow.createCell(i);
                bodyCell.setCellValue(bodyDatas[i]); // 데이터 추가
                bodyCell.setCellStyle(bodyXssfCellStyle); // 스타일 추가
            }
        }

        /**
         * download
         */
        String fileName = "spring_excel_download";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        ServletOutputStream servletOutputStream = response.getOutputStream();

        workbook.write(servletOutputStream);
        workbook.close();
        servletOutputStream.flush();
        servletOutputStream.close();
    }

}
