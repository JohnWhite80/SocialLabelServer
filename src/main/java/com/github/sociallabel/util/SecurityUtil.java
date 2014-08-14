package com.github.sociallabel.util;


public class SecurityUtil {

	public static String encrypt(String password) {
		//TODO just return password
		return password;
//		MessageDigest digest;
//		try {
//			digest = MessageDigest.getInstance("SHA-256");
//			byte[] hash = digest.digest(password.getBytes("UTF-8"));
//			return Hex.encodeHexString(hash);
//		} catch (NoSuchAlgorithmException e) {
//			throw new APIException(500, "internal error");
//		} catch (UnsupportedEncodingException e) {
//			throw new APIException(500, "internal error");
//		}
	}

}
