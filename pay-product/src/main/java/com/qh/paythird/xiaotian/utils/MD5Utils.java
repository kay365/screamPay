package com.qh.paythird.xiaotian.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * <p>
 * 功能说明：
 * </p>
 * <ul>
 * <li></li>
 * </ul>
 * <p>
 * Created by Chen,Wenbi 2015/6/26.
 * </p>
 * <p>
 * Email Address: <a href=“chenwb@lianlian.com.cn”>chenwb@lianlian.com.cn</a>
 * </p>
 */
public class MD5Utils
{
    
    public static String getKeyedDigest(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF-8"));
            
            String result="";
            byte[] temp;
            temp=md5.digest(key.getBytes("UTF-8"));
            for (int i=0; i<temp.length; i++){
                result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
            }
            
            return result;
            
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
            
        }catch(Exception e)
        {
          e.printStackTrace();
        }
        return null;
    }
    
    public static String getSignParam(Map<String,String> params){
        StringBuilder buf = new StringBuilder((params.size() +1) * 10);
        buildPayParams(buf,params,false);
        String result = buf.toString();    
        return result;
    }
    
    /**
     * @author 
     * @param payParams
     * @return
     */
    public static void buildPayParams(StringBuilder sb,Map<String, String> payParams,boolean encoding){
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for(String key : keys){
            sb.append(key).append("=");
            if(encoding){
                sb.append(urlEncode(payParams.get(key)));
            }else{
                sb.append(payParams.get(key));
            }
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }
    
    public static String urlEncode(String str){
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Throwable e) {
            return str;
        } 
    }

    /**
     * 将指定的字符串用MD5加密 originstr 需要加密的字符串
     * 
     * @param originstr
     * @return
     */

    public static String ecodeByMD5(String originstr)
    {

        String result = null;

        char hexDigits[] =
        {// 用来将字节转换成 16 进制表示的字符

        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        if (originstr != null)
        {

            try
            {

                // 返回实现指定摘要算法的 MessageDigest 对象

                MessageDigest md = MessageDigest.getInstance("MD5");

                // 使用utf-8编码将originstr字符串编码并保存到source字节数组

                byte[] source = originstr.getBytes("utf-8");

                // 使用指定的 byte 数组更新摘要

                md.update(source);

                // 通过执行诸如填充之类的最终操作完成哈希计算，结果是一个128位的长整数

                byte[] tmp = md.digest();

                // 用16进制数表示需要32位

                char[] str = new char[32];

                for (int i = 0, j = 0; i < 16; i++)
                {

                    // j表示转换结果中对应的字符位置

                    // 从第一个字节开始，对 MD5 的每一个字节

                    // 转换成 16 进制字符

                    byte b = tmp[i];

                    // 取字节中高 4 位的数字转换

                    // 无符号右移运算符>>> ，它总是在左边补0

                    // 0x代表它后面的是十六进制的数字. f转换成十进制就是15

                    str[j++] = hexDigits[b >>> 4 & 0xf];

                    // 取字节中低 4 位的数字转换

                    str[j++] = hexDigits[b & 0xf];

                }

                result = new String(str);// 结果转换成字符串用于返回

            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 创建密匙
     * 
     * @param algorithm
     *            加密算法,可用 DES,DESede,Blowfish
     * @return SecretKey 秘密（对称）密钥
     */
    public static SecretKey createSecretKey(String algorithm)
    {
        // 声明KeyGenerator对象
        KeyGenerator keygen;
        // 声明 密钥对象
        SecretKey deskey = null;
        try
        {
            // 返回生成指定算法的秘密密钥的 KeyGenerator 对象
            keygen = KeyGenerator.getInstance(algorithm);
            // 生成一个密钥
            deskey = keygen.generateKey();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        // 返回密匙
        return deskey;
    }

    public static void main(String[] args)
    {
        System.out.println(ecodeByMD5("广谱"));
    }
}
