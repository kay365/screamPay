package com.qh.query.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.qh.common.utils.R;
import com.qh.pay.domain.FooterDO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.constenum.FeeType;
import com.qh.pay.api.constenum.OrderType;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;
import com.qh.query.service.RecordQueryService;
import com.qh.system.domain.UserDO;

/**
 * @ClassName RecordQueryController
 * @Description 流水记录
 * @Date 2017年12月26日 下午2:21:05
 * @version 1.0.0
 */
@Controller
@RequestMapping("/recordQuery")
public class RecordQueryController {
	
	@Autowired
	private RecordQueryService recordQueryService;
	
	
	@GetMapping("/merchBal")
	@RequiresPermissions("pay:recordQuery:merchBal")
	String merchBal(Model model){
		model.addAttribute("feeTypes", FeeType.desc());
		model.addAttribute("orderTypes", OrderType.desc());
		UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("feeTypes", FeeType.merchDesc());
        }
	    return "pay/recordQuery/recordMerchBal";
	}
	
	@ResponseBody
	@GetMapping("/merchBal/list")
	@RequiresPermissions("pay:recordQuery:merchBal")
	public PageUtils merchBallist(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() == user.getUserType()){
        	query.put("merchNo", user.getUsername());
        }
		List<RecordMerchBalDO> recordMerchBalList = recordQueryService.merchBalList(query);
		int total = recordQueryService.merchBalCount(query);
		PageUtils pageUtils = new PageUtils(recordMerchBalList, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/merchBal/list/footer")
	@RequiresPermissions("pay:recordQuery:merchBal")
	public R merchBallistFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
		UserDO user = ShiroUtils.getUser();
		if(UserType.merch.id() == user.getUserType()){
			params.put("merchNo", user.getUsername());
		}
		FooterDO fdo = recordQueryService.merchBalListFooter(params);
		return R.okData(fdo);
	}
	
	
	@GetMapping("/merchAvailBal")
	@RequiresPermissions("pay:recordQuery:merchAvailBall")
	String merchAvailBal(Model model){
		model.addAttribute("feeTypes", FeeType.desc());
		model.addAttribute("orderTypes", OrderType.desc());
		UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("feeTypes", FeeType.merchDesc());
        }
	    return "pay/recordQuery/recordMerchAvailBal";
	}
	
	@ResponseBody
	@GetMapping("/merchAvailBal/list")
	@RequiresPermissions("pay:recordQuery:merchAvailBall")
	public PageUtils merchAvailBalList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() == user.getUserType()){
        	query.put("merchNo", user.getUsername());
        }
		List<RecordMerchBalDO> recordMerchAvailBalList = recordQueryService.merchAvailBalList(query);
		int total = recordQueryService.merchAvailBalCount(query);
		PageUtils pageUtils = new PageUtils(recordMerchAvailBalList, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/merchAvailBal/list/footer")
	@RequiresPermissions("pay:recordQuery:merchAvailBall")
	public R merchAvailBalListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
		UserDO user = ShiroUtils.getUser();
		if(UserType.merch.id() == user.getUserType()){
			params.put("merchNo", user.getUsername());
		}
		FooterDO fdo = recordQueryService.merchAvailBalListFooter(params);
		return R.okData(fdo);
	}
	
	
	
	@GetMapping("/foundAcct")
	@RequiresPermissions("pay:recordQuery:foundAcct")
	String foundAcct(Model model){
		model.addAttribute("feeTypes", FeeType.desc());
		model.addAttribute("orderTypes", OrderType.desc());
		UserDO user = ShiroUtils.getUser();
		if(UserType.agent.id() == user.getUserType()){
			model.addAttribute("feeTypes", FeeType.agentDesc());
        	model.addAttribute("agentNo", user.getUsername());
        }
	    return "pay/recordQuery/recordFoundAcct";
	}
	
	@ResponseBody
	@GetMapping("/foundAcct/list")
	@RequiresPermissions("pay:recordQuery:foundAcct")
	public PageUtils foundAcctList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(UserType.agent.id() == user.getUserType()){
        	query.put("username", user.getUsername());
        }
		List<RecordFoundAcctDO> recordFoundAcctList = recordQueryService.foundAcctList(query);
		int total = recordQueryService.foundAcctCount(query);
		PageUtils pageUtils = new PageUtils(recordFoundAcctList, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/foundAcct/list/footer")
	@RequiresPermissions("pay:recordQuery:foundAcct")
	public R foundAcctListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
		UserDO user = ShiroUtils.getUser();
		if(UserType.agent.id() == user.getUserType()){
			params.put("username", user.getUsername());
		}

		FooterDO fdo = recordQueryService.foundAcctListFooter(params);
		return R.okData(fdo);
	}
	
	
	@GetMapping("/foundAvailAcct")
	@RequiresPermissions("pay:recordQuery:foundAvailAcct")
	String RecordMerchAvailBal(Model model){
		model.addAttribute("feeTypes", FeeType.desc());
		model.addAttribute("orderTypes", OrderType.desc());
		UserDO user = ShiroUtils.getUser();
        if(UserType.agent.id() == user.getUserType()){
        	model.addAttribute("agentNo", user.getUsername());
        	model.addAttribute("feeTypes", FeeType.agentDesc());
        }
	    return "pay/recordQuery/recordFoundAvailAcct";
	}
	
	@ResponseBody
	@GetMapping("/foundAvailAcct/list")
	@RequiresPermissions("pay:recordQuery:foundAvailAcct")
	public PageUtils recordMerchAvailBalList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(UserType.agent.id() == user.getUserType()){
        	query.put("username", user.getUsername());
        }
		List<RecordFoundAcctDO> foundAvailAcctList = recordQueryService.foundAvailAcctList(query);
		int total = recordQueryService.foundAvailAcctCount(query);
		PageUtils pageUtils = new PageUtils(foundAvailAcctList, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/foundAvailAcct/list/footer")
	@RequiresPermissions("pay:recordQuery:foundAvailAcct")
	public R recordMerchAvailBalListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		//查询列表数据
		UserDO user = ShiroUtils.getUser();
		if(UserType.agent.id() == user.getUserType()){
			params.put("username", user.getUsername());
		}
		FooterDO fdo = recordQueryService.foundAvailAcctListFooter(params);
		return R.okData(fdo);
	}
}
