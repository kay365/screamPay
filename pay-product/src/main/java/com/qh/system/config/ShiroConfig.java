package com.qh.system.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qh.system.shiro.ShiroLoginFilter;
import com.qh.system.shiro.UserRealm;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;

@Configuration
public class ShiroConfig {
	
	@Bean
	public EhCacheManager getEhCacheManager() {
		EhCacheManager em = new EhCacheManager();
		em.setCacheManagerConfigFile("classpath:config/ehcache.xml");
		return em;
	}

	@Bean
	UserRealm userRealm(EhCacheManager cacheManager) {
		UserRealm userRealm = new UserRealm();
		userRealm.setCacheManager(cacheManager);
		return userRealm;
	}
	@Bean
	SessionDAO sessionDAO() {
		MemorySessionDAO sessionDAO = new MemorySessionDAO();
		return sessionDAO;
	}

	@Bean
	public SessionManager sessionManager() {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		Collection<SessionListener> listeners = new ArrayList<SessionListener>();
		listeners.add(new BDSessionListener());
		sessionManager.setSessionListeners(listeners);
		sessionManager.setSessionDAO(sessionDAO());
		//去掉地址栏中的sessionId
		sessionManager.setSessionIdUrlRewritingEnabled(false);
		return sessionManager;
	}

	@Bean
	SecurityManager securityManager(UserRealm userRealm) {
		DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
		manager.setRealm(userRealm);
		manager.setCacheManager(getEhCacheManager());
		manager.setSessionManager(sessionManager());
		return manager;
	}

	public static final LinkedHashMap<String,String> filterChainDefinitionMap = new LinkedHashMap<>();
	public static final String anon = "anon";
	public static final String logout = "logout";
	public static final String authc = "authc";
	//定义拦截链
	static{
		filterChainDefinitionMap.put("/favicon.ico", anon);
		filterChainDefinitionMap.put("/css/**", anon);
		filterChainDefinitionMap.put("/js/**", anon);
		filterChainDefinitionMap.put("/fonts/**", anon);
		filterChainDefinitionMap.put("/img/**", anon);
		filterChainDefinitionMap.put("/docs/**", anon);
		filterChainDefinitionMap.put("/druid/**", anon);
		filterChainDefinitionMap.put("/upload/**", anon);
		filterChainDefinitionMap.put("/files/**", anon);
		filterChainDefinitionMap.put("/logout", logout);
		filterChainDefinitionMap.put("/salt", anon);
		filterChainDefinitionMap.put("/login", anon);
		filterChainDefinitionMap.put("/chgPass", anon);
		filterChainDefinitionMap.put("/pay/order", anon);
		filterChainDefinitionMap.put("/pay/return/**", anon);
		filterChainDefinitionMap.put("/pay/card", anon);
		filterChainDefinitionMap.put("/pay/card/**", anon);
		filterChainDefinitionMap.put("/pay/qr", anon);
		filterChainDefinitionMap.put("/pay/qr/**", anon);
		filterChainDefinitionMap.put("/pay/notify/**", anon);
		filterChainDefinitionMap.put("/pay/order/**", anon);
		filterChainDefinitionMap.put("/**", authc);
	}
	
	@Bean
	ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		Map<String,Filter> filtersMap = new LinkedHashMap<>();
		filtersMap.put("shiroLoginFilter", shiroLoginFilter());
		shiroFilterFactoryBean.setFilters(filtersMap);
		shiroFilterFactoryBean.setLoginUrl("/login");
		shiroFilterFactoryBean.setSuccessUrl("/index");
		shiroFilterFactoryBean.setUnauthorizedUrl("/403");
		LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	@Bean("lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	@Bean
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
		proxyCreator.setProxyTargetClass(true);
		return proxyCreator;
	}

	@Bean
	public ShiroDialect shiroDialect() {
		return new ShiroDialect();
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
			@Qualifier("securityManager") SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}
	
	@Bean("shiroLoginFilter")
    public ShiroLoginFilter shiroLoginFilter(){
        return  new ShiroLoginFilter();
    }

}
