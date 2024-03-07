package iljin.framework.ebid.etc.notice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menual")
@CrossOrigin
public class MenualController {
	
	@Value("${file.menual.path}")
    private String menualPath;

	//첨부파일 다운로드
	@PostMapping("/downloadMenual")
    public ByteArrayResource downloadFile(@RequestBody Map<String, Object> params) throws IOException {
		String appPath = System.getProperty("user.dir");//어플리케이션 경로
		String filePath = appPath + menualPath;//파일 경로
		Path path = Paths.get(filePath);
		byte[] fileContent = Files.readAllBytes(path);
        
        // ByteArrayResource를 사용하여 byte 배열을 리소스로 변환한다.
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        return resource;
    }
		
}
