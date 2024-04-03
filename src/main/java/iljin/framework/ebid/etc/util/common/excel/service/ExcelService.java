package iljin.framework.ebid.etc.util.common.excel.service;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDetailDto;
import iljin.framework.ebid.etc.statistics.dto.BiInfoDto;
import iljin.framework.ebid.etc.util.CommonUtils;
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

    //전자입찰>입찰이력 Excel DownLoad
    public void downLoadBidCompleteListExcel(Map<String, Object> params, HttpServletResponse response) throws IOException{

        //로그인 유저가 롯데 에너지 머트리얼즈인지 확인
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<TCoUser> userOptional = tCoUserRepository.findById(principal.getUsername());
        String userInterrelatedCustCode = userOptional.get().getInterrelatedCustCode();

        //롯데 에너지 머트리얼즈 코드 값 '02'로 구분
        if("02".equals(userInterrelatedCustCode)) {
            List<BidCompleteDto> data = excelRepository.findComplateBidList(params);
            List<BidHistoryMatExcelDto> excelData = new ArrayList<>();
            String tmpBiNo = "";

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
            //excelUtils.downLoadExcel(BidHistoryMatExcelDto.class, excelData, "downLoad", response);
            excelUtils.downLoadExcelPaging(BidHistoryMatExcelDto.class, excelData, "downLoad", response);
            //excelUtils.downLoadCsv(BidHistoryMatExcelDto.class, excelData, "downLoad", response);

        } else {
            //롯데머트리얼즈 제외
            List<BidCompleteDto> data = excelRepository.findComplateBidList(params);
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
            excelUtils.downLoadExcelPaging(BidHistoryExcelDto.class, excelData, "downLoad", response);
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
    public void downLoadExcelbiddingStatusV2(Map<String, Object> params, HttpServletResponse response) {
        excelUtils.downLoadExcelBiddingStatus(BiddingStatusDto.class, params, "downLoad", response);
    }

    //통계>입찰 상세내역 Excel DownLoad
    public void downLoadExcelBiddingDetailV3(Map<String, Object> params, HttpServletResponse response) {
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
