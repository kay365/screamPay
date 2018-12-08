package com.qh.pay.controller;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.controller.BaseController;
import com.qh.common.domain.UserBankDO;
import com.qh.common.service.LocationService;
import com.qh.common.service.UserBankService;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.EncryptType;
import com.qh.pay.api.constenum.OrderParamKey;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Base64Utils;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.PasswordCheckUtils;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.pay.service.PayService;
import com.qh.redis.constenum.ConfigParent;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
/**
 * 
 * @ClassName PayController
 * @Description pay
 * @Date 2017年10月24日 上午11:30:22
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pay")
public class PayController  extends BaseController{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayController.class);
    @Autowired
    private MerchantService merchantService;
    @Autowired
	private AgentService agentService;
    @Autowired
    private PayService payService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private UserBankService userBankService;
    @Autowired
	private PayConfigCompanyService payConfigCompanyService;
    
    @GetMapping("/merchant/{merchNo}")
    public Merchant findByMerchNo(@PathVariable String merchNo){
        return merchantService.get(merchNo);
    }
    
    /**
     * 
     * @Description 支付下单
     * @param request
     * @return
     */
    @PostMapping("/order")
    public Object order(HttpServletRequest request){
    	
		String closePay = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_PAY, ConfigParent.outChannelConfig.name());
		if(closePay!=null && Integer.valueOf(closePay).equals(YesNoType.not.id())) {
			return R.error("系统正在更新,请稍后！");
		}
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		return payService.order((Merchant)r.get(Constant.param_merch), (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }

	
    /**
     * 
     * @Description 代付下单
     * @param request
     * @return
     */
    @PostMapping("/order/acp")
    public Object orderAcp(HttpServletRequest request){
    	
    	String closeAcp = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_ACP, ConfigParent.outChannelConfig.name());
    	if(closeAcp!=null && Integer.valueOf(closeAcp).equals(YesNoType.not.id())) {
			return R.error("系统正在更新,请稍后！");
		}
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		Merchant merchant = (Merchant)r.get(Constant.param_merch);
    		if(!merchant.getSupportPaid().equals(YesNoType.yes.id())) {
    			logger.error("商户不支持代付！" + merchant.getMerchNo());
				return R.error("商户不支持代付！" + merchant.getMerchNo());
    		}
    				
    		return payService.orderAcp(merchant, (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }
    /**
     * 
     * @Description 支付查询
     * @param request
     * @return
     */
    @PostMapping("/order/query")
    public Object query(HttpServletRequest request){
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		return payService.query((Merchant)r.get(Constant.param_merch), (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }
    
    /**
     * 
     * @Description 代付查询
     * @param request
     * @return
     */
    @PostMapping("/order/acp/query")
    public Object acpQuery(HttpServletRequest request){
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		return payService.acpQuery((Merchant)r.get(Constant.param_merch), (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }
    /**
	 * @Description 通用检查方法
	 * @param request
	 * @return
	 */
	private R commDataCheck(HttpServletRequest request) {
		JSONObject jsonObject =  RequestUtils.getJsonResultStream(request);
    	if(jsonObject == null){
    		return R.error("请检查请求参数！");
    	}
    	String sign = jsonObject.getString("sign");
    	logger.info("请求签名：{}",sign);
    	if(ParamUtil.isEmpty(sign)){
    		return R.error("请检查签名参数！");
    	}
    	byte[] context = jsonObject.getBytes("context");
    	if(ParamUtil.isEmpty(context)){
    		return R.error("请检查加密内容！");
    	}
    	logger.info("请求加密结果：{}", context);
    	try {
    		//解密
    		boolean isMD5 = false;
    		String encryptType = EncryptType.RSA.name();
        	if(jsonObject.containsKey("encryptType")) {
        		encryptType = jsonObject.getString("encryptType");
        		if(EncryptType.desc().containsKey(encryptType)) {
        			isMD5 = EncryptType.isMD5(encryptType);
        		}else
        			return R.error("不支持的加密方式！");
        	}
        	String source = "";
        	if(isMD5) {
        		source = new String(context,"UTF-8");
        	}else
        		source = new String(RSAUtil.decryptByPrivateKey(context, QhPayUtil.getQhPrivateKey()));
    		logger.info("解密结果！" + source);
    		JSONObject jo = JSON.parseObject(source);
    		String merchNo = jo.getString(OrderParamKey.merchNo.name());
    		if(ParamUtil.isEmpty(merchNo)){
    			logger.error("商户号为空！" + source);
    			return R.error("商户号为空！" + source);
    		}
    		
    		Merchant merchant = merchantService.get(merchNo);
			if(merchant == null){
				logger.error("商户不存在！" + merchNo);
				return R.error("商户不存在！" + merchNo);
			}
			if(!merchant.getStatus().equals(YesNoType.yes.id())) {
				logger.error("商户被禁用！" + merchNo);
				return R.error("商户异常！" + merchNo);
			}
			Agent agent = agentService.get(merchant.getParentAgent());
			if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
				logger.error("商户上级代理被禁用！" + merchNo);
				return R.error("商户异常!");
			}else {
				if(agent.getLevel().equals(AgentLevel.two.id())) {
					agent = agentService.get(agent.getParentAgent());
					if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
						logger.error("商户上级一级代理被禁用！" + merchNo);
						return R.error("商户异常!");
					}
				}
			}
			EncryptType.setEncrypt(merchNo, encryptType);
			if((!isMD5 && RSAUtil.verify(context, merchant.getPublicKey(), sign)) || (isMD5 && Md5Util.verify(source, sign, merchant.getPublicKey(), "UTF-8"))){
				logger.info("验签成功！", merchant.getPublicKey());
				jo.put(OrderParamKey.reqIp.name(), ParamUtil.getIpAddr(request));
				return R.ok().put(Constant.param_merch, merchant).put(Constant.param_jsonData, jo);
			}else{
				logger.error("验签失败！" + merchNo);
				return R.error("验签失败！" + merchNo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info(e.getMessage());
			return R.error("支付异常！" + e.getMessage());
		}
	}
	
	
	/**
	 * 
	 * @Description 同步订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/syncOrder/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:syncOrder")
	public Object syncOrder(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		return payService.syncOrder(merchNo,orderNo,businessNo);
	}
	
	/**
	 * 
	 * @Description 通知订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/noticeOrder/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:noticeOrder")
	public Object noticeOrder(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		String result = payService.syncOrderNotifyMsg(merchNo,orderNo,businessNo);
		if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
			return R.ok("通知成功："+result);
		}else {
			return R.error("通知失败："+result);
		}
	}
	
	/**
	 * 
	 * @Description 同步代付订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/syncOrderAcp/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:syncOrderAcp")
	public Object syncOrderAcp(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		return payService.syncOrderAcp(merchNo,orderNo,businessNo);
	}
	
	/**
	 * 
	 * @Description 通知代付订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/noticeOrderAcp/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:noticeOrder")
	public Object noticeOrderAcp(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		String result = payService.syncOrderAcpNotifyMsg(merchNo,orderNo,businessNo);
		if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
			return R.ok("通知成功："+result);
		}else {
			return R.error("通知失败："+result);
		}
	}
	
	/**
     * 
     * @Description 提现跳转
     * @param context
     * @param model
     * @return
     */
    @RequiresPermissions("pay:withdraw")
    @GetMapping("/withdraw")
    public ModelAndView withdraw(Model model){
    	
        UserDO user = ShiroUtils.getUser();
        if(UserType.user.id() != user.getUserType()) {
	        String closeAcp = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_CLOSE_WITHDRAW, ConfigParent.outChannelConfig.name());
	    	if(closeAcp!=null && Integer.valueOf(closeAcp).equals(YesNoType.not.id())) {
				model.addAttribute("msg", "系统正在更新,请稍后！");
	        	return new ModelAndView(PayConstants.url_pay_error_frame);
			}
        }
        /*if(UserType.merch.id() != user.getUserType() && UserType.agent.id() != user.getUserType() && UserType.subAgent.id() != user.getUserType()){
        	model.addAttribute("msg", "目前只支持商户或代理提现");
        	return new ModelAndView(PayConstants.url_pay_error_frame);
        }*/
    	
    	this.buildWithdrawParam(model, user.getUsername(), user.getUserType());
        return new ModelAndView(PayConstants.url_pay_withdraw);
    }
    /**
	 * @Description 组装提现页面参数
	 * @param model
	 * @param merchNo
	 */
	private void buildWithdrawParam(Model model, String username, Integer userType) {
		//商户号
		model.addAttribute("username", username);
		PayAcctBal payAcctBal = null;
		if(UserType.merch.id() == userType){
			payAcctBal = RedisUtil.getMerchBal(username);
//			Merchant merchant = merchantService.getById(username);
//			model.addAttribute("merObj",merchant);
		}else if(UserType.user.id() == userType) {
			payAcctBal = RedisUtil.getPayFoundBal();
		}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
			payAcctBal = RedisUtil.getAgentBal(username);
//			Agent agent = agentService.get(username);
//			model.addAttribute("merObj",agentService.getById(agent.getAgentId()));
		}
		//资金余额
        model.addAttribute("payAcctBal", payAcctBal);
        //用户卡列表信息
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", username);
        List<UserBankDO> userBanks = userBankService.list(map);
        model.addAttribute("userBanks", userBanks);
        //所有的省信息
        model.addAttribute("provinces", locationService.listProvinces());
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        //银行卡类型
        model.addAttribute("cardTypes", CardType.desc());
        
        model.addAttribute("userType", userType);
        
        Map<String,Object> companyMap = new HashMap<String,Object>();
        companyMap.put("ifClose", "0");
        companyMap.put("outChannel", "acp");
        List<PayConfigCompanyDO> list = payConfigCompanyService.list(companyMap);
        for (PayConfigCompanyDO payConfigCompanyDO : list) {
        	payConfigCompanyDO.setCallbackDomain(PayCompany.desc().get(payConfigCompanyDO.getCompany()));
		}
		model.addAttribute("payConfigCompany", list);
	}
    
	/**
	 * 
	 * @Description 提现确认
	 * @param order
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@PostMapping("/withdraw/confirm")
	@RequiresPermissions("pay:withdraw")
	public Object withdraw(@RequestParam("fundPassword") String fundPassword,Order order,@RequestParam("orderNum") Integer orderNum) throws IllegalAccessException, InvocationTargetException{
		UserDO user = ShiroUtils.getUser();
        /*if(UserType.merch.id() != user.getUserType() && UserType.agent.id() != user.getUserType() && UserType.subAgent.id() != user.getUserType()){
        	return R.error("目前只支持商户或代理提现");
        }*/
        if(UserType.merch.id() == user.getUserType()) {
        	Merchant merchant = merchantService.get(user.getUsername());
        	if(!merchant.getSupportWithdrawal().equals(YesNoType.yes.id())) {
        		return R.error("您的提现状态被禁用!");
        	}
        }
        if(UserType.agent.id() == user.getUserType() || UserType.subAgent.id() == user.getUserType()) {
        	Agent agent = agentService.get(user.getUsername());
        	if(!agent.getStatus().equals(YesNoType.yes.id())) {
        		return R.error("您的状态被禁用!");
        	}
        }
        //验证资金密码
        R r = PasswordCheckUtils.checkFundPassword(fundPassword);
        if(R.ifError(r)){
            return r;
        }
        order.setMerchNo(user.getUsername());
        order.setUserType(user.getUserType());
        orderNum = 1;
        if(orderNum>1) {
        	int num = 0;
        	Order olOrder = null;
        	for (int i = 0; i < orderNum; i++) {
        		olOrder = new Order();
            	BeanUtils.copyProperties(olOrder, order);
        		R wr = payService.withdraw(olOrder);
        		if(R.ifSucc(wr)) {
        			num ++;
        		}
			}
        	return R.ok("共 "+num+" 笔提现处理成功");
        }else
        	return payService.withdraw(order);
	}
	
	/**
     * 
     * @Description 跳转动态表单提交
     * @param request
     * @return
     */
    @GetMapping("/order/jump")
    public ModelAndView jump(@RequestParam(PayConstants.web_context) String context,Model model){
    	logger.info("order/jump："+PayConstants.web_context + "=" + context);
    	if(ParamUtil.isNotEmpty(context)){
    		try {
    			context = new String(RSAUtil.decryptByPrivateKey(Base64Utils.decode(context), QhPayUtil.getQhPrivateKey()));
			} catch (Exception e) {
				model.addAttribute(Constant.result_msg, "解密异常！");
    			return new ModelAndView(PayConstants.url_pay_error);
			}
    		JSONObject jo = JSONObject.parseObject(context);
    		String merchNo = jo.getString(OrderParamKey.merchNo.name());
    		String orderNo = jo.getString(OrderParamKey.orderNo.name());
    		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    			model.addAttribute(Constant.result_msg, "订单号或者商户号为空！");
    			return new ModelAndView(PayConstants.url_pay_error);
    		}
    		Order order = RedisUtil.getOrder(merchNo, orderNo);
    		if(order == null){
    			model.addAttribute(Constant.result_msg, "订单不存在！");
    	    	return new ModelAndView(PayConstants.url_pay_error);
    		}
    		model.addAttribute(PayConstants.web_jumpData, order.getJumpData());
    		return new ModelAndView(PayConstants.url_pay_jump);
    	}
    	model.addAttribute(Constant.result_msg, "请勿频繁测试！");
    	return new ModelAndView(PayConstants.url_pay_error);
    }
    
    @ResponseBody
    @GetMapping("/transfer/{merchNo}")
    @RequiresPermissions("moneyacct:merchant")
    public R merchantTransfer(@PathVariable("merchNo") String merchNo,@RequestParam("money") String money,@RequestParam("isplate") Integer isplate){
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        if(UserType.user.id() != userType) {
        	return R.error("权限不允许");
        }
        BigDecimal moneyBd = new BigDecimal(money);
        if(moneyBd.compareTo(BigDecimal.ZERO) != 1)
        	return R.error("划拨金额不能小于0");
        return payService.transfer(merchNo, moneyBd,isplate);
    }
    
    @ResponseBody
    @GetMapping("/freeze/{merchNo}")
    @RequiresPermissions("moneyacct:merchant")
    public R merchantFreeze(@PathVariable("merchNo") String merchNo,@RequestParam("money") String money,@RequestParam("freeze") Integer freeze){
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        if(UserType.user.id() != userType) {
        	return R.error("权限不允许");
        }
        BigDecimal moneyBd = new BigDecimal(money);
        if(moneyBd.compareTo(BigDecimal.ZERO) != 1)
        	return R.error("金额不能小于0");
        return payService.freeze(merchNo, moneyBd,freeze);
    }
}