package com.qh.paythird.mobao.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtil {

	public static String POSTAcp(String urls,String response){
		//创建连接  
        try {
        	URL url = new URL(urls);  
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(120000);
			connection.setDoOutput(true);  
			connection.setDoInput(true);  
			connection.setRequestMethod("POST");  
			connection.setUseCaches(false);  
			connection.setInstanceFollowRedirects(true);               
			connection.setRequestProperty("Content-Type","application/json; charset=GBK");                     
			connection.connect();
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());  
			out.write(response.getBytes("GBK"));
            out.flush();  
            out.close();  
              
            //读取响应  
            BufferedReader reader = new BufferedReader(new InputStreamReader(  
                    connection.getInputStream()));  
            String lines;  
            StringBuffer sb = new StringBuffer("");  
            while ((lines = reader.readLine()) != null) {  
                lines = new String(lines.getBytes(), "gbk");  
                sb.append(lines);  
            }  
            return sb.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return null;
	}
	
	/***
	 * 
	 * @param url
	 * @param map
	 * @param charSet
	 * @return
	 */
	public static  String POSTReturnString( String url, Map<String, String> map,String charSet) {
		
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPost httpost = new HttpPost(url); 
		httpost.setHeader("Content-Type","application/x-www-form-urlencoded;charset=" + charSet);
		List<NameValuePair> paramList = new ArrayList<NameValuePair>(); 
		for (Map.Entry<String, String> entry : map.entrySet()) {
			paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			httpost.setEntity(new UrlEncodedFormEntity(paramList, charSet));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}  
		CloseableHttpResponse  httpResponse = null;
		String content = null;   
		try {
			httpResponse = closeableHttpClient.execute(httpost);
			HttpEntity entity = httpResponse.getEntity();    
            content = EntityUtils.toString(entity, charSet);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {    
                httpResponse.close();    
            } catch (IOException e) {    
                e.printStackTrace();    
            }    
			try {  //关闭连接、释放资源    
	            closeableHttpClient.close();    
	        } catch (IOException e) {    
	            e.printStackTrace();    
	        }      
		}
		return content;
	}
}
