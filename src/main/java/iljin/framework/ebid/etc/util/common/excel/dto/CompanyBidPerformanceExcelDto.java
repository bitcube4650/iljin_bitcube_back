package iljin.framework.ebid.etc.util.common.excel.dto;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@AllArgsConstructor
@Data
public class CompanyBidPerformanceExcelDto {

    @ExcelColumnName(name = "회사명")
    String interrelatedNm;
    @ExcelColumnName(name = "입찰건수")
    String cnt;
    @ExcelColumnName(name = "예산금액(1)")
    String bdAnt;
    @ExcelColumnName(name = "낙찰금액(2)")
    String succAmt;
    @ExcelColumnName(name = "차이(1)-(2)")
    String mamt;
    @ExcelColumnName(name = "비고")
    String test6;

    public CompanyBidPerformanceExcelDto() {
    }
}
