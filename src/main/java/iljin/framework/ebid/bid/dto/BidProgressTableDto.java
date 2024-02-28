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
public class BidProgressTableDto {
    String biNo;
    int seq;
    String name;
    String ssize;
    BigDecimal orderQty;
    String unitCode;
    BigDecimal orderUc;

    public BidProgressTableDto(
        String biNo, int seq, String name, String ssize,
        BigDecimal orderQty, String unitCode, BigDecimal orderUc
    ){
        this.biNo = biNo;
        this.seq = seq;
        this.name = name;
        this.ssize = ssize;
        this.orderQty = orderQty;
        this.unitCode = unitCode;
        this.orderUc = orderUc;
    }
}
