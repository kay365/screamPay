package com.qh.paythird.tx.utils;

import com.qh.redis.service.RedisUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.TreeMap;

public class TXPayRequestUtils {

	
	public static void main(String[] args) {
		System.out.println("java.nio.charset.Charset.defaultCharset():" + java.nio.charset.Charset.defaultCharset());
        String s = "中文";
        try {
            System.out.println("GBK:" + new String(s.getBytes(), "GBK"));
            System.out.println("UTF-8:" + new String(s.getBytes(), "UTF-8"));
            System.out.println("ISO8859-1:" + new String(s.getBytes(), "ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("java.nio.charset.Charset.defaultCharset():" + java.nio.charset.Charset.defaultCharset());
    }

    /**
     * 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
     */
    public static String getParamSrc(TreeMap<String, String> paramsMap) {
        StringBuffer paramstr = new StringBuffer();
        for (String pkey : paramsMap.keySet()) {
            String pvalue = paramsMap.get(pkey);
            if (null != pvalue && "" != pvalue) {// 空值不传递，不签名
                paramstr.append(pkey + "=" + pvalue + "&"); // 签名原串，不url编码
            }
        }
        // 去掉最后一个&
        String result = paramstr.substring(0, paramstr.length() - 1);
        System.out.println("签名原串：" + result);
        System.out.println("java.nio.charset.Charset.defaultCharset():" + java.nio.charset.Charset.defaultCharset());
        return result;
    }

    /**
     * 分解解密后的字符串，保存为map
     */
    public static HashMap<String, String> parseString(String responseData) {
        HashMap<String, String> map = new HashMap<String, String>();
        String[] s1 = responseData.split("&");
        String[] s2 = new String[2];
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s1.length; i++) {
            s2 = s1[i].split("=", 2);
            map.put(s2[0], s2[1]);
            if (!s2[0].equals("sign")) {
                sb.append(s2[0] + "=" + s2[1] + "&");
            }
        }
        String source = sb.substring(0, sb.length() - 1);
        map.put("source", source);
        return map;
    }

    /**
     * 解析xml
     */
    public static String getXmlElement(String responseData, String element) {
        String result = null;

        try {
            Document dom = DocumentHelper.parseText(responseData);
            Element root = dom.getRootElement();
            result = root.element(element).getText();
        } catch (DocumentException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public static String doPost(String url, String param) {
        String serverEncode = RedisUtil.getPayCommonValue(TXPayConst.SERVICE_ENCODE);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), serverEncode));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
