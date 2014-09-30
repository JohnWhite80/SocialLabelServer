package com.github.sociallabel.action;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.sociallabel.APIException;
import com.github.sociallabel.service.UserService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
	private UserService userService;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@RequestMapping(value = "/ota", method = RequestMethod.GET)
	public @ResponseBody FileSystemResource ota(HttpServletRequest request, HttpServletResponse response) throws Exception {
		File client = userService.getCurrentClientFile();
		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", client.getName());
        response.setHeader(headerKey, headerValue);
		return new FileSystemResource(client); 
	}
	
	
	@RequestMapping(value = "/ota/{version}", method = RequestMethod.GET)
	public @ResponseBody FileSystemResource otaByVersion(@PathVariable("version") String version, HttpServletRequest request, HttpServletResponse response) throws Exception {
		File client = userService.getClientFileByVersion(version);
		response.setContentType("application/octet-stream");
		String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", client.getName());
        response.setHeader(headerKey, headerValue);
		return new FileSystemResource(client); 
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
