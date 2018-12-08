package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName AuditType
 * @Description 审核类型
 * @Date 2017年11月16日 下午3:50:28
 * @version 1.0.0
 */
public enum AuditType {
	/***代付审核**/
	order_acp(0),
	
	/***支付审核**/
	order(1),
	
	/***提现审核**/
	order_withdraw(2),
	;

	/****审核类型描述****/
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(order_acp.id(), "下发审核");
		descMap.put(order.id(), "支付审核");
		descMap.put(order_withdraw.id(), "提现审核");
	}
	
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private AuditType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
