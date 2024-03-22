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
public class ItemDto {
    String biNo;
    int seq;
    BigDecimal orderQty;
    String name;
    String ssize;
    String unitcode;
    BigDecimal esmtUc;
    String custCode;

    public ItemDto(String biNo, int seq, BigDecimal orderQty, String name, 
        String ssize, String unitcode, BigDecimal esmtUc, String custCode){
        this.biNo = biNo;
        this.seq = seq;
        this.orderQty = orderQty;
        this.name = name;
        this.ssize = ssize;
        this.unitcode = unitcode;
        this.esmtUc = esmtUc;
        this.custCode = custCode;
    }
}
