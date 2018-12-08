package com.qh.pay.domain;

import java.io.Serializable;
import java.util.Date;



/**
 * 
 * 
 * @date 2018-02-26 10:41:24
 */
public class IndustryDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//ID
	private Long id;
	//栏目名
	private String name;
	//父栏目
	private Long parentid;
	//
	private String describe;

	/**
	 * 设置：ID
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * 获取：ID
	 */
	public Long getId() {
		return id;
	}
	/**
	 * 设置：栏目名
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 获取：栏目名
	 */
	public String getName() {
		return name;
	}
	/**
	 * 设置：父栏目
	 */
	public void setParentid(Long parentid) {
		this.parentid = parentid;
	}
	/**
	 * 获取：父栏目
	 */
	public Long getParentid() {
		return parentid;
	}
	/**
	 * 设置：
	 */
	public void setDescribe(String describe) {
		this.describe = describe;
	}
	/**
	 * 获取：
	 */
	public String getDescribe() {
		return describe;
	}
}
