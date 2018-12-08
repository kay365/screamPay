package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CardSendType
 * @Description 用于快捷绑卡、或支付短信发送类型
 * @Date 2017年12月12日 下午5:18:31
 * @version 1.0.0
 */
public enum CardSendType {
	/***行用卡和储蓄卡都不支持***/
	bind(1),
	/***信用卡和储蓄卡***/
	pay(2)
	;
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(bind.id(), "绑卡");
		descMap.put(pay.id(), "支付");
	}
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private CardSendType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
