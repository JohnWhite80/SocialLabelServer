package com.github.sociallabel.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class APIController {
	
	private static final Logger logger = LoggerFactory.getLogger(APIController.class);
	
	private ConcurrentHashMap<String, String> sessionMap = new ConcurrentHashMap<String, String>();
	
	@Autowired
	private UserService userService;
	
	protected final RestTemplate template = new RestTemplate();
	
	@RequestMapping(value = "/register")	
	public @ResponseBody ResponseEntity<Map<String, String>> registerUser(@RequestBody User user) {
		User u = userService.addUser(user);
		Map<String, String> result = new HashMap<String, String>();
		result.put("id", u.getId());
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/login")
	public ResponseEntity<Map<String, String>> login(@RequestParam("email") String email, @RequestParam String password){
		User user = userService.validate(email, password);
		String sessionId = UUID.randomUUID().toString();
		sessionMap.put(sessionId, user.getId());
		Map<String, String> result = new HashMap<String, String>();
		result.put("sessionId", sessionId);
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/addtag")
	public @ResponseBody ResponseEntity<String> addTag(@RequestParam("sessionId") String sessionId, @RequestBody List<Tag> tags){
		String userId = getUseridBySessionId(sessionId);
		userService.addtag(userId, tags);
		return new ResponseEntity<String>("addtag success", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/recommend")
	public @ResponseBody ResponseEntity<String> recommend(@RequestParam("email") String email){		
		userService.recommend(email);
		return new ResponseEntity<String>("recommend success", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/search")
	public @ResponseBody ResponseEntity<String> search(@RequestParam("tagname") String tagname){		
		//userService.findByTagContaining(tagname);
		return new ResponseEntity<String>("search success", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/profile", method = RequestMethod.POST,  produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<String> profile(@RequestParam("sessionId") String sessionId, @RequestParam("filename") String filename, @RequestPart("image") MultipartFile file,@RequestParam("birthday") String birthday,@RequestParam("sex") String sex,@RequestParam("city") String city) throws Exception {
		String userId = getUseridBySessionId(sessionId);
		userService.updateProfile(userId, filename, file);
		User u=new User();
		u.setSex(sex);
		u.setBirthday(birthday);
		u.setCity(city);
		userService.updateUser(u);
		return new ResponseEntity<String>("update success", HttpStatus.OK);
	}
	
	private String getUseridBySessionId(String sessionId){
		String userId = sessionMap.get(sessionId);
		return userId;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody ResponseEntity<String> handleException(Exception e) {
		HttpStatus errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
		String errorMessage = e.getMessage();
		if(e instanceof APIException) {
			errorCode = HttpStatus.valueOf(((APIException)e).getErrorCode());
		}
		return new ResponseEntity<String>(errorMessage, errorCode);
	}
}
