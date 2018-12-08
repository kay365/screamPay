package com.qh.common.utils;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import com.qh.pay.api.constenum.UserType;
import com.qh.system.domain.UserDO;

public class ShiroUtils {
	public static Subject getSubjct() {
		return SecurityUtils.getSubject();
	}
	public static UserDO getUser() {
		return (UserDO)getSubjct().getPrincipal();
	}
	public static Integer getUserId() {
		return getUser().getUserId();
	}
	public static String getUsername() {
		UserDO user = getUser();
		if(user==null)return "系统";
		return user.getUsername();
	}
	public static void logout() {
		getSubjct().logout();
	}
	
	
	public static boolean ifFoundAcct(){
		return ifFoundAcct();
	}
	
	/***
	 * 
	 * @Description 是否为资金账户
	 * @param user
	 * @return
	 */
	public static boolean ifFoundAcct(UserDO user){
		return UserType.foundAcct.id() == user.getUserType();
	}
	
	
	public static boolean ifMerch(){
		return ifMerch(getUser());
	}
	
	/**
	 * @Description 是否为商户
	 * @param user
	 * @return
	 */
	public static boolean ifMerch(UserDO user) {
		return UserType.merch.id() == user.getUserType();
	}
	
	public static boolean ifAgent(){
		return ifAgent(getUser());
	}
	/**
	 * @Description 是否为代理
	 * @param user
	 * @return
	 */
	public static  boolean ifAgent(UserDO user) {
		return UserType.agent.id() == user.getUserType();
	}
	
	
}
