package com.qh.paythird.xinfu.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p/>
 * //请求信息类型MIME每种响应类型的输出（普通文本、html 和 XML，json）。允许的响应类型应当匹配资源类中生成的 MIME 类型
 * //资源类生成的 MIME 类型应当匹配一种可接受的 MIME 类型。如果生成的 MIME 类型和可接受的 MIME 类型不 匹配，那么将
 * //生成 com.sun.jersey.api.client.UniformInterfaceException。例如，将可接受的 MIME 类型设置为 text/xml，而将
 * //生成的 MIME 类型设置为 application/xml。将生成 UniformInterfaceException。
 * //代理: new HttpHost("10.0.0.172", 80, "http");
 * <p/>
 */
public class HttpHelper {
    private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);
    private static Map<String, List<Cookie>> cookiesMap = Collections.synchronizedMap(new HashMap<String, List<Cookie>>());
    static private HttpClient httpclient;
    private static Map<String, Map<String, String>> globalParam = new HashMap<String, Map<String, String>>(5);
    private Map<String, String> headers = new HashMap<String, String>();

    static {
        RequestConfig config = RequestConfig.copy(RequestConfig.DEFAULT).setConnectionRequestTimeout(30000).setSocketTimeout(40000).build();
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE)).build();
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(requestWriterFactory, new DefaultHttpResponseParserFactory());
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry, connFactory, new SystemDefaultDnsResolver());
        connManager.setMaxTotal(100);
        connManager.setDefaultMaxPerRoute(100);
        httpclient = HttpClientBuilder.create().setConnectionManager(connManager).setDefaultRequestConfig(config).build();
    }
    /**
     * 通过HTTP协议访问站点，并且将返回的数据转换成json对象
     * 注意
     * 对方返回的数据最外层对象必须是单个对象
     *
     * @param url
     * @param param
     * @param method
     * @return
     * @throws IOException
     * @throws IOException
     */
    public JSONObject getJSONFromHttp(String url, Map<String, Object> param, HttpMethodType method) throws Exception {
        String rs;
        switch (method) {
            case GET: {
                rs = sendGetHttp(url, param);
                break;
            }
            case POST: {
                rs = sendPostHttp(url, param, false);
                break;
            }
            case FILE: {
                rs = sendFileHttp(url, param);
                break;
            }
            default: {
                throw new IllegalArgumentException("HTTP访问方式设置有误");
            }
        }
        logger.debug("return is:" + rs);
        return rs == null || "".equals(rs) ? null : new JSONObject(rs);
    }

    public String sendHttp(String url, Map<String, Object> param, HttpMethodType method) throws RuntimeException {
        switch (method) {
            case GET: {
                return sendGetHttp(url, param);
            }
            case POST: {
                return sendPostHttp(url, param, false);
            }
            case FILE: {
                return sendFileHttp(url, param);
            }
            default:
                throw new IllegalArgumentException("参数中的HTTP访问方式有误，只支持GET、POST、FILE");
        }
    }


    /**
     * 发送一个HTTP协议的GET请求
     *
     * @param url
     * @param param
     * @return
     * @throws IOException
     */
    public String sendGetHttp(String url, Map<String, Object> param) throws RuntimeException {
        StringBuilder parmStr = new StringBuilder();
        if (null != param && !param.isEmpty()) {
            List<NameValuePair> parm = new ArrayList<NameValuePair>(param.size());
            for (Map.Entry<String, Object> paramEntity : param.entrySet()) {
                Object value = paramEntity.getValue();
                if (null != value && !StringUtils.isBlank(value.toString())) {
                    parm.add(new BasicNameValuePair(paramEntity.getKey(), value.toString()));
                }
            }
            parmStr.append(URLEncodedUtils.format(parm, "UTF-8"));
        }
        return sendGetHttp(url, parmStr.toString());
    }

    /**
     * 发送一个HTTP协议的GET请求
     *
     * @param url
     * @param param
     * @return
     * @throws IOException
     */
    public String sendGetHttp(String url, String param) throws RuntimeException {
        HttpContext localContext = new BasicHttpContext();
        setCookie(localContext, url);
        if (!StringUtils.isBlank(param)) url = url + ((url.indexOf("?") > 0) ? "&" + param : "?" + param);
        url = appendGlobalParam(url, param);
        logger.debug("远程URL:{}", url);
        //创建HttpGet对象
        HttpGet request = new HttpGet(url);
        setHeader(request);
        String result;
        try {
            HttpResponse response = httpclient.execute(request, localContext);
            result = responseProc(response);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            request.reset();
        }
        return result;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private String appendGlobalParam(String url, Object param) {
        for (Map.Entry<String, Map<String, String>> stringMapEntry : globalParam.entrySet()) {
            if (url.startsWith(stringMapEntry.getKey())) {
                for (Map.Entry<String, String> paramEntry : stringMapEntry.getValue().entrySet()) {
                    logger.debug("HTTP处理过程发送了参数:" + paramEntry.getKey() + "|" + paramEntry.getValue());
                    if (param instanceof List) ((List) param).add(new BasicNameValuePair(paramEntry.getKey(), paramEntry.getValue()));
                    else url += "&" + paramEntry.getKey() + "=" + paramEntry.getValue();
                }
            }
        }
        return url;
    }

    /**
     * 发送一个HTTP协议的POST请求
     *
     * @param url
     * @param param
     * @return
     * @throws IOException
     */
    public String sendPostHttp(String url, Map<String, Object> param, boolean postTxtBody) throws RuntimeException {
        HttpContext localContext = new BasicHttpContext();
        setCookie(localContext, url);
        logger.debug("远程URL:{}", url);
        HttpPost request = new HttpPost(url);
        List<NameValuePair> parm = new ArrayList<NameValuePair>();
        if (null != param && !param.isEmpty()) for (Map.Entry<String, Object> paramEntity : param.entrySet()) {
            Object value = paramEntity.getValue();
            if (null != value && !StringUtils.isBlank(value.toString())) {
                logger.debug("HTTP处理过程发送了参数:" + paramEntity.getKey() + "|" + value);
                parm.add(new BasicNameValuePair(paramEntity.getKey(), value.toString()));
            }
        }
        appendGlobalParam(url, parm);
        HttpResponse response;
        try {
            request.setEntity(generyEntity(parm, "UTF-8", postTxtBody));
            setHeader(request);
            response = httpclient.execute(request, localContext);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        String result;
        try {
            result = responseProc(response);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            request.reset();
        }
        logger.debug("return is:" + result);
        return result;
    }

    public String sendFileHttp(String url, Map<String, Object> param) throws RuntimeException {
        HttpClientContext localContext = HttpClientContext.create();
        setCookie(localContext, url);
        logger.debug("远程URL:{}", url);
        HttpPost request = new HttpPost(url);
        setHeader(request);
        if (param != null) {
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                if (entry.getValue() instanceof File) {
                    FileBody fileBody = new FileBody((File)entry.getValue(), ContentType.APPLICATION_OCTET_STREAM);
                    builder.addPart(fileBody.getFilename(), fileBody);
                } else {
                    builder.addTextBody(entry.getKey(), String.valueOf(entry.getValue()), ContentType.MULTIPART_FORM_DATA);
                }
            }
            request.setEntity(builder.build());
        }
        String result;
        try {
            HttpResponse response = httpclient.execute(request, localContext);
            result = responseProc(response);
            parseCookie(localContext, url);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        } finally {
            request.reset();
        }
        logger.debug("return is:" + result);
        return result;
    }
    Pattern urlPrePat = Pattern.compile("https?://([^/]*)?/?");

    private String getUrlPerfix(String url) {
        Matcher mat = urlPrePat.matcher(url);
        if (mat.find()) return mat.group(1);
        return "";
    }

    private void setCookie(HttpContext localContext, String url) {
        String urlPrefix = getUrlPerfix(url);
        CookieStore cookieStore = new BasicCookieStore();
        List<Cookie> cookieList = cookiesMap.get(urlPrefix);
        if (cookieList != null && cookieList.size() > 0) {
            for (Cookie cookie : cookiesMap.get(urlPrefix)) {
                cookieStore.addCookie(cookie);
            }
            localContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        }
    }

    private void parseCookie(HttpClientContext context, String url) {
        List<Cookie> cookies = context.getCookieStore().getCookies();
        String urlPrefix = getUrlPerfix(url);
        List<Cookie> oldCookies = cookiesMap.get(urlPrefix);
        if (oldCookies != null) {
            for (Cookie cookie : cookies) {
                for (Cookie oldCookie : oldCookies) {
                    if (cookie.getName().equals(oldCookie.getName())) {
                        oldCookies.remove(oldCookie);
                        oldCookies.add(cookie);
                    }
                }
            }
        } else cookiesMap.put(urlPrefix, cookies);
    }

    private String responseProc(HttpResponse response) throws IOException {
        switch (response.getStatusLine().getStatusCode()) {
            case 200: {
                HttpEntity entity = response.getEntity();
                    return EntityUtils.toString(entity, "UTF-8");
                }
            case 302: {
                return sendGetHttp(response.getFirstHeader("location").getValue(), "");
            }
            case 303:
            case 304: {
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    logger.debug(header.getName() + " : " + header.getValue());
                }
            }
            default:
                throw new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
        }
    }

    public void setHeader(HttpRequestBase request) {
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            request.setHeader(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    public HttpEntity generyEntity(List<NameValuePair> parm, String encode, boolean postTxtBody) throws Exception {
        if (postTxtBody && parm.size() > 0) {
            JSONObject paramJson = new JSONObject();
            for (NameValuePair nameValuePair : parm) {
                paramJson.put(nameValuePair.getName(), nameValuePair.getValue());
            }
            return (new StringEntity(paramJson.toString(), encode));
        } else
            return (new UrlEncodedFormEntity(parm, encode));
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[1];
        if (bytes instanceof byte[]) {
            System.out.println(1);
        }
    }
}
