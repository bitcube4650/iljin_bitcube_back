package iljin.framework.core.file;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "U_FILE")
@Getter
@Setter
@ToString
public class UploadFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "original_name")
    String originalName;
    @Column(name = "stored_name")
    String storedName;
    @Column(name = "download_url")
    String downloadUrl;

    @Column(name = "file_type")
    String fileType;

//    @ManyToOne(fetch=FetchType.LAZY)
//    @JoinColumn(name = "document_h_id", insertable = false, updatable = false)
//    @JsonIgnore
//    Document document;

    @Column(name = "document_h_id")
    Long documentHId;


    @Column(name = "seq")
    Long seq;

    @Column(name = "remark")
    String remark;

    @Column(name = "created_by")
    Long createdBy;
    @Column(name = "creation_date", insertable = false, updatable = false)
    LocalDateTime creationDate;
    @Column(name = "modified_by")
    Long modifiedBy;
    @Column(name = "modified_date", insertable = false, updatable = false)
    LocalDateTime modifiedDate;
}
