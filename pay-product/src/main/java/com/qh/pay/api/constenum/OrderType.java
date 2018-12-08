package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OrderType
 * @Description 订单类型
 * @Date 2017年11月3日 上午10:51:21
 * @version 1.0.0
 */
public enum OrderType {
	pay(0),//支付
	acp(1),//代付
	charge(2),//充值
	withdraw(3);//提现
	/**** 支付订单状态描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(pay.id(), "支付");
		descMap.put(acp.id(), "代付");
		descMap.put(charge.id(), "充值");
		descMap.put(withdraw.id(), "提现");
	}

	private int id;

	private OrderType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
	
}
