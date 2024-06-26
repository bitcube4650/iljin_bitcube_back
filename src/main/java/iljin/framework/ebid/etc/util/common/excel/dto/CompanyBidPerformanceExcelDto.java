package iljin.framework.ebid.etc.util.common.excel.dto;

import java.math.BigDecimal;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CompanyBidPerformanceExcelDto {

    @ExcelColumnName(name = "회사명")
    String interrelatedNm;
    @ExcelColumnName(name = "입찰건수")
    String cnt;
    @ExcelColumnName(name = "예산금액(1)")
    BigDecimal bdAmt;
    @ExcelColumnName(name = "낙찰금액(2)")
    BigDecimal succAmt;
    @ExcelColumnName(name = "차이(1)-(2)")
    BigDecimal mAmt;
    @ExcelColumnName(name = "비고")
    String temp;

    public CompanyBidPerformanceExcelDto() {
    }
}
