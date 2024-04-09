package iljin.framework.ebid.etc.util.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import iljin.framework.ebid.etc.util.Constances;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    // 파일 업로드 메서드
    public String uploadFile(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 현재 날짜를 기준으로 연도와 월 폴더 생성
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        Date currentDate = new Date();
        String year = yearFormat.format(currentDate);
        String month = monthFormat.format(currentDate);

        // 현재 연도 폴더 생성
        Path yearPath = Paths.get(Constances.FILE_UPLOAD_DIRECTORY, year);
        if (!Files.exists(yearPath)) {
            Files.createDirectories(yearPath);
        }

        // 현재 월 폴더 생성
        Path monthPath = Paths.get(yearPath.toString(), month);
        if (!Files.exists(monthPath)) {
            Files.createDirectories(monthPath);
        }

        // 파일명에 UUID를 사용하여 고유성 확보
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // 파일 저장 경로 설정
        Path filePath = Paths.get(monthPath.toString().replace("\\", "/"), uniqueFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath);

        String returnFilePath = filePath.toString().substring(Constances.FILE_UPLOAD_DIRECTORY.length());

        // 파일 저장 경로 반환
        return returnFilePath;
    }
    
    //첨부파일 다운로드
    public ByteArrayResource downloadFile(String filePath) throws Exception {

        filePath = Constances.FILE_UPLOAD_DIRECTORY + filePath;

        Path path = Paths.get(filePath);
        byte[] fileContent = Files.readAllBytes(path);
        
        // ByteArrayResource를 사용하여 byte 배열을 리소스로 변환
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return resource;
    }


    //암호화 첨부파일
    public String uploadEncryptedFile(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 현재 날짜를 기준으로 연도와 월 폴더 생성
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        Date currentDate = new Date();
        String year = yearFormat.format(currentDate);
        String month = monthFormat.format(currentDate);

        // 현재 연도 폴더 생성
        Path yearPath = Paths.get(Constances.FILE_UPLOAD_DIRECTORY, year);
        if (!Files.exists(yearPath)) {
            Files.createDirectories(yearPath);
        }

        // 현재 월 폴더 생성
        Path monthPath = Paths.get(yearPath.toString(), month);
        if (!Files.exists(monthPath)) {
            Files.createDirectories(monthPath);
        }

        // 파일명에 UUID를 사용하여 고유성 확보
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;



        // 파일 저장 경로 설정
        Path filePath = Paths.get(monthPath.toString(), uniqueFileName);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath);

        String encryptFilePath = AES_FileEncryption.encryptFile(filePath.toString());

        // 파일 저장 경로 반환
        return encryptFilePath;
    }

    //복호화 첨부파일
    public ByteArrayResource downloadDecryptedFile(String filePath) throws Exception {


        String decryptFile = null;
        try {
            //암호화된 파일
            if(filePath.contains("encrypted")) {
                decryptFile = AES_FileEncryption.decryptFile(Constances.FILE_UPLOAD_DIRECTORY + filePath);

                Path path = Paths.get(decryptFile);
                byte[] fileContent = Files.readAllBytes(path);

                // ByteArrayResource를 사용하여 byte 배열을 리소스로 변환
                ByteArrayResource resource = new ByteArrayResource(fileContent);
                return resource;

            //암호화되지 않은 파일.
            } else {
                ByteArrayResource resource = downloadFile(filePath);
                return resource;
            }
        } finally {
            deleteFile(decryptFile);
        }
    }

    private void deleteFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception e) {
            // 파일 삭제 중에 오류가 발생해도 무시
            e.printStackTrace();
        }
    }


}
