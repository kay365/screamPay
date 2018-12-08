/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-webgateway
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午2:24:55
 * $URL$
 * <p>
 * Change Log
 * Author      Change Date    Comments
 * -------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.utils;

import com.alibaba.fastjson.JSON;
import com.qh.paythird.sand.utils.util.*;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pan.xl
 *
 */
public class SandpayClient {

    private static Logger logger = LoggerFactory.getLogger(SandpayClient.class);

    private static int connectTimeout = 3000;
    private static int readTimeout = 15000;

    public static <T extends SandPayResponse> T execute(SandPayRequest<T> req, String serverUrl,String merchId) throws Exception {
        return execute(req, serverUrl, connectTimeout, readTimeout,merchId);
    }

    public static <T extends SandPayResponse> T execute(SandPayRequest<T> request, String serverUrl, int connectTimeout, int readTimeout,String merchId) throws Exception {

        Map<String, String> reqMap = new HashMap<String, String>();

        // 将请求转换成字符串
        String reqData = JSON.toJSONString(request);
        PrivateKey pk = CertUtil.getPrivateKey(merchId);
        logger.info("杉德私钥"+pk);
        // 签名
        String reqSign = new String(Base64.encodeBase64(CryptoUtil.digitalSign(reqData.getBytes(SandpayConstants.UTF8_CHARSET), pk, SandpayConstants.SIGN_ALGORITHM)));

        reqMap.put(SandpayConstants.param_charset, SandpayConstants.UTF8_CHARSET);
        reqMap.put(SandpayConstants.param_data, reqData);
        reqMap.put(SandpayConstants.param_signType, "01");
        //reqMap.put(SandPayConstants.param_sign, URLEncoder.encode(reqSign, SandPayConstants.UTF8_CHARSET));
        reqMap.put(SandpayConstants.param_sign, reqSign);
        reqMap.put(SandpayConstants.param_extend, "");
        System.out.println("杉德请求参数"+reqMap);
        logger.info("杉德请求参数"+request.getTxnDesc()+reqMap);
        String result = HttpClient.doPost(serverUrl, reqMap, connectTimeout, readTimeout);
        logger.debug("杉德返回参数"+request.getTxnDesc()+result);
        result = URLDecoder.decode(result, SandpayConstants.UTF8_CHARSET);
        logger.info("杉德返回结果"+request.getTxnDesc()+result);
        System.out.println("杉德请求参数"+result);

        Map<String, String> respMap = SDKUtil.convertResultStringToMap(result);

        String respData = respMap.get(SandpayConstants.param_data);
        String respSign = respMap.get(SandpayConstants.param_sign);

        // 验证签名
        boolean valid = CryptoUtil.verifyDigitalSign(respData.getBytes(SandpayConstants.UTF8_CHARSET), Base64.decodeBase64(respSign), CertUtil.getPublicKey(), SandpayConstants.SIGN_ALGORITHM);
        if (!valid) {
            logger.error("verify sign fail.");
            throw new RuntimeException("verify sign fail.");
        }

        logger.info("verify sign success");

        return JSON.parseObject(respData, request.getResponseClass());

    }

}
