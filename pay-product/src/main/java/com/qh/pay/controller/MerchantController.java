package com.qh.pay.controller;

import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.service.LocationService;
import com.qh.common.service.UnionPayService;
import com.qh.common.utils.*;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.api.constenum.AuditResult;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.CertType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayChannelType;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.api.constenum.RateUnit;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.IndustryDO;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.IndustryService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.ConfigDO;
import com.qh.system.domain.UserDO;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚富商户
 * 
 * @date 2017-11-01 10:05:41
 */
 
@Controller
@RequestMapping("/pay/merchant")
public class MerchantController {
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private AgentService agentService;
	
	@Autowired
	private IndustryService industryService;
	@Autowired
	private UnionPayService unionPayService;
	@Autowired
	private LocationService locationService;
	@Autowired
	private PayConfigCompanyService payConfigCompanyService;



	@GetMapping()
	@RequiresPermissions("pay:merchant:merchant")
	String Merchant(Model model){
		model.addAttribute("outChannels", OutChannel.merchAll());
		model.addAttribute("auditStatus", AuditResult.desc());
		model.addAttribute("auditStatusColor", AuditResult.descColor());
		model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("agentTypes", AcctType.descMer());
		model.addAttribute("status", YesNoType.descStatus());
		model.addAttribute("payChannelType", PayChannelType.desc());
	    return "pay/merchant/merchant";
	}
//	@PostMapping("/existLicenseNum")
//	@ResponseBody
//	public boolean existLicenseNum(@RequestParam  String merchantRegisteredNumber){
//		if(merchantRegisteredNumber==null||merchantRegisteredNumber.equals("")){
//			return false;
//		}
//
//		return !merchantService.existLicenseNum(merchantRegisteredNumber);
//	}
	@PostMapping("/existLicenseNum")
	@ResponseBody
	public boolean existEditLicenseNum(@RequestParam  String merchantRegisteredNumber,String merchNo){
		if(merchantRegisteredNumber==null||merchantRegisteredNumber.equals("")){
			return false;
		}
		Merchant current =merchantService.getById(merchNo);
		if(current!=null&&current.getMerchantRegisteredNumber().equals(merchantRegisteredNumber)){
				return true;
		}
		return !merchantService.existLicenseNum(merchantRegisteredNumber);
	}

	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:merchant:merchant")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
		UserDO u = ShiroUtils.getUser();
		if(u.getUserType() == UserType.agent.id() ||u.getUserType() == UserType.subAgent.id()){
			params.put("pAgent", u.getUsername());
			if(u.getUserType() == UserType.agent.id())
				params.put("level", 1);
		}else {
			if(u.getUserType() != UserType.user.id()) {
				return new PageUtils(null,0);
			}
		}
        Query query = new Query(params);
		List<Merchant> merchantList = merchantService.list(query);
		for (Merchant merchant : merchantList) {
			String parentNumber = merchant.getParentAgent();
			if(StringUtils.isNotBlank(parentNumber)) {
				Agent parentAgent = agentService.get(parentNumber);
				merchant.setParentAgent(parentAgent.getMerchantsName());
			}
		}
		int total = merchantService.count(query);
		PageUtils pageUtils = new PageUtils(merchantList, total);
		return pageUtils;
	}
	
	@GetMapping("/merchantInfo/{merchantNO}")
	@RequiresPermissions("pay:merchant:edit")
	String merchantInfo(@PathVariable("merchantNO") String merchantNO,Model model){
		Merchant merchant = merchantService.getById(merchantNO);
		model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("merchant", merchant);
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(merchant.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(merchant.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(merchant.getAccountBankCode(), merchant.getAccountCityCode()));
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        String legalerCardEffectiveTime = merchant.getLegalerCardEffectiveTime();
        if(ParamUtil.isNotEmpty(legalerCardEffectiveTime)){
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }
        String merchantBusinessTerm = merchant.getMerchantBusinessTerm();
        if(ParamUtil.isEmpty(merchantBusinessTerm)){
        	
        }else{
        	String[] businessTerm = merchantBusinessTerm.split("~");
        	model.addAttribute("businessTerm1", businessTerm[0]);
        	model.addAttribute("businessTerm2", businessTerm[1]);
        }
		return "pay/merchant/realName";
	}
	
	@GetMapping("/rateInfo/{merchantNO}")
	@RequiresPermissions("pay:merchant:edit")
	String rateInfo(@PathVariable("merchantNO") String merchantNO,Model model){
		Merchant merchant = merchantService.getById(merchantNO);
		
		Map<String,Map<String,Map<String,BigDecimal>>> rate_d0_map = merchant.getdZero();
		model.addAttribute("rates", rate_d0_map);
		Map<String,BigDecimal> rate_paid = merchant.getPaid();
		model.addAttribute("rate_paid", rate_paid);
		Map<String,Integer> channelSwitch = merchant.getChannelSwitch();
		model.addAttribute("channelSwitch", channelSwitch);
		model.addAttribute("merchant", merchant);
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
        
        Agent agent = agentService.get(merchant.getParentAgent());
		agent = agentService.getById(agent.getAgentId());
		Map<String,Map<String,Map<String,BigDecimal>>> parentRates = agent.getdZero();
		Map<String,BigDecimal> parentPaid = agent.getPaid();
		model.addAttribute("parentRates", parentRates);
		model.addAttribute("parentPaid", parentPaid);
		return "pay/merchant/rate";
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:merchant:add")
	String add(Model model){
		model.addAttribute("merchNo", merchantService.defaultMerchantNo());
		Map<String,String> agentMap = new HashMap<>();
		agentMap.put("isall", String.valueOf(YesNoType.yes.id()));
		agentService.getAgents(agentMap);
		model.addAttribute("agentNs",agentMap);
		model.addAttribute("IndustryP",industryService.listParent(null));
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("outChannels", OutChannel.merchAll());
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("bankCodes", BankCode.desc());
		model.addAttribute("yesOrNo", YesNoType.desc());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("ifClose", 0);
		map.put("outChannel", "acp");
		
		model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
        List<PayConfigCompanyDO> list = payConfigCompanyService.list(map);
        for (PayConfigCompanyDO payConfigCompanyDO : list) {
        	payConfigCompanyDO.setCallbackDomain(PayCompany.desc().get(payConfigCompanyDO.getCompany()));
		}
		model.addAttribute("payConfigCompany", list);
		
	    return "pay/merchant/add";
	}



	@GetMapping("/edit/{merchNo}")
	@RequiresPermissions("pay:merchant:edit")
	String edit(@PathVariable("merchNo") String merchNo,Model model){
		Merchant merchant = merchantService.getById(merchNo);
		model.addAttribute("merchant", merchant);
		model.addAttribute("IndustryP",industryService.listParent(null));
		Map<String,String> agentMap = new HashMap<>();
		agentMap.put("isall", String.valueOf(YesNoType.yes.id()));
		agentService.getAgents(agentMap);
		model.addAttribute("agentNs",agentMap);
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("ifClose", 0);
		map.put("outChannel", "acp");
		List<PayConfigCompanyDO> list = payConfigCompanyService.list(map);
        for (PayConfigCompanyDO payConfigCompanyDO : list) {
        	payConfigCompanyDO.setCallbackDomain(PayCompany.desc().get(payConfigCompanyDO.getCompany()));
		}
		model.addAttribute("payConfigCompany", list);
        model.addAttribute("paidChannelStr",JSONObject.fromObject(merchant.getPaidChannel()).toString());
		/*Map<String,Object> map1 = new HashMap<String,Object>();
		map1.put("parentid", merchant.getMerchantsIndustryCode());
		List<IndustryDO> listS = industryService.listSub(map1);
		
		
		model.addAttribute("paidChannelStr",JSONObject.fromObject(merchant.getPaidChannel()).toString());
		model.addAttribute("IndustryS",listS);
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(merchant.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(merchant.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(merchant.getAccountBankCode(), merchant.getAccountCityCode()));
		
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        model.addAttribute("payChannelTypes", PayChannelType.desc());
        model.addAttribute("certTypes", CertType.desc());
        model.addAttribute("acctTypes", AcctType.desc());
        model.addAttribute("rateUnits", RateUnit.desc());
        model.addAttribute("outChannels", OutChannel.merchAll());
        model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("yesOrNo", YesNoType.desc());
        //银行卡类型
        model.addAttribute("cardTypes", CardType.desc());
        Map<String,Object> map = new HashMap<String,Object>();
		map.put("ifClose", 0);
		map.put("outChannel", "acp");
        model.addAttribute("payConfigCompany", payConfigCompanyService.list(map));
        Map<String,String> pcmap = merchant.getPaidChannel();
        model.addAttribute("pcmap", pcmap);
		Map<String,Map<String,BigDecimal>> rate_t1_map = merchant.gettOne();
        model.addAttribute("rate_t1", rate_t1_map);
        Map<String,Map<String,BigDecimal>> rate_d0_map = merchant.getdZero();
        model.addAttribute("rate_d0", rate_d0_map);
        Map<String,BigDecimal> rate_paid = merchant.getPaid();
        model.addAttribute("rate_paid", rate_paid);
        Map<String,Integer> chanelS = merchant.getChannelSwitch();
        model.addAttribute("chanelS", chanelS);
		if(ParamUtil.isNotEmpty(merchant.getFeeRate())){
			model.addAttribute("feeRates",merchant.getFeeRate());
		}else{
			model.addAttribute("feeRates",merchant.getFeeRate());
		}
		
		String contractEffectiveTime = merchant.getContractEffectiveTime();
        if(ParamUtil.isEmpty(contractEffectiveTime)){
        	
        }else{
        	String[] effectiveTime = contractEffectiveTime.split("~");
        	model.addAttribute("effectiveTime1", effectiveTime[0]);
        	model.addAttribute("effectiveTime2", effectiveTime[1]);
        }
        
        String legalerCardEffectiveTime = merchant.getLegalerCardEffectiveTime();
        if(ParamUtil.isEmpty(legalerCardEffectiveTime)){
        	
        }else{
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }
        String merchantBusinessTerm = merchant.getMerchantBusinessTerm();
        if(ParamUtil.isEmpty(merchantBusinessTerm)){
        	
        }else{
        	String[] businessTerm = merchantBusinessTerm.split("~");
        	model.addAttribute("businessTerm1", businessTerm[0]);
        	model.addAttribute("businessTerm2", businessTerm[1]);
        }*/
	    return "pay/merchant/info";
	}
	
	@GetMapping("/infoQuery")
	@RequiresPermissions("pay:merchant:infoQ")
	String infoQuery(String merNo,Model model){
		UserDO u = ShiroUtils.getUser();
		if(merNo!=null && !"".equals(merNo)) {
			Merchant merchant = merchantService.get(merNo);
			Agent agent = agentService.get(merchant.getParentAgent());
			if(agent.getLevel().equals(AgentLevel.one.id())) {
				if(!u.getUsername().equals(merchant.getParentAgent())) {
					return null;
				}
			}else if(agent.getLevel().equals(AgentLevel.two.id())) {
				if(!u.getUsername().equals(merchant.getParentAgent()) && !u.getUsername().equals(agent.getParentAgent())) {
					return null;
				}
			}else {
				return null;
			}
		}else {
			merNo = u.getUsername();
		}
		model.addAttribute("userType",u.getUserType());
		Merchant merchant = merchantService.getById(merNo);
		model.addAttribute("merchant", merchant);
		model.addAttribute("IndustryP",industryService.listParent(null));
		Map<String,String> agentMap = new HashMap<>();
		agentMap.put("isall", String.valueOf(YesNoType.yes.id()));
		agentService.getAgents(agentMap);
		model.addAttribute("agentNs",agentMap.get(merchant.getParentAgent()));
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("ifClose", 0);
		map.put("outChannel", "acp");
        
        model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("merchant", merchant);
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(merchant.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(merchant.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(merchant.getAccountBankCode(), merchant.getAccountCityCode()));
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        String legalerCardEffectiveTime = merchant.getLegalerCardEffectiveTime();
        if(ParamUtil.isNotEmpty(legalerCardEffectiveTime)){
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }
        String merchantBusinessTerm = merchant.getMerchantBusinessTerm();
        if(ParamUtil.isEmpty(merchantBusinessTerm)){
        	
        }else{
        	String[] businessTerm = merchantBusinessTerm.split("~");
        	model.addAttribute("businessTerm1", businessTerm[0]);
        	model.addAttribute("businessTerm2", businessTerm[1]);
        }
		
		
		Map<String,Map<String,Map<String,BigDecimal>>> rate_d0_map = merchant.getdZero();
		model.addAttribute("rates", rate_d0_map);
		Map<String,BigDecimal> rate_paid = merchant.getPaid();
		model.addAttribute("rate_paid", rate_paid);
		Map<String,Integer> channelSwitch = merchant.getChannelSwitch();
		model.addAttribute("channelSwitch", channelSwitch);
		model.addAttribute("merchant", merchant);
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("status", YesNoType.descStatus());
		model.addAttribute("auditStatus", AuditResult.desc());
		model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
        
        Agent agent = agentService.get(merchant.getParentAgent());
		agent = agentService.getById(agent.getAgentId());
		Map<String,Map<String,Map<String,BigDecimal>>> parentRates = agent.getdZero();
		Map<String,BigDecimal> parentPaid = agent.getPaid();
		model.addAttribute("parentRates", parentRates);
		model.addAttribute("parentPaid", parentPaid);
        
		model.addAttribute("qhPublicKey",QhPayUtil.getQhPublicKey());
	    return "pay/merchant/InfoQuery";
	}
	
	/**
	 * 保存
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("pay:merchant:add")
	public R save(Merchant merchant,@RequestParam String outChannelStr,@RequestParam String paidStr,@RequestParam String tOneStr,@RequestParam String dZeroStr,@RequestParam String paidChannelStr){
		if(merchant.getChannelSwitch() == null){
			merchant.setChannelSwitch(new HashMap<>());
		}
		JSONObject jb = JSONObject.fromObject(paidStr);
		Map<String, BigDecimal> map = (Map<String,BigDecimal>)jb;
		merchant.setPaid(map);
		JSONObject jb1 = JSONObject.fromObject(tOneStr);
		Map<String,Map<String, BigDecimal>> map1 = (Map<String,Map<String, BigDecimal>>)jb1;
		merchant.settOne(map1);;
		JSONObject jb2 = JSONObject.fromObject(dZeroStr);
		Map<String,Map<String, Map<String,BigDecimal>>> map2 = (Map<String,Map<String, Map<String,BigDecimal>>>)jb2;
		merchant.setdZero(map2);
		if(paidChannelStr==null || "".equals(paidChannelStr))
			paidChannelStr = "{\"payMerch\":\"\"}";
		JSONObject jb3 = JSONObject.fromObject(paidChannelStr);
		Map<String, String> map3 = (Map<String,String>)jb3;
		merchant.setPaidChannel(map3);
		JSONObject jb4 = JSONObject.fromObject(outChannelStr);
		Map<String, Integer> map4 = (Map<String,Integer>)jb4;
		merchant.setChannelSwitch(map4);
		merchant.setStatus(0);
		merchant.setAuditStatus(0);
		/*if(merchant.getFeeRate() == null){
			merchant.setFeeRate(new HashMap<>());
		}
		if(merchant.getHandRate() == null){
			merchant.setHandRate(new HashMap<>());
		}*/
		Agent agent = agentService.get(merchant.getParentAgent());
		if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
			return R.error("上级代理异常或不存在!");
		}else {
			if(agent.getLevel().equals(AgentLevel.two.id())) {
				agent = agentService.get(agent.getParentAgent());
				if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
					return R.error("上级代理异常或不存在!");
				}
			}
		}
		int count = merchantService.save(merchant);
		if(count == 1 ){
			return R.ok();
		}else if(count == Constant.data_exist){
			return R.error(merchant.getMerchNo() + "商户已经存在");
		}
		return R.error();
	}
	
	
	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:merchant:edit")
	public R update(Merchant merchant,@RequestParam String paidChannelStr){
		
		JSONObject jb3 = JSONObject.fromObject(paidChannelStr);
		Map<String, String> map3 = (Map<String,String>)jb3;
		merchant.setPaidChannel(map3);
		merchantService.update(merchant);
		return R.ok();
	}
	
	/**
	 * 修改实名
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateRealName")
	@RequiresPermissions("pay:merchant:edit")
	public R update(Merchant merchant){
		
		merchantService.update(merchant);
		return R.ok();
	}
	
	/**
	 * 修改公有密钥,同时保存私钥到缓存
	 */
	@ResponseBody
	@RequestMapping("/updatePKey")
	@RequiresPermissions("pay:merchant:updatePKey")
	public R updatePKey(@RequestParam  Map<String,Object> params){
		Merchant merchantU = new Merchant();
		String merchNo = params.get("merchNo").toString();
		String publicKey=params.get("publicKey").toString();
		String privateKey=params.get("privateKey").toString();
		if(StringUtils.isBlank(merchNo)) {
			UserDO u = ShiroUtils.getUser();
			merchNo = u.getUsername();
		}
		publicKey = publicKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
		merchantU.setPublicKey(publicKey);
		merchantU.setMerchNo(merchNo);
		merchantService.update(merchantU);
		privateKey = privateKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
		RedisUtil.setHashValue(CfgKeyConst.qhPrivateKey,merchNo,privateKey);
		return R.ok();
	}
	
	/**
	 * 修改费率
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateRate")
	@RequiresPermissions("pay:merchant:edit")
	public R update(Merchant merchant,@RequestParam String outChannelStr,@RequestParam String paidStr,@RequestParam String tOneStr,@RequestParam String dZeroStr){
		if(ParamUtil.isEmpty(outChannelStr)){
			merchant.setChannelSwitch(new HashMap<>());
		}else{
			JSONObject jb4 = JSONObject.fromObject(outChannelStr);
			Map<String, Integer> map4 = (Map<String,Integer>)jb4;
			merchant.setChannelSwitch(map4);
		}
		
		JSONObject jb = JSONObject.fromObject(paidStr);
		Map<String, BigDecimal> map = (Map<String,BigDecimal>)jb;
		merchant.setPaid(map);
		JSONObject jb1 = JSONObject.fromObject(tOneStr);
		Map<String,Map<String, BigDecimal>> map1 = (Map<String,Map<String, BigDecimal>>)jb1;
		merchant.settOne(map1);
		JSONObject jb2 = JSONObject.fromObject(dZeroStr);
		Map<String,Map<String, Map<String,BigDecimal>>> map2 = (Map<String,Map<String, Map<String,BigDecimal>>>)jb2;
		merchant.setdZero(map2);;
		merchantService.update(merchant);
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:merchant:remove")
	public R remove(String merchNo){
		if(merchantService.remove(merchNo)>0){
		return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchRemove")
	public R remove(@RequestParam("merchNos[]") String[] merchNos){
		merchantService.batchRemove(merchNos);
		return R.ok();
	}
	
	@PostMapping("/exist")
	@ResponseBody
	boolean exist(@RequestParam("merchNo") String merchNo) {
		// 存在，不通过，false
		return !merchantService.exist(merchNo);
	}



	@GetMapping("/merchantPersonal")
	@RequiresPermissions("pay:merchant:merchant")
	String merchantPersonal(Model model){
		UserDO user = ShiroUtils.getUser();
		Merchant merchant = merchantService.getWithBalance(user.getUsername());
		PayAcctBal payAcctBal = RedisUtil.getMerchBal(user.getUsername());
		merchant.setBalance(payAcctBal.getBalance());
		merchant.setAvailBal(payAcctBal.getAvailBal());
		model.addAttribute("merchant",merchant);
		model.addAttribute("outChannels", OutChannel.merchAll());
		return "pay/merchantPersonal/merchantPersonal";
	}
	
	/**
	 * 启用  禁用代理
	 */
	@PostMapping( "/batchOperate")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchOperate")
	public R batchOperate(@RequestParam("merchantIds[]") Integer[] merchantIds,@RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Merchant merchant = merchantService.get(merchantIds[0]);
			if(merchant.getAuditStatus() == AuditResult.pass.id()) {
				merchantService.batchOperate(flag,merchantIds);
			}else {
				return R.error("请先审核通过该商户资料！");
			}
		}
		return R.ok();
	}
	/**
	 * 审核
	 */
	@PostMapping( "/batchAudit")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchAudit")
	public R batchAudit(@RequestParam("merchantIds[]") Integer[] merchantIds,@RequestParam("flag") boolean flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("auditStatus", flag?1:2);
			map.put("array", merchantIds);
			merchantService.batchAudit(map);

		}
		return R.ok();
	}
	
	/**
	 * 审核
	 */
	@PostMapping( "/batchWithdrawal")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchWithdrawal")
	public R batchWithdrawal(@RequestParam("merchantIds[]") Integer[] merchantIds,@RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("withdrawalStatus", flag);
			map.put("array", merchantIds);
			merchantService.batchWithdrawal(map);
		}
		return R.ok();
	}
	
	/**
	 * 审核
	 */
	@PostMapping( "/batchPaid")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchPaid")
	public R batchPaid(@RequestParam("merchantIds[]") Integer[] merchantIds,@RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("paidStatus", flag);
			map.put("array", merchantIds);
			merchantService.batchPaid(map);
		}
		return R.ok();
	}

	@PostMapping( "/createPrivateKey")
	@RequiresPermissions("pay:merchant:createPrivateKey")
	@ResponseBody
	public List<String> createPrivateKey(){
		List<String> list =new ArrayList<>();
		Map<String, Object>  map=null;
		try {
			 map=RSAUtil.genKeyPair();
			list.add(RSAUtil.getPublicKey(map));
			list.add(RSAUtil.getPrivateKey(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@PostMapping( "/sendEmail")
	@RequiresPermissions("pay:merchant:sendEmail")
	@ResponseBody
	public R sendEmail(@RequestParam("merchNo") String merchNo,Integer state){
		Merchant merchant =merchantService.get(merchNo);

		if(RedisUtil.getHashValue(CfgKeyConst.qhPrivateKey,merchNo)==null||merchant.getPublicKey()==null||"".equals(merchant.getPublicKey())){
			return R.error("请先配置秘钥!");
		}
		if(RedisUtil.getHashValue(CfgKeyConst.email_message,merchNo)!=null&&state==0){
			return  R.error("已发送邮箱");
		}

		String password=RedisUtil.getSysConfigValue(CfgKeyConst.pass_default_merch);
		/*if(password==null||"".equals(password)){
			password = ParamUtil.random(100000,999999)+"";
			merchant.setManagerPass(password);
			merchantService.update(merchant);
		}*/
		String email =merchant.getContactsEmail();
		String privateKey= RedisUtil.getHashValue(CfgKeyConst.qhPrivateKey,merchNo).toString();

		ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "publicKeyPath");
		if(config==null){
			return R.error("请先配置公钥路径！");
		}
		String paydoMain = RedisUtil.getSysConfigValue(CfgKeyConst.pay_domain);
		if(paydoMain==null||"".equals(paydoMain)){
			return R.error("请先配置平台地址！");
		}
		String html =SendMailUtil.getHtml(paydoMain,config.getConfigValue(),privateKey,merchNo,password);
		R r = SendMailUtil.sendEmail(email,html);
		if(R.ifSucc(r)) {
			RedisUtil.setHashValue(CfgKeyConst.email_message,merchNo,html);
			return R.ok();
		}
		return r;
	}


	@GetMapping("/RSAConfig/{merchNo}")
	String RSAConfig(@PathVariable("merchNo") String merchNo,Model model){
		Merchant merchant =merchantService.get(merchNo);
		model.addAttribute("merchant",merchant);
		return "pay/merchant/RSAConfig";
	}



}
