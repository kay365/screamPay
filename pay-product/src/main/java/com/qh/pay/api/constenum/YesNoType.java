package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 是否
 * @author Swell
 *
 */
public enum YesNoType {
	/**是**/
	yes(1),
	/**否**/
	not(0);

	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(yes.id(), "是");
		descMap.put(not.id(), "否");
	}
	public static Map<Integer, String> desc() {
		return descMap;
	}
	
	/**** 描述 ****/
	private static final Map<Integer, String> descStatusMap = new HashMap<>(4);
	static {
		descStatusMap.put(yes.id(), "启用");
		descStatusMap.put(not.id(), "禁用");
	}
	public static Map<Integer, String> descStatus() {
		return descStatusMap;
	}
	
	/**** 描述 ****/
	private static final Map<Integer, String> noticeStatusMap = new HashMap<>(4);
	static {
		noticeStatusMap.put(yes.id(), "已通知");
		noticeStatusMap.put(not.id(), "通知中");
	}
	public static Map<Integer, String> noticeStatus() {
		return noticeStatusMap;
	}

	private int id;

	private YesNoType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

}
