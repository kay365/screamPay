package com.qh.paythird.beecloud.utils;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;

public class BKUtil {

	public static boolean verifySign(String text,String masterKey,String signature) {
        boolean isVerified = verify(text, signature, masterKey, "UTF-8");
        if (!isVerified) {
            return false;
        }
        return true;
    }


	public static boolean verify(String text, String sign, String key, String inputCharset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, inputCharset));
        return mysign.equals(sign);
    }

	public static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
}
