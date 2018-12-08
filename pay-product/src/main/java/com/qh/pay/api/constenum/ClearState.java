package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName ClearState
 * @Description 清算状态
 * @Date 2017年12月6日 下午7:24:52
 * @version 1.0.0
 */
public enum ClearState {
	/***未清算***/
	init(0),
	/***清算成功**/
	succ(1),
	/***清算失败**/
	fail(2),
	/**部分清算**/
	ing(3),
	;
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(init.id(), "未清算");
		descMap.put(succ.id(), "清算成功");
		descMap.put(fail.id(), "清算失败");
		descMap.put(ing.id(), "部分清算");
	}
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private ClearState(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
