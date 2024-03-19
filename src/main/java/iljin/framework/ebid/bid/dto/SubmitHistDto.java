package iljin.framework.ebid.bid.dto;

import lombok.Data;

@Data
public class SubmitHistDto {
    String biOrder;
    String esmtCurr;
    String esmtAmt;
    String submitDate;
    String esmtUc;

    public SubmitHistDto(String biOrder, String esmtCurr, String esmtAmt, String submitDate, String esmtUc){
        this.biOrder = biOrder;
        this.esmtCurr = esmtCurr;
        this.esmtAmt = esmtAmt;
        this.submitDate = submitDate;
        this.esmtUc = esmtUc;
    }
}
