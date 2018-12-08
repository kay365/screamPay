package com.qh.paythird.huiFuBao.utils;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/27.
 */
public class SmallTools {
    /**
     * MD5加密
     * @param str 需要加密的值
     * @return 加密完成的值(小写)
     */
    public static String MD5en(String str){
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return re_md5;
    }


    /**
     * 获取当前时间（格式自传）
     * @param dateFormat 要返回的时间格式，例如yyyy/MM/dd HH:mm:ss
     * @return
     */
    public static String getDate(String dateFormat){
        Date date = new Date();
        SimpleDateFormat dateF = new SimpleDateFormat(dateFormat);//可以方便地修改日期格式
        String retu = dateF.format(date);
        return retu;
    }


    /**
     * 解析出url参数中的键值对
     * 如 "index.jsp?Action=del|id=123"，解析出Action:del,id:123存入map中
     * @param
     * @return  url请求参数部分
     */
    public static Map<String, String> URLRequest(String result)
    {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit=null;
        //每个键值为一组
        arrSplit=result.split("[|]");
        for(String strSplit:arrSplit)
        {
            String[] arrSplitEqual=null;
            arrSplitEqual= strSplit.split("[=]");

            //解析出键值
            if(arrSplitEqual.length>1)
            {
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

            }
            else
            {
                if(arrSplitEqual[0]!="")
                {
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
}
