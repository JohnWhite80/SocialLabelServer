package com.github.sociallabel.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.github.sociallabel.APIException;
import com.github.sociallabel.entity.Tag;
import com.github.sociallabel.entity.User;
import com.github.sociallabel.service.UserService;

@RestController
@RequestMapping(value = "/api")
public class APIController {
	
	private static final Logger logger = LoggerFactory.getLogger(APIController.class);
	
	private ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<String, String>();
	
	@Autowired
	private UserService userService;
	
	protected final RestTemplate template = new RestTemplate();
	
	@RequestMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)	
	public @ResponseBody ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
		User u = userService.addUser(user);
		String userId = u.getId();
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", "200");
		result.put("message", "ok");
		result.put("userId", userId);
		result.put("sessionId", putUserIdToSession(userId));
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, String>> login(@RequestParam("email") String email, @RequestParam("password") String password){
		User user = userService.validate(email, password);
		String userId = user.getId();
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", "200");
		result.put("message", "ok");
		result.put("userId", userId);
		result.put("sessionId", putUserIdToSession(userId));
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/addtag/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, String>> addTag(@PathVariable("sessionId") String sessionId, @RequestBody List<Tag> tags){
		String userId = getUseridBySessionId(sessionId);
		userService.addtag(userId, tags);
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", "200");
		result.put("message", "ok");
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/recommend/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Object>> recommend(@PathVariable("sessionId") String sessionId){		
		String userId = getUseridBySessionId(sessionId);
		List<Map> recommend = userService.recommend(userId);		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", "200");
		result.put("message", "ok");
		result.put("result", recommend);
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/search/{sessionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, Object>> search(@PathVariable("sessionId") String sessionId, @RequestParam("tagname") String tagname){
		String userId = getUseridBySessionId(sessionId);
		List<Map> recommend = userService.recommendByTagName(userId, tagname);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", "200");
		result.put("message", "ok");
		result.put("result", recommend);
		return new ResponseEntity<Map<String, Object>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/profile/{sessionId}", method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Map<String, String>> profile(@PathVariable("sessionId") String sessionId, @RequestParam("filename") String filename, @RequestPart("image") MultipartFile file,@RequestParam("birthday") String birthday,@RequestParam("sex") String sex,@RequestParam("city") String city) throws Exception {
		String userId = getUseridBySessionId(sessionId);
		userService.updateProfile(userId, birthday, city, sex, filename, file);		
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", "200");
		result.put("message", "ok");
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/image/{filename:.+}", method = RequestMethod.GET)
	public @ResponseBody FileSystemResource profile(@PathVariable String filename) throws Exception {
		return new FileSystemResource(userService.getImage(filename)); 
	}
	
	private String putUserIdToSession(String userId){
		String sessionId = UUID.randomUUID().toString();
		sessionMap.put(sessionId, userId);
		return sessionId;
	}
	
	private String getUseridBySessionId(String sessionId){
		String userId = sessionMap.get(sessionId);
		return userId;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody ResponseEntity<Map<String, String>> handleException(Exception e) {
		logger.error("an exception occured", e);
		HttpStatus errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
		String errorMessage = e.getMessage();
		if(e instanceof APIException) {
			errorCode = HttpStatus.valueOf(((APIException)e).getErrorCode());
		}
		Map<String, String> result = new HashMap<String, String>();
		result.put("code", String.valueOf(errorCode));
		result.put("message", errorMessage);
		return new ResponseEntity<Map<String, String>>(result, errorCode);
	}
}
