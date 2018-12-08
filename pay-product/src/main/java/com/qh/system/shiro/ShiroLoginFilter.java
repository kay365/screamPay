package com.qh.system.shiro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.system.config.ShiroConfig;
import com.qh.system.domain.UserDO;

/**
 * @ClassName ShiroLoginFilter
 * @Description 登录拦截
 * @Date 2017年12月13日 下午3:47:39
 * @version 1.0.0
 */
public class ShiroLoginFilter extends AdviceFilter{
	
	protected PatternMatcher pathMatcher = new AntPathMatcher();
	
	public static final List<String> noNeedLoginUrl = new ArrayList<>();
	static{
		Set<Entry<String, String>> entries = ShiroConfig.filterChainDefinitionMap.entrySet();
		for (Entry<String, String> entry : entries) {
			if(ShiroConfig.anon == entry.getValue() || ShiroConfig.logout == entry.getValue()){
				noNeedLoginUrl.add(entry.getKey());
			}
		}
	}
	
	/**
     * 在访问controller前判断是否登录，返回json，不进行重定向。
     * @param request
     * @param response
     * @return true-继续往下执行，false-该filter过滤器已经处理，不继续执行其他过滤器
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        UserDO user = ShiroUtils.getUser();
        String requestURI = WebUtils.getPathWithinApplication(httpServletRequest);
        if (null == user && !ifNoNeedLogin(requestURI)) {
            String requestedWith = httpServletRequest.getHeader("X-Requested-With");
            if (StringUtils.isNotEmpty(requestedWith) && StringUtils.equals(requestedWith, "XMLHttpRequest")) {//如果是ajax返回指定数据
            	httpServletResponse.setCharacterEncoding("UTF-8");
            	httpServletResponse.setContentType("application/json");
                httpServletResponse.getWriter().write(JSONObject.toJSONString(R.error("亲，请登录！")));
                return false;
            } else {//不是ajax进行重定向处理
                httpServletResponse.sendRedirect("/login");
                return false;
            }
        }
        return true;
    }
    
    
    private boolean ifNoNeedLogin(String requestURI){
    	for (String url : noNeedLoginUrl) {
			if(pathMatcher.matches(url, requestURI)){
				return true;
			}
		}
    	return false;
    }
}
