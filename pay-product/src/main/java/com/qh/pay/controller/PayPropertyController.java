package com.qh.pay.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.common.config.Constant;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.PayConfigType;
import com.qh.pay.domain.PayPropertyDO;
import com.qh.pay.service.PayPropertyService;
import com.qh.redis.service.RedisUtil;

/**
 * 支付参数配置
 * 
 * @date 2017-10-27 17:52:44
 */
 
@Controller
@RequestMapping("/pay/payProperty")
public class PayPropertyController {
	@Autowired
	private PayPropertyService payPropertyService;
	
	@GetMapping()
	@RequiresPermissions("pay:payProperty:payProperty")
	String PayProperty(Model model){
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("payConfigTypes", PayConfigType.desc());
	    return "pay/payProperty/payProperty";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payProperty:payProperty")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);
		List<PayPropertyDO> payPropertyList = payPropertyService.list(query);
		int total = payPropertyService.count(query);
		PageUtils pageUtils = new PageUtils(payPropertyList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:payProperty:add")
	String add(Model model){
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("payConfigTypes", PayConfigType.desc());
	    return "pay/payProperty/add";
	}

	@GetMapping("/getMechNoByCompany")
	@ResponseBody
	public R getMechNoByCompany(String payCompany){
		return R.okData(RedisUtil.getMechNoByCompany(payCompany));
	}
	
	@GetMapping("/edit/{id}")
	@RequiresPermissions("pay:payProperty:edit")
	String edit(@PathVariable("id") Integer id,Model model){
		PayPropertyDO payProperty = payPropertyService.get(id);
		model.addAttribute("payProperty", payProperty);
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("payConfigTypes", PayConfigType.desc());
	    return "pay/payProperty/edit";
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:payProperty:add")
	public R save( PayPropertyDO payProperty){
		int count = 0;
		try {
			count = payPropertyService.save(payProperty);
		} catch (Exception e) {
			return R.error(e.getMessage());
		}
		if(count == 1){
			return R.ok();
		}else if(count == Constant.data_exist){
			return  R.error(payProperty.getConfigKey() + "配置项已经存在");
		}
		return R.error();
	}
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:payProperty:edit")
	public R update( PayPropertyDO payProperty){
		try {
			payPropertyService.update(payProperty);
		} catch (Exception e) {
			return R.error(e.getMessage());
		}
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:payProperty:remove")
	public R remove(Integer id){
		if(payPropertyService.remove(id)>0){
		return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:payProperty:batchRemove")
	public R remove(@RequestParam("ids[]") Integer[] ids){
		payPropertyService.batchRemove(ids);
		return R.ok();
	}
	
	@PostMapping("/exist")
	@ResponseBody
	boolean exist(@RequestParam("merchantno") String merchantno,@RequestParam("configKey") String configKey) {
		// 存在，不通过，false
		return !payPropertyService.exist(merchantno,configKey);
	}
}
