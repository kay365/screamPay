package com.qh.system.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName UserStatus
 * @Description 用户状态
 * @Date 2017年11月6日 上午9:50:58
 * @version 1.0.0
 */
public enum UserStatus {
	normal(1),//正常
	forbidden(0);//禁用
	
	private static final Map<Integer,String> descMap = new HashMap<>();
	static{
		descMap.put(normal.id, "正常");
		descMap.put(forbidden.id, "禁用");
	}
	
	private Integer id;
	
	public int id(){
		return this.id;
	}
	
	private UserStatus(int id){
		this.id = id;
	}
}
