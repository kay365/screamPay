package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AcctType
 * @Description 账户性质
 * @Date 2017年10月31日 上午11:20:35
 * @version 1.0.0
 */
public enum RateUnit {
	baifenhao(1),yuan(2);
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(baifenhao.id(), "%/笔");
		descMap.put(yuan.id(), "元/笔");
	}
	public static Map<Integer, String> desc() {
		return descMap;
	}
	
	private int id;

	private RateUnit(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
