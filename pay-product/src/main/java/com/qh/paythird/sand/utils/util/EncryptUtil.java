package com.qh.paythird.sand.utils.util;


import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptUtil
{
  private static final Logger logger = LoggerFactory.getLogger(EncryptUtil.class);
  private String publicKeyPath;
  private String privateKeyPath;
  private String keyPassword;
  private String merchId;

  public static String getRandomString(int length){
    String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    Random random=new Random();
    StringBuffer sb=new StringBuffer();
    for(int i=0;i<length;i++){
      int number=random.nextInt(str.length());
      sb.append(str.charAt(number));
    }
    return sb.toString();
  }

  public EncryptUtil(String publicKeyPath, String privateKeyPath, String keyPassword,String merchId)
  {
    this.publicKeyPath = publicKeyPath;
    this.privateKeyPath = privateKeyPath;
    this.keyPassword = keyPassword;
    this.merchId = merchId;
  }

  public List<NameValuePair> genEncryptData(String merchId, String transCode, String data)
    throws Exception
  {
    if ((merchId == null) || (transCode == null) || (data == null)) {
      logger.error("merchId or transCode or data is null");
      return null;
    }

    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    formparams.add(new BasicNameValuePair("merId", merchId));
    formparams.add(new BasicNameValuePair("transCode", transCode));
    try
    {
      CertUtil.init(this.publicKeyPath, this.privateKeyPath, this.keyPassword,this.merchId);
      byte[] plainBytes = data.getBytes("UTF-8");

      String aesKey = getRandomString(16);
      byte[] aesKeyBytes = aesKey.getBytes("UTF-8");
      
      String encryptData = new String(Base64.encodeBase64(
        CryptoUtil.AESEncrypt(plainBytes, aesKeyBytes, "AES", 
        "AES/ECB/PKCS5Padding", null)), 
        "UTF-8");

      String sign = new String(Base64.encodeBase64(
        CryptoUtil.digitalSign(plainBytes, CertUtil.getPrivateKey(this.merchId), 
        "SHA1WithRSA")), "UTF-8");

      String encryptKey = new String(Base64.encodeBase64(
        CryptoUtil.RSAEncrypt(aesKeyBytes, CertUtil.getPublicKey(), 2048, 11, 
        "RSA/ECB/PKCS1Padding")), "UTF-8");

      formparams.add(new BasicNameValuePair("encryptData", encryptData));
      formparams.add(new BasicNameValuePair("encryptKey", encryptKey));
      formparams.add(new BasicNameValuePair("sign", sign));
      logger.info("aesKey:{}", aesKey);
      logger.info("encryptData:{}", encryptData);
      logger.info("encryptKey:{}", encryptKey);
      logger.info("sign:{}", sign);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

    return formparams;
  }

  public List<NameValuePair> genEncryptData(String merchId, String transCode, String accessType, String plId, String data) throws Exception
  {
    if ((merchId == null) || (transCode == null) || (data == null)) {
      logger.error("merchId or transCode or data is null");
      return null;
    }

    List<NameValuePair> formparams = new ArrayList<NameValuePair>();
    formparams.add(new BasicNameValuePair("merId", merchId));
    formparams.add(new BasicNameValuePair("transCode", transCode));
    formparams.add(new BasicNameValuePair("accessType", accessType));
    formparams.add(new BasicNameValuePair("plId", plId));
    try
    {
      CertUtil.init(this.publicKeyPath, this.privateKeyPath, this.keyPassword,this.merchId);
      byte[] plainBytes = data.getBytes("UTF-8");

      String aesKey = getRandomString(16);
      byte[] aesKeyBytes = aesKey.getBytes("UTF-8");

      String encryptData = new String(Base64.encodeBase64(
        CryptoUtil.AESEncrypt(plainBytes, aesKeyBytes, "AES", 
        "AES/ECB/PKCS5Padding", null)), 
        "UTF-8");

      String sign = new String(Base64.encodeBase64(
        CryptoUtil.digitalSign(plainBytes, CertUtil.getPrivateKey(this.merchId), 
        "SHA1WithRSA")), "UTF-8");

      String encryptKey = new String(Base64.encodeBase64(
        CryptoUtil.RSAEncrypt(aesKeyBytes, CertUtil.getPublicKey(), 2048, 11, 
        "RSA/ECB/PKCS1Padding")), "UTF-8");

      formparams.add(new BasicNameValuePair("encryptData", encryptData));
      formparams.add(new BasicNameValuePair("encryptKey", encryptKey));
      formparams.add(new BasicNameValuePair("sign", sign));
      logger.info("encryptData:{}", encryptData);
      logger.info("encryptKey:{}", encryptKey);
      logger.info("sign:{}", sign);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

    return formparams;
  }
  public String decryptRetData(String data) throws Exception {
    Map<String,String> responseMap = convertResultStringToMap(data);
    String retEncryptKey = (String)responseMap.get("encryptKey");
    String retEncryptData = (String)responseMap.get("encryptData");
    String retSign = (String)responseMap.get("sign");

    logger.info("retEncryptKey:{}", retEncryptKey);
    logger.info("retEncryptData:{}", retEncryptData);
    logger.info("retSign:{}", retSign);

    byte[] decodeBase64KeyBytes = Base64.decodeBase64(retEncryptKey
      .getBytes("UTF-8"));

    byte[] merchantAESKeyBytes = CryptoUtil.RSADecrypt(
      decodeBase64KeyBytes, CertUtil.getPrivateKey(this.merchId), 2048, 11, 
      "RSA/ECB/PKCS1Padding");

    byte[] decodeBase64DataBytes = Base64.decodeBase64(retEncryptData
      .getBytes("UTF-8"));

    byte[] retDataBytes = CryptoUtil.AESDecrypt(decodeBase64DataBytes, 
      merchantAESKeyBytes, "AES", "AES/ECB/PKCS5Padding", null);

    logger.info("retData:{}", new String(retDataBytes, "UTF-8"));

    byte[] signBytes = Base64.decodeBase64(retSign
      .getBytes("UTF-8"));

    boolean isValid = CryptoUtil.verifyDigitalSign(retDataBytes, signBytes, 
      CertUtil.getPublicKey(), "SHA1WithRSA");
    if (!isValid) {
      logger.error("报文验签不通过");
      throw new Exception("报文验签不通过");
    }
    logger.info("报文验签通过");
    String ret = new String(retDataBytes, "UTF-8");
    return ret;
  }

  private static Map<String, String> convertResultStringToMap(String result)
  {
    Map<String, String> map = null;
    if (StringUtils.isNotBlank(result)) {
      if ((result.startsWith("\"")) && (result.endsWith("\""))) {
        System.out.println(result.length());
        result = result.substring(1, result.length() - 1);
      }
      map = SDKUtil.convertResultStringToMap(result);
    }
    return map;
  }
}