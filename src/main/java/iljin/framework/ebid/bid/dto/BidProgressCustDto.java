package iljin.framework.ebid.bid.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;

@Data
public class BidProgressCustDto {
    String biNo;
    String custCode;
    String custName;
    String esmtCurr;
    String esmtYn;
    String fileNm;
    String filePath;
    String etcFile;
    String etcPath;
    String submitDate;

    public BidProgressCustDto(String biNo, String custCode, String custName, String esmtCurr, String esmtYn,
                String fileNm, String filePath, String etcFile, String etcPath, String submitDate){
        this.biNo = biNo;
        this.custCode = custCode;
        this.custName = custName;
        this.esmtCurr = esmtCurr;
        this.esmtYn = esmtYn;
        this.fileNm = fileNm;
        this.filePath = filePath;
        this.etcFile = etcFile;
        this.etcPath = etcPath;
        this.submitDate = submitDate;
    }
}
