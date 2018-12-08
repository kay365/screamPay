package com.qh.sms.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.util.Map;

public class HttpClientUtils {

    public static final String CHARSET_DEFAULT = "utf-8";

    private static PoolingHttpClientConnectionManager cm;

    private static void init() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(50);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(5);// 每路由最大连接数，默认值是2
        }
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    public static CloseableHttpClient getHttpClient() {
        init();
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    /**
     * @Description 获取GET请求结果
     * @Author chensi
     * @Time 2017/12/19 20:33
     */
    public static HttpEntity getResult(String url) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        return entity;
    }

    /**
     * @Description 获取POST请求结果
     * @Author chensi
     * @Time 2017/12/19 20:33
     */
    public static HttpEntity postResult(String url, Map<String, Object> params) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        HttpPost httpPost = new HttpPost(url);
        StringEntity se = new StringEntity(JSON.toJSONString(params), CHARSET_DEFAULT);
        httpPost.setEntity(se);
        CloseableHttpResponse response = httpClient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();
        return resEntity;
    }
}
