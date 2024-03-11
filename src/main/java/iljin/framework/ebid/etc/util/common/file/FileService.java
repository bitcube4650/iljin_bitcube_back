package iljin.framework.ebid.etc.util.common.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
	
	@Value("${file.upload.directory}")
    private String uploadDirectory;

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
        Path yearPath = Paths.get(uploadDirectory, year);
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

        // 파일 저장 경로 반환
        return filePath.toString();
    }
    
    //첨부파일 다운로드
    public ByteArrayResource downloadFile(String filePath) throws IOException {

		Path path = Paths.get(filePath);
		byte[] fileContent = Files.readAllBytes(path);
        
        // ByteArrayResource를 사용하여 byte 배열을 리소스로 변환
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return resource;
    }
}
