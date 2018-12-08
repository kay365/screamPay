package com.qh.paythird.hx.utils;

import com.qh.pay.api.utils.ParamUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;
import java.util.TreeMap;

public class MD5Utils {

    private static final Logger logger = LoggerFactory.getLogger(com.qh.paythird.dianxin.utils.MD5Utils.class);

    /**
     * MD5签名
     *
     * @param paramSrc     the source to be signed
     * @return
     * @throws Exception
     */
    public static String sign(String paramSrc,String key) {
        return md5(paramSrc + "&key=" + key);
    }

    /***
     *
     * @Description 验签检查
     * @param resMap
     * @param dx_key
     * @return
     */
    public static boolean checkParam(Map<String, String> params, String key, String encodeType) {
        boolean result = false;
        if(params.containsKey("sign")){
            String sign = params.get("sign");
            params.remove("sign");
            StringBuilder buf = new StringBuilder((params.size() +1) * 10);
            ParamUtil.buildPayParams(buf,params,false);
            String preStr = buf.toString();
            String signRecieve = sign(preStr, key, encodeType);
            result = sign.equalsIgnoreCase(signRecieve);
        }
        return result;
    }

    /***
     *
     * @Description 验签检查
     * @param resMap
     * @param dx_key
     * @return
     */
    public static boolean checkParamMd5(Map<String, String> params, String key, String encodeType,String sign_key) {
        boolean result = false;
        if(params.containsKey(sign_key)){
            String sign = params.get(sign_key);
            params.remove(sign_key);
            StringBuilder buf = new StringBuilder((params.size() +1) * 10);
            ParamUtil.buildPayParams(buf,params,false);
            String preStr = buf.toString();
            String signRecieve = sign_notkey(preStr+key, encodeType);
            logger.info("原始签名："+sign);
            logger.info("参数签名："+signRecieve);
            result = sign.equalsIgnoreCase(signRecieve);
        }
        return result;
    }

    /***
     * 先去除空值，然后进行签名
     * @Description 验签检查
     * @param resMap
     * @param dx_key
     * @return
     */
    public static boolean checkParam_space(Map<String, String> params, String key, String encodeType) {
        boolean result = false;
        if(params.containsKey("sign")){
            String sign = params.get("sign");
            params.remove("sign");
            StringBuilder buf = new StringBuilder((params.size() +1) * 10);
            ParamUtil.buildPayParams_space(buf,params,false);
            String preStr = buf.toString();
            String signRecieve = sign(preStr, key, encodeType);
            result = sign.equalsIgnoreCase(signRecieve);
        }
        return result;
    }

    /**
     *
     * @Description 指定编码
     * @param paramSrc
     * @param key
     * @param encodeType
     * @return
     */
    public static String sign(String paramSrc, String key, String encodeType) {
        return DigestUtils.md5Hex(getContentBytes(paramSrc + "&key=" + key, encodeType));
    }

