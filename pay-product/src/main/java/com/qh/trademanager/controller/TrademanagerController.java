package com.qh.trademanager.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
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
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.system.domain.UserDO;
import com.qh.trademanager.querydao.TrademanagerQueryDao;

@Controller
@RequestMapping("/trademanager")
public class TrademanagerController {
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private MerchantDao merchantDao;
    @Autowired
    private TrademanagerQueryDao tradeManaQueryDao;
    @Autowired
	private MerchantService merchantService;
    @Autowired
    private AgentService agentService;
    //交易
    @GetMapping("/pay")
    @RequiresPermissions("trademanager:pay")
    public String pay(Model model){
        this.pageQueryInit(model,OrderType.pay.id());
        return "trademanager/pay";
    }
    //充值
    @GetMapping("/charge")
    @RequiresPermissions("trademanager:charge")
    public String charge(Model model){
        this.pageQueryInit(model,OrderType.charge.id());
        return "trademanager/charge";
    }
    //代付
    @GetMapping("/acp")
    @RequiresPermissions("trademanager:acp")
    public String acp(Model model){
        this.pageQueryInit(model,OrderType.acp.id());
        return "trademanager/acp";
    }
    //提现
    @GetMapping("/withdraw")
    @RequiresPermissions("trademanager:withdraw")
    public String withdraw(Model model){
        this.pageQueryInit(model,OrderType.withdraw.id());
        return "trademanager/withdraw";
    }
    private void pageQueryInit(Model model,Integer orderType) {
        model.addAttribute("outChannels", OutChannel.desc());
        model.addAttribute("payCompanys", PayCompany.desc());
        model.addAttribute("orderStatesSimple", OrderState.simple());
        model.addAttribute("orderStates", OrderState.desc());
        model.addAttribute("clearStates", ClearState.desc());
        model.addAttribute("cardTypes", CardType.desc());
        model.addAttribute("acctTypes", AcctType.desc());
        model.addAttribute("orderTypes", OrderType.desc());
        model.addAttribute("userTypes", UserType.desc());
        model.addAttribute("noticeStates", YesNoType.noticeStatus());
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        if(UserType.merch.id() == user.getUserType()){
            model.addAttribute("merchNo", userName);
        }else if(UserType.subAgent.id() == userType) {
            if(orderType == OrderType.charge.id() || orderType == OrderType.withdraw.id()) {
                model.addAttribute("merchNo", userName);
            }else {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
            }
        }else if(UserType.agent.id() == userType) {
            if(orderType == OrderType.charge.id() || orderType == OrderType.withdraw.id()) {
                model.addAttribute("merchNo", userName);
            }else {
                model.addAttribute("agentNumber", user.getUsername());
                model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
                model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
            }
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
            model.addAttribute("user", user);
        }
        model.addAttribute("userType",userType);
    }
    @ResponseBody
    @GetMapping("/pay/list")
    @RequiresPermissions("trademanager:pay")
    public PageUtils payList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.pay.id());
        return payQuery(beginDate, endDate, params);
    }
    
    @ResponseBody
    @GetMapping("/charge/list")
    @RequiresPermissions("trademanager:charge")
    public PageUtils chargeList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.charge.id());
        return payQuery(beginDate, endDate, params);
    }
    
    @ResponseBody
    @GetMapping("/acp/list")
    @RequiresPermissions("trademanager:acp")
    public PageUtils acpList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.acp.id());
        return acpQuery(beginDate, endDate, params);
    }
    
    @ResponseBody
    @GetMapping("/withdraw/list")
    @RequiresPermissions("trademanager:withdraw")
    public PageUtils withdrawList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.withdraw.id());
        return acpQuery(beginDate, endDate, params);
    }
    
    
    private PageUtils payQuery(Date beginDate, Date endDate, Map<String, Object> params) {
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        Query query = this.initQuery(beginDate, endDate, params, user, userType);
        List<Order> orders = tradeManaQueryDao.payList(query);
        this.handOrder(userType, orders);
        int total = tradeManaQueryDao.payCount(query);
        PageUtils pageUtils = new PageUtils(orders, total);
        return pageUtils;
    }
    
    private PageUtils acpQuery(Date beginDate, Date endDate, Map<String, Object> params) {
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        Query query = this.initQuery(beginDate, endDate, params, user, userType);
        List<Order> orders = tradeManaQueryDao.acpList(query);
        this.handOrder(userType, orders);
        int total = tradeManaQueryDao.acpCount(query);
        PageUtils pageUtils = new PageUtils(orders, total);
        return pageUtils;
    }
    
    private void handOrder(Integer userType, List<Order> orders) {
        for (Order order : orders) {
        	String merchName = "";
        	Merchant merchant = merchantService.get(order.getMerchNo());
        	if(merchant ==null) {
        		Agent agent = agentService.get(order.getMerchNo());
        		merchName = agent==null?"":agent.getMerchantsShortName();
        	}else
        		merchName = merchant.getMerchantsShortName();
    		order.setMerchName(merchName);
            if(UserType.merch.id() == userType || UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
                order.setCostAmount(null);
                order.setAgentAmount(null);
                order.setPayCompany(null);
                order.setPayMerch(null);
            }
        }
    }
    private Query initQuery(Date beginDate, Date endDate, Map<String, Object> params, UserDO user, Integer userType) {
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        String userName = user.getUsername();
        Query query = new Query(params);
        query.put("userType", userType);
        query.put("username", userName);
        return query;
    }
    private Map<String, Object> initParam(Date beginDate, Date endDate, Map<String, Object> params, UserDO user, Integer userType) {
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        params.put("userType", userType);
        params.put("username", user.getUsername());
        return params;
    }

    @ResponseBody
    @PostMapping("/pay/list/footer")
    @RequiresPermissions("trademanager:pay")
    public R payListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.pay.id());
        return this.payFooter(beginDate, endDate, params);
    }
    @ResponseBody
    @PostMapping("/charge/list/footer")
    @RequiresPermissions("trademanager:charge")
    public R chargeListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.charge.id());
        return this.payFooter(beginDate, endDate, params);
    }
    @ResponseBody
    @PostMapping("/acp/list/footer")
    @RequiresPermissions("trademanager:acp")
    public R acpListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.acp.id());
        return this.acpFooter(beginDate, endDate, params);
    }
    @ResponseBody
    @PostMapping("/withdraw/list/footer")
    @RequiresPermissions("trademanager:withdraw")
    public R withdrawListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.withdraw.id());
        return this.acpFooter(beginDate, endDate, params);
    }
    private R payFooter(Date beginDate, Date endDate, Map<String, Object> params) {
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        this.initParam(beginDate, endDate, params, user, userType);
        FooterDO fdo = tradeManaQueryDao.payListFooter(params);
        this.handFooter(fdo,userType);
        return R.okData(fdo);
    }
    private R acpFooter(Date beginDate, Date endDate, Map<String, Object> params) {
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        this.initParam(beginDate, endDate, params, user, userType);
        FooterDO fdo = tradeManaQueryDao.acpListFooter(params);
        this.handFooter(fdo,userType);
        return R.okData(fdo);
    }
    private void handFooter(FooterDO fdo,Integer userType) {
        if(ParamUtil.isNotEmpty(fdo)){
            fdo.setCostAmount(new BigDecimal(0));
            fdo.setAgentAmount(new BigDecimal(0));
            fdo.setSubAgentAmount(new BigDecimal(0));
        }
    }
    
    
    @ResponseBody
    @PostMapping("/pay/list/stati")
    @RequiresPermissions("trademanager:pay")
    public R payListStati(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.pay.id());
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        params.put("userType", userType);
        params.put("username", user.getUsername());
        FooterDO fdo = tradeManaQueryDao.payListStati(params);
        this.handFooter(fdo,userType);
        return R.okData(fdo);
    }
    
    @ResponseBody
    @PostMapping("/acp/list/stati")
    @RequiresPermissions("trademanager:acp")
    public R acpListStati(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.acp.id());
        return acpStati(beginDate, endDate, params);
    }
    
    @ResponseBody
    @PostMapping("/withdraw/list/stati")
    @RequiresPermissions("trademanager:withdraw")
    public R withdrawListStati(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.withdraw.id());
        return acpStati(beginDate, endDate, params);
    }
   /* @ResponseBody
    @PostMapping("/acp/list/footer")
    @RequiresPermissions("trademanager:acp")
    public R acpListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.acp.id());
        return this.acpFooter(beginDate, endDate, params);
    }
    @ResponseBody
    @PostMapping("/withdraw/list/footer")
    @RequiresPermissions("trademanager:withdraw")
    public R withdrawListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        params.put("orderType", OrderType.withdraw.id());
        return this.acpFooter(beginDate, endDate, params);
    }*/
	private R acpStati(Date beginDate, Date endDate, Map<String, Object> params) {
		UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        params.put("userType", userType);
        params.put("username", user.getUsername());
        FooterDO fdo = tradeManaQueryDao.acpListStati(params);
        this.handFooter(fdo,userType);
        return R.okData(fdo);
	}

}
