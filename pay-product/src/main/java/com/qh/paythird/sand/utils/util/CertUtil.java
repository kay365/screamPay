/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : paychannel-cmsb-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-10-12 上午10:57:24
 * $URL$
 * <p>
 * Change Log
 * Author      Change Date    Comments
 * -------------------------------------------------------------
 * pxl         2016-10-12        Initailized
 */
package com.qh.paythird.sand.utils.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : pxl
 * @version 2.0.0
 * @ClassName ：CertiUtil
 * @Date : 2016-10-12 上午10:57:24
 */
public class CertUtil {

    private static final Logger logger = LoggerFactory.getLogger(CertUtil.class);

    private static  Map<String, Object> keys = null;
    /*static {
    	keys = new HashMap<String, Object>();
        String publicKeyPath = SandPayConst.getCert();
        String privateKeyPath = SandPayConst.getPfx();
        String keyPassword = SandPayConst.getPassword();

        logger.info("加载衫德安全证书...");
        // 加载证书
        try {
            CertUtil.init(publicKeyPath, privateKeyPath, keyPassword);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("加载衫德安全证书失败...");
        }
    }*/

   // private static final ConcurrentHashMap<String, Object> keys = new ConcurrentHashMap<String, Object>();

    public static void init(String publicKeyPath, String privateKeyPath, String keyPassword,String merchId) throws Exception {
    	if(keys==null) {
    		keys = new HashMap<String, Object>();
    	}
    	if(!keys.containsKey(SandpayConstants.PARAM_PUBLIC_KEY)){
	    	// 加载私钥
	        initPulbicKey(publicKeyPath);
    	}
    	if(!keys.containsKey(SandpayConstants.PARAM_PRIVATE_KEY+"_"+merchId)){
    		// 加载公钥
    		initPrivateKey(privateKeyPath, keyPassword,merchId);
    	}
    }

    public static PublicKey getPublicKey() {
        return (PublicKey) keys.get(SandpayConstants.PARAM_PUBLIC_KEY);
    }

    public static PrivateKey getPrivateKey(String merchId) {
        return (PrivateKey) keys.get(SandpayConstants.PARAM_PRIVATE_KEY+"_"+merchId);
    }

    private static void initPulbicKey(String publicKeyPath) throws Exception {

        String classpathKey = "classpath:";
        if (publicKeyPath != null) {
        	InputStream inputStream = null;
            try {
                if (publicKeyPath.startsWith(classpathKey)) {
                    inputStream = CertUtil.class.getClassLoader()
                            .getResourceAsStream(
                                    publicKeyPath.substring(classpathKey
                                            .length()));
                } else {
                    inputStream = new FileInputStream(publicKeyPath);
                }
                PublicKey publicKey = CertUtil.getPublicKey(inputStream);
                keys.put(SandpayConstants.PARAM_PUBLIC_KEY, publicKey);
            } catch (Exception e) {
                logger.error("无法加载银行公钥[{}]", new Object[]{publicKeyPath});
                logger.error(e.getMessage(), e);
                throw e;
            }finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    private static void initPrivateKey(String privateKeyPath, String keyPassword,String merchId) throws Exception {

        String classpathKey = "classpath:";

        InputStream inputStream = null;
        try {
            if (privateKeyPath.startsWith(classpathKey)) {
                inputStream = CertUtil.class.getClassLoader()
                        .getResourceAsStream(
                                privateKeyPath.substring(classpathKey
                                        .length()));
            } else {
                inputStream = new FileInputStream(privateKeyPath);
            }
            PrivateKey privateKey = CertUtil.getPrivateKey(inputStream, keyPassword);
            keys.put(SandpayConstants.PARAM_PRIVATE_KEY+"_"+merchId, privateKey);
        } catch (Exception e) {
            logger.error("无法加载本地私银[{}]", new Object[]{privateKeyPath});
            logger.error(e.getMessage(), e);
            throw e;
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    // 加载公钥证书
    public static PublicKey getPublicKey(InputStream inputStream) throws Exception {
        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate oCert = (X509Certificate) cf.generateCertificate(inputStream);
            PublicKey publicKey = oCert.getPublicKey();
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("读取公钥异常");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    // 加载私钥证书

    /**
     * 获取私钥对象
     *
     * @param inputStream  私钥输入流
     * @return 私钥对象
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(InputStream inputStream, String password) throws Exception {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            char[] nPassword = null;
            if ((password == null) || password.trim().equals("")) {
                nPassword = null;
            } else {
                nPassword = password.toCharArray();
            }

            ks.load(inputStream, nPassword);
            Enumeration<String> enumas = ks.aliases();
            String keyAlias = null;
            if (enumas.hasMoreElements()) {
                keyAlias = (String) enumas.nextElement();
            }

            PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias, nPassword);
            return privateKey;
        } catch (FileNotFoundException e) {
            throw new Exception("私钥路径文件不存在");
        } catch (IOException e) {
            throw new Exception(e);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("生成私钥对象异常");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

}
