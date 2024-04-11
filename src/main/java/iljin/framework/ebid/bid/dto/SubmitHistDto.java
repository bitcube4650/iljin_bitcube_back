package iljin.framework.ebid.bid.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SubmitHistDto {
    String insMode;
    Short biOrder;
    String esmtCurr;
    BigDecimal esmtAmt;
    String submitDate;

    public SubmitHistDto(Short biOrder, String esmtCurr, BigDecimal esmtAmt, String submitDate){
        this.biOrder = biOrder;
        this.esmtCurr = esmtCurr;
        this.esmtAmt = esmtAmt;
        this.submitDate = submitDate;
    }
}
