package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CollType
 * @Description 签约类型
 * @Date 2017年11月27日 上午11:37:36
 * @version 1.0.0
 */
public enum CollType {
	qpay(1),
	acp(2);
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(qpay.id(), "快捷支付");
		descMap.put(acp.id(), "代扣扣款");
	}
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private CollType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
