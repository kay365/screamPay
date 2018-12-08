/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : httpclient-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-14 下午2:21:49
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-14        Initailized
 */
package com.qh.paythird.sand.utils.util;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @ClassName ：HttpUtil
 * @author : pxl
 * @Date : 2016-12-14 下午2:21:49
 * @version 2.0.0
 *
 */
public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	private EncryptUtil encyptUtil;
	
	public static String buildQuery(Map<String, String> params, String charset) throws IOException {
		 
		List<NameValuePair> nvps = new LinkedList<NameValuePair>();
		
		Set<Entry<String, String>> entries = params.entrySet();
		for (Entry<String, String> entry : entries) {
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		String str = URLEncodedUtils.format(nvps, charset);
		
		return str;
	 }
	
	public static URL buildGetUrl(String strUrl, String query) throws IOException {
        URL url = new URL(strUrl);
        if (StringUtils.isEmpty(query)) {
            return url;
        }

        if (StringUtils.isEmpty(url.getQuery())) {
            if (strUrl.endsWith("?")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "?" + query;
            }
        } else {
            if (strUrl.endsWith("&")) {
                strUrl = strUrl + query;
            } else {
                strUrl = strUrl + "&" + query;
            }
        }

        return new URL(strUrl);
    }
	
	  
	  public HttpUtil(String publicKeyPath,String privateKeyPath,String keyPassword,String merchId)
	  {
	    this.encyptUtil = new EncryptUtil(publicKeyPath, privateKeyPath,keyPassword,merchId);
	  }

	  public String post(String url, String merchId, String transCode, String data) throws Exception {
	    String res = post(url, this.encyptUtil.genEncryptData(merchId, transCode, data));
	    if (res == null) {
	      return null;
	    }
	    logger.info("杉德代付 返回参数:"+res);
	    return this.encyptUtil.decryptRetData(res);
	  }

	  public String post(String url, String merchId, String transCode, String accessType, String plId, String data) throws Exception
	  {
	    String res = post(url, this.encyptUtil.genEncryptData(merchId, transCode, accessType, plId, data));
	    if (res == null) {
	      return null;
	    }
	    return this.encyptUtil.decryptRetData(res);
	  }

	  private String post(String url, List<NameValuePair> formparams)
	    throws Exception
	  {
	    CloseableHttpClient httpclient = HttpClients.createDefault();
	    HttpPost httppost = new HttpPost(url);
	    try
	    {
	      UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
	      httppost.setEntity(uefEntity);
	      logger.info("executing request url:{} ", httppost.getURI());
	      CloseableHttpResponse response = httpclient.execute(httppost);
	      try {
	        HttpEntity entity = response.getEntity();
	        String res = EntityUtils.toString(entity, "UTF-8");
	        res = URLDecoder.decode(res, "UTF-8");
	        if (StringUtils.isBlank(res)) {
	          logger.info("null response");

	          response.close();
	          try
	          {
	            httpclient.close();
	          } catch (IOException e) {
	            e.printStackTrace();
	          }
	          return null;
	        }
	        logger.info("res:{}", res);
	        String str1 = res;

	        response.close();
	        try
	        {
	          httpclient.close();
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	        return str1;
	      } finally {
	        response.close();
	      }
	    } catch (ClientProtocolException e) {
	      e.printStackTrace();
	    } catch (UnsupportedEncodingException e1) {
	      e1.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    finally {
	      try {
	        httpclient.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    return null;
	  }

}
