package com.qh.system.domain;

import java.io.Serializable;
import java.util.Date;



/**
 * 公告表
 * 
 * @date 2018-03-08 15:17:29
 */
public class NoticeDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//
	private Integer id;
	//标题
	private String title;
	//内容
	private String content;
	//更新时间
	private Date createTime;
	//创建人
	private String creator;

	/**
	 * 设置：
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * 获取：
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * 设置：标题
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * 获取：标题
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * 设置：内容
	 */
	public void setContent(String content) {
		this.content = content;
	}
	/**
	 * 获取：内容
	 */
	public String getContent() {
		return content;
	}
	/**
	 * 设置：更新时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 获取：更新时间
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * 设置：创建人
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	/**
	 * 获取：创建人
	 */
	public String getCreator() {
		return creator;
	}
}
