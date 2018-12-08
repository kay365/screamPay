/**

 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-26 下午8:30:38
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-26        Initailized
 */
package com.qh.paythird.sand.utils.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author pan.xl
 *
 */
public class HttpClient {
	
private static Logger logger = LoggerFactory.getLogger(HttpClient.class);
	
	private static final String DEFAULT_CHARSET = SandpayConstants.UTF8_CHARSET;
	
	private static SSLContext sslcontext;
	
	private static SSLConnectionSocketFactory sslsf;
	
	
	public static String doPost(String url, Map<String, String> params,
			int connectTimeout, int readTimeout) throws IOException {
		return doPost(url, params, DEFAULT_CHARSET, connectTimeout, readTimeout);
	}

	public static String doPost(String url, Map<String, String> params, String charset, int connectTimeout, int readTimeout)
			throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-type", "application/x-www-form-urlencoded;charset=" + charset);
		return doPost(url, headers, params, charset, connectTimeout, readTimeout);
	}

	public static String doPost(String url, Map<String, String> headers, Map<String, String> params, final String charset, int connectTimeout,
			int readTimeout) throws IOException {
		
		URL targetUrl = new URL(url);
		HttpHost httpHost = new HttpHost(targetUrl.getHost(), targetUrl.getPort(), targetUrl.getProtocol());
		logger.info("host:" + targetUrl.getHost() + ",port:" + targetUrl.getPort() + ",protocol:" + targetUrl.getProtocol() + ",path:" + targetUrl.getPath());
		
		CloseableHttpClient httpclient = getHttpClient(targetUrl);
		
		try {
			HttpPost httpPost = getHttpPost(targetUrl, headers, params, charset,
					connectTimeout, readTimeout);
	
			String resp = httpclient.execute(httpHost, httpPost,
					new ResponseHandler<String>() {
						public String handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							
							int status = response.getStatusLine().getStatusCode();
	
							logger.info("status:[{}]", new Object[] { status });
							if (status == 200) {
								return EntityUtils.toString(response.getEntity(), charset);
							} else {
								return "";
							}
						}
					});
			return resp; 
		} finally {
			httpclient.close();
		}
		
	}
	
	/**
     * 执行HTTP GET请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @return 响应字符串
     * @throws IOException
     */
    public static String doGet(String url, Map<String, String> params) throws IOException {
        return doGet(url, params, DEFAULT_CHARSET);
    }
    
    /**
     * 执行HTTP GET请求。
     * 
     * @param url 请求地址
     * @param params 请求参数
     * @param charset 字符集，如UTF-8, GBK, GB2312
     * @return 响应字符串
     * @throws IOException
     */
    public static String doGet(String url, Map<String, String> params,
                               String charset) throws IOException {
    	
    	Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-type", "application/x-www-form-urlencoded;charset=" + charset);
		return doGet(url, headers, params, charset);
    	
    }

	/** 
	* 
	* @param url
	* @param headers
	* @param params
	* @param charset
	* @return     
	 * @throws IOException 
	 * @throws ClientProtocolException
	*/
	public static String doGet(String url, Map<String, String> headers,
			Map<String, String> params, final String charset) throws IOException {
		
		URL targetUrl = new URL(url);
		CloseableHttpClient httpclient = getHttpClient(targetUrl);
		
		try {
			
			HttpGet httpGet = getHttpGet(url, headers, params, charset);
			
			String resp = httpclient.execute(httpGet,
					new ResponseHandler<String>() {
						public String handleResponse(HttpResponse response)
								throws ClientProtocolException, IOException {
							int status = response.getStatusLine().getStatusCode();
	
							logger.info("status:[{}]", new Object[] { status });
							if (status == 200) {
								return EntityUtils.toString(response.getEntity(), charset);
							} else {
								return "";
							}
						}
					});
			return resp;
			
		} finally {
			httpclient.close();
		}
	}

	/** 
	* 
	* @param targetUrl
	* @param headers
	* @param params
	* @param charset
	 * @param isProxy 
	* @return     
	 * @throws IOException 
	*/
	private static HttpGet getHttpGet(String url,
                                      Map<String, String> headers, Map<String, String> params,
                                      String charset) throws IOException {
		
		URL targetUrl = HttpUtil.buildGetUrl(url, HttpUtil.buildQuery(params, charset));
		HttpGet httpGet = new HttpGet(targetUrl.toString());
		
		Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			httpGet.setHeader(entry.getKey(), entry.getValue());
		}
		
		return httpGet;
		
	}

	/**
	 * @param isProxy 
	 * 
	 * @param targetUrl 
	 * @param headers
	 * @param params
	 * @param charset
	 * @param connectTimeout
	 * @param readTimeout
	 * @return
	 * @throws IOException 
	 * @throws  
	 */
	private static HttpPost getHttpPost(URL targetUrl, Map<String, String> headers, Map<String, String> params, String charset,
                                        int connectTimeout, int readTimeout) throws IOException {
		
		HttpPost httpPost = new HttpPost(targetUrl.getPath());
		
		Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			httpPost.setHeader(entry.getKey(), entry.getValue());
		}
		
		RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(readTimeout)
                .setConnectTimeout(connectTimeout) //Connection timeout is the timeout until a connection with the server is established. 
                .build();
		httpPost.setConfig(requestConfig);
		
		StringEntity entity = new StringEntity(HttpUtil.buildQuery(params, charset), charset);
		httpPost.setEntity(entity);
		
		return httpPost;
	}

	/**
	 * 
	 * @param targetUrl
	 * @return
	 */
	private static CloseableHttpClient getHttpClient(URL targetUrl) {
		
		CloseableHttpClient httpClient = null;
		if ("https".equals(targetUrl.getProtocol())) {
			
			httpClient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.build();
		} else {
			httpClient = HttpClients.createDefault();
		}
		return httpClient;
	}
	
	private static class DefaultTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    }
	
	static {
		try {
			sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() },
	                new SecureRandom());
			
			 // Allow TLSv1 protocol only
	        sslsf = new SSLConnectionSocketFactory(
	                sslcontext,
	                new String[] { "TLSv1" },
	                null,
	                new HostnameVerifier() {
	    	            public boolean verify(String hostname, SSLSession session) {
	    	                return true;//默认认证不通过，进行证书校验。
	    	            }
	    	        });
	        
	        
	        // javax.net.ssl.SSLPeerUnverifiedException: Host name '192.168.92.124' does not match the certificate subject provided by the peer (EMAILADDRESS=lsq1015@qq.com, CN=ipay, OU=CMBC, O=XMCMBC, L=Xiamen, ST=Fujian, C=CN)
	         //at org.apache.http.conn.ssl.SSLConnectionSocketFactory.verifyHostname(SSLConnectionSocketFactory.java:394)
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
