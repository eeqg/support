package com.kycq.library.support;

import java.security.MessageDigest;

public class MD5Utils {
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	private MD5Utils() {
	}
	
	/**
	 * MD5加密
	 *
	 * @param str 加密字符串
	 * @return 加密结果
	 */
	public static String encode(String str) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(str.getBytes());
			byte[] md = digest.digest();
			int length = md.length;
			char set[] = new char[length * 2];
			int index = 0;
			for (byte byteCode : md) {
				set[index++] = DIGITS[byteCode >>> 4 & 0xf];
				set[index++] = DIGITS[byteCode & 0xf];
			}
			return new String(set);
		} catch (Exception ignored) {
			return null;
		}
	}
}
