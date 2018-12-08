package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName UserType
 * @Description 用户类型
 * @Date 2017年11月3日 上午11:09:55
 * @version 1.0.0
 */
public enum UserType {
	user(0),//普通用户
	foundAcct(1),//资金账号
	merch(2),//聚富商户
	agent(3),//聚富商户下的代理
	subAgent(6),//聚富商户下的子集代理
	payMerch(4),//第三方支付公司下的商户，
	payAgent(5);//第三方支付公司下的代理
	
	/****支付订单状态描述****/
	private static final Map<Integer,String> descMap = new HashMap<>(8);
	static{
		descMap.put(user.id(), "普通用户");
		descMap.put(foundAcct.id(), "资金账户");
		descMap.put(merch.id(), "聚富商户");
		descMap.put(agent.id(), "商户代理");
		descMap.put(subAgent.id, "子级代理");
		descMap.put(payMerch.id(), "支付商户");
		descMap.put(payAgent.id(), "支付代理");
	}
	
	private int id;
	
	private UserType(int id){
		this.id = id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private static final Map<Integer,String> addDescMap = new HashMap<>(8);
	static{
		addDescMap.put(user.id(), "普通用户");
		/*addDescMap.put(agent.id(), "商户代理");
		addDescMap.put(payMerch.id(), "支付商户");
		addDescMap.put(payAgent.id(), "支付代理");*/
	}
	public static Map<Integer, String> addDesc(){
		return addDescMap;
	}
	
	/**
	 * @return
	 */
	public int id() {
		return this.id;
	}
}
