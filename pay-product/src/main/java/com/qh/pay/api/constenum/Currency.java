package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName Currency
 * @Description 币种 CNY:人民币
 * @Date 2017年10月31日 上午10:48:28
 * @version 1.0.0
 */
public enum Currency {
	CNY;
	private static final Map<String,String> descMap = new HashMap<String,String>(4);
	static{
		descMap.put(CNY.name(), "人民币");
	}
	public static Map<String, String> desc() {
		return descMap;
	}
	
}
