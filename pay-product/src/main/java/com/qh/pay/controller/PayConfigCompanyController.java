package com.qh.pay.controller;

import java.math.BigDecimal;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayChannelType;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.redis.constenum.ConfigParent;
import com.qh.redis.service.RedisUtil;
import com.qh.common.config.CfgKeyConst;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;

/**
 * 支付公司配置
 * 
 * @date 2017-11-06 16:00:33
 */
 
@Controller
@RequestMapping("/pay/payConfigCompany")
public class PayConfigCompanyController {
	@Autowired
	private PayConfigCompanyService payConfigCompanyService;
	
	@GetMapping()
	@RequiresPermissions("pay:payConfigCompany:payConfigCompany")
	String PayConfigCompany(Model model,String payWay){
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("outChannels", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("payWay",payWay);
		model.addAttribute("paymentMethods",PaymentMethod.desc());
	    return "pay/payConfigCompany/payConfigCompany";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payConfigCompany:payConfigCompany")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);
		List<PayConfigCompanyDO> payConfigCompanyList = payConfigCompanyService.list(query);
		int total = payConfigCompanyService.count(query);
		PageUtils pageUtils = new PageUtils(payConfigCompanyList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:payConfigCompany:add")
	String add(Model model,String payWay){
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("outChannels", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("payWay",payWay);
		model.addAttribute("paymentMethods",PaymentMethod.desc());
	    return "pay/payConfigCompany/add";
	}

	@GetMapping("/edit/{company}/{payMerch}/{outChannel}")
	@RequiresPermissions("pay:payConfigCompany:edit")
	String edit(@PathVariable("company") String company, @PathVariable("payMerch") String payMerch, @PathVariable("outChannel") String outChannel, Model model){
		PayConfigCompanyDO payConfigCompany = payConfigCompanyService.get(company,payMerch,outChannel);
		payConfigCompany.setPayPeriod(DateUtil.intFormatToTime(payConfigCompany.getPayPeriod()));
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("outChannels", OutChannel.desc());
		model.addAttribute("payConfigCompany", payConfigCompany);
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("paymentMethods",PaymentMethod.desc());
	    return "pay/payConfigCompany/edit";
	}
	
	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:payConfigCompany:add")
	public R save( PayConfigCompanyDO payConfigCompany){
		if(payConfigCompanyService.save(payConfigCompany) != null){
			return R.ok();
		}
		return R.error();
	}
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:payConfigCompany:edit")
	public R update(PayConfigCompanyDO payConfigCompany){
		payConfigCompanyService.update(payConfigCompany);
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:payConfigCompany:remove")
	public R remove(@RequestParam("company") String company, @RequestParam("payMerch") String payMerch, @RequestParam("outChannel") String outChannel){
		if(payConfigCompanyService.remove(company,payMerch,outChannel)>0){
		return R.ok();
		}
		return R.error();
	}
	
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:payConfigCompany:batchRemove")
	public R batchRemove(@RequestParam("companys[]") String[] companys, @RequestParam("payMerchs[]") String[] payMerchs, @RequestParam("outChannels[]") String[] outChannels){
		if(payConfigCompanyService.batchRemove(companys,payMerchs,outChannels)>0){
			return R.ok();
		}
		return R.error();
	}
	
	@GetMapping("/settingPage")
	@RequiresPermissions("pay:payConfigCompany:setting")
	String settingPage(Model model){
		String pollMoneyValue = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_SIGLE_POLL_MONEY, ConfigParent.outChannelConfig.name());
		String closeAcp = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_ACP, ConfigParent.outChannelConfig.name());
		String closePay = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_PAY, ConfigParent.outChannelConfig.name());
		String closeWithdraw = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_WITHDRAW, ConfigParent.outChannelConfig.name());
		model.addAttribute("companySinglePollMoney",pollMoneyValue);
		model.addAttribute("companyCloseAcp",closeAcp);
		model.addAttribute("companyClosePay",closePay);
		model.addAttribute("companyCloseWithdraw",closeWithdraw);
	    return "pay/payConfigCompany/setting";
	}

	@GetMapping("/payChannelType")
	@RequiresPermissions("pay:payConfigCompany:payChannelType")
	String payChannel(Model model){
		return "pay/payConfigCompany/payChannelType";
	}

	@ResponseBody
	@PostMapping("/payChannelType/list")
	@RequiresPermissions("pay:payConfigCompany:payChannelType")
	public String getTypelist(@RequestParam Map<String,Object> params){
		String channelName="";
		if(params.get("channelName")!=null) {
			channelName = params.get("channelName").toString();
		}
//        Map map =new HashMap();
		Set<Integer> set =RedisUtil.getHashValueKeys(CfgKeyConst.pay_channel_type);
		JSONArray jsonArray = new JSONArray();
		if(channelName==null||"".equals(channelName)){
//			for (PayChannelType e : PayChannelType.values()) {
//				JSONObject object = new JSONObject();
//				object.put("typeId", e.id());
//				object.put("typeName", e.desc().get(e.id()));
//				jsonArray.add(object);
//			}
			for(Integer i :set ){
				JSONObject object =new JSONObject();
				object.put("typeId",i);
				object.put("typeName",RedisUtil.getHashValue(CfgKeyConst.pay_channel_type,i));
				jsonArray.add(object);
			}
		 	return jsonArray.toString();
        }else{
			for(Integer i :set ){
				JSONObject object =new JSONObject();
				object.put("typeId",i);
				object.put("typeName",RedisUtil.getHashValueListForStringObjBlur(CfgKeyConst.pay_channel_type,channelName));
				jsonArray.add(object);
			}
			return jsonArray.toString();
		}
	}



	@ResponseBody
	@PostMapping("/payChannelType/getTypeId")
	public R getTypeId(){
		Set<Integer> set =RedisUtil.getHashValueKeys(CfgKeyConst.pay_channel_type);
		int max=0;
		for(Integer i:set){
			if(max<i){
				max=i;
			}
		}
		return R.okData(max);
	}

//	@ResponseBody
//	@PostMapping("/payChannelType/update")
//	public R add(@RequestParam  Integer typeId ,String typeName){
//		if(typeId==null||typeName==null){
//			return R.error("id和名称不能为空!");
//		}
//		RedisUtil.setHashValue(CfgKeyConst.pay_channel_type,typeId,typeName);
//		return R.ok();
//	}
	@ResponseBody
	@PostMapping("/payChannelType/update")
	public R update(@RequestParam  Integer typeId ,String typeName){
		if(typeId==null||typeName==null){
			return R.error("id和名称不能为空!");
		}
		RedisUtil.setHashValue(CfgKeyConst.pay_channel_type,typeId,typeName);
		return R.ok();
	}
}
