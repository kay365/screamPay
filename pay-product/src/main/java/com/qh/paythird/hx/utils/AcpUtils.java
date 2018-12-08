package com.qh.paythird.hx.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

public class AcpUtils {

	public static String encrypt3DES(String encryptString, String encryptKey, String iv) throws Exception {
        byte encryptedData[];
        IvParameterSpec zeroIv = new IvParameterSpec(iv.getBytes("UTF-8"));
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes("UTF-8"), "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        cipher.init(1, key, zeroIv);
        encryptedData = cipher.doFinal(encryptString.getBytes("UTF-8"));
        String  is3DesString= new String(org.apache.commons.codec.binary.Base64.encodeBase64(encryptedData));
        return is3DesString;
    }
	
	/**
	 * 验签
	 * @param xml
	 * @return
	 */
	public static boolean checkSign(String xml, String directStr) {

		if (xml.trim() == null){
			return false;
		}
		String OldSign = getSign(xml); // 返回签名
		String text = getBodyXml(xml); // body
		System.out.println("MD5验签，验签文：" + text + "\n待比较签名值:" + OldSign);

		String result = DigestUtils.md5Hex(Verify.getBytes(text  + directStr,"UTF-8"));
		System.out.println("签名值:"+result);
		if (OldSign == null || result == null || !OldSign.equals(result)) {
			return false;
		}
		return true;
	}
	
