package com.github.sociallabel.action;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.sociallabel.APIException;

public class AbsController  {
	
	private static final Logger logger = LoggerFactory.getLogger(AbsController.class);	

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
