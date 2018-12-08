package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CardType
 * @Description 银行卡类型
 * @Date 2017年10月31日 上午11:06:36
 * @version 1.0.0
 */
public enum CardType {
	/***行用卡和储蓄卡都不支持***/
	none(-1),
	/***信用卡和储蓄卡***/
	both(2),
	/***储蓄卡**/
	savings(0),
	/***信用卡***/
	credit(1);
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(savings.id(), "储蓄卡");
		descMap.put(credit.id(), "信用卡");
	}
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private CardType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
	
}
