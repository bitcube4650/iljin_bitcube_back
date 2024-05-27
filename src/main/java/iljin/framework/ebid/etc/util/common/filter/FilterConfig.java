package iljin.framework.ebid.etc.util.common.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<CustomFilter> customFilterRegistrationBean() {
		FilterRegistrationBean<CustomFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new CustomFilter());
		registrationBean.addUrlPatterns("/*"); // 모든 URL 패턴에 대해 필터를 적용
		return registrationBean;
	}
}
