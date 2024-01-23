package iljin.framework.core.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UploadFileResponse {
    private Long id;
    private String originalName;
    private String storedName;
    private String fileDownloadUri;
    private String fileType;
    private long size;
}
