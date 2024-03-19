package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class SubmitHistDto {
    String insMode;
    String biOrder;
    String esmtCurr;
    String esmtAmt;
    String submitDate;

    public SubmitHistDto(String insMode, String biOrder, String esmtCurr, String esmtAmt, String submitDate){
        this.insMode = insMode;
        this.biOrder = biOrder;
        this.esmtCurr = esmtCurr;
        this.esmtAmt = esmtAmt;
        this.submitDate = submitDate;
    }
}
