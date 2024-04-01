package iljin.framework.ebid.etc.util.common.excel.service;

import iljin.framework.ebid.custom.entity.TCoUser;
import iljin.framework.ebid.custom.repository.TCoUserRepository;
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
import java.math.BigDecimal;
import java.math.BigInteger;
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
            List<BidCompleteDto> data = excelRepository.findComplateBidListV2(params);
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
            List<BidCompleteDto> data = excelRepository.findComplateBidListV2(params);
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
    public void downLoadExcelCompanyBidPerformance(Map<String, Object> params, HttpServletResponse response) {
        List<CompanyBidPerformanceExcelDto> excelData = new ArrayList<>();
        List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("biInfoList");

        for(int i = 0; i < data.size(); i++) {
            CompanyBidPerformanceExcelDto companyBidPerformanceExcelDto = new CompanyBidPerformanceExcelDto();

            String interrelatedNm = CommonUtils.getString(data.get(i).get("interrelatedNm"), "");
            String cnt = CommonUtils.getString(data.get(i).get("cnt"), "");
            String bdAnt = CommonUtils.getString(data.get(i).get("bdAnt"), "");
            String succAmt = CommonUtils.getString(data.get(i).get("succAmt"), "");
            String mamt = CommonUtils.getString(data.get(i).get("mamt"), "");

            companyBidPerformanceExcelDto.setInterrelatedNm(interrelatedNm);
            companyBidPerformanceExcelDto.setCnt(CommonUtils.getFormatNumber(cnt));
            companyBidPerformanceExcelDto.setBdAnt(CommonUtils.getFormatNumber(bdAnt));
            companyBidPerformanceExcelDto.setSuccAmt(CommonUtils.getFormatNumber(succAmt));
            companyBidPerformanceExcelDto.setMamt(CommonUtils.getFormatNumber(mamt));

            excelData.add(companyBidPerformanceExcelDto);
        }

        excelUtils.downLoadExcelPaging(CompanyBidPerformanceExcelDto.class, excelData, "downLoad", response);
    }

    //통계>입찰실적 상세내역 Excel DownLoad 개발해야함.
    public void downLoadExcelBidPerformanceDetail(Map<String, Object> params, HttpServletResponse response) {
        List<BidHistoryExcelDto> excelData = new ArrayList<>();

        excelUtils.downLoadExcelPaging(BidPerformanceDetailDto.class, excelData, "downLoad", response);
    }

    //통계>입찰현황 Excel DownLoad
    public void downLoadExcelbiddingStatus(Map<String, Object> params, HttpServletResponse response) {
        List<BiInfoDto> data = excelRepository.findBidPresentList(params);
        List<BiddingStatusDto> excelData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++) {
            BiddingStatusDto biddingStatusDto = new BiddingStatusDto();

            String interrelatedNm = CommonUtils.getString(data.get(i).getInterrelatedNm(), " ");
            String planCnt = CommonUtils.getString(data.get(i).getPlanCnt(), " ");
            String planAmt = CommonUtils.getString(data.get(i).getPlanAmt(), " ");
            String ingCnt = CommonUtils.getString(data.get(i).getIngCnt(), " ");
            String ingAmt = CommonUtils.getString(data.get(i).getIngAmt(), " ");
            String succCnt = CommonUtils.getString(data.get(i).getSuccCnt(), " ");
            String succAmt = CommonUtils.getString(data.get(i).getSuccAmt(), " ");
            String custCnt = CommonUtils.getString(data.get(i).getCustCnt(), " ");
            String regCustCnt = CommonUtils.getString(data.get(i).getRegCustCnt(), " ");
            String testNull = "";

            biddingStatusDto.setInterrelatedNm(interrelatedNm);
            biddingStatusDto.setPlanCnt(CommonUtils.getFormatNumber(planCnt));
            biddingStatusDto.setPlanAmt(CommonUtils.getFormatNumber(planAmt));
            biddingStatusDto.setIngCnt(CommonUtils.getFormatNumber(ingCnt));
            biddingStatusDto.setIngAmt(CommonUtils.getFormatNumber(ingAmt));
            biddingStatusDto.setSuccCnt(CommonUtils.getFormatNumber(succCnt));
            biddingStatusDto.setSuccAmt(CommonUtils.getFormatNumber(succAmt));
            biddingStatusDto.setCustCnt(CommonUtils.getFormatNumber(custCnt));
            biddingStatusDto.setRegCustCnt(CommonUtils.getFormatNumber(regCustCnt));
            biddingStatusDto.setTestNull("");

            //쿼리 롤업 과정에서 발생하는 이슈
            if(i == data.size() - 1) {
                biddingStatusDto.setInterrelatedNm("계");
            }
            excelData.add(biddingStatusDto);
        }

        excelUtils.downLoadExcelPaging(BiddingStatusDto.class, excelData, "downLoad", response);
    }


    //통계>입찰상세내역 ExcelDownLoad
    public void downLoadExcelBiddingDetail(Map<String, Object> params, HttpServletResponse response) {
        List<BidDetailListDto> data = excelRepository.findBidDetailList(params);
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
        excelUtils.downLoadExcelPaging(BiddingDetailExcelDto.class, excelData, "downLoad", response);
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
