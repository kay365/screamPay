package com.qh.pay.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @ClassName RequestUtils
 * @Description 请求类
 * @Date 2017年10月31日 下午2:48:35
 * @version 1.0.0
 */
public class RequestUtils {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RequestUtils.class);

	public static final String defaultEncodeType = "UTF-8";

	/**
	 * 
	 * @Description 获取xml中的参数
	 * @param xml
	 * @return
	 * @throws DocumentException
	 */
	public static TreeMap<String, String> Dom2Map(String xml) throws DocumentException {
		Document doc = DocumentHelper.parseText(xml);
		TreeMap<String, String> map = new TreeMap<String, String>();
		if (doc == null)
			return map;
		Element root = doc.getRootElement();
		for (Iterator<?> iterator = root.elementIterator(); iterator.hasNext();) {
			Element e = (Element) iterator.next();
			map.put(e.getName(), e.getText());
		}
		return map;
	}

	/**
	 * 
	 * @Description 获取接口回调参数
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static TreeMap<String, String> getRequestParam(HttpServletRequest request)
			throws UnsupportedEncodingException {
		return getRequestParam(request, defaultEncodeType);
	}

	/**
	 * @Description 获取接口回调参数
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static TreeMap<String, String> getRequestParam(HttpServletRequest request, String serverEncodeType)
			throws UnsupportedEncodingException {
		TreeMap<String, String> params = new TreeMap<String, String>();
		request.setCharacterEncoding(serverEncodeType);
		java.util.Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			if (ParamUtil.isNotEmpty(name)) {
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
				}
				params.put(name.trim(), valueStr == null ? "" : valueStr.trim());
			}
		}
		return params;
	}

	/**
	 * 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
	 */
	public static String getParamSrc(SortedMap<String, String> paramsMap) {
		StringBuffer paramstr = new StringBuffer();
		for (String pkey : paramsMap.keySet()) {
			String pvalue = paramsMap.get(pkey);
			if (ParamUtil.isNotEmpty(pvalue) && ParamUtil.isNotEmpty(pkey)) {// 空值不传递，不签名
				paramstr.append(pkey + "=" + pvalue + "&"); // 签名原串，不url编码
			}
		}
		// 去掉最后一个&
		logger.info("参数组装结果：" + paramstr.toString());
		if (ParamUtil.isNotEmpty(paramstr.toString())) {
			return paramstr.substring(0, paramstr.length() - 1);
		} else {
			return paramstr.toString();
		}
	}

	/***
	 * 
	 * @Description 参数组装结果
	 * @param paramsMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getEncodeParamSrc(SortedMap<String, String> paramsMap) throws UnsupportedEncodingException {
		return getEncodeParamSrc(paramsMap, defaultEncodeType);
	}

	/***
	 * 
	 * @Description 参数组装结果
	 * @param paramsMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getEncodeParamSrc(SortedMap<String, String> paramsMap, String serverEncodeType)
			throws UnsupportedEncodingException {
		StringBuffer paramstr = new StringBuffer();
		for (String pkey : paramsMap.keySet()) {
			String pvalue = paramsMap.get(pkey);
			if (ParamUtil.isNotEmpty(pvalue) && ParamUtil.isNotEmpty(pkey)) {// 空值不传递，不签名
				paramstr.append(pkey + "=" + URLEncoder.encode(pvalue, serverEncodeType) + "&"); // 签名原串，不url编码
			}
		}
		logger.info("参数组装结果：" + paramstr.toString());
		// 去掉最后一个&
		return paramstr.substring(0, paramstr.length() - 1);
	}

	 /**
     * 
     * @Description 通过流获取请求数据
     * @param request
     * @return
     */
    public static String parseRequst(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        try {
            ServletInputStream inputStream = request.getInputStream(); 
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String info = null;
            while((info = br.readLine()) != null){
                sb.append(info);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }            
        return sb.toString();
    }
    /**
     * 
     * @Description 获取请求中的参数
     * @param request
     * @param encoding
     * @return
     */
    public static Map<String, String> getAllRequestParamStream(final HttpServletRequest request, String encoding) {
        Map<String, String> res = new HashMap<String, String>();
        try {
            String notifyStr = new String(IOUtils.toByteArray(request.getInputStream()), encoding);
            String[] kvs = notifyStr.split("&");
            for (String kv : kvs) {
                String[] tmp = kv.split("=");
                if (tmp.length >= 2) {
                    String key = tmp[0];
                    String value = URLDecoder.decode(tmp[1], encoding);
                    res.put(key, value);
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("getAllRequestParamStream.UnsupportedEncodingException error: " + e.getClass() + ":" + e.getMessage());
        } catch (IOException e) {
            logger.error("getAllRequestParamStream.IOException error: " + e.getClass() + ":" + e.getMessage());
        }
        return res;
    }
    
    /**
     * 
     * @Description 获取请求中的参数
     * @param request
     * @param encoding
     * @return
     */
    public static Map<String, String> getAllRequestParamStream(final HttpServletRequest request) {
       return getAllRequestParamStream(request, defaultEncodeType);
    }
    /**
     * 
     * @Description 获取请求中的参数
     * @param request
     * @param encoding
     * @return
     */
    public static JSONObject getJsonResultStream(final HttpServletRequest request) {
       return getJsonResultStream(request, defaultEncodeType);
    }
    /**
     * 
     * @Description 获取请求中的参数
     * @param request
     * @param encoding
     * @return
     */
    public static JSONObject getJsonResultStream(final HttpServletRequest request, String encoding) {
        try {
            String notifyStr = new String(IOUtils.toByteArray(request.getInputStream()), encoding);
            if(ParamUtil.isNotEmpty(notifyStr)){
            	return JSONObject.parseObject(notifyStr);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("getAllRequestParamStream.UnsupportedEncodingException error: " + e.getClass() + ":" + e.getMessage());
        } catch (IOException e) {
            logger.error("getAllRequestParamStream.IOException error: " + e.getClass() + ":" + e.getMessage());
        }
        return null;
    }
    /**
     * 
     * @Description 获取请求中的数据
     * @param request
     * @param encoding
     * @return
     */
    public static String getStringResultStream(final HttpServletRequest request, String encoding) {
        try {
            return new String(IOUtils.toByteArray(request.getInputStream()), encoding);
        } catch (UnsupportedEncodingException e) {
            logger.error("getAllRequestParamStream.UnsupportedEncodingException error: " + e.getClass() + ":" + e.getMessage());
        } catch (IOException e) {
            logger.error("getAllRequestParamStream.IOException error: " + e.getClass() + ":" + e.getMessage());
        }
        return null;
    }
    /**
     * 
     * @Description 获取请求中的数据
     * @param request
     * @param encoding
     * @return
     */
    public static String getStringResultStream(final HttpServletRequest request) {
        try {
            return new String(IOUtils.toByteArray(request.getInputStream()), defaultEncodeType);
        } catch (UnsupportedEncodingException e) {
            logger.error("getAllRequestParamStream.UnsupportedEncodingException error: " + e.getClass() + ":" + e.getMessage());
        } catch (IOException e) {
            logger.error("getAllRequestParamStream.IOException error: " + e.getClass() + ":" + e.getMessage());
        }
        return null;
    }
    /**
     * 
     * @Description string转map
     * @param respMsg
     * @return
     */
    public static Map<String,String> coverString2Map(String respMsg) {
        String[] resArr = StringUtils.split(respMsg, "&");
        HashMap<String,String> resMap = new HashMap<>();

        for(int i = 0; i < resArr.length; ++i) {
            String data = resArr[i];
            int index = StringUtils.indexOf(data, '=');
            String nm = StringUtils.substring(data, 0, index);
            String val = StringUtils.substring(data, index + 1);
            resMap.put(nm, val);
        }
        return resMap;
    }
	/**
	 * 分解解密后的字符串，保存为map
	 */
	public static Map<String, String> parseString(String responseData) {
		return parseString(responseData, null);
	}

	/**
	 * 分解解密后的字符串，保存为map
	 */
	public static Map<String, String> parseString(String responseData, Map<String, String> map) {
		if (map == null) {
			map = new HashMap<String, String>();
		}
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
	 * 分解解密后的字符串，保存为map
	 */
	public static Map<String, String> parseStringNoSource(String responseData) {
		return parseStringNoSource(responseData, null);
	}

	/**
	 * 分解解密后的字符串，保存为map
	 */
	public static Map<String, String> parseStringNoSource(String responseData, Map<String, String> map) {
		if (map == null) {
			map = new HashMap<String, String>();
		}
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
		logger.info("分解解密之后的字符串：" + source);
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

	/***
	 * 
	 * @Description 发生post请求
	 * @param url
	 * @param param
	 * @return
	 */
	public static String doPost(String url, String param, String serverEncodeType) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), serverEncodeType));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), serverEncodeType));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送 POST 请求出现异常！" + e.getMessage());
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

	/**
	 * 发送Http请求类
	 * 
	 * @requestUrl 请求地址
	 * @requestData 请求参数
	 */
	public static String doPostJson(String requestUrl, String requestData, String serverEncodeType) {
		HttpURLConnection connection = null;
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuffer result = new StringBuffer();
		try {
			URL url = new URL(requestUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(connection.getOutputStream());
			// 发送请求参数
			out.print(requestData);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), serverEncodeType));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (Exception e) {
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
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
		return "Http请求错误";
	}

	
	/**
	 * 发送Http请求类
	 * 
	 * @requestUrl 请求地址
	 * @requestData 请求参数
	 */
	public static String doPostStream(String requestUrl, String requestData) {
		return doPostStream(requestUrl, requestData, defaultEncodeType);
	}
	
	/***
	 * 
	 * @Description 发生post请求
	 * @param url
	 * @param param
	 * @return
	 */
	public static String doPostStream(String url, String param, String serverEncodeType) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), serverEncodeType));
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			return new String(IOUtils.toByteArray(conn.getInputStream()), serverEncodeType);
		} catch (Exception e) {
			logger.error("发送 POST 请求出现异常！" + e.getMessage());
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

	/**
	 * 发送Http请求类
	 * 
	 * @requestUrl 请求地址
	 * @requestData 请求参数
	 */
	public static String doPostJson(String requestUrl, String requestData) {
		return doPostJson(requestUrl, requestData, defaultEncodeType);
	}
	
	/***
	 * 
	 * @Description 发生post请求
	 * @param url
	 * @param param
	 * @return
	 */
	public static String doPost(String url, String param) {
		return doPost(url, param, defaultEncodeType);
	}

	/****
	 * send get请求
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally { // 使用finally块来关闭输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				logger.error(url + "发送失败！" + e2.getMessage());
			}
		}
		return result;
	}

	/****
	 * send get请求
	 */
	public static String sendGet(String url, String param, String charSet) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url + "?" + param;
			// System.out.print(urlNameString+"\n");
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.setRequestProperty("Accept-Charset", charSet);
			// 建立实际的连接
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送get请求失败！" + e.getMessage());
			e.printStackTrace();
		} finally { // 使用finally块来关闭输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				logger.error(url + "发送失败！" + e2.getMessage());
			}
		}
		return result;
	}
	
	 public static String sendPost(String url, String param) {
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
	            conn.setRequestProperty("user-agent",
	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
	            in = new BufferedReader(
	                    new InputStreamReader(conn.getInputStream()));
	            String line;
	            while ((line = in.readLine()) != null) {
	                result += line;
	            }
	        } catch (Exception e) {
	        	logger.error("发送 POST 请求出现异常！"+e);
	            e.printStackTrace();
	        }
	        //使用finally块来关闭输出流、输入流
	        finally{
	            try{
	                if(out!=null){
	                    out.close();
	                }
	                if(in!=null){
	                    in.close();
	                }
	            }
	            catch(IOException ex){
	                ex.printStackTrace();
	            }
	        }
	        return result;
	    }    

}
