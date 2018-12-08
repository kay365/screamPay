package com.qh.pay.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.qh.pay.api.utils.ParamUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.pay.api.constenum.UserType;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.PayAcctBalService;

/**
 * 账号余额表
 * 
 * @date 2017-11-06 11:41:35
 */
 
@Controller
@RequestMapping("/pay/payAcctBal")
public class PayAcctBalController {
	@Autowired
	private PayAcctBalService payAcctBalService;
	
	@GetMapping()
	@RequiresPermissions("pay:payAcctBal:payAcctBal")
	String PayAcctBal(Model model){
		model.addAttribute("userTypes", UserType.desc());
	    return "pay/payAcctBal/payAcctBal";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payAcctBal:payAcctBal")
	public List<?> list(@RequestParam Map<String, String> params){
		String username = params.get("username");
		String userType = params.get("userType");
		List<PayAcctBal> payAcctBalList = new ArrayList<>();
		if(ParamUtil.isNotEmpty(userType)){
			//查询列表数据
			payAcctBalList = payAcctBalService.listBlur(Integer.parseInt(userType),username);
		}else {
			//查询列表数据
			payAcctBalList = payAcctBalService.listBlur(username);
		}

		return payAcctBalList;
	}

}
