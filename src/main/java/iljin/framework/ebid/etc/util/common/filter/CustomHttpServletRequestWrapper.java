package iljin.framework.ebid.etc.util.common.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final String modifiedBody;

	public CustomHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);

		// 기존 요청 바디를 읽어들입니다.
		String body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

		// JSON 파싱을 위한 ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> bodyMap;

		if (body.isEmpty()) {
			bodyMap = new HashMap<>();
		} else {
			bodyMap = objectMapper.readValue(body, Map.class);
		}

		// 쿠키 로깅
		String encodedCookieValue = "";
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if(cookie.getName().equals("loginInfo")) {
					encodedCookieValue = cookie.getValue();
				}
			}
			
			String decodedValue = URLDecoder.decode(encodedCookieValue, "UTF-8");
			
			if(!decodedValue.equals("")) {
				Map<String, Object> loginInfoMap = objectMapper.readValue(decodedValue, Map.class);
				bodyMap.put("loginInfoMap", loginInfoMap);
			}
		}
		
		// 변경된 JSON 문자열로 변환
		modifiedBody = objectMapper.writeValueAsString(bodyMap);
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(modifiedBody.getBytes());
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
			return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// not used
			}
		};
	}
}
