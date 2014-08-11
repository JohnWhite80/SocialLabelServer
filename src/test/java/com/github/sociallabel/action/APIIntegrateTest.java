package com.github.sociallabel.action;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class APIIntegrateTest {
		
	public static <T> ResponseEntity<T> postJson(String url, Object data, Class<T> type,  Object... uriVariables){
		RestTemplate template = new RestTemplate();
	    template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		template.getMessageConverters().add(new StringHttpMessageConverter());						
		return template.postForEntity(url, data, type, uriVariables);
	}
	
	public static <T> ResponseEntity<T> postForm(String url, Object data, Class<T> type, Object... uriVariables){
		RestTemplate template = new RestTemplate();
		FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
	    formConverter.setCharset(Charset.forName("UTF8"));
	    template.getMessageConverters().add(formConverter);
	    template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		template.getMessageConverters().add(new StringHttpMessageConverter());						
		return template.postForEntity(url, data, type, uriVariables);
	}
	
	@Test
	public void testaddTag() {		
		Map<String, String> map = new HashMap<String, String>();
		String message = null;
		int code = 1;
		String sessionId = null;
		String userId;
		try {
			map.put("email", "ccc@ccc.com");
			map.put("password", "123456");
			map.put("username", "cccc");
			ResponseEntity<Map> result = postJson("http://10.104.151.197:8080/server/api/register", map, Map.class);
			Map body = result.getBody();
			if (result.getStatusCode() == HttpStatus.OK) {
				code = 0;				
				userId = (String) body.get("userId");
				sessionId = (String) body.get("sessionId");				
				message = (String) body.get("message");
			} else {
				message = (String) body.get("message");
			}
		} catch (Exception e) {
			// TODO log exception
			e.printStackTrace();
		}
						
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("image", new FileSystemResource("D:\\source\\my\\SocialLabelServer\\456118.jpg"));
		
		parts.add("filename", "a.jpg");
		parts.add("birthday", "2012-12-12");
		parts.add("city", "tianjin");
		parts.add("sex", "man");

		ResponseEntity<Map> result = postForm("http://10.104.151.197:8080/server/api/profile/{sessionId}", parts, Map.class, sessionId);
		
		System.out.println(result);
		
		if (result.getStatusCode() == HttpStatus.OK) {
			code = 0;
			message = (String) result.getBody().get("message");
		} else {
			message = (String) result.getBody().get("message");
		}
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(code, 0);
		assertEquals(message, "ok");
		
		List<Map<String, String>> tags = new ArrayList<Map<String, String>>();
		for (int i = 0; i < 3; i++) {
			Map<String, String> tag = new HashMap<String, String>();
			tag.put("name", "name" + i);
			tags.add(tag);
		}
		
		try {
			result = postJson("http://10.104.151.197:8080/server/api/addtag/{sessionId}", tags, Map.class, sessionId);
			Map body = result.getBody();
			if (result.getStatusCode() == HttpStatus.OK) {
				code = 0;				
				userId = (String) body.get("userId");
				sessionId = (String) body.get("sessionId");				
				message = (String) body.get("message");
			} else {
				message = (String) body.get("message");
			}
		} catch (Exception e) {
			// TODO log exception
			e.printStackTrace();
		}
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(code, 0);
		assertEquals(message, "ok");
	}
	
	@Test
	public void testProfile() {		
		Map<String, String> map = new HashMap<String, String>();
		String message = null;
		int code = 1;
		String sessionId = null;
		String userId;
		try {
			map.put("email", "bbb@bbb.com");
			map.put("password", "123456");
			map.put("username", "bbb");
			ResponseEntity<Map> result = postJson("http://10.104.151.197:8080/server/api/register", map, Map.class);
			Map body = result.getBody();
			if (result.getStatusCode() == HttpStatus.OK) {
				code = 0;				
				userId = (String) body.get("userId");
				sessionId = (String) body.get("sessionId");				
				message = (String) body.get("message");
			} else {
				message = (String) body.get("message");
			}
		} catch (Exception e) {
			// TODO log exception
			e.printStackTrace();
		}
						
		MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
		parts.add("image", new FileSystemResource("D:\\source\\my\\SocialLabelServer\\456118.jpg"));
		
		parts.add("filename", "a.jpg");
		parts.add("birthday", "2012-12-12");
		parts.add("city", "tianjin");
		parts.add("sex", "man");

		ResponseEntity<Map> result = postForm("http://10.104.151.197:8080/server/api/profile/{sessionId}", parts, Map.class, sessionId);
		
		System.out.println(result);
		
		if (result.getStatusCode() == HttpStatus.OK) {
			code = 0;
			message = (String) result.getBody().get("message");
		} else {
			message = (String) result.getBody().get("message");
		}
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(code, 0);
		assertEquals(message, "ok");
	}
}
