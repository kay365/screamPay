package com.qh.common.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.qh.redis.service.RedisUtil;

@Component
class WebConfigurer extends WebMvcConfigurerAdapter {
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/files/**").addResourceLocations("file:///" + RedisUtil.getSysConfigValue(CfgKeyConst.payFilePath),
				"file:///" + RedisUtil.getSysConfigValue(CfgKeyConst.qr_money_path));
	}

}