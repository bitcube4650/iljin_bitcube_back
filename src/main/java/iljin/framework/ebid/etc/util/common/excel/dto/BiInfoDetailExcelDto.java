package iljin.framework.ebid.etc.util.common.excel.dto;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
public class BiInfoDetailExcelDto {

    @ExcelColumnName(name = "입찰번호")
    private String biNo;
    @ExcelColumnName(name = "입찰명")
    private String biName;
    @ExcelColumnName(name = "입찰품명")
    private String itemName;
    @ExcelColumnName(name = "예산금액")
    private String bdAmt;
    @ExcelColumnName(name = "낙찰금액")
    private String succAmt;
    @ExcelColumnName(name = "계약금액")
    private String realAmt;
    @ExcelColumnName(name = "참여업체수")
    private String custCnt;
    @ExcelColumnName(name = "낙찰사")
    private String custName;
    @ExcelColumnName(name = "제출시작일")
    private String estStartDate;
    @ExcelColumnName(name = "제출마감일")
    private String estCloseDate;
    @ExcelColumnName(name = "투찰최고가(1)")
    private String esmtAmtMax;
    @ExcelColumnName(name = "투찰최저가(2)")
    private String esmtAmtMin;
    @ExcelColumnName(name = "편차(1)-(2)")
    private String esmtAmtDev;
    @ExcelColumnName(name = "재입찰횟수")
    private String reBidCnt;

    public BiInfoDetailExcelDto() {

    }
}