	/**
	 * 验签
	 * @param xml
	 * @return
	 */
	public static boolean checkSign(String xml, String merCode, String directStr) {

		if (xml.trim() == null){
			return false;
		}
		String OldSign = getSign(xml); // 返回签名
		String text = getBodyXmlL(xml); // body
		System.out.println("MD5验签，验签文：" + text + "\n待比较签名值:" + OldSign);

		String result = DigestUtils.md5Hex(Verify.getBytes(text + merCode + directStr,"UTF-8"));
		System.out.println("签名值:"+result);
		if (OldSign == null || result == null || !OldSign.equals(result)) {
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Ips><IssuedTradeRsp><head><ReferenceID>msg20180416172451</ReferenceID><RspCode>000000</RspCode><RspMsg><![CDATA[处理成功]]></RspMsg><ReqDate>20180416172451</ReqDate><RspDate>20180416172451</RspDate><Signature>5273b457b96e57863cdb46e3110471a3</Signature></head><body><IssuedTradeList><IssuedTrade><BatchNo>BA170520180416092043064098</BatchNo><IpsBillNo>BO170520180416155042010255</IpsBillNo><MerBillNo>JFSH15740920180416092046</MerBillNo><TrdAmt>1</TrdAmt><TrdInCash>1</TrdInCash><TrdOutCash>4</TrdOutCash><OrdStatus>10</OrdStatus><TrdStatus>0</TrdStatus><ErrorMsg></ErrorMsg><TrdDoTime>20180416160535</TrdDoTime><PayReason></PayReason><Remark>出金</Remark></IssuedTrade></IssuedTradeList></body></IssuedTradeRsp></Ips>";
		String merCode = "207254";
		String key = "TpgUnkpjPzHaaoQJXnMOvZ7pMyBl6seGnyd5kBtDLNq1LCQgHwGwlrMad9P7KATLX84n52jcAuB0ExTvq3UvhA1QOBFM0B7LBlc9Zhx2Cv2YqKFvansZlFHru86AGfO9";
		checkSign(xml,merCode,key);
		String result = DigestUtils.md5Hex(Verify.getBytes("<body><IssuedTradeList><IssuedTrade><BatchNo>BA170520180416092043064098</BatchNo><IpsBillNo>BO170520180416155042010255</IpsBillNo><MerBillNo>JFSH15740920180416092046</MerBillNo><TrdAmt>1</TrdAmt><TrdInCash>1</TrdInCash><TrdOutCash>4</TrdOutCash><OrdStatus>10</OrdStatus><TrdStatus>0</TrdStatus><ErrorMsg></ErrorMsg><TrdDoTime>20180416160535</TrdDoTime><PayReason></PayReason><Remark>出金</Remark></IssuedTrade></IssuedTradeList></body>" + merCode + key,"UTF-8"));
		System.out.println(result);
	}
	
	/**
	 * 获取报文中<RspCode></RspCode>部分
	 * @param xml
	 * @return
	 */
	public static String getRspCode(String xml) {
		int s_index = xml.indexOf("<RspCode>");
		int e_index = xml.indexOf("</RspCode>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 9, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<RspCode></RspCode>部分
	 * @param xml
	 * @return
	 */
	public static String getRspMsg(String xml) {
		int s_index = xml.indexOf("<RspMsg>");
		int e_index = xml.indexOf("</RspMsg>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 8, e_index);
		}
		return sign;
	}
	/**
	 * 获取报文中<Signature></Signature>部分
	 * @param xml
	 * @return
	 */
	public static String getSign(String xml) {
		int s_index = xml.indexOf("<Signature>");
		int e_index = xml.indexOf("</Signature>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 11, e_index);
		}
		return sign;
	}

	/**
	 * 获取body部分
	 * @param xml
	 * @return
	 */
	public static String getBodyXml(String xml) {
		int s_index = xml.indexOf("<Body>");
		int e_index = xml.indexOf("</Body>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index, e_index + 7);
		}
		System.out.println("返回body："+sign);
		return sign;
	}
	
	/**
     * 获取body部分
     *
     * @param xml
     * @return
     */
    public static String getBodyXmlL(String xml) {
        int s_index = xml.indexOf("<body>");
        int e_index = xml.indexOf("</body>");
        String sign = null;
        if (s_index > 0) {
            sign = xml.substring(s_index, e_index + 7);
        }
        System.out.println("返回body：" + sign);
        return sign;
    }

	/**
	 * 获取报文中<ReportStatus></ReportStatus>部分
	 * @param xml
	 * @return
	 */
	public static String getReportStatus(String xml) {
		int s_index = xml.indexOf("<ReportStatus>");
		int e_index = xml.indexOf("</ReportStatus>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 14, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<ErrorCode></ErrorCode>部分
	 * @param xml
	 * @return
	 */
	public static String getIssuedErrorCode(String xml) {
		int s_index = xml.indexOf("<ErrorCode>");
		int e_index = xml.indexOf("</ErrorCode>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 14, e_index);
		}
		return sign;
	}

	/**
	 * 获取报文中<ErrMsg></ErrMsg>部分
	 * @param xml
	 * @return
	 */
	public static String getErrorMsg(String xml) {
		int s_index = xml.indexOf("<ErrorMsg>");
		int e_index = xml.indexOf("</ErrorMsg>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 11, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<BatchBillno></BatchBillno>部分
	 * @param xml
	 * @return
	 */
	public static String getBatchBillno(String xml) {
		int s_index = xml.indexOf("<BatchBillno>");
		int e_index = xml.indexOf("</BatchBillno>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 13, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<BatchStatus></BatchStatus>部分
	 * @param xml
	 * @return
	 */
	public static String getBatchStatus(String xml) {
		int s_index = xml.indexOf("<BatchStatus>");
		int e_index = xml.indexOf("</BatchStatus>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 13, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<BatchErrorMsg></BatchErrorMsg>部分
	 * @param xml
	 * @return
	 */
	public static String getBatchErrorMsg(String xml) {
		int s_index = xml.indexOf("<BatchErrorMsg>");
		int e_index = xml.indexOf("</BatchErrorMsg>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 15, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<OrdStatus></OrdStatus>部分
	 * @param xml
	 * @return
	 */
	public static String getOrdStatus(String xml) {
		int s_index = xml.indexOf("<OrdStatus>");
		int e_index = xml.indexOf("</OrdStatus>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 11, e_index);
		}
		return sign;
	}
	
	/**
	 * 获取报文中<TrdStatus></TrdStatus>部分
	 * @param xml
	 * @return
	 */
	public static String getTrdStatus(String xml) {
		int s_index = xml.indexOf("<TrdStatus>");
		int e_index = xml.indexOf("</TrdStatus>");
		String sign = null;
		if (s_index > 0) {
			sign = xml.substring(s_index + 11, e_index);
		}
		return sign;
	}
}
