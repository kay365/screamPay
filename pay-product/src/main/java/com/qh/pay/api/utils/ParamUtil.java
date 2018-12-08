package com.qh.pay.api.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/***
 * 
 * @ClassName ParamUtil
 * @Date 2017年5月30日 下午3:52:07
 * @version 1.0.0
 */
public class ParamUtil {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ParamUtil.class);

	private static AtomicLong acacAtomicLong = new AtomicLong();
	private static AtomicLong acacAtomicLongYY = new AtomicLong();
	private static ThreadLocalRandom random = ThreadLocalRandom.current();

	/***
	 * 
	 * @Description 判断内容是否为空
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if (obj == null) {
			return true;
		} else if (obj instanceof String) {
			return StringUtils.isEmpty((String) obj);
		}
		return false;
	}

	/***
	 * 
	 * @Description 判断内容是否不为空
	 * @return
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}

	/**
	 * @Description 生成4位随机验证码
	 * @return
	 */
	public static int generateCode() {
		return 1000 + random.nextInt(9000);
	}

	/**
	 * @Description 生成2位随机验证码
	 * @return
	 */
	public static int generateCode2() {
		return 10 + random.nextInt(90);
	}

	/**
	 * @Description 生成6位随机验证码
	 * @return
	 */
	public static int generateCode6() {
		return 100000 + random.nextInt(900000);
	}

	/**
	 * @Description 生成8位随机验证码
	 * @return
	 */
	public static int generateCode8() {
		return 10000000 + random.nextInt(90000000);
	}

	/**
	 * 
	 * @Description uuid生成算法
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	/***
	 * 
	 * @Description 订单编号生成算法
	 * @return
	 */
	public static String getOrderId() {
		return 4 + (System.currentTimeMillis() + "" + acacAtomicLong.addAndGet(1)).substring(2);
	}

	/**
	 * 
	 * @Description 判断是否为图片结尾的url
	 * @param url
	 */
	public static boolean ifImg(String url) {
		return isNotEmpty(url) && url.contains(".")
				&& ".png.bmp.jpeg.jpg".contains(url.substring(url.lastIndexOf(".")));
	}

	/***
	 * 
	 * @Description
	 * @param args
	 */
	public static boolean biggerThanZero(BigDecimal big) {
		return big != null && big.doubleValue() > 0;
	}

	/***
	 * 
	 * @Description
	 * @param args
	 */
	public static boolean biggerThanOther(BigDecimal big, BigDecimal other) {
		return big != null && other != null && big.doubleValue() > 0 && big.doubleValue() >= other.doubleValue();
	}

	/***
	 * 
	 * @Description
	 * @param args
	 */
	public static boolean biggerThanOther(BigDecimal big, Double other) {
		return big != null && other != null && big.doubleValue() >= other.doubleValue();
	}

	/**
	 * 
	 * @Description 元变为分
	 * @param yuan
	 * @return
	 */
	public static String yuanToFen(BigDecimal yuan) {
		return String.valueOf(yuan.multiply(BigDecimal.valueOf(100)).longValue());
	}

	/**
	 * @Description 分变为元
	 * @param yuan
	 * @return
	 */
	public static BigDecimal fenToYuan(String fen) {
		if (isNotEmpty(fen)) {
			return new BigDecimal(fen).divide(BigDecimal.valueOf(100));
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 
	 * @Description 过滤请求参数
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> trimValue(HttpServletRequest request) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, ?> properties = request.getParameterMap();
		Iterator<?> entries = properties.entrySet().iterator();
		String name = "";
		String value = "";
		Entry<String, ?> entry;
		while (entries.hasNext()) {
			entry = (Entry<String, ?>) entries.next();
			name = (String) entry.getKey();
			Object valueObj = entry.getValue();
			if (ParamUtil.isNotEmpty(valueObj)) {
				if (valueObj instanceof String[]) {
					String[] values = (String[]) valueObj;
					for (int i = 0; i < values.length; i++) {
						if (ParamUtil.isNotEmpty(values[i])) {
							value = values[i] + ",";
						}
					}
					if (ParamUtil.isNotEmpty(value)) {
						params.put(name, value.substring(0, value.length() - 1));
					}
				} else if (ParamUtil.isNotEmpty(valueObj)) {
					params.put(name, valueObj);
				}
			}
			value = "";
		}
		return params;
	}

	/**
	 * @Description 过滤空格
	 * @param resultMap
	 */
	@SuppressWarnings("unchecked")
	public static void trimValue(Map<String, ?> resultMap) {
		Iterator<?> entries = resultMap.entrySet().iterator();
		Entry<String, ?> entry;
		while (entries.hasNext()) {
			entry = (Entry<String, ?>) entries.next();
			Object valueObj = entry.getValue();
			if (isEmpty(valueObj)) {
				entries.remove();
			}
		}
	}

	/**
	 * 大陆号码或香港号码均可
	 */
	public static boolean isPhoneLegal(String str) throws PatternSyntaxException {
		return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
	}

	/**
	 * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数 此方法中前三位格式有： 13+任意数 15+除4的任意数 18+除1和4的任意数
	 * 17+除9的任意数 147
	 */
	public static boolean isChinaPhoneLegal(String str) throws PatternSyntaxException {
		String regExp = "^[0-9]{11}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 香港手机号码8位数，5|6|8|9开头+7位任意数
	 */
	public static boolean isHKPhoneLegal(String str) throws PatternSyntaxException {
		String regExp = "^(5|6|8|9)\\d{7}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 获取当前网络ip
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		// String ipAddress = request.getHeader("X-Forwarded-For");
		String ipAddress = (String) request.getHeader("X-real-ip");
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
			ipAddress = request.getRemoteAddr();
			if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
				// 根据网卡取本机配置的IP
				InetAddress inet = null;
				try {
					inet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				ipAddress = inet.getHostAddress();
			}
		}
		// 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
		if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
															// = 15
			if (ipAddress.indexOf(",") > 0) {
				ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
			}
		}
		return ipAddress;
	}

	/**
	 * 获取当前网络ip
	 * 
	 * @param request
	 * @return
	 */
	public static String getDomain(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		if (ParamUtil.isEmpty(contextPath)) {
			return "http://" + request.getServerName() + ":" + request.getServerPort() + "/";
		} else if (contextPath.endsWith("/") && contextPath.startsWith("/")) {
			return "http://" + request.getServerName() + ":" + request.getServerPort() + contextPath;
		} else if (contextPath.startsWith("/")) {
			return "http://" + request.getServerName() + ":" + request.getServerPort() + contextPath + "/";
		} else if (contextPath.endsWith("/")) {
			return "http://" + request.getServerName() + ":" + request.getServerPort() + "/" + contextPath;
		} else {
			return "http://" + request.getServerName() + ":" + request.getServerPort() + "/" + contextPath + "/";
		}
	}

	/**
	 * 获取request中的域名
	 * 
	 * @param request
	 * @return
	 */
	public static String getDNS(HttpServletRequest request) {
		return "http://" + request.getServerName() + "/";
	}

	public static void buildPayParams(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
		List<String> keys = new ArrayList<String>(payParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			sb.append(key).append("=");
			if (encoding) {
				sb.append(urlEncode(payParams.get(key)));
			} else {
				sb.append(payParams.get(key));
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
	}

	/**
	 * 
	 * @Description 连接所有的参数
	 * @param sb
	 * @param payParams
	 * @param encoding
	 */
	public static String buildAllParams(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		List<String> keys = new ArrayList<String>(payParams.keySet());
		for (String key : keys) {
			sb.append(key).append("=");
			if (encoding) {
				sb.append(urlEncode(payParams.get(key)));
			} else {
				sb.append(payParams.get(key));
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 
	 * @Description 连接所有的参数
	 * @param sb
	 * @param payParams
	 * @param encoding
	 */
	public static String buildAllParams(Map<String, String> payParams, boolean encoding) {
		return buildAllParams(null, payParams, encoding);
	}

	/**
	 * 去除空格 在进行签名
	 * 
	 * @author
	 * @param payParams
	 * @return
	 */
	public static void buildPayParams_space(StringBuilder sb, Map<String, String> payParams, boolean encoding) {
		List<String> keys = new ArrayList<String>(payParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			String value = payParams.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
				continue;
			}
			sb.append(key).append("=");
			if (encoding) {
				sb.append(urlEncode(payParams.get(key)));
			} else {
				sb.append(payParams.get(key));
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Throwable e) {
			return str;
		}
	}

	public static byte[] getBytes(String content, String charset) {
		if (content == null) {
			content = "";
		}
		if (StringUtils.isBlank(charset))
			throw new IllegalArgumentException("charset can not null");
		try {
			return content.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
		}
		throw new RuntimeException("charset is not valid,charset is:" + charset);
	}

	/**
	 * 过滤参数
	 * 
	 * @author
	 * @param sArray
	 * @return
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {
		Map<String, String> result = new HashMap<String, String>(sArray.size());
		if (sArray == null || sArray.size() <= 0) {
			return result;
		}
		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}

	/**
	 * 
	 * @Description 读取文件返回字符串
	 * @param FileName
	 * @return
	 * @throws Exception
	 */
	public static String readTxtFile(String FileName) throws Exception {
		if (ParamUtil.isNotEmpty(FileName) && (FileName.endsWith(".xls") || FileName.endsWith(".xlsx"))) {
			return null;
		}
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(FileName));
		ByteArrayOutputStream memStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = bufferedInputStream.read(buffer)) != -1) {
			memStream.write(buffer, 0, len);
		}
		byte[] data = memStream.toByteArray();
		bufferedInputStream.close();
		memStream.close();
		bufferedInputStream.close();
		return new String(data);
	}

	/**
	 * 
	 * @Description 读取文件返回字符串
	 * @param FileName
	 * @return
	 * @throws Exception
	 */
	public static String readTxtFileFilter(String FileName) throws Exception {
		File file = new File(FileName);
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(file);
			br = new BufferedReader(new InputStreamReader(in));
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
				}
			}
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					logger.error("加载" + FileName + "失败！");
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 
	 * @Description
	 * @return
	 */
	public static Long getNumForYY() {
		return acacAtomicLongYY.addAndGet(1);
	}

	/**
	 * 根据正则表达式 在字符串 str中查找 符合的字符串
	 * 
	 * @param str
	 * @param regex
	 * @return
	 */
	public static String patternQuery(String str, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		String respCode = "";
		while (matcher.find()) {
			respCode = matcher.group(0);
		}
		respCode = respCode.substring(respCode.indexOf("=") + 1);
		return respCode;
	}

	/**
	 * 
	 * @return
	 */
	public static int random(int range) {
		return random.nextInt(0, range);
	}

	/**
	 * 
	 * @return
	 */
	public static double random2Point(int range) {
		return random.nextInt(0, range * 100) / 100.0;
	}

	/**
	 * 
	 * @return
	 */
	public static int random(int from, int to) {
		return random.nextInt(from, to);
	}

	/**
	 * 
	 * @return
	 */
	public static double random2Point(int from, int to) {
		return random.nextInt(from * 100, to * 100) / 100.0;
	}

	/**
	 * 
	 * @Description
	 * @param priceRange
	 * @return
	 */
	public static boolean isRangePattern(String priceRange) {
		if (isNotEmpty(priceRange) && priceRange.matches("\\d+-\\d+")) {
			String[] datas = priceRange.split("-");
			if (Integer.parseInt(datas[1]) > Integer.parseInt(datas[0])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @Description 金额相乘,保留两位小数 四舍五入
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal mult(BigDecimal amount, BigDecimal costRate) {
		return amount.multiply(costRate).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * @Description 金额相乘,保留两位小数 往大的靠
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal multBig(BigDecimal amount, BigDecimal costRate) {
		return amount.multiply(costRate).setScale(2, RoundingMode.CEILING);
	}

	/**
	 * @Description 金额相乘,保留两位小数,往小的靠
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal multSmall(BigDecimal amount, BigDecimal costRate) {
		return amount.multiply(costRate).setScale(2, RoundingMode.FLOOR);
	}

	
	/**
	 * @Description 金额相减,保留两位小数 四舍五入
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal sub(BigDecimal amount, BigDecimal costRate) {
		return amount.subtract(costRate).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * @Description 金额相减,保留两位小数 往大的靠
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal subBig(BigDecimal amount, BigDecimal costRate) {
		return amount.subtract(costRate).setScale(2, RoundingMode.CEILING);
	}

	/**
	 * @Description 金额相减,保留两位小数,往小的靠
	 * @param amount
	 * @param costRate
	 */
	public static BigDecimal subSmall(BigDecimal amount, BigDecimal costRate) {
		return amount.subtract(costRate).setScale(2, RoundingMode.FLOOR);
	}

	/**
	 * 
	 * @Description 是否金钱元单位
	 * @param amount
	 * @return
	 */
	public static boolean ifMoney(String amount){
		return amount.matches("\\d+(\\.\\d{1,2})?");
	}
	
	 /**  
     * 使用java正则表达式去掉多余的.与0  
     * @param s  
     * @return   
     */    
    public static String subZeroAndDot(String s){    
        if(isNotEmpty(s) && s.indexOf(".") > 0){    
            s = s.replaceAll("0+?$", "");//去掉多余的0    
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉    
        }    
        return s;    
    }

	/**
	 * @Description 增加最小金额 0.01元
	 * @param monAmount
	 * @return
	 */
	public static String addMinMonAmount(String monAmount) {
		return new BigDecimal(monAmount).add(new BigDecimal("0.01")).toPlainString();
	}    
}
