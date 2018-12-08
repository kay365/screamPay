package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 结算费率 单位
 * @author Swell
 *
 */
public enum PaymentRateUnit {
	/**百分比**/
	PRECENT(1),
	/**元**/
	YUAN(2);

	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(PRECENT.id(), "百分比");
		descMap.put(YUAN.id(), "元");
	}

	private int id;

	private PaymentRateUnit(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
}
