package com.qh.paythird.mobao.utils;

import java.security.MessageDigest;

public class MD5 {
	
   public static String md5(String message) {  
       MessageDigest messageDigest = null;  
       StringBuffer md5StrBuff = new StringBuffer();  
       try {  
           messageDigest = MessageDigest.getInstance("MD5");  
           messageDigest.reset();  
           messageDigest.update(message.getBytes("gbk"));  
             
           byte[] byteArray = messageDigest.digest();  
           for (int i = 0; i < byteArray.length; i++)   
           {  
               if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
                   md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
               else  
                   md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
           }  
       } catch (Exception e) {  
           throw new RuntimeException();  
       }  
       return md5StrBuff.toString().toUpperCase();//字母大写  
   }  
   
   public static void main(String[] args) {
	   System.out.println(md5("versionId=001&businessType=1100&insCode=&merId=818310048160000&orderId=5392923828800866170&transDate=20160704160022&transAmount=0.1&transCurrency=156&transChanlName=ICBC&openBankName=&pageNotifyUrl=http//localhsot:8182/merDemo/pageBack&backNotifyUrl=http://localhsot:8182/merDemo/backFrom&orderDesc=&dev=1FDD2547FA4FB61F1FDD2547FA4FB61F"));
   }
}
