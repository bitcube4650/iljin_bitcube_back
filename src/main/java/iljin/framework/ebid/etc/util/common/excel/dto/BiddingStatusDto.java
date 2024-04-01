package iljin.framework.ebid.etc.util.common.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@Data
public class BiddingStatusDto {
    private String interrelatedNm;
    private String planCnt;	// 입찰계획 건수
    private String planAmt;	// 입찰계획 예산금액
    private String ingCnt;	// 입찰진행 건수
    private String ingAmt;	// 입찰진행 예산금악
    private String succCnt;	// 입찰완료 건수
    private String succAmt;	// 입찰 완료 낙찰금액
    private String custCnt;		// 업체수/건수
    private String regCustCnt;	//등록업체수
    private String testNull; // 기타


    public BiddingStatusDto() {
    }
}
