package com.qh.system.service;

import java.util.Collection;
import java.util.List;

import org.apache.shiro.session.Session;
import org.springframework.stereotype.Service;

import com.qh.system.domain.UserOnline;

@Service
public interface SessionService {
	List<UserOnline> list();

	Collection<Session> sessionList();
	
	boolean forceLogout(String sessionId);
	
	/**
	 * 根据用户名强制下线
	 * 暂时用于下线前一次登录的相同用户名
	 * @param userName
	 */
	void forceLogoutByUserName(String userName);
}
