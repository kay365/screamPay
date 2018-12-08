package com.qh.common.controller;

import java.util.List;
import java.util.Map;

import com.qh.common.domain.UserBankDO;
import com.qh.common.service.UserBankService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;

/**
 * 用户银行卡
 * 
 * @date 2018-01-10 14:39:21
 */
 
@Controller
@RequestMapping("/pay/userBank")
public class UserBankController {
	@Autowired
	private UserBankService userBankService;
	
	@GetMapping()
	@RequiresPermissions("pay:userBank:userBank")
	String UserBank(){
	    return "pay/userBank/userBank";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:userBank:userBank")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);
		List<UserBankDO> userBankList = userBankService.list(query);
		int total = userBankService.count(query);
		PageUtils pageUtils = new PageUtils(userBankList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:userBank:add")
	String add(){
	    return "pay/userBank/add";
	}

	@GetMapping("/edit/{username}/{bankNo}")
	@RequiresPermissions("pay:userBank:edit")
	String edit(@PathVariable("username") String username,@PathVariable("bankNo") String bankNo,Model model){
		UserBankDO userBank = userBankService.get(username,bankNo);
		model.addAttribute("userBank", userBank);
	    return "pay/userBank/edit";
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:userBank:add")
	public R save( UserBankDO userBank){
		if(userBankService.save(userBank)>0){
			return R.ok();
		}
		return R.error();
	}
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:userBank:edit")
	public R update( UserBankDO userBank){
		userBankService.update(userBank);
		return R.ok();
	}
	
	
}
