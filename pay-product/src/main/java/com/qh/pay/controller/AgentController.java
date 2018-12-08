package com.qh.pay.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.api.constenum.AuditResult;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.CertType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.api.constenum.RateUnit;
import com.qh.pay.api.constenum.UserRole;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.IndustryDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.IndustryService;
import com.qh.pay.service.MerchantService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.RoleDO;
import com.qh.system.domain.UserDO;
import com.qh.system.service.RoleService;
import com.qh.system.service.UserService;

import net.sf.json.JSONObject;

import com.qh.common.service.LocationService;
import com.qh.common.service.UnionPayService;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;

/**
 * 
 * 
 * @date 2018-02-24 17:25:59
 */
 
@Controller
@RequestMapping("/pay/agent")
public class AgentController {
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private AgentService agentService;
	@Autowired
	private IndustryService industryService;
	@Autowired
	private UnionPayService unionPayService;
	@Autowired
	private LocationService locationService;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	
	@GetMapping()
	@RequiresPermissions("pay:agent:agent")
	String Agent(Model model){
		model.addAttribute("auditStatus", AuditResult.desc());
		model.addAttribute("auditStatusColor", AuditResult.descColor());
		model.addAttribute("merTypes", AcctType.descMer());
		model.addAttribute("status", YesNoType.descStatus());
	    return "pay/agent/agent";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:agent:agent")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
		UserDO u = ShiroUtils.getUser();
		if(u.getUserType() == UserType.agent.id()){
			params.put("pAgent", u.getUsername());
		}else {
			if(u.getUserType() != UserType.user.id()) {
				return new PageUtils(null,0);
			}
		}
        Query query = new Query(params);
		List<Agent> agentList = agentService.listAgent(query);
		for (Agent agent : agentList) {
			String parentAgentNumber = agent.getParentAgent();
			if(StringUtils.isNotBlank(parentAgentNumber)) {
				Agent parentAgent = agentService.get(parentAgentNumber);
			agent.setParentAgent(parentAgent.getMerchantsName());
			}
		}
		int total = agentService.count(query);
		PageUtils pageUtils = new PageUtils(agentList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:agent:add")
	String add(Model model){
		/*if(UserType.merch.id() != user.getUserType()){
        	model.addAttribute("msg", "不可提现");
        	return PayConstants.url_pay_error_frame;
        }*/
		Map<String,String> map = new HashMap<>();
		agentService.getAgents(map);
		model.addAttribute("agentNs",map);
		model.addAttribute("agentTypes",AcctType.descMer());
		model.addAttribute("IndustryP",industryService.listParent(null));
		model.addAttribute("provinces", locationService.listProvinces());
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        model.addAttribute("certTypes", CertType.desc());
        model.addAttribute("acctTypes", AcctType.desc());
        model.addAttribute("rateUnits", RateUnit.desc());
        //银行卡类型
        model.addAttribute("cardTypes", CardType.desc());
        model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
	    return "pay/agent/add";
	}

	@GetMapping("/edit/{agentId}")
	@RequiresPermissions("pay:agent:edit")
	String edit(@PathVariable("agentId") Integer agentId,Model model){
		Agent agent = agentService.getById(agentId);
		model.addAttribute("agent", agent);
		Map<String,String> map = new HashMap<>();
		agentService.getAgents(map);
		model.addAttribute("agentNs",map);
		model.addAttribute("agentTypes",AcctType.descMer());
		model.addAttribute("IndustryP",industryService.listParent(null));
		/*Map<String,Object> map1 = new HashMap<String,Object>();
		map1.put("parentid", agent.getMerchantsIndustryCode());
		List<IndustryDO> listS = industryService.listSub(map1);
		
		model.addAttribute("IndustryS",listS);
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(agent.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(agent.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(agent.getAccountBankCode(), agent.getAccountCityCode()));
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        model.addAttribute("certTypes", CertType.desc());
        model.addAttribute("acctTypes", AcctType.desc());
        model.addAttribute("rateUnits", RateUnit.desc());
        //银行卡类型
        model.addAttribute("cardTypes", CardType.desc());
        
        Map<String,Map<String,BigDecimal>> rate_t1_map = agent.gettOne();
        model.addAttribute("rate_t1", rate_t1_map);
        Map<String,Map<String,BigDecimal>> rate_d0_map = agent.getdZero();
        model.addAttribute("rate_d0", rate_d0_map);
        Map<String,BigDecimal> rate_paid = agent.getPaid();
        model.addAttribute("rate_paid", rate_paid);
        
        String contractEffectiveTime = agent.getContractEffectiveTime();
        if(ParamUtil.isEmpty(contractEffectiveTime)){
        	
        }else{
        	String[] effectiveTime = contractEffectiveTime.split("~");
        	model.addAttribute("effectiveTime1", effectiveTime[0]);
        	model.addAttribute("effectiveTime2", effectiveTime[1]);
        }
        
        String legalerCardEffectiveTime = agent.getLegalerCardEffectiveTime();
        if(ParamUtil.isEmpty(legalerCardEffectiveTime)){
        	
        }else{
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }*/
	    return "pay/agent/info";
	}
	
	@GetMapping("/agentInfo/{agentId}")
	@RequiresPermissions("pay:agent:edit")
	String agentInfo(@PathVariable("agentId") Integer agentId,Model model){
		Agent agent = agentService.getById(agentId);
		model.addAttribute("agent", agent);
		model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(agent.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(agent.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(agent.getAccountBankCode(), agent.getAccountCityCode()));
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        String legalerCardEffectiveTime = agent.getLegalerCardEffectiveTime();
        if(ParamUtil.isNotEmpty(legalerCardEffectiveTime)){
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }
		return "pay/agent/realName";
	}
	
	@GetMapping("/rateInfo/{agentId}")
	@RequiresPermissions("pay:agent:edit")
	String rateInfo(@PathVariable("agentId") Integer agentId,Model model){
		Agent agent = agentService.getById(agentId);
		Map<String,Map<String,Map<String,BigDecimal>>> rate_map = agent.getdZero();
		model.addAttribute("rates", rate_map);
		Map<String,BigDecimal> rate_paid = agent.getPaid();
		model.addAttribute("rate_paid", rate_paid);
		model.addAttribute("agent", agent);
		model.addAttribute("rateUnits", RateUnit.desc());
        model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
        if(agent.getLevel().equals(AgentLevel.two.id())) {
	        Agent parentAgent = agentService.get(agent.getParentAgent());
	        parentAgent = agentService.getById(parentAgent.getAgentId());
			Map<String,Map<String,Map<String,BigDecimal>>> parentRates = parentAgent.getdZero();
			Map<String,BigDecimal> parentPaid = parentAgent.getPaid();
			model.addAttribute("parentRates", parentRates);
			model.addAttribute("parentPaid", parentPaid);
        }
		return "pay/agent/rate";
	}
	
	@GetMapping("/infoQuery")
	@RequiresPermissions("pay:agent:infoQuery")
	String infoQuery(String agentNo,Model model){
		UserDO u = ShiroUtils.getUser();
		Integer agentId = null;
		if(agentNo!=null && !"".equals(agentNo)) {
			Agent agent = agentService.get(agentNo);
			if(u.getUsername().equals(agent.getParentAgent())) {
				agentId = agent.getAgentId();
			}else {
				return null;
			}
		}else {
			Agent agent = agentService.get(u.getUsername());
			agentId = agent.getAgentId();
		}
		Agent agent = agentService.getById(agentId);
		model.addAttribute("agent", agent);
		Map<String,String> map = new HashMap<>();
		agentService.getAgents(map);
		model.addAttribute("agentName",map.get(agent.getParentAgent()));
		model.addAttribute("agentTypes",AcctType.descMer());
		model.addAttribute("IndustryP",industryService.listParent(null));
		
		model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("provinces", locationService.listProvinces());
		model.addAttribute("accountcitys", locationService.listCitysByProvinceId(agent.getAccountProvinceCode()));
		model.addAttribute("citys", locationService.listCitysByProvinceId(agent.getProvinceCode()));
		model.addAttribute("bankBranch", unionPayService.listByBankAndCity(agent.getAccountBankCode(), agent.getAccountCityCode()));
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        String legalerCardEffectiveTime = agent.getLegalerCardEffectiveTime();
        if(ParamUtil.isNotEmpty(legalerCardEffectiveTime)){
        	String[] cardEffectiveTime = legalerCardEffectiveTime.split("~");
        	model.addAttribute("cardEffectiveTime1", cardEffectiveTime[0]);
        	model.addAttribute("cardEffectiveTime2", cardEffectiveTime[1]);
        }
		
		Map<String,Map<String,Map<String,BigDecimal>>> rate_map = agent.getdZero();
		model.addAttribute("rates", rate_map);
		Map<String,BigDecimal> rate_paid = agent.getPaid();
		model.addAttribute("rate_paid", rate_paid);
		model.addAttribute("rateUnits", RateUnit.desc());
        model.addAttribute("paymentMethods", PaymentMethod.desc());
        model.addAttribute("outChannels", OutChannel.desc());
        
        model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("status", YesNoType.descStatus());
		model.addAttribute("auditStatus", AuditResult.desc());
	    return "pay/agent/InfoQuery";
	}
	
	
	//代理商费率
	@ResponseBody
	@RequestMapping("/getRate/{agentId}")
	public R getRate(@PathVariable("agentId") String agentId){
		Agent agent = agentService.get(agentId);
		agent = agentService.getById(agent.getAgentId());
		Map<String,Map<String,Map<String,BigDecimal>>> rate_map = agent.getdZero();
		Map<String,BigDecimal> rate_paid = agent.getPaid();
		Map<String,Object> map = new HashMap<>();
		map.put("rate",rate_map);
		map.put("paid",rate_paid);
		return R.okData(map);
	}
	
	/**
	 * 保存
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:agent:add")
	public R save( Agent agent,@RequestParam String tOneStr,@RequestParam String dZeroStr,@RequestParam String paidStr){
		String agentNumber = QhPayUtil.getAgentNoPrefix()+ParamUtil.generateCode6();
		while(agentService.exist(agentNumber)){
			agentNumber = QhPayUtil.getAgentNoPrefix()+ParamUtil.generateCode6();
		}
		
		JSONObject jb = JSONObject.fromObject(tOneStr);
		Map<String, Map<String,BigDecimal>> map = (Map<String,Map<String,BigDecimal>>)jb;
		agent.settOne(map);
		JSONObject jb1 = JSONObject.fromObject(dZeroStr);
		Map<String, Map<String,Map<String,BigDecimal>>> map1 = (Map<String,Map<String,Map<String,BigDecimal>>>)jb1;
		agent.setdZero(map1);
		JSONObject jb2 = JSONObject.fromObject(paidStr);
		Map<String,BigDecimal> map2 = (Map<String,BigDecimal>)jb2;
		agent.setPaid(map2);
		
		Date d = new Date();
		agent.setAgentNumber(agentNumber);
		agent.setManagerPass(Md5Util.MD5(agent.getManagerPass()));
		agent.setCreateTime(d);
		agent.setModifyTime(d);
		agent.setStatus(0);
		agent.setAuditStatus(0);
		UserDO u = ShiroUtils.getUser();
		
		UserDO user = new UserDO();
//		List<RoleDO> list = roleService.list();
		List<Integer> roleIds = new ArrayList<Integer>();
		/*for(int i=0;i<list.size();i++){
			if(list.get(i).getRoleId().equals(UserRole.agent.id())){
				roleIds.add(list.get(i).getRoleId());
			}
		}*/
		if(ParamUtil.isEmpty(agent.getParentAgent())){
			agent.setLevel(AgentLevel.one.id());
			user.setUserType(UserType.agent.id());
			roleIds.add(UserRole.agent.id());
		}else{
			agent.setLevel(AgentLevel.two.id());
			user.setUserType(UserType.subAgent.id());
			roleIds.add(UserRole.subAgent.id());
			Agent parentAgent = agentService.get(agent.getParentAgent());
			if(parentAgent==null || !parentAgent.getStatus().equals(YesNoType.yes.id())) {
				return R.error("上级代理异常或不存在!");
			}
		}
		user.setRoleIds(roleIds);
		
		user.setMobile(agent.getManagerPhone());
		user.setUsername(agent.getAgentNumber());
		user.setName(agent.getManagerName());
		user.setPassword(agent.getManagerPass());
		user.setEmail(agent.getContactsEmail());
		user.setStatus(1);
		user.setUserIdCreate(u.getUserId());
		user.setGmtCreate(d);
		
		if(agentService.save(agent)>0){
			userService.save(user);
			redisTemplate.opsForHash().put(RedisConstants.cache_agent, agent.getAgentNumber(), agent);
			return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateRate")
	@RequiresPermissions("pay:agent:edit")
	public R updateRate( Agent agent,@RequestParam String tOneStr,@RequestParam String dZeroStr,@RequestParam String paidStr){
		JSONObject jb = JSONObject.fromObject(tOneStr);
		Map<String, Map<String,BigDecimal>> map = (Map<String,Map<String,BigDecimal>>)jb;
		agent.settOne(map);
		JSONObject jb1 = JSONObject.fromObject(dZeroStr);
		Map<String, Map<String,Map<String,BigDecimal>>> map1 = (Map<String,Map<String,Map<String,BigDecimal>>>)jb1;
		agent.setdZero(map1);
		JSONObject jb2 = JSONObject.fromObject(paidStr);
		Map<String,BigDecimal> map2 = (Map<String,BigDecimal>)jb2;
		agent.setPaid(map2);
		
		Date d = new Date();
		
		agent.setModifyTime(d);
		agentService.update(agent);
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:agent:edit")
	public R update( Agent agent){
		
		Date d = new Date();
		agent.setModifyTime(d);
		agentService.update(agent);
		return R.ok();
	}
	//获取子行业
	@ResponseBody
	@RequestMapping("/getSubs")
	public R getSubs(String industryId){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("parentid", industryId);
		List<IndustryDO> list = industryService.listSub(map);
		return R.okData(list);
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:agent:remove")
	public R remove( Integer agentId){
		if(agentService.remove(agentId)>0){
		return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchRemove")
	public R remove(@RequestParam("ids[]") Integer[] agentIds){
		agentService.batchRemove(agentIds);
		return R.ok();
	}
	/**
	 * 启用  禁用代理
	 */
	@PostMapping( "/batchOperate")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchOperate")
	public R batchOperate(@RequestParam("agentIds[]") Integer[] agentIds,@RequestParam("flag") String flag){
		if(agentIds.length == 1) {
			Agent agent = agentService.get(agentIds[0]);
			if(agent.getAuditStatus() == AuditResult.pass.id()) {
				agentService.batchOperate(flag,agentIds);
			}else {
				return R.error("请先审核通过该商户资料！");
			}
		}
		return R.ok();
	}
	
	/**
	 * 审核代理
	 */
	@PostMapping( "/batchAudit")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchAudit")
	public R batchAudit(@RequestParam("agentIds[]") Integer[] agentIds,@RequestParam("flag") boolean flag){
		
		if(agentIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("auditStatus", flag?1:2);
			map.put("array", agentIds);
			agentService.batchAudit(map);
		}
		return R.ok();
	}
	
}
