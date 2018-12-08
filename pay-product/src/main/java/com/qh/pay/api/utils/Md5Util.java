package com.qh.pay.api.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: Md5Util
 * @Description: md5加密
 * @date 2017年10月25日 上午9:28:36
 *
 */
public class Md5Util {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Md5Util.class);

	public static String MD5(String content) {
		if (ParamUtil.isNotEmpty(content)) {
			try {
				return HexUtil.byte2hex(MessageDigest.getInstance("md5").digest(content.getBytes()));
			} catch (NoSuchAlgorithmException e) {
				logger.error("MD5加密错误！" + e.getMessage());
			}
		} else {
			logger.error("MD5加密内容为空！");
		}
		return null;
	}

	public static String SHA(String content) {
		if (ParamUtil.isNotEmpty(content)) {
			try {
				return HexUtil.byte2hex(MessageDigest.getInstance("SHA").digest(content.getBytes()));
			} catch (NoSuchAlgorithmException e) {
				logger.error("SHA加密错误！" + e.getMessage());
				throw new RuntimeException("SHA加密错误！" + e.getMessage());
			}
		} else {
			logger.error("SHA加密内容为空！");
		}
		return null;
	}

	public static String MD5Update(String content) {
		if (ParamUtil.isNotEmpty(content)) {
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				logger.error("MD5加密错误！" + e.getMessage());
				throw new RuntimeException("MD5加密错误！" + e.getMessage());
			}
			messageDigest.update(content.getBytes());
			return HexUtil.byte2hex(messageDigest.digest());
		} else {
			logger.error("MD5加密内容为空！");
		}
		return null;

	}

	public static String SHAUpdate(String content) {
		if (ParamUtil.isNotEmpty(content)) {
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance("SHA");
			} catch (NoSuchAlgorithmException e) {
				logger.error("SHA加密错误！" + e.getMessage());
				throw new RuntimeException("SHA加密错误！" + e.getMessage());
			}
			messageDigest.update(content.getBytes());
			return HexUtil.byte2hex(messageDigest.digest());
		} else {
			logger.error("SHA加密内容为空！");
		}
		return null;

	}
	
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
	
	public static String sign(String text, String key, String inputCharset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, inputCharset));
        return mysign;
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