package iljin.framework.ebid.etc.util.common.excel.dto;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class BidHistoryExcelDto {

    @ExcelColumnName(name = "입찰번호")
    private String biNo;
    @ExcelColumnName(name = "입찰명")
    private String biName;
    @ExcelColumnName(name = "예산금액")
    private BigDecimal bdAmt;
    @ExcelColumnName(name = "낙찰금액")
    private BigDecimal succAmt;
    @ExcelColumnName(name = "낙찰사")
    private String custName;
    @ExcelColumnName(name = "제출시작일")
    private String estStartDate;
    @ExcelColumnName(name = "제출마감일")
    private String estCloseDate;
    @ExcelColumnName(name = "입찰담당자")
    private String userName;

    //투찰업체 정보
    @ExcelColumnName(name = "회사명")
    private String custName2;
    @ExcelColumnName(name = "투찰가")
    private BigDecimal esmtAmt;
    @ExcelColumnName(name = "투찰시간")
    private String submitDate;



    public BidHistoryExcelDto() {

    }
}
