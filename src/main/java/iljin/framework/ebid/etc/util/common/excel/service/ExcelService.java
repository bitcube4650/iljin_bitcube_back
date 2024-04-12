package iljin.framework.ebid.etc.util.common.excel.service;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.util.common.excel.dto.*;
import iljin.framework.ebid.etc.util.common.excel.repository.ExcelRepository;
import iljin.framework.ebid.etc.util.common.excel.utils.ExcelUtils;
import iljin.framework.ebid.etc.util.common.excel.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExcelService {
    private final ExcelRepository excelRepository;
    private final ExcelUtils excelUtils;
    private final TCoUserRepository tCoUserRepository;




    private static final int MAX_ROW = 1000;

    /*
     * 공통 엑셀 다운로드.
     * 현재 FileEntity로 하는 예시가 들어가 있음.
     */
    public void downloadCommonExcel(HttpServletResponse response) {

        // 데이터 조회.
        List<FileEntity> result = excelRepository.findAll();

        //10000건 이상 CSV, 10000건 미만 Excel
        if(result.size() >= MAX_ROW) {
            downLoadCsv(FileEntity.class, result, "downLoad", response);
        } else {
            downLoadExcel(FileEntity.class, result, "donLoad", response);
        }
    }


    //전자입찰>입찰계획>입찰계획상세 Excel DownLoad
    public void downloadBidProgressExcel(BidProgressResponseDto param, HttpServletResponse response) throws IOException {
        excelUtils.createBidProgressDetailExcel(param, response);
    }

    //통계>입찰이력
    public void downLoadBidCompleteListExcel(Map<String, Object> param, HttpServletResponse response) throws IOException{

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();
        
        //롯데 에너지 머트리얼즈 코드 값 '02'로 구분
        if("02".equals(userInterrelatedCustCode)) {
            excelUtils.downLoadBidCompleteList(BidHistoryMatExcelDto.class, param, "downLoad", response);
        } else {
            excelUtils.downLoadBidCompleteList(BidHistoryExcelDto.class, param, "downLoad", response);
        }
    }

    //통계>회사별 입찰실적 Excel DownLoad
    public void downLoadExcelCompanyBidPerformance(Map<String, Object> param, HttpServletResponse response) {
        excelUtils.downLoadExcelCompanyBidPerformance(CompanyBidPerformanceExcelDto.class, param, "downLoad", response);
    }

    //통계>입찰실적 상세내역 Excel DownLoad
    public void downLoadExcelBidPerformanceDetail(Map<String, Object> params, HttpServletResponse response) {
        //페이징 적용
        excelUtils.downLoadExcelBiInfoDetailList(BiInfoDetailExcelDto.class, params, "downLoad", response);
    }

    //통계>입찰현황 Excel DownLoad
    public void downLoadExcelbiddingStatus(Map<String, Object> params, HttpServletResponse response) {
        excelUtils.downLoadExcelBiddingStatus(BiddingStatusDto.class, params, "downLoad", response);
    }

    //통계>입찰 상세내역 Excel DownLoad
    public void downLoadExcelBiddingDetail(Map<String, Object> params, HttpServletResponse response) {
        excelUtils.downLoadExcelBiddingDetail(BiddingDetailExcelDto.class, params, "downLoad", response);
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
