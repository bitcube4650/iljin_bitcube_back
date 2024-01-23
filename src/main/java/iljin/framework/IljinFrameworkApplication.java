package iljin.framework;

import iljin.framework.core.config.FileStorageConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(FileStorageConfig.class)
@SpringBootApplication
@EnableScheduling
public class IljinFrameworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(IljinFrameworkApplication.class, args);
    }
}