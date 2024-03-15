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
    String biModeCode;
    String insMode;
    String insModeCode;
    String bidJoinSpec;
    String specialCond;
    String supplyCond;
    String spotDate;
    String spotArea;
    String succDeciMeth;
    String succDeciMethCode;
    String estStartDate;
    String estCloseDate;
    String estOpener;
    String estOpenerCode;
    String cuser;
    String cuserCode;
    String estOpenDate;
    String openAtt1;
    String openAtt1Code;
    String estBidder;
    String estBidderCode;
    String openAtt1Sign;
    String openAtt2;
    String openAtt2Code;
    String openAtt2Sign;
    String ingTag;
    String createUser;
    String createDate;
    String updateUser;
    String updateDate;
    String itemCode;
    String itemName;
    String gongoId;
    String gongoIdCode;
    String cuserDept;
    String payCond;
    String whyA3;
    String whyA7;
    String biOpen;
    String interrelatedCustCode;
    String interrelatedNm;
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
        String biNo, String biName, String biMode, String biModeCode, String insMode, String insModeCode, String bidJoinSpec,
        String specialCond, String supplyCond, String spotDate, String spotArea,
        String succDeciMeth, String succDeciMethCode, String estStartDate, String estCloseDate, String estOpener, String estOpenerCode,
        String cuser, String cuserCode, String estOpenDate, String openAtt1, String openAtt1Code, String estBidderCode, String estBidder, String openAtt1Sign, String openAtt2, String openAtt2Code,
        String openAtt2Sign, String ingTag, String itemCode, String itemName, String gongoId, String gongoIdCode, String cuserDept,
        String payCond, String whyA3, String whyA7, String biOpen, String interrelatedCustCode, String interrelatedNm,
        String realAmt, String amtBasis, BigDecimal bdAmt, String addAccept, String matDept, String matProc, String matCls,
        String matFactory, String matFactoryLine, String matFactoryCnt){
            this.biNo = biNo;
            this.biName = biName;
            this.biMode = biMode;
            this.biModeCode = biModeCode;
            this.insMode = insMode;
            this.insModeCode = insModeCode;
            this.bidJoinSpec = bidJoinSpec;
            this.specialCond = specialCond;
            this.supplyCond = supplyCond;
            this.spotDate = spotDate;
            this.spotArea = spotArea;
            this.succDeciMeth = succDeciMeth;
            this.succDeciMethCode = succDeciMethCode;
            this.estStartDate = estStartDate;
            this.estCloseDate = estCloseDate;
            this.estOpener = estOpener;
            this.estOpenerCode = estOpenerCode;
            this.cuser = cuser;
            this.cuserCode = cuserCode;
            this.estOpenDate = estOpenDate;
            this.openAtt1 = openAtt1;
            this.openAtt1Code = openAtt1Code;
            this.estBidderCode = estBidderCode;
            this.estBidder = estBidder;
            this.openAtt1Sign = openAtt1Sign;
            this.openAtt2 = openAtt2;
            this.openAtt2Code = openAtt2Code;
            this.openAtt2Sign = openAtt2Sign;
            this.ingTag = ingTag;
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.gongoId = gongoId;
            this.gongoIdCode = gongoIdCode;
            this.cuserDept = cuserDept;
            this.payCond = payCond;
            this.whyA3 = whyA3;
            this.whyA7 = whyA7;
            this.biOpen = biOpen;
            this.interrelatedCustCode = interrelatedCustCode;
            this.interrelatedNm = interrelatedNm;
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
