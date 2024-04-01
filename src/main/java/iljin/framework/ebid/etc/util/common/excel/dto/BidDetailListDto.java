package iljin.framework.ebid.etc.util.common.excel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
public class BidDetailListDto {
    String biNo;
    String biName;
    BigDecimal bdAmt;
    BigDecimal succAmt;
    String custName;
    BigInteger joinCustCnt;
    String estStartDate;
    String estCloseDate;
    String userName;
    String custName2;
    BigDecimal esmtAmt;
    String submitDate;
}
