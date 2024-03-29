package iljin.framework.ebid.etc.util.common.excel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class BidCompleteDto {

    String biNo;
    String matDept;
    String matProc;
    String matCls;
    String matFactory;
    String matFactoryLine;
    String matFactoryCnt;
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

    public BidCompleteDto() {
    }


    //입찰이력
    public BidCompleteDto(
            String biNo, String matDept, String matProc, String matCls, String matFactory, String matFactoryLine, String matFactoryCnt,
            String biName, BigDecimal bdAmt, BigDecimal succAmt, String custName, BigInteger joinCustCnt, String estStartDate, String estCloseDate,
            String userName, String custName2, BigDecimal esmtAmt, String submitDate
    ) {
        this.biNo = biNo;
        this.matDept = matDept;
        this.matProc = matProc;
        this.matCls = matCls;
        this.matFactory = matFactory;
        this.matFactoryLine = matFactoryLine;
        this.matFactoryCnt = matFactoryCnt;
        this.biName = biName;
        this.bdAmt = bdAmt;
        this.succAmt = succAmt;
        this.custName = custName;
        this.joinCustCnt = joinCustCnt;
        this.estStartDate = estStartDate;
        this.estCloseDate = estCloseDate;
        this.userName = userName;
        this.custName2 = custName2;
        this.esmtAmt = esmtAmt;
        this.submitDate = submitDate;
    }
}
