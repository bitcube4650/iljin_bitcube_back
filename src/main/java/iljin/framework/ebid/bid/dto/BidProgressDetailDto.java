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
public class BidProgressDetailDto {
    String biNo;
    String biName;
    String biMode;
    String insMode;
    String bidJoinSpec;
    String specialCond;
    String supplyCond;
    String spotDate;
    String spotArea;
    String succDeciMeth;
    String estStartDate;
    String estCloseDate;
    String estOpener;
    String estOpenDate;
    String openAtt1;
    String openAtt1Sign;
    String openAtt2;
    String openAtt2Sign;
    String ingTag;
    String createUser;
    String createDate;
    String updateUser;
    String updateDate;
    String itemCode;
    String gongoId;
    String payCond;
    String whyA3;
    String whyA7;
    String biOpen;
    String interrelatedCustCode;
    String realAmt;
    String amtBasis;
    BigDecimal bdAmt;
    String addAccept;
    String matDept;
    String matProc;
    String matCls;
    String matFactory;
    String matFactoryLine;
    String matFactoryCnt;


    public BidProgressDetailDto(
        String biNo, String biName, String biMode, String insMode, String bidJoinSpec,
        String specialCond, String supplyCond, String spotDate, String spotArea,
        String succDeciMeth, String estStartDate, String estCloseDate, String estOpener,
        String estOpenDate, String openAtt1, String openAtt1Sign, String openAtt2,
        String openAtt2Sign, String ingTag, String itemCode, String gongoId,
        String payCond, String whyA3, String whyA7, String biOpen, String interrelatedCustCode,
        String realAmt, String amtBasis, BigDecimal bdAmt, String addAccept, String matDept, String matProc, String matCls,
        String matFactory, String matFactoryLine, String matFactoryCnt){
            this.biNo = biNo;
            this.biName = biName;
            this.biMode = biMode;
            this.insMode = insMode;
            this.bidJoinSpec = bidJoinSpec;
            this.specialCond = specialCond;
            this.supplyCond = supplyCond;
            this.spotDate = spotDate;
            this.spotArea = spotArea;
            this.succDeciMeth = succDeciMeth;
            this.estStartDate = estStartDate;
            this.estCloseDate = estCloseDate;
            this.estOpener = estOpener;
            this.estOpenDate = estOpenDate;
            this.openAtt1 = openAtt1;
            this.openAtt1Sign = openAtt1Sign;
            this.openAtt2 = openAtt2;
            this.openAtt2Sign = openAtt2Sign;
            this.ingTag = ingTag;
            this.itemCode = itemCode;
            this.gongoId = gongoId;
            this.payCond = payCond;
            this.whyA3 = whyA3;
            this.whyA7 = whyA7;
            this.biOpen = biOpen;
            this.interrelatedCustCode = interrelatedCustCode;
            this.realAmt = realAmt;
            this.amtBasis = amtBasis;
            this.bdAmt = bdAmt;
            this.addAccept = addAccept;
            this.matDept = matDept;
            this.matProc = matProc;
            this.matCls = matCls;
            this.matFactory = matFactory;
            this.matFactoryLine = matFactoryLine;
            this.matFactoryCnt = matFactoryCnt;
    }



}
