package com.qh.pay.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.constenum.CardSendType;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.OrderParamKey;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.utils.Base64Utils;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.pay.service.MerchUserSignService;
import com.qh.pay.service.PayBankService;
import com.qh.pay.service.PayHandlerService;
import com.qh.pay.service.PayService;
import com.qh.paythird.PayBaseService;
import com.qh.redis.service.RedisUtil;
import com.qh.redis.service.RedissonLockUtil;

/**
 * @ClassName PayBindCardController
 * @Description 支付快捷绑卡以及相关支付操作
 * @Date 2017年11月24日 下午3:30:44
 * @version 1.0.0
 */
@Controller
@RequestMapping("/pay")
public class PayCardController {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayCardController.class);
	@Autowired 
	private PayBaseService payBaseService;
	@Autowired
	private MerchUserSignService merchUserSignService;
	@Autowired
	private PayHandlerService payHandlerService;
	@Autowired
	private PayBankService payBankService;
	
	/**
     * 
     * @Description 银行卡绑卡页面
     * @param request
     * @return
     */
    @GetMapping("/card")
    public String card(@RequestParam(PayConstants.web_context) String context,Model model){
    	logger.info(PayConstants.web_context + context);
    	if(ParamUtil.isNotEmpty(context)){
    		try {
    			context = new String(RSAUtil.decryptByPrivateKey(Base64Utils.decode(context), QhPayUtil.getQhPrivateKey()));
			} catch (Exception e) {
				model.addAttribute(Constant.result_msg, "解密异常！");
    			return PayConstants.url_pay_error;
			}
    		JSONObject jo = JSONObject.parseObject(context);
    		String merchNo = jo.getString(OrderParamKey.merchNo.name());
    		String orderNo = jo.getString(OrderParamKey.orderNo.name());
    		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    			model.addAttribute(Constant.result_msg, "订单号或者商户号为空！");
    			return PayConstants.url_pay_error;
    		}
    		Order order = RedisUtil.getOrder(merchNo, orderNo);
    		if(order == null){
    			model.addAttribute(Constant.result_msg, "快捷订单不存在");
    			return PayConstants.url_pay_error;
    		}
    		Integer cardType = PayCompany.companyCardType(order.getPayCompany());
    		if(cardType == null){
    			model.addAttribute(Constant.result_msg, "未配置支付卡类型");
    			return PayConstants.url_pay_error;
    		}
    		model.addAttribute(PayConstants.card_type, cardType);
    		
    		//查询个人签约信息
    		MerchUserSignDO merchUserSign = new MerchUserSignDO();
    		merchUserSign.setUserId(order.getUserId());
    		merchUserSign.setMerchNo(order.getMerchNo());
    		merchUserSign.setPayMerch(order.getPayMerch());
    		merchUserSign.setPayCompany(order.getPayCompany());
    		//对私
    		merchUserSign.setAcctType(AcctType.pri.id());
    		//银行卡类型
    		if(CardType.savings.id() == cardType || CardType.credit.id() == cardType){
    			merchUserSign.setCardType(cardType);
    		}
    		List<MerchUserSignDO> userSings = merchUserSignService.getMerchUserSigns(merchUserSign);
    		model.addAttribute(PayConstants.bankCodeDesc, BankCode.desc());
    		model.addAttribute(PayConstants.userSigns, userSings);
    		if(order.getUserSign() != null){
    			model.addAttribute(PayConstants.userSign, order.getUserSign());
    			order.getUserSign().setBankName(BankCode.desc().get(order.getUserSign().getBankCode()));
    		}
    		boolean savingsFlag = (CardType.both.id() == cardType || CardType.savings.id() == cardType);
    		boolean creditFlag = (CardType.both.id() == cardType || CardType.credit.id() == cardType);
    		//获取银行卡列表 ---储蓄卡
    		List<String> bank_savings = payBankService.getBanks(order.getPayCompany(), order.getPayMerch(),CardType.savings.id());
    		//获取银行卡列表 ---信用卡
    		List<String> bank_credits = payBankService.getBanks(order.getPayCompany(), order.getPayMerch(),CardType.credit.id());
    		
    		if((savingsFlag && CollectionUtils.isEmpty(bank_savings)) 
    				|| ( creditFlag && CollectionUtils.isEmpty(bank_credits))){
    			bank_savings = new ArrayList<>();
    			bank_credits = new ArrayList<>();
    			payBaseService.refreshBanks(order,bank_savings,bank_credits);
    			payBankService.setBanks(CardType.savings.id(), order.getPayCompany(), order.getPayMerch(), bank_savings);
    			payBankService.setBanks(CardType.credit.id(), order.getPayCompany(), order.getPayMerch(), bank_credits);
    		}
    		if(savingsFlag){
    			Map<String,String> bank_savingMap = new HashMap<>();
    			for (String bankNo : bank_savings) {
    				bank_savingMap.put(bankNo, BankCode.desc().get(bankNo));
    			}
    			model.addAttribute(PayConstants.bank_savings, bank_savingMap);
    		}
    		if(creditFlag){
    			Map<String,String> bank_creditMap = new HashMap<>();
    			for (String bankNo : bank_credits) {
        			bank_creditMap.put(bankNo, BankCode.desc().get(bankNo));
    			}
        		model.addAttribute(PayConstants.bank_credits, bank_creditMap);
    		}
    		model.addAttribute("merchNo", merchNo);
    		model.addAttribute("orderNo", orderNo);
    		model.addAttribute("amount", order.getAmount().toPlainString());
    		model.addAttribute("company", order.getPayCompany());
    		model.addAttribute("returnUrl", order.getReturnUrl());
    		model.addAttribute("bindCardSMS", PayCompany.companyBindCardSMS(order.getPayCompany()));
    		model.addAttribute("resendSMS", PayCompany.companyResendSMS(order.getPayCompany()));
    		return PayConstants.url_pay_card;
    	}
    	model.addAttribute(Constant.result_msg, "请勿频繁测试！");
    	return PayConstants.url_pay_error;
    }
	
    /**
     * 
     * @Description 银行卡绑卡
     * @param request
     * @return
     */
    @PostMapping("/card/bind")
    @ResponseBody
    public Object cardBind(@RequestParam("merchNo") String merchNo, @RequestParam("orderNo")String orderNo
    		,MerchUserSignDO userSign){
    	
    	if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    		return R.error("商户订单信息为空");
    	}
    	
    	Order order = RedisUtil.getOrder(merchNo, orderNo);
    	if(order == null){
    		return R.error("商户订单参数有误");
    	}
    	return cardBindGeneral(merchNo, orderNo, userSign, order);
    }

    /**
     * 通用绑卡
     * @param merchNo
     * @param orderNo
     * @param userSign
     * @param order
     * @return
     */
	private Object cardBindGeneral(String merchNo, String orderNo, MerchUserSignDO userSign, Order order) {
		
		userSign.setMerchNo(merchNo);
    	userSign.setPayCompany(order.getPayCompany());
    	userSign.setPayMerch(order.getPayMerch());
    	userSign.setUserId(order.getUserId());
    	//检查参数
    	String msg = payHandlerService.checkUserSign(userSign);
    	
		if(ParamUtil.isNotEmpty(msg)){
			return R.error(msg);
		}
		RLock rLock =RedissonLockUtil.getOrderLock(merchNo, orderNo);
    	if(rLock.tryLock()){
    		try {
    			MerchUserSignDO merchUserSign = merchUserSignService.get(order.getUserSign());
    			if(merchUserSign == null || ParamUtil.isEmpty(merchUserSign.getSign())){
    				R r =  payBaseService.cardBind(order, userSign);
    	        	if(R.ifSucc(r)){
    	        		order.setUserSign(userSign);
    	        		RedisUtil.setOrder(order);
    	        	}
    	        	return r;
    			}else{
    				return R.error("该卡已经绑定");
    			}
			} finally {
				rLock.unlock();
			}
    	}else{
    		return R.error("正在处理中");
    	}
	}
    
    
    
    
    /***
     * 短信重发
     */
    @PostMapping("/card/msgResend")
    @ResponseBody
    public Object cardMsgResend(@RequestParam("merchNo") String merchNo, @RequestParam("orderNo")String orderNo,
    		@RequestParam("sign") String sign,@RequestParam("sendType") Integer sendType){
    	if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    		return R.error("商户订单信息为空");
    	}
    	Order order = RedisUtil.getOrder(merchNo, orderNo);
    	if(order == null){
    		return R.error("商户订单参数有误");
    	}
    	
    	if(ParamUtil.isEmpty(sign)){
    		return R.error("商户签约信息不能为空");
    	}
    	
    	if(ParamUtil.isEmpty(sendType) || !CardSendType.desc().containsKey(sendType)){
    		return R.error("短信发送类型不能为空或不正确");
    	}
    	RLock rLock =RedissonLockUtil.getOrderLock(merchNo, orderNo);
    	if(rLock.tryLock()){
    		try {
    			order.setSign(sign);
				return payBaseService.cardMsgResend(order,sendType);
			} finally {
				rLock.unlock();
			}
    	}else{
    		return R.ok("正在处理中");
    	}
    }
    
    
    /**
     * 
     * @Description 银行卡绑卡确认 并发起支付
     * @param request
     * @return
     */
    @PostMapping("/card/bind/confirm")
    @ResponseBody
    public Object cardBindConfirm(@RequestParam("merchNo") String merchNo, @RequestParam("orderNo")String orderNo,
    		@RequestParam("checkCode") String checkCode,MerchUserSignDO userSign){
    	
    	if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    		return R.error("商户订单信息为空");
    	}
    	Order order = RedisUtil.getOrder(merchNo, orderNo);
    	if(order == null){
    		return R.error("商户订单参数有误");
    	}
    	
    	//绑卡不需要验证码处理块  开始 ------------------------------------------------------------
    	Integer bindCardSMS = PayCompany.companyBindCardSMS(order.getPayCompany());
    	if(YesNoType.not.id() == bindCardSMS){
    		R r = (R)cardBindGeneral(merchNo, orderNo, userSign, order);
			if(R.ifSucc(r)){
				int count = 0;
				count = merchUserSignService.save(order.getUserSign());
				if(count == 0){
	    			return R.error("签约信息数据异常");
	    		}
	    		order.setSign(order.getUserSign().getSign());
	    		order.setAcctType(order.getUserSign().getAcctType());
				order.setCardType(order.getUserSign().getCardType());
				order.setBankNo(order.getUserSign().getBankNo());
				order.setBankCode(order.getUserSign().getBankCode());
//				order.setUserSign(null);
				RedisUtil.setOrder(order);
			}
    		return r;
    	}
    	//绑卡不需要验证码处理块  结束 ------------------------------------------------------------
    	
    	if(ParamUtil.isEmpty(checkCode)){
    		return R.error("验证码为空");
    	}
    	if(order.getUserSign() == null || ParamUtil.isEmpty(order.getUserSign().getSign())){
    		return R.error("签约信息为空");
    	}
    	RLock rLock =RedissonLockUtil.getOrderLock(merchNo, orderNo);
    	if(rLock.tryLock()){
    		try {
    			MerchUserSignDO merchUserSign = merchUserSignService.get(order.getUserSign());
    			if(merchUserSign == null || ParamUtil.isEmpty(merchUserSign.getSign())){
    				order.getUserSign().setCheckCode(checkCode);
    	    		R r =  payBaseService.cardBindConfirm(order, order.getUserSign());
    	    		if(R.ifSucc(r)){
    	    			int count = 0;
    	    			if(merchUserSign == null){
    	    				count = merchUserSignService.save(order.getUserSign());
    	    			}else{
    	    				count = merchUserSignService.update(order.getUserSign());
    	    			}
    		    		if(count == 0){
    		    			return R.error("签约信息数据异常");
    		    		}
    		    		order.setSign(order.getUserSign().getSign());
    		    	}else{
    		    		return r;
    		    	}
    			}else{
    				order.setSign(merchUserSign.getSign());
    			}
    			//设置账户性质、银行卡类型、银行卡信息
    			order.setAcctType(order.getUserSign().getAcctType());
    			order.setCardType(order.getUserSign().getCardType());
    			order.setBankNo(order.getUserSign().getBankNo());
    			order.setBankCode(order.getUserSign().getBankCode());
    			order.setUserSign(null);
				RedisUtil.setOrder(order);
				return payBaseService.order(order);
			} finally {
				rLock.unlock();
			}
		}else{
			return R.error("正在发起快捷支付");
		}
    }
    
    /**
     * 
     * @Description 直接快捷发起 发送短信
     * @param request
     * @return
     */
    @PostMapping("/card/pay")
    @ResponseBody
    public Object cardPay(@RequestParam("merchNo") String merchNo, @RequestParam("orderNo")String orderNo,
    		@RequestParam("sign") String sign,  @RequestParam("cardType") Integer cardType, @RequestParam Map<String,Object> paramMap){
    	if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    		return R.error("商户订单信息为空");
    	}
    	if(ParamUtil.isEmpty(sign)){
    		return R.error("协议号参数为空！");
    	}
    	if(cardType == null || !CardType.desc().containsKey(cardType)){
    		return R.error("请检查银行卡类型参数");
    	}
    	RLock rLock = RedissonLockUtil.getOrderLock(merchNo, orderNo);
		if(rLock.tryLock()){
			try {
				Order order = RedisUtil.getOrder(merchNo, orderNo);
		    	if(order == null){
		    		return R.error("商户订单参数有误");
		    	}
		    	if(OrderState.succ.id() == order.getOrderState()){
		    		return orderState(order.getOrderState());
		    	}
		    	order.setCardType(cardType);
				order.setSign(sign);
				String bankNo = (String) paramMap.get(OrderParamKey.bankNo.name());
				if(ParamUtil.isNotEmpty(bankNo)){
					order.setBankNo(bankNo);
				}
				String bankCode = (String) paramMap.get(OrderParamKey.bankCode.name());
				if(ParamUtil.isNotEmpty(bankCode) && BankCode.desc().containsKey(bankCode)){
					order.setBankCode(bankCode);
				}
				
				R r = R.error("未知状态");
				//绑卡不需要验证码处理块  开始 ------------------------------------------------------------
		    	Integer bindCardSMS = PayCompany.companyBindCardSMS(order.getPayCompany());
		    	if (YesNoType.not.id() == bindCardSMS) {
		    		MerchUserSignDO userSign = new MerchUserSignDO();
					userSign.setMerchNo(merchNo);
			    	userSign.setPayCompany(order.getPayCompany());
			    	userSign.setPayMerch(order.getPayMerch());
			    	userSign.setUserId(order.getUserId());
			    	userSign.setBankNo(order.getBankNo());
			    	
	    			MerchUserSignDO merchUserSign = merchUserSignService.get(userSign);
	    			if(merchUserSign == null || ParamUtil.isEmpty(merchUserSign.getSign())){
	    				return R.error("签名不存在");
	    			}else{
	    				r =  payBaseService.cardBind(order, merchUserSign);
	    	        	if(R.ifSucc(r)){
	    	        		order.setUserSign(merchUserSign);
	    	        		order.setSign(order.getUserSign().getSign());
	    		    		order.setAcctType(order.getUserSign().getAcctType());
	    	        	}
	    			}
	    			//绑卡不需要验证码处理块  结束 ------------------------------------------------------------
		    	} else {
		    		r =  payBaseService.order(order);
		    	}
				if(R.ifSucc(r)){
					RedisUtil.setOrder(order);
				}
				return r;
			} finally {
				rLock.unlock();
			}
		}else{
			return R.error("正在发起快捷支付");
		}
    }
    
    /**
     * 
     * @Description 快捷支付短信确认
     * @param request
     * @return
     */
    @PostMapping("/card/pay/confirm")
    @ResponseBody
    public Object cardPayConfirm(@RequestParam("merchNo") String merchNo, @RequestParam("orderNo")String orderNo,
    		@RequestParam("checkCode") String checkCode){
    	if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    		return R.error("商户订单信息为空");
    	}
    	if(ParamUtil.isEmpty(checkCode)){
    		return R.error("短信验证码为空");
    	}
    	RLock rLock = RedissonLockUtil.getOrderLock(merchNo, orderNo);
		if(rLock.tryLock()){
			try {
				Order order = RedisUtil.getOrder(merchNo, orderNo);
		    	if(order == null){
		    		return R.error("商户订单参数有误");
		    	}
		    	if(OrderState.succ.id() == order.getOrderState()){
		    		return orderState(order.getOrderState());
		    	}
				order.setCheckCode(checkCode);
				if(ParamUtil.isEmpty(order.getSign())){
		    		return R.error("签约信息为空");
		    	}
				R r =  payBaseService.order(order);
				if(R.ifSucc(r)){
					RedisUtil.setOrder(order);
					Map<String, String> data = PayService.initRspData(order);
					data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
					data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
					if(order.getRealAmount() != null){
						data.put(OrderParamKey.amount.name(), order.getRealAmount().toPlainString());
					}else{
						data.put(OrderParamKey.amount.name(), order.getAmount().toPlainString());
					}
					data.put("company", order.getPayCompany());
					data.put(OrderParamKey.returnUrl.name(), order.getReturnUrl());
					r.put(Constant.result_data, data);
				}
				return r;
			} finally {
				rLock.unlock();
			}
		}else{
			return R.error("正在发起快捷支付确认");
		}
    }

	/**
	 * @Description 返回订单描述状态
	 * @param orderState
	 * @return
	 */
	private R orderState(Integer orderState) {
		if(OrderState.succ.id() == orderState){
			return R.ok("订单支付完成");
		}
		if(OrderState.ing.id() == orderState){
			return R.ok("订单支付处理中");
		}
		if(OrderState.fail.id() == orderState){
			return R.ok("订单支付失败");
		}
		if(OrderState.close.id() == orderState){
			return R.ok("订单支付已关闭");
		}
		return null;
	}
}
