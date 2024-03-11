package iljin.framework.ebid.etc.util.common.excel.entity;

import iljin.framework.ebid.etc.util.common.excel.utils.ExcelColumnName;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "T_BI_UPLOAD")
@Data
public class FileEntity {

    @Id
    @Column(name="FILE_ID")
    @ExcelColumnName(name = "No")
    String fileId;

    @Column(name="BI_NO")
    @ExcelColumnName(name = "파일번호")
    String biNo;

    @Column(name="CREATE_DATE")
    @ExcelColumnName(name = "생성날짜")
    LocalDate createDate;

    @Column(name="F_CUST_CODE")
    @ExcelColumnName(name = "코드값")
    String fCustCode;

    @Column(name="FILE_FLAG")
    @ExcelColumnName(name = "파일여부")
    String fileFlag;


    @Column(name="FILE_NM")
    @ExcelColumnName(name = "파일이름")
    String fileNm;

    @Column(name="FILE_PATH")
    @ExcelColumnName(name = "파일주소")
    String filePath;

    @Column(name="USE_YN")
    @ExcelColumnName(name = "사용여부")
    String useYn;

    public FileEntity() {
    }


}
