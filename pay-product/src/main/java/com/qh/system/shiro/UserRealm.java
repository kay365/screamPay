package com.qh.system.shiro;

import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import com.qh.common.utils.CacheUtils;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.system.dao.UserDao;
import com.qh.system.domain.UserDO;
import com.qh.system.service.MenuService;

public class UserRealm extends AuthorizingRealm {
	@Autowired
	UserDao userMapper;
	@Autowired
	MenuService menuService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
		Integer userId = ShiroUtils.getUserId();
		Set<String> perms = menuService.listPerms(userId);
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		info.setStringPermissions(perms);
		return info;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		String username = (String) token.getPrincipal();
		String password = new String((char[]) token.getCredentials());
		// 查询用户信息
		UserDO user = userMapper.getByUserName(username);
		// 账号不存在
		if (user == null) {
			throw new UnknownAccountException("账号或密码不正确");
		}
		String salt = CacheUtils.getLoginSalt(username);
		if(ParamUtil.isEmpty(salt)){
			throw new UnknownAccountException("登录验证失败");
		}
		// 密码错误
		if (!password.equals(Md5Util.MD5(username + user.getPassword() + salt))) {
			throw new IncorrectCredentialsException("账号或密码不正确");
		}

		// 账号锁定
		if (user.getStatus() == 0) {
			throw new LockedAccountException("账号已被锁定,请联系管理员");
		}
		user.setPassword("");
		user.setFundPassword("");
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, password, getName());
		return info;
	}

}
