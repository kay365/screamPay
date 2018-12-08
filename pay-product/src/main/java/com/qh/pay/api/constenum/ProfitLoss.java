package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ProfitLoss
 * @Description 盈亏显示
 * @Date 2017年11月14日 下午4:04:21
 * @version 1.0.0
 */
public enum ProfitLoss {
	/**盈利**/
	profit(1),
	/**亏损**/
	loss(0);

	/**** 支付订单状态描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(profit.id(), "盈");
		descMap.put(loss.id(), "亏");
	}

	private int id;

	private ProfitLoss(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
}
