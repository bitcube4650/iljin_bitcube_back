package iljin.framework.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:/slip.properties"}, ignoreResourceNotFound = true)
public class SlipConfig {
    
}
