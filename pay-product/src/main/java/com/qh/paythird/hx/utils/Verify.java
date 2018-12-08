package com.qh.paythird.hx.utils;

import java.io.UnsupportedEncodingException;

/**
* Created by IH1334 on 2016/11/16.
*/
public class Verify {

	public static byte[] getBytes(String content, String charset) {
		if (isNULL(content)) {
			content = "";
		}
		if (isBlank(charset))
			throw new IllegalArgumentException("charset can not null");
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
		}
		throw new RuntimeException("charset is not valid,charset is:" + charset);
	}

	public static boolean isNULL(String str) {
		return str == null;
	}

	public static boolean isBlank(String str) {
		int strLen;
		if ((str == null) || ((strLen = str.length()) == 0))
			return true;

		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
