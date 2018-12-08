package com.qh.paythird.dianxin.utils;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * 
 * @ClassName XmlUtils
 * @Description XML的工具方法
 * @Date 2017年8月1日 下午4:16:41
 * @version 1.0.0
 */
public class XmlUtils {
    
    /** <一句话功能简述>
     * <功能详细描述>request转字符串
     * @param request
     * @return
     * @see [类、类#方法、类#成员]
     */
    public static String parseRequst(HttpServletRequest request){
        String body = "";
        try {
            ServletInputStream inputStream = request.getInputStream(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while(true){
                String info = br.readLine();
                if(info == null){
                    break;
                }
                if(body == null || "".equals(body)){
                    body = info;
                }else{
                    body += info;
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }            
        return body;
    }
    
    /***
     * 
     * @Description key appkey 被过滤
     * @param parameters
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static String parseXML(SortedMap<String, String> parameters) {
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        Set<?> es = parameters.entrySet();
        Iterator<?> it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if (null != v && !"".equals(v) && (!"appkey".equals(k) || !"key".equals(k))) {
                sb.append("<" + k + ">" + parameters.get(k) + "</" + k + ">\n");
            }
        }
        sb.append("</xml>");
        return sb.toString();
    }

    /**
     * 从request中获得参数Map，并返回可读的Map
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static SortedMap<String, String> getParameterMap(HttpServletRequest request) {
        // 参数Map
        Map<?, ?> properties = request.getParameterMap();
        // 返回值Map
        SortedMap<String, String> returnMap = new TreeMap<String, String>();
        Iterator<?> entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name = "";
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if(null == valueObj){
                value = "";
            }else if(valueObj instanceof String[]){
                String[] values = (String[])valueObj;
                for(int i=0;i<values.length;i++){
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length()-1);
            }else{
                value = valueObj.toString();
            }
            returnMap.put(name, value.trim());
        }
        return returnMap;
    }
    
    /**
     * 转XMLmap
     * @author  
     * @param xmlBytes
     * @param charset
     * @return
     * @throws Exception
     */
    public static Map<String, String> toMap(byte[] xmlBytes,String charset) throws Exception{
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(xmlBytes));
        source.setEncoding(charset);
        Document doc = reader.read(source);
        Map<String, String> params = XmlUtils.toMap(doc.getRootElement());
        return params;
    }
    
    /**
     * 转XMLmap
     * @author  
     * @param xmlBytes
     * @param charset
     * @return
     * @throws Exception
     */
    public static Map<String, Object> toMap(String sourceStr,String enCoding) throws Exception{
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(sourceStr.getBytes()));
        source.setEncoding(enCoding);
        Document doc = reader.read(source);
        return Dom2Map(doc.getRootElement());
    }
    /**
     * 转XMLmap
     * @author  
     * @param xmlBytes
     * @param charset
     * @return
     * @throws Exception
     */
    public static Map<String, Object> toMap(String sourceStr) throws Exception{
        SAXReader reader = new SAXReader(false);
        InputSource source = new InputSource(new ByteArrayInputStream(sourceStr.getBytes("UTF-8")));
        source.setEncoding("UTF-8");
        Document doc = reader.read(source);
        return Dom2Map(doc.getRootElement());
    }
    /**
     * 转MAP
     * @author  
     * @param element
     * @return
     */
    public static Map<String, String> toMap(Element element){
        Map<String, String> rest = new HashMap<String, String>();
        @SuppressWarnings("unchecked")
        List<Element> els = element.elements();
        for(Element el : els){
            rest.put(el.getName().toLowerCase(), el.getTextTrim());
        }
        return rest;
    }
    
    public static String toXml(Map<String, String> params){
        StringBuilder buf = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        buf.append("<xml>");
        for(String key : keys){
            buf.append("<").append(key).append(">");
            buf.append("<![CDATA[").append(params.get(key)).append("]]>");
            buf.append("</").append(key).append(">\n");
        }
        buf.append("</xml>");
        return buf.toString();
    }
    
    
    /**
     * 验签
     * @param xml
     * @return
     */
    public static boolean checkSign(String merCode, String directStr, String xml) {

        if (xml == null){
            return false;
        }
        String OldSign = getSign(xml); // 返回签名
        String text = getBodyXml(xml); // body
        System.out.println("MD5验签，验签文：" + text + "\n待比较签名值:" + OldSign);
        String  result = DigestUtils
                .md5Hex(getBytes(text + merCode + directStr,
                        "UTF-8"));
        if (OldSign == null || result == null || !OldSign.equals(result)) {
            return false;
        }
        return true;
    }

    /**
     * 获取报文中<Signature></Signature>部分
     * @param xml
     * @return
     */
    public static String getSign(String xml) {
        int s_index = xml.indexOf("<Signature>");
        int e_index = xml.indexOf("</Signature>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index + 11, e_index);
        }
        return sign;
    }

    /**
     * 获取body部分
     * @param xml
     * @return
     */
    public static String getBodyXml(String xml) {
        int s_index = xml.indexOf("<body>");
        int e_index = xml.indexOf("</body>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index, e_index + 7);
        }
        return sign;
    }

    /**
     * 获取报文中<RspCode></RspCode>部分
     * @param xml
     * @return
     */
    public static String getRspCode(String xml) {
        int s_index = xml.indexOf("<RspCode>");
        int e_index = xml.indexOf("</RspCode>");
        String rspCode = null;
        if (s_index > 0) {
            rspCode = xml.substring(s_index + 9, e_index);
        }
        return rspCode;
    }
    /**
     * 获取报文中<RspCode></RspCode>部分
     * @param xml
     * @return
     */
    public static String getRetCode(String xml) {
        int s_index = xml.indexOf("<RET_CODE>");
        int e_index = xml.indexOf("</RET_CODE>");
        String recCode = null;
        if (s_index > 0) {
            recCode = xml.substring(s_index + 10, e_index);
        }
        return recCode;
    }
    /**
     * 获取报文中<RspCode></RspCode>部分
     * @param xml
     * @return
     */
    public static String getErrMsg(String xml) {
        int s_index = xml.indexOf("<ERR_MSG>");
        int e_index = xml.indexOf("</ERR_MSG>");
        String errMsg = null;
        if (s_index > 0) {
            errMsg = xml.substring(s_index + 9, e_index);
        }
        return errMsg;
    }

    /**
     * 获取报文中<Status></Status>部分
     * @param xml
     * @return
     */
    public static String getStatus(String xml) {
        int s_index = xml.indexOf("<Status>");
        int e_index = xml.indexOf("</Status>");
        String status = null;
        if (s_index > 0) {
            status = xml.substring(s_index + 8, e_index);
        }
        return status;
    }
    /**
     * 获取报文中<IpsBillNo></IpsBillNo>部分
     * @param xml
     * @return
     */
    public static String getIpsBillNo(String xml) {
        int s_index = xml.indexOf("<IpsBillNo>");
        int e_index = xml.indexOf("</IpsBillNo>");
        String ipsBillNo = null;
        if (s_index > 0) {
            ipsBillNo = xml.substring(s_index + 11, e_index);
        }
        return ipsBillNo;
    }
    /**
     * 获取报文中<Amount></Amount>部分
     * @param xml
     * @return
     */
    public static String getAmount(String xml) {
        int s_index = xml.indexOf("<Amount>");
        int e_index = xml.indexOf("</Amount>");
        String amount = "0";
        if (s_index > 0) {
            amount = xml.substring(s_index + 8, e_index);
        }
        return amount;
    }
    /**
     * 获取报文中<RspMsg></RspMsg>部分
     * @param xml
     * @return
     */
    public static String getRspMsg(String xml) {
        int s_index = xml.indexOf("<RspMsg>");
        int e_index = xml.indexOf("</RspMsg>");
        String rspMsg = null;
        if (s_index > 0) {
            rspMsg = xml.substring(s_index + 8, e_index);
        }
        return rspMsg;
    }
    
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
    
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> Dom2Map(Document doc){  
        Map<String, Object> map = new HashMap<String, Object>();  
        if(doc == null)  
            return map;  
        Element root = doc.getRootElement();  
        for (Iterator<?> iterator = root.elementIterator(); iterator.hasNext();) {  
            Element e = (Element) iterator.next();  
            //System.out.println(e.getName());  
            List<Element> list = e.elements();  
            if(list.size() > 0){  
                map.put(e.getName(), Dom2Map(e));  
            }else  
                map.put(e.getName(), e.getText());  
        }  
        return map;  
    }  
      
  
    @SuppressWarnings("unchecked")
    public static Map<String, Object> Dom2Map(Element e){  
        Map<String, Object> map = new HashMap<>();  
        List<Element> list = e.elements();  
        if(list.size() > 0){  
            for (int i = 0;i < list.size(); i++) {  
                Element iter = list.get(i);  
                List<Object> mapList = new ArrayList<>();  
                  
                if(iter.elements().size() > 0){  
                    Map<?, ?> m = Dom2Map(iter);  
                    if(map.get(iter.getName()) != null){  
                        Object obj = map.get(iter.getName());  
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){  
                            mapList = new ArrayList<Object>();  
                            mapList.add(obj);  
                            mapList.add(m);  
                        }  
                        if(obj.getClass().getName().equals("java.util.ArrayList")){  
                            mapList = (List<Object>) obj;  
                            mapList.add(m);  
                        }  
                        map.put(iter.getName(), mapList);  
                    }else  
                        map.put(iter.getName(), m);  
                }  
                else{  
                    if(map.get(iter.getName()) != null){  
                        Object obj = map.get(iter.getName());  
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){  
                            mapList = new ArrayList<Object>();  
                            mapList.add(obj);  
                            mapList.add(iter.getText());  
                        }  
                        if(obj.getClass().getName().equals("java.util.ArrayList")){  
                            mapList = (List<Object>) obj;  
                            mapList.add(iter.getText());  
                        }  
                        map.put(iter.getName(), mapList);  
                    }else  
                        map.put(iter.getName(), iter.getText());  
                }  
            }  
        }else  
            map.put(e.getName(), e.getText());  
        return map;  
    }  
  
}  

