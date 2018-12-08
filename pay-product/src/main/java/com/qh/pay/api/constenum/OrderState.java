package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName OrderState
 * @Description 订单支付状态 0:初始化 1:成功  2:处理中 3:失败 4:关闭
 * @Date 2017年10月31日 上午10:16:20
 * @version 1.0.0
 */
public enum OrderState {
	
	init(0),succ(1), fail(2), ing(3), close(4);
	
	/****支付订单状态描述****/
	private static final Map<Integer,String> descMap = new HashMap<>(8);
	static{
		descMap.put(init.id(), "初始化");
		descMap.put(succ.id(), "成功");
		descMap.put(fail.id(), "失败");
		descMap.put(ing.id(), "处理中");
		descMap.put(close.id(), "关闭");
	}
	
	/****支付订单状态描述****/
	private static final Map<Integer,String> simpleMap = new HashMap<>(8);
	static{
		simpleMap.put(ing.id(), "处理中");
		simpleMap.put(succ.id(), "成功");
		simpleMap.put(fail.id(), "失败");
	}
	
	private int id;

	private OrderState(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
	
	public static Map<Integer, String> simple() {
		return simpleMap;
	}
	
}
