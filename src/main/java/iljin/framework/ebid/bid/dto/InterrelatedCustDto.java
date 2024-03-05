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
public class InterrelatedCustDto {
    String custCode;
    String custName;
    String combinedAddr;
    String presName;
    String interrelatedCustCode;

    public InterrelatedCustDto(String custCode, String custName, String presName, String combinedAddr, String interrelatedCustCode){
        this.custCode = custCode;
        this.custName = custName;   
        this.presName = presName;
        this.combinedAddr = combinedAddr;
        this.interrelatedCustCode = interrelatedCustCode;
    }
}
