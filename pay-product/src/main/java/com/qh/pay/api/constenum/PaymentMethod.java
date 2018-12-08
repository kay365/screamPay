package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 结算方式
 * @author Swell
 *
 */
public enum PaymentMethod {
	/**D0垫付结算 自然日秒结算**/
	D0(0),
	/**T1工作日第二天结算**/
	T1(1),
	/**D+1 自然日第二天结算**/
	D1(2),
	/**T+0 工作日秒结算**/
	T0(3)
	;

	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(D0.id(), "D+0自然日秒结算");
		descMap.put(D1.id(), "D+1自然日隔天结算");
		descMap.put(T0.id(), "T+0工作日秒结算");
		descMap.put(T1.id(), "T+1工作日隔天结算");
	}
	
	private static final Map<String, String> descNameMap = new HashMap<>(4);
	static {
		descNameMap.put(D0.name(), "D+0自然日秒结算");
		descNameMap.put(D1.name(), "D+1自然日隔天结算");
		descNameMap.put(T0.name(), "T+0工作日秒结算");
		descNameMap.put(T1.name(), "T+1工作日隔天结算");
	}
	
	/**** 描述 ****/
	private static final Map<String, Integer> idMap = new HashMap<>(4);
	static {
		idMap.put(D0.name(), D0.id());
		idMap.put(D1.name(), D1.id());
		idMap.put(T0.name(), T0.id());
		idMap.put(T1.name(), T1.id());
	}

	private Integer id;

	private PaymentMethod(Integer id) {
		this.id = id;
	}

	public Integer id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
	
	public static Map<String, String> descName() {
		return descNameMap;
	}
	
	public static Map<String, Integer> idMap() {
		return idMap;
	}
}
