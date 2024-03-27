package iljin.framework.ebid.custom.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TCoCustMasterDto {
    int custCode;
    String custName;
    String interrelatedNm;
    String custType1;
    String custType2;
    String custTypeNm1;
    String custTypeNm2;
    String regnum;
    String regnum1;
    String regnum2;
    String regnum3;
    String presName;
    String presJuminNo;
    String presJuminNo1;
    String presJuminNo2;
    String tel;
    String fax;
    String zipcode;
    String addr;
    String addrDetail;
    String regnumFile;
    String regnumPath;
    String bFile;
    String bFilePath;
    String certYn;
    String etc;
    BigInteger capital;
    String foundYear;
    String createDate;

    String userName;
    String userEmail;
    String userId;
    String userHp;
    String userTel;
    String userBuseo;
    String userPosition;

    /**
     * 업체 목록 (/api/v1/cust/approvalList)
     */
    public TCoCustMasterDto(int custCode, String custName, String custType1, String regnum, String presName, String userName, String createDate) {
        this.custCode = custCode;
        this.custName = custName;
        this.custType1 = custType1;
        this.regnum = regnum;
        this.presName = presName;
        this.userName = userName;
        this.createDate = createDate;
    }

    /**
     * 타계열사 업체 목록 (/api/v1/cust/otherCustList)
     */
    public TCoCustMasterDto(int custCode, String custName, String custType1, String regnum, String presName, String interrelatedNm) {
        this.custCode = custCode;
        this.custName = custName;
        this.custType1 = custType1;
        this.regnum = regnum;
        this.presName = presName;
        this.interrelatedNm = interrelatedNm;
    }

    /**
     * 업체 상세 (/api/v1/cust/approvalDetail)
     */
    public TCoCustMasterDto(int custCode, String custName, String interrelatedNm, String custType1, String custType2, String custTypeNm1, String custTypeNm2, String regnum
            , String regnum1, String regnum2, String regnum3, String presName, String presJuminNo, String presJuminNo1, String presJuminNo2, BigInteger capital
            , String foundYear, String tel, String fax, String zipcode, String addr, String addrDetail, String regnumFile, String regnumPath, String bFile, String bFilePath
            , String certYn, String etc, String userName, String userEmail, String userId, String userHp, String userTel, String userBuseo, String userPosition) {
        this.custCode = custCode;
        this.custName = custName;
        this.interrelatedNm = interrelatedNm;
        this.custType1 = custType1;
        this.custType2 = custType2;
        this.custTypeNm1 = custTypeNm1;
        this.custTypeNm2 = custTypeNm2;
        this.regnum = regnum;
        this.regnum1 = regnum1;
        this.regnum2 = regnum2;
        this.regnum3 = regnum3;
        this.presName = presName;
        this.presJuminNo = presJuminNo;
        this.presJuminNo1 = presJuminNo1;
        this.presJuminNo2 = presJuminNo2;
        this.capital = capital;
        this.foundYear = foundYear;
        this.tel = tel;
        this.fax = fax;
        this.zipcode = zipcode;
        this.addr = addr;
        this.addrDetail = addrDetail;
        this.regnumFile = regnumFile;
        this.regnumPath = regnumPath;
        this.bFile = bFile;
        this.bFilePath = bFilePath;
        this.certYn = certYn;
        this.etc = etc;

        this.userName = userName;
        this.userEmail = userEmail;
        this.userId = userId;
        this.userHp = userHp;
        this.userTel = userTel;
        this.userBuseo = userBuseo;
        this.userPosition = userPosition;
    }

}
