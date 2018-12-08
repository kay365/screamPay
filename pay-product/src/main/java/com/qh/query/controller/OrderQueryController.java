package com.qh.query.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qh.common.utils.R;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.Merchant;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.ShiroUtils;
import com.qh.moneyacct.querydao.AgentDao;
import com.qh.moneyacct.querydao.MerchantDao;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.ClearState;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OrderType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.MerchantService;
import com.qh.query.service.OrderQueryService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;

/**
 * @ClassName OrderQueryController
 * @Description 订单查询
 * @Date 2017年11月16日 下午2:28:26
 * @version 1.0.0
 */
@Controller
@RequestMapping("/orderQuery")
public class OrderQueryController {
	@Autowired
	private OrderQueryService orderQueryService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
    private AgentDao agentDao;
    @Autowired
    private MerchantDao merchantDao;
	/***
	 * 
	 * @Description 在途订单
	 * @param model
	 * @return
	 */
	@GetMapping("/order")
	@RequiresPermissions("orderQuery:order")
	String order(Model model){
		model.addAttribute("outChannels", OutChannel.all());
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("orderStates", OrderState.desc());
		model.addAttribute("clearStates",ClearState.desc());
		model.addAttribute("clearStates", ClearState.desc());
		model.addAttribute("jfOutChannels", OutChannel.jfDesc());
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		UserDO user = ShiroUtils.getUser();
       /* if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("outChannels", OutChannel.merchAll());
        	model.addAttribute("payCompanys", PayCompany.jfDesc());
        }else{
        	model.addAttribute("merchNos", merchantService.getAllMerchNos());
        }*/
        String userName = user.getUsername();
        Integer userType = user.getUserType();
        if(UserType.merch.id() == user.getUserType()){
            model.addAttribute("merchNo", userName);
        }else if(UserType.subAgent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.agent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
            model.addAttribute("user", user);
        }
	    return "pay/orderQuery/order";
	}
	
