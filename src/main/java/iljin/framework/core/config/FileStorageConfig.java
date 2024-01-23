package iljin.framework.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "file")
@Getter
@Setter
@Configuration
public class FileStorageConfig {

    @Value("${file.upload-dir}")
    String uploadDir;

    @Value("${xmlFile.upload-dir}")
    String xmlUploadDir;
}