    /**
     *
     * @Description 指定编码
     * @param paramSrc
     * @param key
     * @param encodeType
     * @return
     */
    public static String sign_notkey(String paramSrc, String encodeType) {
        return DigestUtils.md5Hex(getContentBytes(paramSrc, encodeType));
    }

    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String paramSrc, String sign, String key, String encodeType) {
        return DigestUtils.md5Hex(getContentBytes(paramSrc + "&key=" + key, encodeType)).equals(sign);
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    /**
     * MD5验签
     *
     * @param source  签名内容
     * @param sign    签名值
     * @return
     */
    public static boolean verify(String source, String tfbSign,String key) {
        return tfbSign.equals(md5(source + "&key=" + key));
    }

    public final static String md5(String paramSrc) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        try {
            byte[] btInput = paramSrc.getBytes("UTF-8");
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Md5错误！");
        }
    }

    /**
     * 签名
     * @param params
     * @return
     * @throws Exception
     */
    public static String sign(TreeMap<String,String> params, String appkey, String encodeType){
        if(params.containsKey("sign"))//签名明文组装不包含sign字段
            params.remove("sign");
        params.put("key", appkey);
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry:params.entrySet()){
            if(entry.getValue()!=null&&entry.getValue().length()>0){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        String sign;
        try {
            sign = md5(sb.toString().getBytes(encodeType));
        } catch (UnsupportedEncodingException e) {
            return "";
        }//记得是md5编码的加签
        params.remove("key");
        return sign;
    }
    /**
     * 签名
     * @param params
     * @return
     * @throws Exception
     */
    public static String sign(TreeMap<String,String> params,String appkey){
        if(params.containsKey("sign"))//签名明文组装不包含sign字段
            params.remove("sign");
        params.put("key", appkey);
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry:params.entrySet()){
            if(entry.getValue()!=null&&entry.getValue().length()>0){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        logger.info("通联支付签名原串：" + sb.toString());
        String sign;
        try {
            sign = md5(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return "";
        }//记得是md5编码的加签
        params.remove("key");
        logger.info("通联支付签名结果：" + sign);
        return sign;
    }
    /**
     * 签名
     * @param params
     * @return
     * @throws Exception
     */
    public static String commonSign(TreeMap<String,String> params,String appkey){
        if(params.containsKey("sign"))//签名明文组装不包含sign字段
            params.remove("sign");
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry:params.entrySet()){
            if(entry.getValue()!=null&&entry.getValue().length()>0){
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key").append("=").append(appkey);
        logger.info("通用签名原串：" + sb.toString());
        String sign = "";
        try {
            sign = md5(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        params.remove("key");
        logger.info("通用签名结果：" + sign);
        return sign;
    }
    /**
     * 签名
     * @param params
     * @return
     * @throws Exception
     */
    public static String commonSignLowerKey(TreeMap<String,String> params,String appkey){
        if(params.containsKey("sign"))//签名明文组装不包含sign字段
            params.remove("sign");
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, String> entry:params.entrySet()){
            if(entry.getValue()!=null&&entry.getValue().length()>0){
                sb.append(entry.getKey().toLowerCase()).append("=").append(entry.getValue()).append("&");
            }
        }
        sb.append("key").append("=").append(appkey);
        logger.info("通用签名原串：" + sb.toString());
        String sign = "";
        try {
            sign = md5(sb.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        params.remove("key");
        logger.info("通用签名结果：" + sign);
        return sign;
    }
    /***
     *
     * @Description 验签
     * @param param
     * @param appkey
     * @param encodeType
     * @return
     * @throws Exception
     */
    public static boolean validSign(TreeMap<String,String> param,String appkey){
        if(param!=null&&!param.isEmpty()){
            if(!param.containsKey("sign"))
                return false;
            String sign = param.get("sign").toString();
            String mysign = sign(param, appkey);
            return sign.toLowerCase().equals(mysign.toLowerCase());
        }
        return false;
    }
    /***
     *
     * @Description 验签
     * @param param
     * @param appkey
     * @param encodeType
     * @return
     * @throws Exception
     */
    public static boolean validSign(TreeMap<String,String> param,String appkey,String encodeType) throws Exception{
        if(param!=null&&!param.isEmpty()){
            if(!param.containsKey("sign"))
                return false;
            String sign = param.get("sign").toString();
            String mysign = sign(param, appkey, encodeType);
            return sign.toLowerCase().equals(mysign.toLowerCase());
        }
        return false;
    }
    /**
     * md5
     * @param b
     * @return
     */
    public static String md5(byte[] b) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(b);
            byte[] hash = md.digest();
            StringBuffer outStrBuf = new StringBuffer(32);
            for (int i = 0; i < hash.length; i++) {
                int v = hash[i] & 0xFF;
                if (v < 16) {
                    outStrBuf.append('0');
                }
                outStrBuf.append(Integer.toString(v, 16).toLowerCase());
            }
            return outStrBuf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new String(b);
        }
    }
    /***
     * MD5加码 生成32位md5
     */
    public static String string2MD5(String inStr){
        System.err.println(inStr);
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }


    public static void main(String[] args) {

    }
}
