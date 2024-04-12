package iljin.framework.ebid.etc.util.common.excel.controller;

import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import iljin.framework.ebid.etc.util.common.excel.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/excel")
@Slf4j
public class ExcelController {
    private final ExcelService excelService;

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

    //통계>회사별 입찰실적 Excel DownLoad
    @PostMapping("/statistics/biInfoList/downLoad")
    public ResponseEntity downLoadExcelCompanyBidPerformance(HttpServletResponse response,
                                                             @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelCompanyBidPerformance(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //통계>입찰실적 상세내역 Excel DownLoad
    @PostMapping("/statistics/biInfoDetailList/downLoad")
    public ResponseEntity downLoadExcelBidPerformanceDetail(HttpServletResponse response,
                                                         @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelBidPerformanceDetail(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }


    //통계>입찰현황 Excel DownLoad
    @PostMapping("/statistics/bidPresentList/downLoad")
    public ResponseEntity biddingStatus(HttpServletResponse response,
                                                         @RequestBody Map<String, Object> params) throws IOException {
        excelService.downLoadExcelbiddingStatusV2(params,response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //통계>입찰상세내역 Excel DownLoad 완성본
    @PostMapping("/statistics/bidDetailList/downLoad")
    public ResponseEntity downLoadExcelBiddingDetail(HttpServletResponse response,
                                                     @RequestBody Map<String, Object> params) throws IOException {
       // excelService.downLoadExcelBiddingDetail(params, response);
       excelService.downLoadExcelBiddingDetailV3(params, response);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
