package iljin.framework.ebid.etc.statistics.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Data;

@Data
public class BiInfoDetailDto {

    private String biNo;
    private String biName;
    private String itemName;
    private BigDecimal bdAmt;
    private BigDecimal succAmt;
    private BigDecimal realAmt;
    private BigInteger custCnt;
    private String custName;
    private String estStartDate;
    private String estCloseDate;
    private BigDecimal esmtAmtMax;
    private BigDecimal esmtAmtMin;
    private BigDecimal esmtAmtDev;
    private BigDecimal reBidCnt;

	
    public BiInfoDetailDto() {}
    
	/**
	 * 입찰실적 상세내역 리스트 (/api/v1/statistics/biInfoDetailList)
	 */

    public BiInfoDetailDto(
    	    String biNo,
    	    String biName,
    	    String itemName,
    	    BigDecimal bdAmt,
    	    BigDecimal succAmt,
    	    BigDecimal realAmt,
    	    BigInteger custCnt,
    	    String custName,
    	    String estStartDate,
    	    String estCloseDate,
    	    BigDecimal esmtAmtMax,
    	    BigDecimal esmtAmtMin,
    	    BigDecimal esmtAmtDev,
    	    BigDecimal reBidCnt

    ) {
        this.biNo = biNo;
        this.biName = biName;
        this.itemName = itemName;
	    this.bdAmt = bdAmt;
	    this.succAmt = succAmt;
	    this.realAmt = realAmt;
	    this.custCnt =  custCnt;
	    this.custName = custName;
	    this.estStartDate = estStartDate;
	    this.estCloseDate = estCloseDate;
	    this.esmtAmtMax =  esmtAmtMax;
	    this.esmtAmtMin =  esmtAmtMin;
	    this.esmtAmtDev = esmtAmtDev;
	    this.reBidCnt = reBidCnt;

    }
}
