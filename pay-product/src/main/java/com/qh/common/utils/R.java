package com.qh.common.utils;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.Constant;

public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;

	public R() {
		put(Constant.result_code, Constant.result_code_succ);
		put(Constant.result_msg, Constant.result_msg_succ);
	}

	public R(int code, String msg) {
		put(Constant.result_code, code);
		put(Constant.result_msg, msg);
	}
	public static R error() {
		return error(Constant.result_code_error, Constant.result_msg_error);
	}

	public static R error(String msg) {
		return error(Constant.result_code_error, msg);
	}

	public static R error(int code, String msg) {
		return new R(code,msg);
	}

	public static R ok(String msg) {
		R r = new R();
		r.put(Constant.result_msg, msg);
		return r;
	}

	public static R okData(Object data) {
		R r = new R();
		r.put(Constant.result_data, data);
		return r;
	}

	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.putAll(map);
		return r;
	}

	public static R ok() {
		return new R();
	}

	public String jsonStr() {
		return JSONObject.toJSONString(this);
	}
	@Override
	public R put(String key, Object value) {
		super.put(key, value);
		return this;
	}

	public String getMsg(){
		return (String)this.get(Constant.result_msg);
	}

	public Object getData(){
		return this.get(Constant.result_data);
	}

	public static boolean ifSucc(R r){
		return String.valueOf(Constant.result_code_succ).equals(String.valueOf(r.get(Constant.result_code)));
	}
	public static boolean ifError(R r){
		return !String.valueOf(Constant.result_code_succ).equals(String.valueOf(r.get(Constant.result_code)));
	}
}
