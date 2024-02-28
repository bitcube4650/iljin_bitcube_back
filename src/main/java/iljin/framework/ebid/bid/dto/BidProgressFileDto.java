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
public class BidProgressFileDto {
    String biNo;
    String fileFlag;
    String fileFlagKo;
    String fileNm;
    String filePath;

    public BidProgressFileDto(
        String biNo, String fileFlag, String fileFlagKo, String fileNm,
        String filePath){
            this.biNo = biNo;
            this.fileFlag = fileFlag;
            this.fileFlagKo = fileFlagKo;
            this.fileNm = fileNm;
            this.filePath = filePath;
    }
}
