package com.qh.paythird.tx.utils;


import com.qh.common.config.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @// TODO: 2018/4/10
 */
public class TXAgencyRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(TXAgencyRequestUtil.class);

    /**
     * @param paramMap
     * @return
     * @// TODO: 2018/4/10 将参数转换成字符串
     */
    private static String getParamsStr(TreeMap<String, String> paramMap) {
        StringBuffer paramBuffer = new StringBuffer();
        Set<String> keySet = paramMap.keySet();
        if (keySet != null) {
            String[] keys = keySet.toArray(new String[0]);
            for (int keyIndex = 0; keyIndex < keys.length; keyIndex++) {
                String paramValue = paramMap.get(keys[keyIndex]);
                if (paramValue != null && !"".equals(paramValue)) {
                    paramBuffer.append(keys[keyIndex] + "=" + paramValue);
                    if (keyIndex != keys.length - 1) {
                        paramBuffer.append("&");
                    }
                }
            }
        }
        return paramBuffer.toString();
    }


    /**
     * @return
     * @// TODO: 2018/4/10 转换时间格式 2018-04-04 12:12:12 >> 20180404121212
     */
    public static String getNumberDate() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }


    /**
     * @param url
     * @param paramMap
     * @return
     * @// TODO: 2018/4/10 发送请求
     */
    public static Map<String, String> sendRequest(final String url, TreeMap<String, String> paramMap) {
        Map<String, String> requestResult = new HashMap<>();
        // 签名原字符串
        StringBuffer paramStr = new StringBuffer(getParamsStr(paramMap));
        // 签名字符串
        String signStr = sign(paramStr.toString());
        // 原字符串
        StringBuffer signParamStr = new StringBuffer(paramStr).append("&sign=" + signStr);
        // 加密结果
        String cipherData = encrypt(signParamStr.toString());
        // 发送POST请求
        String responseData = doPost(url, "cipher_data=" + URLEncoder.encode(cipherData));
        Map<String, String> xmlMap = MapUtils.parseXmlToMap(responseData);
        String rsaCipherData = xmlMap.get("cipher_data");
        if (null == rsaCipherData) {//    请求异常
            String errorCode = xmlMap.get("retcode");
            requestResult.put(Constant.result_code, String.valueOf(Constant.result_code_error));
            requestResult.put(Constant.result_msg, "错误代码：" + errorCode);
            logger.error("天下支付 请求参数异常>>：{retcode : " + errorCode + "}");
        } else {
            String rsaResponseData = decryptResponseData(rsaCipherData);
            String sign = rsaResponseData.substring(rsaResponseData.indexOf("sign=") + 5, rsaResponseData.length());
            String source = rsaResponseData.substring(0, rsaResponseData.lastIndexOf("&sign"));
            if (verify(source, sign)) {//   验证成功
                requestResult.put(Constant.result_code, String.valueOf(Constant.result_code_succ));
                String[] params = rsaResponseData.split("&");
                //  将响应的参数返回
                for (int paramIndex = 0; paramIndex < params.length; paramIndex++) {
                    String[] entry = params[paramIndex].split("=");
                    requestResult.put(entry[0].trim(), entry[1].trim());
                }
            } else {//    验证失败
                requestResult.put(Constant.result_code, String.valueOf(Constant.result_code_error));
                requestResult.put(Constant.result_msg, "验证签名失败。");
            }
        }
        return requestResult;
    }

    /**
     * @param source
     * @param sign
     * @return
     * @// TODO: 2018/4/10 验证签名
     */
    private static boolean verify(String source, String sign) {
        //MD5验签，把返回的报文串（去掉sign和空串），作MD5加签，然后跟sign 比对
        return (sign(source).equals(sign)) ? true : false;
    }


    /**
     * @param paramsStr
     * @return
     * @// TODO: 2018/4/10 获取签名
     */
    private static String sign(String paramsStr) {
        StringBuffer signBuffer = new StringBuffer();
        signBuffer.append(paramsStr + "&key=" + TXAgencyRSAUtils.MD5_KEY);
        String sign = null;
        try {
            sign = TXAgencyRSAUtils.getMD5Str(signBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    /**
     * @param paramsStr
     * @return
     * @// TODO: 2018/4/10 加密得到cipherData
     */
    public static String encrypt(String paramsStr) {
        String publicKey = TXAgencyRSAUtils.loadPublicKey(TXAgencyRSAUtils.GC_PUBLIC_KEY_PATH);
        String cipherData = null;
        try {
            cipherData = TXAgencyRSAUtils.encryptByPublicKey(paramsStr.getBytes("UTF-8"), publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherData;
    }

    /**
     * @param cipherData
     * @return
     * @// TODO: 2018/4/10 RSA解密
     */
    private static String decryptResponseData(String cipherData) {
        String privateKey = TXAgencyRSAUtils.loadPrivateKey(TXAgencyRSAUtils.PRIVATE_KEY_PATH);
        String result;
        try {
            result = TXAgencyRSAUtils.decryptByPrivateKey(TXAgencyBase64.decode(cipherData), privateKey);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param url
     * @param param
     * @return
     * @// TODO: 2018/4/10 发送Post请求
     */
    public static String doPost(String url, String param) {
        PrintWriter printWriter = null;
        BufferedReader reader = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // post请求必要设置
            connection.setDoOutput(true);
            connection.setDoInput(true);
            printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(param);
            printWriter.flush();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeUrlStream(printWriter, reader);
        }
        return result;
    }

    /**
     * @param printWriter
     * @param reader
     * @// TODO: 2018/4/10 关闭URL读写流
     */
    private static void closeUrlStream(PrintWriter printWriter, BufferedReader reader) {
        try {
            if (printWriter != null) {
                printWriter.close();
            }
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            logger.error("天下支付 发送 POST 请求出现异常！" + e.getMessage());
        }
    }

    

}
