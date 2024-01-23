package iljin.framework.core.file;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UploadFileDto {

    Long id;
    String originalName;
    String storedName;
    String downloadUrl;
    String fileType;
    Long documentHId;
    Long seq;
    String remark;
    Long createdBy;
    String creator;

    LocalDateTime creationDate;
    Long modifiedBy;
    LocalDateTime modifiedDate;

    public UploadFileDto() {
    }
}
