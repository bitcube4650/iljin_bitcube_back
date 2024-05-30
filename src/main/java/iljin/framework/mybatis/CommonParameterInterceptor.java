package iljin.framework.mybatis;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import iljin.framework.core.security.user.UserDto;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Properties;

@Intercepts({ @Signature(type = ParameterHandler.class, method = "setParameters", args = { PreparedStatement.class }) })
public class CommonParameterInterceptor implements Interceptor {
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
		Object parameterObject = parameterHandler.getParameterObject();
		
		Map<String, Object> paramMap = (Map<String, Object>) parameterObject;
		
		// token의 userDto 세팅 부분 공통 파라미터로 세팅
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
			UserDto userDto = (UserDto) SecurityContextHolder.getContext().getAuthentication().getDetails();
			paramMap.put("userDto", userDto);
		}

		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
