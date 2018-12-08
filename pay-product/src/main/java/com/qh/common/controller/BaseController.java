package com.qh.common.controller;

import org.springframework.stereotype.Controller;

import com.qh.common.utils.ShiroUtils;
import com.qh.system.domain.UserDO;

@Controller
public class BaseController {
	public UserDO getUser() {
		return ShiroUtils.getUser();
	}

	public Integer getUserId() {
		return getUser().getUserId();
	}

	public String getUsername() {
		return getUser().getUsername();
	}
}