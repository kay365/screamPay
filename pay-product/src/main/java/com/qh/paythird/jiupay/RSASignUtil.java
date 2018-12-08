package com.qh.paythird.jiupay;


import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.Map.Entry;

public class RSASignUtil {
    private String service = null;
    private String certFilePath = null;
    private String password = null;
    private String hexCert = null;
    private String rootCertPath = null;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RSASignUtil.class);

    public static void func3() throws Exception {
    	RSASignUtil util = new RSASignUtil("D:\\passkey\\jiupay\\800001407980001.p12", "fUpyfv");
        util.setRootCertPath("D:\\passkey\\jiupay\\rootca.cer");
        String indata = "charset=00&merchantId=800010000020003&orderId=12233&oriOrderId=89876122&requestId=1495866812457&requestTime=20170527143332&service=rpmRefundQuery&signType=RSA256&version=1.0";
        log.info(util.sign(indata, "UTF-8"));
        log.info(util.hexCert);
        String oriData = "charset=00&merchantId=800010000020003&orderId=12233&oriOrderId=89876122&requestId=1495866812457&requestTime=20170527143332&service=rpmRefundQuery&signType=RSA256&version=1.0";
        String serverSign = util.sign(indata, "GB18030");
        String serverCert = util.hexCert;
        log.info("oriData is:" + oriData);
        boolean verifyResult = util.verify(oriData, serverSign, serverCert, "UTF-8");
        log.info("verifyResult is1:" + verifyResult);
    }
    public static void main (String[] arg) throws Exception {
        func3();
    }

    public RSASignUtil(String certFilePath, String password) {
        this.certFilePath = certFilePath;
        this.password = password;
    }

    public RSASignUtil(String rootCertPath) {
        this.rootCertPath = rootCertPath;
    }

    public RSASignUtil() {
    }

    public void setRootCertPath(String rootCertPath) {
        this.rootCertPath = rootCertPath;
    }

    public String sign(String indata, String encoding) throws Exception {
        String serverSign = null;
        if(StringUtils.isBlank(encoding)) {
            encoding = "GBK";
        }
        log.info("[{}]",indata);
//        Map signMap = coverString2Map(indata);
//        RpmVerifyService rvs = new RpmVerifyService();


        try {
            //rvs.execute(signMap, String.valueOf(signMap.get("service")));

            CAP12CertTool singData = new CAP12CertTool(this.certFilePath, this.password);
            X509Certificate cert = singData.getCert();
            byte[] si = singData.getSignData(indata.getBytes(encoding));
            byte[] cr = cert.getEncoded();
            this.hexCert = HexStringByte.byteToHex(cr);
            serverSign = HexStringByte.byteToHex(si);
        } catch (CertificateEncodingException var8) {
            log.error("Certificate encoding exception!",var8);
        } catch (FileNotFoundException var9) {
            log.error("File not found exception",var9);
        } catch (SecurityException var10) {
            log.error("Security exception", var10);
//        } catch (MrpException mrp) {
//            Map<Integer, List<String>> messageMap = mrp.getMessageMap();
//            if (messageMap.containsKey(MrpException.MRPCODE)) {
//                List<String> messageList = messageMap.get(MrpException.MRPCODE);
//                StringBuilder messageBuilder = new StringBuilder();
//                for (int i = 0; i < messageList.size();i++) {
//                    messageBuilder.append(messageList.get(i)).append("=?&");
//                }
//                String res = messageBuilder.substring(0, messageBuilder.length() - 1).toString();
//                log.error("Non empty fields are not filled:[{}]",res);
//            }
//            if (messageMap.containsKey(MrpException.MPPCODE)) {
//                List<String> messageList = messageMap.get(MrpException.MPPCODE);
//                StringBuilder messageBuilder = new StringBuilder();
//                for (int i = 0; i < messageList.size();i++) {
//                    messageBuilder.append(messageList.get(i)).append("&");
//                }
//                String res = messageBuilder.substring(0, messageBuilder.length() - 1).toString();
//                log.error("Multi pass parameter:[{}]",res);
//            }
//        } catch (ServiceNotSpecifiedException snse) {
//            log.error("Service is null");
        }

        return serverSign;
    }

    public String getCertInfo() {
        return this.hexCert;
    }

    public boolean verify(String oridata, String signData, String svrCert, String encoding) throws Exception {
        boolean res = false;
        if(StringUtils.isBlank(encoding)) {
            encoding = "GBK";
        }

        try {
            byte[] e = HexStringByte.hexToByte(signData.getBytes());
            byte[] inDataBytes = oridata.getBytes(encoding);
            byte[] signaturepem = checkPEM(e);
            if(signaturepem != null) {
                e = Base64.decode(signaturepem);
            }

            X509Certificate cert = this.getCertFromHexString(svrCert);
            if(cert != null) {
                PublicKey pubKey = cert.getPublicKey();
                Signature signet = Signature.getInstance("SHA256WITHRSA");
                signet.initVerify(pubKey);
                signet.update(inDataBytes);
                res = signet.verify(e);
            }
            if (!res) {
                log.warn("verify signature failed!");
                return false;
            }
            //使用根证书，验证证书是否有效
            X509Certificate root = getCertFromPath(rootCertPath);
            res = verifyCert(cert, root);

            return res;

        } catch (InvalidKeyException var12) {
            log.error("Invalid key exception",var12);
        } catch (NoSuchAlgorithmException var13) {
            log.error("No such algorithm exception", var13);
        } catch (SignatureException var14) {
            log.error("Signature exception", var14);
        } catch (SecurityException var15) {
            log.error("Security exception", var15);
        }

        return res;
    }
    public boolean verifyCert(X509Certificate userCert, X509Certificate rootCert) throws SecurityException {
        boolean res = false;

        try {
            PublicKey e = rootCert.getPublicKey();
            userCert.checkValidity();
            userCert.verify(e);
            res = true;
            if (!userCert.getIssuerDN().equals(rootCert.getSubjectDN())) {
                res = false;
            }

            return res;
        } catch (CertificateExpiredException | CertificateNotYetValidException | InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException | SignatureException var7) {
            throw new SecurityException(var7.getMessage());
        } catch (CertificateException var8) {
            throw new SecurityException(var8.getMessage());
        }
    }

    private X509Certificate getCertFromPath(String crt_path) throws Exception {
        X509Certificate cert;
        FileInputStream fis;

        try {
            fis = new FileInputStream(new File(crt_path));
            CertificateFactory e = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate)e.generateCertificate(fis);
            return cert;
        }catch (Exception e) {
            throw new SecurityException(e.getMessage());
        }
    }
    private X509Certificate getCertFromHexString(String hexCert) throws SecurityException {
        ByteArrayInputStream bIn = null;
        X509Certificate certobj = null;

        try {
            byte[] e = HexStringByte.hexToByte(hexCert.getBytes());
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            bIn = new ByteArrayInputStream(e);
            certobj = (X509Certificate)fact.generateCertificate(bIn);
            bIn.close();
            bIn = null;
        } catch (CertificateException var16) {
            log.error("Certificate exception", var16);
        } catch (IOException var17) {
            log.error("IO exception", var17);
        } finally {
            try {
                if(bIn != null) {
                    bIn.close();
                }
            } catch (IOException var15) {
                ;
            }

        }

        return certobj;
    }

    public static byte[] checkPEM(byte[] paramArrayOfByte) {
        String str1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/+= \r\n-";

        for(int localStringBuffer = 0; localStringBuffer < paramArrayOfByte.length; ++localStringBuffer) {
            if(str1.indexOf(paramArrayOfByte[localStringBuffer]) == -1) {
                return null;
            }
        }

        StringBuffer var5 = new StringBuffer(paramArrayOfByte.length);
        String str2 = new String(paramArrayOfByte);

        for(int j = 0; j < str2.length(); ++j) {
            if(str2.charAt(j) != 32 && str2.charAt(j) != 13 && str2.charAt(j) != 10) {
                var5.append(str2.charAt(j));
            }
        }

        return var5.toString().getBytes();
    }

    public String getFormValue(String respMsg, String name) {
        String[] resArr = StringUtils.split(respMsg, "&");
        HashMap<String,String> resMap = new HashMap<>();

        for(int i = 0; i < resArr.length; ++i) {
            String data = resArr[i];
            int index = StringUtils.indexOf(data, '=');
            String nm = StringUtils.substring(data, 0, index);
            String val = StringUtils.substring(data, index + 1);
            resMap.put(nm, val);
        }

        return (String)resMap.get(name) == null?"":(String)resMap.get(name);
    }

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

    @SuppressWarnings("unchecked")
	public static String coverMap2String(Map<String, ?> data) {
        TreeMap<String,Object> tree = new TreeMap<>();
        Iterator<?> it = data.entrySet().iterator();

        while(it.hasNext()) {
            Entry<String,?> sf = (Entry<String,?>)it.next();
            if(!"merchantSign".equals(((String)sf.getKey()).trim()) && !"merchantCert".equals(((String)sf.getKey()).trim()) && 
            		!"serverSign".equals(((String)sf.getKey()).trim()) && !"serverCert".equals(((String)sf.getKey()).trim())) {
                if ("null".equals(sf.getValue()) || sf.getValue() == null) {
                    continue;
                }
                tree.put(sf.getKey(), sf.getValue());
            }
        }


        it = tree.entrySet().iterator();
        StringBuffer sf1 = new StringBuffer();

        while(it.hasNext()) {
            Entry<String,String> en1 = (Entry<String,String>)it.next();
            sf1.append((String)en1.getKey() + "=" + (Object)en1.getValue() + "&");
        }

        return sf1.substring(0, sf1.length() - 1);
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }


}
