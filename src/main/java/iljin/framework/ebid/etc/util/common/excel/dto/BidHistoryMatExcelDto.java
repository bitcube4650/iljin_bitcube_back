package iljin.framework.ebid.etc.util.common.excel.dto;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class BidHistoryMatExcelDto {

    @ExcelColumnName(name = "입찰번호")
    private String biNo;
    @ExcelColumnName(name = "사업부")
    private String matDept;
    @ExcelColumnName(name = "공정")
    private String matProc;
    @ExcelColumnName(name = "분류")
    private String matCls;
    @ExcelColumnName(name = "공장동")
    private String matFactory;
    @ExcelColumnName(name = "라인")
    private String matFactoryLine;
    @ExcelColumnName(name = "호기")
    private String matFactoryCnt;
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



    public BidHistoryMatExcelDto() {

    }
}
