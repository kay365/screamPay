package com.qh.system.domain;

import java.io.Serializable;


/**
 * 系统配置
 * 
 * @date 2017-10-26 17:12:22
 */
public class ConfigDO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	//主键id
	private Integer id;
	//配置项
	private String configItem;
	//配置值
	private String configValue;
	//配置描述
	private String configName;
	//父类配置
	private String parentItem;
	//是否关闭    -1不需要开启这功能 0开启   1关闭
	private Integer isClose;

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
	 * 设置：
	 */
	public void setConfigItem(String configItem) {
		this.configItem = configItem;
	}
	/**
	 * 获取：
	 */
	public String getConfigItem() {
		return configItem;
	}
	/**
	 * 设置：
	 */
	public void setConfigValue(String configValue) {
		this.configValue = configValue;
	}
	/**
	 * 获取：
	 */
	public String getConfigValue() {
		return configValue;
	}
	/**
	 * 设置：
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}
	/**
	 * 获取：
	 */
	public String getConfigName() {
		return configName;
	}
	/**
	 * 设置：
	 */
	public void setParentItem(String parentItem) {
		this.parentItem = parentItem;
	}
	/**
	 * 获取：
	 */
	public String getParentItem() {
		return parentItem;
	}
	/**
	 * 设置：是否关闭    -1不需要开启这功能 0开启   1关闭
	 */
	public void setIsClose(Integer isClose) {
		this.isClose = isClose;
	}
	/**
	 * 获取：是否关闭    -1不需要开启这功能 0开启   1关闭
	 */
	public Integer getIsClose() {
		return isClose;
	}
}
