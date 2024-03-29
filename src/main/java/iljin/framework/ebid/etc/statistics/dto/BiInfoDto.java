package iljin.framework.ebid.etc.statistics.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import lombok.Data;

@Data
public class BiInfoDto {

    private String interrelatedNm;
    private BigInteger cnt;
    private BigDecimal bdAnt;
    private BigDecimal succAmt;
    private BigDecimal mamt;
    private String interrelatedCustCode;
    
    private BigDecimal planCnt;	// 입찰계획 건수
    private BigDecimal planAmt;	// 입찰계획 예산금액
    private BigDecimal ingCnt;	// 입찰진행 건수
    private BigDecimal ingAmt;	// 입찰진행 예산금악
    private BigDecimal succCnt;	// 입찰완료 건수
    //private BigDecimal succAmt;	// 입찰 완료 낙찰금액
    private BigDecimal custCnt;		// 업체수/건수
    private BigInteger regCustCnt;	//등록업체수
    
	
    public BiInfoDto() {}
    
	/**
	 * 회사별 입찰실적 리스트 (/api/v1/statistics/biInfoList)
	 */

    public BiInfoDto(
    		String interrelatedNm, 
            BigInteger cnt, 
            BigDecimal bdAnt, 
            BigDecimal succAmt,
            BigDecimal mamt,
            String interrelatedCustCode

    ) {
        this.interrelatedNm = interrelatedNm;
        this.cnt = cnt;
        this.bdAnt = bdAnt;
        this.succAmt = succAmt;
        this.mamt = mamt;
        this.interrelatedCustCode = interrelatedCustCode;

    }
    
    /**
     * 입찰현황  리스트
     * @param interrelatedNm
     * @param planCnt
     * @param planAmt
     * @param ingCnt
     * @param ingAmt
     * @param succCnt
     * @param succAmt
     * @param custCnt
     * @param regCustCnt
     */
    public BiInfoDto(
    		String interrelatedNm, 
    		BigDecimal planCnt,
    		BigDecimal planAmt,
    		BigDecimal ingCnt,
    		BigDecimal ingAmt,
    		BigDecimal succCnt,
    		BigDecimal succAmt,
    		BigDecimal custCnt,
    		BigInteger regCustCnt

    ) {
        this.interrelatedNm = interrelatedNm;
        this.planCnt = planCnt;
        this.planAmt = planAmt;
        this.ingCnt = ingCnt;
        this.ingAmt = ingAmt;
        this.succCnt = succCnt;
        this.succAmt = succAmt;
        this.custCnt = custCnt;
        this.regCustCnt = regCustCnt;

    }
}
