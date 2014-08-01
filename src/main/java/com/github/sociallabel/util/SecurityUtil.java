package com.github.sociallabel.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.github.sociallabel.APIException;

public class SecurityUtil {

	public static String encrypt(String password) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes("UTF-8"));
			return Hex.encodeHexString(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new APIException(500, "internal error");
		} catch (UnsupportedEncodingException e) {
			throw new APIException(500, "internal error");
		}
	}

}
