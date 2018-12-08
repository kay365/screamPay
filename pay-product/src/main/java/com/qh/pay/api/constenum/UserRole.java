package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName UserRole
 * @Description 用户角色
 * @Date 2017年11月3日 上午11:09:55
 * @version 1.0.0
 */
public enum UserRole {
	admin(1),//超级用户角色
	agent(2),//一级代理
	subAgent(3),//二级代理
	merch(4),//聚富商户
	yunying(5);//运营
	
	/****支付订单状态描述****/
	private static final Map<Integer,String> descMap = new HashMap<>(8);
	static{
		descMap.put(admin.id(), "超级用户角色");
		descMap.put(agent.id(), "一级代理");
		descMap.put(subAgent.id(), "二级代理");
		descMap.put(merch.id(), "聚富商户");
		descMap.put(yunying.id(), "运营");
	}
	
	private int id;
	
	private UserRole(int id){
		this.id = id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}

	
	/**
	 * @return
	 */
	public int id() {
		return this.id;
	}
}
