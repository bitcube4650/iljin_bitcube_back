package iljin.framework.ebid.etc.util.common.excel.controller;

import iljin.framework.ebid.etc.util.common.excel.dto.BidProgressResponseDto;
import iljin.framework.ebid.etc.util.common.excel.service.ExcelService;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelUtils;
import iljin.framework.ebid.etc.util.common.excel.entity.FileEntity;
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
import java.util.List;


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


    /**
     * Service단 제외, 간단하게 컨트롤러에서 처리.
     */
    @PostMapping("/downLoad2")
    public void excelDownLoad(HttpServletResponse response) throws IOException {

        //TEST DATA를 가져오기 위함.
        List<FileEntity> result = excelRepository.findAll();

        excelUtils.downLoadExcel(FileEntity.class, result, "downLoad", response);
    }

}
