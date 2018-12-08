package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName CertType
 * @Description 证件类型
 * @Date 2017年11月27日 上午11:15:38
 * @version 1.0.0
 */
public enum CertType {
	identity(1);
	private static final Map<Integer,String> descMap = new HashMap<>(4);
	static{
		descMap.put(identity.id(), "身份证");
	}
	private int id;

	public static Map<Integer, String> desc() {
		return descMap;
	}

	private CertType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}
}
