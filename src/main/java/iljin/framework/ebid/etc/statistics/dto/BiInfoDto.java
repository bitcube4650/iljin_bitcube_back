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
}
