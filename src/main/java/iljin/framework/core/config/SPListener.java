package iljin.framework.core.config;


import com.rathontech.sso.sp.agent.controller.ServiceProviderDispatcher;
import com.rathontech.sso.sp.config.SPConfigurationListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContextListener;

@Configuration
public class SPListener implements WebMvcConfigurer {

    @Bean
    public ServletListenerRegistrationBean<ServletContextListener> servletListener(){
        ServletListenerRegistrationBean<ServletContextListener> bean = new ServletListenerRegistrationBean<>();
        bean.setListener(new SPConfigurationListener());
        return bean;
    }

    @Bean
    public ServletRegistrationBean<ServiceProviderDispatcher> ssoServiceProvider(){
        return new ServletRegistrationBean<ServiceProviderDispatcher>(new ServiceProviderDispatcher(),"/dispatch/*");
    }
}
