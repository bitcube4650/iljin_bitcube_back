package iljin.framework.ebid.etc.util.common.excel.dto;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
public class BiddingDetailExcelDto {

    @ExcelColumnName(name = "입찰번호")
    String biNo;
    @ExcelColumnName(name = "입찰명")
    String biName;
    @ExcelColumnName(name = "예산금액")
    BigDecimal bdAmt;
    @ExcelColumnName(name = "낙찰금액")
    BigDecimal succAmt;
    @ExcelColumnName(name = "낙찰회사")
    String custName;
    @ExcelColumnName(name = "제출시작일자")
    String estStartDate;
    @ExcelColumnName(name = "제출마감일자")
    String estCloseDate;
    @ExcelColumnName(name = "입찰담당자")
    String userName;
    @ExcelColumnName(name = "회사명")
    String custName2;
    @ExcelColumnName(name = "투찰가")
    BigDecimal esmtAmt;
    @ExcelColumnName(name = "투찰시간")
    String submitDate;

    public BiddingDetailExcelDto() {

    }
}
