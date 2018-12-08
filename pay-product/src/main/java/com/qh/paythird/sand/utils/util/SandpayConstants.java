/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : paychannel-cmsb-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-10-19 上午11:26:32
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-10-19        Initailized
 */
package com.qh.paythird.sand.utils.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @ClassName ：Constants
 * @author : pxl
 * @Date : 2016-10-19 上午11:26:32
 * @version 2.0.0
 *
 */
public class SandpayConstants {
	
	public static final String UTF8_CHARSET = "UTF-8";
	
	public static final String PARAM_PUBLIC_KEY = "public_key";

	public static final String PARAM_PRIVATE_KEY = "private_key";
	
	public static final String DEFAULT_VERSION = "1.0";
	
	public static final String SIGN_ALGORITHM = "SHA1WithRSA";
	
	public static final String SUCCESS_RESP_CODE = "000000";
	
	
	/** 编码方式*/
	public static final String param_charset = "charset";
	/** 交易报文*/
	public static final String param_data = "data";
	/** 签名类型*/
	public static final String param_signType = "signType";
	/** 签名*/
	public static final String param_sign = "sign";
	/** 扩展域*/
	public static final String param_extend = "extend";
	
	
	public static enum ChannelType {
		INTERNET("07"),MOBILETERMINA("08");
		
		public String code;
		
		private ChannelType(String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
	}
	
	
	public static enum AccessType {
		merchant("1"),platform("2");
		
		public String code;
		
		private AccessType(String code) {
			this.code = code;
		}
		public String getCode() {
			return code;
		}
	}
	
	public static final  Map<String,String> bankNumberMap = new HashMap<>();
    static{
    	bankNumberMap.put("ICBC","01020000");
    	bankNumberMap.put("ABC","01030000");
    	bankNumberMap.put("BOC","01040000");
    	bankNumberMap.put("CCB","01050000");
    	bankNumberMap.put("BCOM","03010000");
    	bankNumberMap.put("CMB","03080000");
    	bankNumberMap.put("GDB","03060000");
    	bankNumberMap.put("CITIC","03020000");
    	bankNumberMap.put("CMBC","03050000");
    	bankNumberMap.put("CEB","03030000");
    	bankNumberMap.put("PABC","03070000");
    	bankNumberMap.put("PSBC","01000000");
    	bankNumberMap.put("HXB","03040000");
    	bankNumberMap.put("CIB","03090000");
    	bankNumberMap.put("BOB","04031000");
    	bankNumberMap.put("BOS","04012900");
    	bankNumberMap.put("SPDB","03100000");
    
    }
	
	
	public static void main(String[] args) {
		System.out.println(ChannelType.INTERNET.getCode());
	}

}
