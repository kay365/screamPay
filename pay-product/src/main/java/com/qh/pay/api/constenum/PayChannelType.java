package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * 是否
 * @author Swell
 *
 */
public enum PayChannelType {
	/**A**/
	a(0),
	/**B**/
	b(1),
	/**C**/
	c(2),
	/**D**/
	d(3),

	e(4),

	f(5);

	/**** 描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(4);
	static {
		descMap.put(a.id(), "A类");
		descMap.put(b.id(), "B类");
		descMap.put(c.id(), "C类");
		descMap.put(d.id(), "D类");
		descMap.put(e.id(), "E类");
		descMap.put(f.id(), "F类");
	}

	private int id;

	private PayChannelType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
}
