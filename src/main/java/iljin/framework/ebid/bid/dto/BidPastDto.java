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
public class BidPastDto {
    String biNo;
    String biName;
    String biMode;
    String insMode;
    String estCloseDate;
    String ingTag;

    public BidPastDto(String biNo, String biName, String biMode, String insMode, String estCloseDate, String ingTag){
        this.biNo = biNo;
        this.biName = biName;
        this.biMode = biMode;
        this.insMode = insMode;
        this.estCloseDate = estCloseDate;
        this.ingTag = ingTag;
    }
}

