package com.qh.pay.api.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.LoggerFactory;

/**
 * 
  * @ClassName: AesUtil
  * @Description: AES对称加密算法
  * @date 2017年10月25日 上午9:57:13
  *
 */
public class AesUtil {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AesUtil.class);
	private static final String ALGORITHM = "AES";
	private static final String DEFAULT_CHARSET = "UTF-8";
	public static final String key = "6cfzreGXoUpKsBtekbiPTg==";
	/**
	 * 生成秘钥
	 * 
	 * @return
	 */
	public static String generaterKey(){
		KeyGenerator keygen = null;
		try {
			keygen = KeyGenerator.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Aes生成密钥错误！" + e.getMessage());
			throw new RuntimeException("Aes生成密钥错误！" + e.getMessage());
		}
		keygen.init(128, new SecureRandom()); // 16 字节 == 128 bit
		// keygen.init(128, new SecureRandom(seedStr.getBytes())); //
		// 随机因子一样，生成出来的秘钥会一样
		SecretKey secretKey = keygen.generateKey();
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}

	/**
	 * 生成秘钥
	 * 
	 * @return
	 */
	public static String generaterKey(String seedStr){
		KeyGenerator keygen = null;
		try {
			keygen = KeyGenerator.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.error("Aes生成密钥错误！" + e.getMessage());
			throw new RuntimeException("Aes生成密钥错误！" + e.getMessage());
		}
		keygen.init(128, new SecureRandom(seedStr.getBytes())); //
		// 随机因子一样，生成出来的秘钥会一样
		SecretKey secretKey = keygen.generateKey();
		return Base64.getEncoder().encodeToString(secretKey.getEncoded());
	}
	/**
	 */
	public static SecretKeySpec getSecretKeySpec(String secretKeyStr) {
		byte[] secretKey = Base64.getDecoder().decode(secretKeyStr);
		return new SecretKeySpec(secretKey, ALGORITHM);
	}

	/**
	 * 
	  * encrypt(这里用一句话描述这个方法的作用)
	  * @Title: encrypt
	  * @Description: AES加密
	  * @param content
	  * @param 设定文件
	  * @return String    返回类型
	  * @throws
	 */
	public static String encrypt(String content){
		return encrypt(content, key);
	}
	/**
	 * 加密
	 */
	public static String encrypt(String content, String secretKey){
		if (ParamUtil.isNotEmpty(content)) {
			Key key = getSecretKeySpec(secretKey);
			Cipher cipher;
			try {
				cipher = Cipher.getInstance(ALGORITHM);
				cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
				byte[] result = cipher.doFinal(content.getBytes(DEFAULT_CHARSET));
				// 创建密码器
				return Base64.getEncoder().encodeToString(result);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | 
					IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
				logger.error("Aes加密错误！" + e.getMessage());
				throw new RuntimeException("Aes加密错误！" + e.getMessage());
			}
		}else{
			logger.error("Aes加密内容为空！");
		}
		return null;
	}

	/**
	 * 
	  * decrypt
	  * @Title: decrypt
	  * @Description: 解密
	  * @param content
	  * @param 设定文件
	  * @return String    返回类型
	  * @throws
	 */
	public static String decrypt(String content) {
		return decrypt(content, key);
	}
	/**
	 * 解密
	 */
	public static String decrypt(String content, String secretKey){
		if (ParamUtil.isNotEmpty(content)) {
			Key key = getSecretKeySpec(secretKey);
			Cipher cipher;
			try {
				cipher = Cipher.getInstance(ALGORITHM);
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] result = cipher.doFinal(Base64.getDecoder().decode(content));
				return new String(result);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				logger.error("Aes解密错误！" + e.getMessage());
				throw new RuntimeException("Aes解密错误！" + e.getMessage());
			}
		}else{
			logger.error("Aes解密内容为空！");
		}
		return null;
	}
	
	public static void main(String[] args) {
		String key = generaterKey();
		System.out.println(key);
		key = generaterKey();
		System.out.println(key);
		String content = "陈大侠陈大侠";
		String result = encrypt(content);
		System.out.println(content + " 加密结果：" + result);
		result = decrypt(result);
		System.out.println(result);
	}
}