	@ResponseBody
	@GetMapping("/order/list")
	@RequiresPermissions("orderQuery:order")
	public PageUtils list(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		List<Object> orders = new ArrayList<Object>();
		String orderNo = (String) params.get("orderNo");
		UserDO user = ShiroUtils.getUser();
		
		int beginDateInt =  DateUtil.getBeginTimeIntZero(beginDate);
		int endDateInt = DateUtil.getEndTimeIntLast(endDate);
		
		String merchNo = (String) params.get("merchNo");
		
        if(UserType.merch.id() == user.getUserType()){
        	merchNo = user.getUsername();
        }
    	int total = 0;
        if(ParamUtil.isNotEmpty(orderNo)){
        	Order order = null;
        	if(ParamUtil.isNotEmpty(merchNo)) {
        		orderNo = orderNo.replaceAll(merchNo, "");
        		order = RedisUtil.getOrder(merchNo, orderNo);
        	}else {
	        	List<Object> merchants = RedisUtil.getHashValueList(RedisConstants.cache_merchant);
	        	for (Object object : merchants) {
					Merchant merchant = (Merchant)object;
					orderNo = orderNo.replaceAll(merchant.getMerchNo(), "");
					order = RedisUtil.getOrder(merchant.getMerchNo(), orderNo);
					if(order!=null) break;
				}
        	}
    		if(order != null && order.getCrtDate() >= beginDateInt && order.getCrtDate() <= endDateInt){
    			orders.add(order);
    			total = 1;
    		}		
    	}else{
    		Query query = new Query(params);
    		orders = orderQueryService.getOrders(merchNo,beginDateInt,endDateInt,query);
    		total = orderQueryService.getOrdersCount(merchNo,beginDateInt,endDateInt);
    	}
        
        if(CollectionUtils.isNotEmpty(orders)){
        	for (Object obj : orders) {
        		Order order = (Order)obj;
        		Merchant merchant = merchantService.get(order.getMerchNo());
        		order.setMerchName(merchant.getMerchantsShortName());
        		if(UserType.merch.id() == user.getUserType()) {
	    			order.setCostAmount(null);
	    			order.setAgentAmount(null);
	    			order.setPayCompany(null);
	    			order.setPayMerch(null);
        		}
    		}
        }
		PageUtils pageUtils = new PageUtils(orders, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/order/list/footer")
	@RequiresPermissions("orderQuery:order")
	public R orderFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		List<Object> orders = new ArrayList<Object>();
		String orderNo = (String) params.get("orderNo");
		UserDO user = ShiroUtils.getUser();

		int beginDateInt =  DateUtil.getBeginTimeIntZero(beginDate);
		int endDateInt = DateUtil.getEndTimeIntLast(endDate);

		String merchNo = (String) params.get("merchNo");

		if(UserType.merch.id() == user.getUserType()){
			merchNo = user.getUsername();
		}
		if(ParamUtil.isNotEmpty(orderNo)){
			Order order = RedisUtil.getOrder(merchNo, orderNo);
			if(order != null && order.getCrtDate() >= beginDateInt && order.getCrtDate() <= endDateInt){
				orders.add(order);
			}
		}else{
			orders = orderQueryService.getOrdersFooter(merchNo,beginDateInt,endDateInt,params);
		}

		FooterDO fdo;
		fdo = getOrderFooterSum(orders);
		if(UserType.merch.id() == user.getUserType() && CollectionUtils.isNotEmpty(orders)){
			fdo.setCostAmount(new BigDecimal(0));
			fdo.setAgentAmount(new BigDecimal(0));
		}
		return R.okData(fdo);
	}

	private FooterDO getOrderFooterSum(List<Object> orders){
		FooterDO fdo = new FooterDO();
		BigDecimal amount = new BigDecimal(0);
		BigDecimal realAmount = new BigDecimal(0);
		BigDecimal costAmount = new BigDecimal(0);
		BigDecimal qhAmount = new BigDecimal(0);
		BigDecimal agentAmount = new BigDecimal(0);

		for(Object obj:orders){
			Order order = (Order)obj;
			amount = amount.add(ParamUtil.isEmpty(order.getAmount())?new BigDecimal(0):order.getAmount());
			realAmount = realAmount.add(ParamUtil.isEmpty(order.getRealAmount())?new BigDecimal(0):order.getRealAmount());
			costAmount = costAmount.add(ParamUtil.isEmpty(order.getCostAmount())?new BigDecimal(0):order.getCostAmount());
			qhAmount = qhAmount.add(ParamUtil.isEmpty(order.getQhAmount())?new BigDecimal(0):order.getQhAmount());
			agentAmount = agentAmount.add(ParamUtil.isEmpty(order.getAgentAmount())?new BigDecimal(0):order.getAgentAmount());
		}
		fdo.setAmount(amount);
		fdo.setRealAmount(realAmount);
		fdo.setCostAmount(costAmount);
		fdo.setQhAmount(qhAmount);
		fdo.setAgentAmount(agentAmount);
		return fdo;
	}

	
	/***
	 * 
	 * @Description 历史支付订单
	 * @param model
	 * @return
	 */
	@GetMapping("/orderHis")
	@RequiresPermissions("orderQuery:orderHis")
	String orderHis(Model model){
		model.addAttribute("outChannels", OutChannel.desc());
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("orderStates", OrderState.desc());
		model.addAttribute("clearStates", ClearState.desc());
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		UserDO user = ShiroUtils.getUser();
		/*if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("outChannels", OutChannel.merchAll());
        	model.addAttribute("payCompanys", PayCompany.jfDesc());
        }else{
        	model.addAttribute("merchNos", merchantService.getAllMerchNos());
        }*/
		String userName = user.getUsername();
        Integer userType = user.getUserType();
        if(UserType.merch.id() == user.getUserType()){
            model.addAttribute("merchNo", userName);
        }else if(UserType.subAgent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.agent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
            model.addAttribute("user", user);
        }
	    return "pay/orderQuery/order_his";
	}
	
	public static void main(String[] args) {
		String orderNo = "BFBSH346432bfbtx111112018041600000026";
		Pattern pattern = Pattern.compile("[a-zA-Z]{1,4}(DL|SH)[0-9]{6}");
		Matcher matcher = pattern.matcher(orderNo); 
		System.out.println(matcher.matches());
		 if(matcher.find()){ 
			 int end = matcher.end();
			 String merNo = matcher.group(0);
			 String orderId = orderNo.substring(end);
			 System.out.println(merNo+"_"+orderId);
		 }
		String[] s = orderNo.split("[a-zA-Z]{1,4}(DL|SH)[0-9]{6}");
		System.out.println(s[1]);
		orderNo = orderNo.replaceFirst("[a-zA-Z]{1,4}(DL|SH)[0-9]{6}", "");
	}
	
	@ResponseBody
	@GetMapping("/orderHis/list")
	@RequiresPermissions("orderQuery:orderHis")
	public PageUtils orderHisList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		String orderNo = (String) params.get("orderNo");
		if(ParamUtil.isNotEmpty(orderNo)){
			orderNo = orderNo.replaceFirst("[a-zA-Z]{1,4}(DL|SH)[0-9]{6}", "");
			params.put("orderNo", orderNo);
		}
		
		List<Order> orders;
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(ShiroUtils.ifMerch(user)){
        	query.put("merchNo", user.getUsername());
        }
        orders = orderQueryService.list(query);
    	for (Order order : orders) {
    		Merchant merchant = merchantService.get(order.getMerchNo());
    		order.setMerchName(merchant.getMerchantsShortName());
    		if(ShiroUtils.ifMerch(user)){
				order.setCostAmount(null);
				order.setAgentAmount(null);
				order.setPayCompany(null);
				order.setPayMerch(null);
    		}
		}
        
		int total = orderQueryService.count(query);
		PageUtils pageUtils = new PageUtils(orders, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/orderHis/list/footer")
	@RequiresPermissions("orderQuery:orderHis")
	public R orderHisListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}

		FooterDO fdo;
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user)){
			params.put("merchNo", user.getUsername());
			fdo = orderQueryService.listFooter(params);
			if(ParamUtil.isNotEmpty(fdo)){
				fdo.setCostAmount(new BigDecimal(0));
				fdo.setAgentAmount(new BigDecimal(0));
			}
		}else{
			fdo = orderQueryService.listFooter(params);
		}
		return R.okData(fdo);
	}


	
	/***
	 * 
	 * @Description 在途代付
	 * @param model
	 * @return
	 */
	@GetMapping("/orderAcp")
	@RequiresPermissions("orderQuery:orderAcp")
	String orderAcp(Model model){
		model.addAttribute("outChannels", OutChannel.all());
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("orderStates", OrderState.desc());
		model.addAttribute("clearStates", ClearState.desc());
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("clearStates", ClearState.desc());
		model.addAttribute("jfOutChannels", OutChannel.jfDesc());
		UserDO user = ShiroUtils.getUser();
		/*if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("outChannels", OutChannel.merchAll());
        	model.addAttribute("payCompanys", PayCompany.jfDesc());
        }else{
        	model.addAttribute("merchNos", merchantService.getAllMerchNos());
        }*/
		String userName = user.getUsername();
        Integer userType = user.getUserType();
        if(UserType.merch.id() == user.getUserType()){
            model.addAttribute("merchNo", userName);
        }else if(UserType.subAgent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.agent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
            model.addAttribute("user", user);
        }
	    return "pay/orderQuery/orderAcp";
	}
	
	@ResponseBody
	@GetMapping("/orderAcp/list")
	@RequiresPermissions("orderQuery:orderAcp")
	public PageUtils orderAcpList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		List<Object> orders = new ArrayList<Object>();
		String orderNo = (String) params.get("orderNo");
		UserDO user = ShiroUtils.getUser();
		
		int beginDateInt =  DateUtil.getBeginTimeIntZero(beginDate);
		int endDateInt = DateUtil.getEndTimeIntLast(endDate);
		
		String merchNo = (String) params.get("merchNo");
		
        if(UserType.merch.id() == user.getUserType()){
        	merchNo = user.getUsername();
        }
    	int total = 0;
        if(ParamUtil.isNotEmpty(orderNo)){
        	
        	Order order = null;
        	if(ParamUtil.isNotEmpty(merchNo)) {
        		orderNo = orderNo.replaceAll(merchNo, "");
        		order = RedisUtil.getOrderAcp(merchNo, orderNo);
        	}else {
        		List<Object> merchants = RedisUtil.getHashValueList(RedisConstants.cache_merchant);
	        	for (Object object : merchants) {
					Merchant merchant = (Merchant)object;
					orderNo = orderNo.replaceAll(merchant.getMerchNo(), "");
					order = RedisUtil.getOrderAcp(merchant.getMerchNo(), orderNo);
					if(order!=null) break;
				}
	        	if(order==null)order = RedisUtil.getOrderAcp("admin", orderNo);
        	}
        	
    		if(order != null && order.getCrtDate() >= beginDateInt && order.getCrtDate() <= endDateInt){
    			orders.add(order);
    			total = 1;
    		}		
    	}else{
    		Query query = new Query(params);
    		orders = orderQueryService.getAcpOrders(merchNo,beginDateInt,endDateInt,query);
    		total = orderQueryService.getAcpOrdersCount(merchNo,beginDateInt,endDateInt);
    	}
        
        if(CollectionUtils.isNotEmpty(orders)){
        	for (Object obj : orders) {
        		Order order = (Order)obj;
        		Merchant merchant = merchantService.get(order.getMerchNo());
        		order.setMerchName(merchant==null?"":merchant.getMerchantsShortName());
        		if(UserType.merch.id() == user.getUserType()) {
	    			order.setCostAmount(null);
	    			order.setAgentAmount(null);
	    			order.setPayCompany(null);
	    			order.setPayMerch(null);
        		}
    		}
        }
		PageUtils pageUtils = new PageUtils(orders, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/orderAcp/list/footer")
	@RequiresPermissions("orderQuery:orderAcp")
	public R orderAcpFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		List<Object> orders = new ArrayList<Object>();
		String orderNo = (String) params.get("orderNo");
		UserDO user = ShiroUtils.getUser();

		int beginDateInt =  DateUtil.getBeginTimeIntZero(beginDate);
		int endDateInt = DateUtil.getEndTimeIntLast(endDate);

		String merchNo = (String) params.get("merchNo");

		if(UserType.merch.id() == user.getUserType()){
			merchNo = user.getUsername();
		}
		if(ParamUtil.isNotEmpty(orderNo)){
			Order order = RedisUtil.getOrder(merchNo, orderNo);
			if(order != null && order.getCrtDate() >= beginDateInt && order.getCrtDate() <= endDateInt){
				orders.add(order);
			}
		}else{
			orders = orderQueryService.getAcpOrdersFooter(merchNo,beginDateInt,endDateInt,params);
		}
		FooterDO fdo;
		fdo = getOrderFooterSum(orders);
		if(UserType.merch.id() == user.getUserType() && CollectionUtils.isNotEmpty(orders) && ParamUtil.isNotEmpty(fdo)){
			fdo.setCostAmount(new BigDecimal(0));
			fdo.setAgentAmount(new BigDecimal(0));
		}
		return R.okData(fdo);
	}

	
	/***
	 * 
	 * @Description 历史代付订单
	 * @param model
	 * @return
	 */
	@GetMapping("/orderAcpHis")
	@RequiresPermissions("orderQuery:orderAcpHis")
	String orderAcpHis(Model model){
		model.addAttribute("outChannels", OutChannel.desc());
		model.addAttribute("payCompanys", PayCompany.desc());
		model.addAttribute("orderStates", OrderState.desc());
		model.addAttribute("clearStates", ClearState.desc());
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("clearStates", ClearState.desc());
		UserDO user = ShiroUtils.getUser();
        /*if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        	model.addAttribute("outChannels", OutChannel.merchAll());
        	model.addAttribute("payCompanys", PayCompany.jfDesc());
        }else{
        	model.addAttribute("merchNos", merchantService.getAllMerchNos());
        }*/
		String userName = user.getUsername();
        Integer userType = user.getUserType();
        if(UserType.merch.id() == user.getUserType()){
            model.addAttribute("merchNo", userName);
        }else if(UserType.subAgent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.agent.id() == userType) {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
            model.addAttribute("user", user);
        }
	    return "pay/orderQuery/orderAcp_his";
	}
	
	@ResponseBody
	@GetMapping("/orderAcpHis/list")
	@RequiresPermissions("orderQuery:orderAcpHis")
	public PageUtils orderAcpHis(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		String orderNo = (String) params.get("orderNo");
		if(ParamUtil.isNotEmpty(orderNo)){
			orderNo = orderNo.replaceFirst("[a-zA-Z]{1,4}(DL|SH)[0-9]{6}", "");
			params.put("orderNo", orderNo);
		}
		List<Order> orders;
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(ShiroUtils.ifMerch(user)){
        	query.put("merchNo", user.getUsername());
        	
        }
        orders = orderQueryService.listAcp(query);
    	for (Order order : orders) {
    		Merchant merchant = merchantService.get(order.getMerchNo());
			order.setMerchName(merchant==null?"":merchant.getMerchantsShortName());
    		if(ShiroUtils.ifMerch(user)){
				order.setCostAmount(null);
				order.setAgentAmount(null);
				order.setPayCompany(null);
				order.setPayMerch(null);
    		}
		}
       
		int total = orderQueryService.countAcp(query);
		PageUtils pageUtils = new PageUtils(orders, total);
		return pageUtils;
	}

	@ResponseBody
	@PostMapping("/orderAcpHis/list/footer")
	@RequiresPermissions("orderQuery:orderAcpHis")
	public R orderAcpHisListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}

		FooterDO fdo;
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user)){
			params.put("merchNo", user.getUsername());
			fdo = orderQueryService.listAcpFooter(params);
			if(ParamUtil.isNotEmpty(fdo)){
				fdo.setCostAmount(new BigDecimal(0));
				fdo.setAgentAmount(new BigDecimal(0));
			}
		}else{
			fdo = orderQueryService.listAcpFooter(params);
		}

		return R.okData(fdo);
	}


	/***
	 * 
	 * @Description 掉单页面
	 * @param model
	 * @return
	 */
	@GetMapping("/orderLose")
	@RequiresPermissions("orderQuery:orderLose")
	String orderLose(Model model){
		model.addAttribute("outChannels", OutChannel.jfDesc());
		model.addAttribute("payCompanys", PayCompany.jfDesc());
		model.addAttribute("orderStates", OrderState.desc());
		model.addAttribute("clearStates", ClearState.desc());
		UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() == user.getUserType()){
        	model.addAttribute("merchNo", user.getUsername());
        }else{
        	model.addAttribute("merchNos", merchantService.getAllMerchNos());
        }
	    return "pay/orderQuery/orderLose";
	}
	
	@ResponseBody
	@GetMapping("/orderLose/list")
	@RequiresPermissions("orderQuery:orderLose")
	public PageUtils orderLoseList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
		if(ParamUtil.isNotEmpty(beginDate)){
			params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
		}
		if(ParamUtil.isNotEmpty(endDate)){
			params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
		}
		
		List<Order> orders;
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
        if(ShiroUtils.ifMerch(user)){
        	query.put("merchNo", user.getUsername());
        	orders = orderQueryService.listLose(query);
        	for (Order order : orders) {
				order.setCostAmount(null);
				order.setAgentAmount(null);
				order.setPayCompany(null);
				order.setPayMerch(null);
			}
        }else{
        	orders = orderQueryService.listLose(query);
        }
		int total = orderQueryService.countLose(query);
		PageUtils pageUtils = new PageUtils(orders, total);
		return pageUtils;
	}
}
