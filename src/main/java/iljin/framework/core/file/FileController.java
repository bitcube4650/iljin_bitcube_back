package iljin.framework.core.file;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
@RequestMapping("/api/v1")
public class FileController {

    private final
    FileStorageService fileStorageService;
//    private final
//    UfileSebrvice ufileService;

    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
//        this.ufileService = ufileService;
    }

    @PostMapping("/upload/file")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        UploadFile uf = fileStorageService.storeFile(file);

        return new UploadFileResponse(uf.id, uf.originalName, uf.storedName, uf.downloadUrl, file.getContentType(), file.getSize());
    }

    @PostMapping("/upload/file/document/{id}")
    public UploadFileResponse uploadFileWithDocumentId(@RequestParam("file") MultipartFile file, @PathVariable String id) {
        UploadFile uf = fileStorageService.storeFileWithDocumentId(file, Long.parseLong(id));

        return new UploadFileResponse(uf.id, uf.originalName, uf.storedName, uf.downloadUrl, file.getContentType(), file.getSize());
    }

    @PostMapping("/upload/file/document/{id}/file-type/{fileType}")
    public UploadFileResponse uploadFileWithDocumentIdAndType(@RequestParam("file") MultipartFile file,
                                                              @PathVariable String id,
                                                              @PathVariable String fileType
    ){
        UploadFile uf = fileStorageService.storeFileWithDocumentIdAndFileType(file, Long.parseLong(id), fileType, null);
        return new UploadFileResponse(uf.id, uf.originalName, uf.storedName, uf.downloadUrl, file.getContentType(), file.getSize());
    }

    @PostMapping("/upload/file/document/{id}/file-type/{fileType}/seq/{seq}")
    public UploadFileResponse uploadFileWithDocumentIdAndType(@RequestParam("file") MultipartFile file,
                                                              @PathVariable String id,
                                                              @PathVariable String fileType,
                                                              @PathVariable String seq
    ){
        UploadFile uf = fileStorageService.storeFileWithDocumentIdAndFileType(file, Long.parseLong(id), fileType, Long.parseLong(seq));
        return new UploadFileResponse(uf.id, uf.originalName, uf.storedName, uf.downloadUrl, file.getContentType(), file.getSize());
    }

    @PutMapping("/upload/file/{fileId}/remark")
    public void updateRemarkAndSeq(@PathVariable String fileId, @RequestBody UploadFileDto fileDto) {
        fileStorageService.updateRemark(Long.parseLong(fileId), fileDto.remark, fileDto.seq);
    }

    @PutMapping("/upload/file/{fileId}/document-header/{documentHId}")
    public ResponseEntity<String> updateDocumentHId(@PathVariable String fileId, @PathVariable String documentHId){
        boolean result = fileStorageService.updateDocumentHId(Long.parseLong(fileId), Long.parseLong(documentHId));

        if (result) {
            return new ResponseEntity<>("파일이 문서와 연결되었습니다.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("파일이 문서 연결이 실패하였습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/upload/file/{id}")
    public ResponseEntity<String> deleteFileById(@PathVariable String id) {
        fileStorageService.deleteById(Long.parseLong(id));
        return new ResponseEntity<>("파일이 삭제되었습니다.", HttpStatus.OK);
    }

    @PostMapping("/upload/files")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }

    @PostMapping("/upload/files/document/{id}/file-type/{fileType}")
    public List<UploadFileResponse> uploadMultipleFilesWithDocumentIdAndType(
            @RequestParam("files") MultipartFile[] files,
            @PathVariable String id,
            @PathVariable String fileType
    ) {
        return Arrays.stream(files)
                .map((file) -> uploadFileWithDocumentIdAndType(file, id, fileType))
                .collect(Collectors.toList());
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, @RequestParam(value = "id", required = false) Long id, HttpServletRequest request) throws UnsupportedEncodingException {

        String contentType = null;
        Resource resource = null;
        String originalFileName = null;

        try {
            if(id != null) {
                originalFileName = fileStorageService.getOriginalFileName(id);
            } else {
                originalFileName = fileStorageService.getOriginalFileNameByChangedFileName(fileName);
            }

            resource = fileStorageService.loadFileAsResource(fileName);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8") + "\"");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .headers(headers)
                .body(resource);
    }

    @GetMapping("/download/{fileName:.+}/{id}")
    public ResponseEntity<Resource> downloadFileById(@PathVariable String fileName, @PathVariable String id, HttpServletRequest request) {
        // TODO 리팩토링
        Optional<UploadFile> uf = fileStorageService.getUploadFileInfo(Long.parseLong(id));

        String contentType = null;
        Resource resource = null;
        try {
            resource = fileStorageService.loadFileAsResource(fileName);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (uf.isPresent()) {
                fileName = URLDecoder.decode(uf.get().originalName, "ISO8859-1");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if(contentType == null) {
            contentType = "application/x-msdownload";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping("/download2/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile2(@PathVariable String fileName
            ,@RequestParam(value = "id", required = false) Long id
            ,HttpServletRequest request) throws UnsupportedEncodingException {

        String contentType = null;
        Resource resource = null;
        String originalFileName = null;

//        try {
//            if(id != null) {
//                originalFileName = ufileService.getOriginalFileName(id);
//            } else {
//                originalFileName = ufileService.getOriginalFileNameByChangedFileName(fileName);
//            }
//
//            resource = ufileService.loadFileAsResource(fileName);
//            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        HttpHeaders headers = new HttpHeaders();

        if(contentType == "application/pdf"){
            contentType += "; charset=UTF-8";
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8") + "\"");
        }else{
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(originalFileName, "UTF-8") + "\"");
        }

        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .headers(headers)
                .body(resource);
    }
}
