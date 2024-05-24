package iljin.framework.ebid.etc.util.common.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class CustomFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// 초기화 로직 (필요한 경우)
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			CustomHttpServletRequestWrapper customRequest = new CustomHttpServletRequestWrapper((HttpServletRequest) request);
			chain.doFilter(customRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// 정리 로직 (필요한 경우)
	}
}
