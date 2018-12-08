package com.qh.paythird.baiXingDa.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

/**
 * 加密类型
 *
 * @create 2017-07-19 上午11:36
 **/
public class SignUtils {
    static Logger LOG = Logger.getLogger(SignUtils.class);

    /**
     * 加密
     *
     * @param reqStr
     * @param privateKey
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String sign(String reqStr, String privateKey) throws UnsupportedEncodingException {
        LOG.info("加密：" + reqStr);
        String digestStr = DigestUtils.sha1Hex(reqStr.getBytes("UTF-8"));
        String signValue = RSAUtil.encodeByPrivateKey(digestStr, new String(new Base64().decode(privateKey)));
        return signValue;
    }

}
