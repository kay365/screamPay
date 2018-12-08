package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PayConfigType
 * @Description 支付参数配置类型
 * @Date 2017年10月27日 下午7:07:18
 * @version 1.0.0
 */
public enum PayConfigType {
	text(0), pass(1), ip(2), filePath(3), fileValue(4), merchantNo(5);

	/****支付参数配置类型****/
	private static final Map<Integer,String> descMap = new HashMap<>(8);
	static{
		descMap.put(text.id(), "文本");
		descMap.put(pass.id(), "密码");
		descMap.put(ip.id(), "域名ip");
		descMap.put(filePath.id(), "文件路径");
		descMap.put(fileValue.id(), "文件内容");
		descMap.put(merchantNo.id(), "商户号");
	}
	
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private PayConfigType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
	
}
