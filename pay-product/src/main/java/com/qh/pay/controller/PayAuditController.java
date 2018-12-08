package com.qh.pay.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.AuditResult;
import com.qh.pay.api.constenum.AuditType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAuditDO;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayAuditService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.redis.constenum.ConfigParent;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
import com.qh.common.config.CfgKeyConst;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;

/**
 * 支付审核
 * 
 * @date 2017-11-16 15:59:04
 */
 
@Controller
@RequestMapping("/pay/payAudit")
public class PayAuditController {
	@Autowired
	private PayAuditService payAuditService;
	@Autowired
	private PayConfigCompanyService payConfigCompanyService;
	 @Autowired
	private MerchantService merchantService;
	@Autowired
	private AgentService agentService;
	@GetMapping()
	@RequiresPermissions("pay:payAudit:payAudit")
	String PayAudit(Model model,String orderType){
		model.addAttribute("auditResults", AuditResult.desc());
		model.addAttribute("auditTypes", AuditType.desc());
		model.addAttribute("orderType", Integer.parseInt(orderType));
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("ifClose", "0");
		map.put("outChannel", "acp");
        List<PayConfigCompanyDO> list = payConfigCompanyService.list(map);
        for (PayConfigCompanyDO payConfigCompanyDO : list) {
        	payConfigCompanyDO.setCallbackDomain(PayCompany.desc().get(payConfigCompanyDO.getCompany()));
		}
		model.addAttribute("payConfigCompany", list);
	    return "pay/payAudit/payAudit";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payAudit:payAudit")
	public PageUtils list(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		// 查询列表数据
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
        Query query = new Query(params);
		List<PayAuditDO> payAuditList = payAuditService.list(query);
		for (PayAuditDO payAuditDO : payAuditList) {
			Agent agent = agentService.get(payAuditDO.getMerchNo());
			if(agent==null) {
				Merchant merchant = merchantService.get(payAuditDO.getMerchNo());
				if(merchant!=null)
					payAuditDO.setMerchName(merchant.getMerchantsShortName());
			}else {
				payAuditDO.setMerchName(agent.getMerchantsName());
			}
        	
		}
		int total = payAuditService.count(query);
		PageUtils pageUtils = new PageUtils(payAuditList, total);
		return pageUtils;
	}
	
	/**
	 * 审核
	 */
	@PostMapping( "/audit")
	@ResponseBody
	@RequiresPermissions("pay:payAudit:audit")
	public R audit(@RequestParam("orderNo") String orderNo, 
			@RequestParam("merchNo") String merchNo, @RequestParam("auditType") Integer auditType, @RequestParam("auditResult") Integer auditResult,@RequestParam("company") String company){
		if(payAuditService.audit(orderNo,merchNo,auditType,auditResult,company)>0){
			if(AuditResult.pass.id() == auditResult){
				RedisMsg.orderAcpMsg(merchNo, orderNo);
			}else{
				RedisMsg.orderAcpNopassMsg(merchNo, orderNo);
			}
			return R.ok();
		}
		return R.error("审核失败！订单不存在或代付通道不存在");
	}
	
	/**
	 * 批量审核
	 */
	@PostMapping( "/batchAudit")
	@ResponseBody
	@RequiresPermissions("pay:payAudit:batchAudit")
	public R batchAudit(@RequestParam("orderNos[]") String[] orderNos, @RequestParam("merchNos[]") String[] merchNos, 
			@RequestParam("auditTypes[]") Integer[] auditTypes, @RequestParam("auditResult") Integer auditResult,@RequestParam("companys[]") String[] companys){
		try {
			if(payAuditService.batchAudit(orderNos,merchNos,auditTypes, auditResult,companys) == orderNos.length){
				for (int len = orderNos.length ,i = 0; i < len; i++) {
					if(AuditResult.pass.id() == auditResult){
						RedisMsg.orderAcpMsg(merchNos[i], orderNos[i]);
					}else{
						RedisMsg.orderAcpNopassMsg(merchNos[i], orderNos[i]);
					}
				}
				return R.ok();
			};
		} catch (Exception e) {
			return R.error(e.getMessage());
		}
		
		return R.error("批量审核失败,订单不存在或代付通道不存在");
	}
	
	/**
	 * 线下转账
	 */
	@PostMapping( "/offlineTransfer")
	@ResponseBody
	@RequiresPermissions("pay:offlineTransfer")
	public R offlineTransfer(@RequestParam("orderNo") String orderNo, 
			@RequestParam("merchNo") String merchNo, @RequestParam("auditType") Integer auditType){
		return payAuditService.offlineTransfer(orderNo,merchNo,auditType);
	}
	
	@GetMapping("/settingPage")
	@RequiresPermissions("pay:payAudit:setting")
	String settingPage(Model model){
		String pollMoneyValue = RedisUtil.getConfigValue(CfgKeyConst.PAY_AUDIT_AUTO_ACP, ConfigParent.payAuditConfig.name());
		model.addAttribute("payAuditAutoAcp",Integer.parseInt(pollMoneyValue==null?"1":pollMoneyValue));
	    return "pay/payAudit/setting";
	}
	
	
	@GetMapping("/person")
	@RequiresPermissions("pay:payAudit:payAudit:person")
	String PayAuditPerson(Model model,String orderType){
		model.addAttribute("auditResults", AuditResult.desc());
		model.addAttribute("auditTypes", AuditType.desc());
		model.addAttribute("orderType", Integer.parseInt(orderType));
	    return "pay/payAudit/payAuditPerson";
	}
	
	@ResponseBody
	@GetMapping("/listPerson")
	@RequiresPermissions("pay:payAudit:payAudit:person")
	public PageUtils listPerson(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		// 查询列表数据
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		UserDO user = ShiroUtils.getUser();
		params.put("merchNo", user.getUsername());
        Query query = new Query(params);
		List<PayAuditDO> payAuditList = payAuditService.list(query);
		int total = payAuditService.count(query);
		PageUtils pageUtils = new PageUtils(payAuditList, total);
		return pageUtils;
	}
}
