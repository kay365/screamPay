package com.qh.pay.controller;


import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import com.qh.pay.api.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderParamKey;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.domain.PayQrConfigDO;
import com.qh.pay.service.PayQrConfigService;
import com.qh.pay.service.PayQrService;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;

/**
 * @version 1.0.0
 * @ClassName PayQrController
 * @Description 支付通道扫码支付以及回调
 * @Date 2017年12月19日 下午4:46:40
 */
@Controller
@RequestMapping("/pay")
public class PayQrController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayQrController.class);

    @Autowired
    private PayQrService payQrService;
    @Autowired
    private PayQrConfigService payQrConfigService;
    /**
     * 
     * @Description 人工充值
     * @param context
     * @param model
     * @return
     */
    @RequiresPermissions("super")
    @GetMapping("/qr/super/charge")
    public String qrSuperCharge(Model model){
    	UserDO user = ShiroUtils.getUser();
    	model.addAttribute("username", user.getUsername());
        return PayConstants.url_pay_superCharge;
    }

    
    /**
     * 
     * @Description 人工充值
     * @param context
     * @param model
     * @return
     */
    @RequiresPermissions("super")
    @ResponseBody
	@PostMapping("/qr/super/chargeConfirm")
    public R qrSuperChargeConfirm(@RequestParam("monAmount") String monAmount,@RequestParam("merchNo") String merchNo,
    		@RequestParam("fundPassword") String fundPassword){
    	if(ParamUtil.isEmpty(monAmount) || ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(fundPassword)){
    		return R.error("参数错误！");
    	}
        //验证资金密码
        R r = PasswordCheckUtils.checkFundPassword(fundPassword);
        if(R.ifError(r)){
            return r;
        }
    	
    	//业务逻辑处理
    	return payQrService.superChargeQr(merchNo, OutChannel.man.name(), monAmount, 
        		DateUtil.getCurrentTimeInt() + "-" + new BigDecimal(monAmount).intValue() + "-" + ParamUtil.generateCode2());
         
    }

    /**
     *
     * @Description 验证资金密码
     * @return
     */
    @ResponseBody
    @PostMapping("/qr/checkFundPassword")
    public R checkFundPassword(@RequestParam("fundPassword") String fundPassword){
        return PasswordCheckUtils.checkFundPassword(fundPassword);
    }
    
    /**
     * 
     * @Description 对内授权
     * @param context
     * @param model
     * @return
     */
    @RequiresPermissions("anyone")
    @GetMapping("/qr/perm/charge")
    public String qrPermCharge(Model model){
        UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() != user.getUserType()){
        	model.addAttribute("msg", "非商户不开充值");
        	return PayConstants.url_pay_error_frame;
        }
    	String merchNo = user.getUsername();
    	
    	//组装页面参数
        this.buildChargeParam(model, merchNo);
        
        return PayConstants.url_pay_charge;
    }

    /**
     * 
     * @Description 对外加密
     * @param context
     * @param model
     * @return
     */
    @GetMapping("/qr/charge")
    public String qrCharge(@RequestParam(PayConstants.web_context) String context, Model model){
    	logger.info(PayConstants.web_context + context);
        if (ParamUtil.isNotEmpty(context)) {
            try {
                context = new String(RSAUtil.decryptByPrivateKey(Base64Utils.decode(context), QhPayUtil.getQhPrivateKey()));
            } catch (Exception e) {
                model.addAttribute(Constant.result_msg, "解密异常！");
                return PayConstants.url_pay_error;
            }
            JSONObject jo = JSONObject.parseObject(context);
            String merchNo = jo.getString(OrderParamKey.merchNo.name());
            //组装页面参数
            this.buildChargeParam(model, merchNo);
            
            return PayConstants.url_pay_charge;
        }
        model.addAttribute(Constant.result_msg, "请勿频繁测试！");
        return PayConstants.url_pay_error;
    }


	/**
	 * @Description 组装充值页面参数
	 * @param model
	 * @param merchNo
	 */
	private void buildChargeParam(Model model, String merchNo) {
		model.addAttribute("merchNo", merchNo);
        model.addAttribute("outChannelDesc", OutChannel.jfDesc());
        //按照整数元合并扫码通道的金额
        Map<String,Set<Integer>> monSet = new HashMap<>();
        PayQrConfigDO payQrConfigDO;
        Set<Integer> monIntSet;
        for (String oChannel : OutChannel.jfDesc().keySet()) {
        	//资金账号的充值金额
        	payQrConfigDO = payQrConfigService.get(oChannel, RedisUtil.getPayFoundBal().getUsername());
        	if(payQrConfigDO == null || payQrConfigDO.getQrs() == null || payQrConfigDO.getQrs().isEmpty()){
        		continue;
        	}
        	monIntSet = new TreeSet<>();
        	for (String amountKey : payQrConfigDO.getQrs().keySet()) {
				if(ParamUtil.isNotEmpty(amountKey)){
					monIntSet.add(Integer.parseInt(amountKey.split("\\.")[0]));
				}
			}
        	if(monIntSet.contains(0)){
        		monIntSet.remove(0);
        	}
        	monSet.put(oChannel, monIntSet);
		}
        model.addAttribute("payAcctBal", RedisUtil.getMerchBal(merchNo));
        model.addAttribute("monSet", monSet);
	}
    
    
	/**
	 *
	 * @Description 获取充值金额
	 * @param monAmount
	 * @param merchNo
	 * @param outChannel
	 * @return
	 */
	@ResponseBody
	@PostMapping("/qr/getChargeMon")
	public Object getChargeMon(@RequestParam("monAmount") String monAmount,@RequestParam("merchNo") String merchNo, @RequestParam("outChannel") String outChannel){
		if(ParamUtil.isEmpty(monAmount)|| ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(outChannel)){
			logger.error("参数错误：monAmount-{},merchNo-{},outChannel-{}", monAmount,merchNo,outChannel);
			return R.error("请检查参数");
		}
		return payQrService.getChargeMon(monAmount,merchNo,outChannel);
	}


    /**
     * @param request
     * @return
     * @Description 支付通道扫码界面跳转
     */
    @GetMapping("/qr")
    public String qr(@RequestParam(PayConstants.web_context) String context, Model model) {
        logger.info(PayConstants.web_context + context);
        if (ParamUtil.isNotEmpty(context)) {
            try {
                context = new String(RSAUtil.decryptByPrivateKey(Base64Utils.decode(context), QhPayUtil.getQhPrivateKey()));
            } catch (Exception e) {
                model.addAttribute(Constant.result_msg, "解密异常！");
                return PayConstants.url_pay_error;
            }
            JSONObject jo = JSONObject.parseObject(context);
            String merchNo = jo.getString(OrderParamKey.merchNo.name());
            String orderNo = jo.getString(OrderParamKey.orderNo.name());
            if (ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)) {
                model.addAttribute(Constant.result_msg, "订单号或者商户号为空！");
                return PayConstants.url_pay_error;
            }
            Order order = RedisUtil.getOrder(merchNo, orderNo);
            if (order == null) {
                model.addAttribute(Constant.result_msg, "支付扫码订单不存在");
                return PayConstants.url_pay_error;
            }
            model.addAttribute("merchNo", merchNo);
            model.addAttribute("orderNo", orderNo);
            model.addAttribute("amount", order.getRealAmount().toPlainString());
            model.addAttribute("outChannel", order.getOutChannel());
            model.addAttribute("outChannelDesc", OutChannel.jfDesc());
            model.addAttribute("company", order.getPayCompany());
            int remainSec = RedisUtil.getMonAmountOccupyValidTime(merchNo, order.getOutChannel(), order.getRealAmount().toPlainString());
            model.addAttribute("returnUrl", order.getReturnUrl());
            if (remainSec <= 0) {
                remainSec = 0;
                model.addAttribute("msg", "订单已经过期");
            } else {
            	PayQrConfigDO payQrConfigDo =  payQrConfigService.get(order.getOutChannel(), merchNo);
            	String amount =order.getRealAmount().toPlainString();
            	if(payQrConfigDo != null && payQrConfigDo.getQrs().containsKey(amount)){
            		model.addAttribute("qr_url", "/files/" + merchNo + "/" + order.getOutChannel() + "/" + amount.replace(".", "p") + ".jpg?r=" + ParamUtil.generateCode6());
            		model.addAttribute(PayConstants.qr_any_money_flag, YesNoType.not.id());
            	}else{
            		model.addAttribute("qr_url", "/files/" + merchNo + "/" + order.getOutChannel() + "/0.jpg?r=" + ParamUtil.generateCode6());
            		model.addAttribute(PayConstants.qr_any_money_flag, YesNoType.yes.id());
            	}
            	
            }
            model.addAttribute("remainSec", remainSec);
            return PayConstants.url_pay_qr;
        }
        model.addAttribute(Constant.result_msg, "请勿频繁测试！");
        return PayConstants.url_pay_error;
    }


    /**
     * @param merchNo
     * @param outChannel
     * @param request
     * @return
     * @Description 扫码通道充值后台通知
     */
    @PostMapping("/qr/charge/notify/{outChannel}")
    @ResponseBody
    public JSONObject notifyChargeQr(@PathVariable("outChannel") String outChannel, HttpServletRequest request) {
        logger.info("扫码通道充值后台通知：{}", outChannel);
        try {
			logger.info("充值扫码通道后台通知参数：{}", RequestUtils.getRequestParam(request).toString());
		} catch (UnsupportedEncodingException e) {
			logger.info("编码错误");
		}
        String monAmount = request.getParameter("amount");
        String businessNo = request.getParameter("tradeNo");

        /**   心跳/支付回调 标记  心跳:timePost   支付回调:up    **/
        
        String merchNo = RedisUtil.getPayFoundBal().getUsername();
        String todo = request.getParameter("todo");
        if ("timePost".equals(todo)) {
            return syncCheck(merchNo,outChannel,request);
        }else if("up".equals(todo)){
            //回调验签
            if (verifySign(merchNo, outChannel, request)) {
                //业务逻辑处理
            	String msg = null;
				try {
					msg = URLDecoder.decode( request.getParameter("time"),"UTF-8") + URLDecoder.decode( request.getParameter("username"),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.error("参数解码失败，{}，{}","time","username");
				}
                payQrService.notifyChargeQr(merchNo, outChannel, monAmount, businessNo, msg);
            }
        }
        return null;
    }
    
    /**
     * @param merchNo
     * @param outChannel
     * @param request
     * @return
     * @Description 扫码通道支付后台通知
     */
    @PostMapping("/qr/notify/{merchNo}/{outChannel}")
    @ResponseBody
    public JSONObject notifyQr(@PathVariable("merchNo") String merchNo, @PathVariable("outChannel") String outChannel,
    		HttpServletRequest request) {
        logger.info("扫码通道支付后台通知：{},{}", merchNo, outChannel);
        try {
			logger.info("支付扫码通道后台通知参数：{}", RequestUtils.getRequestParam(request).toString());
		} catch (UnsupportedEncodingException e) {
			logger.info("编码错误");
		}
        String monAmount = request.getParameter("amount");
        String businessNo = request.getParameter("tradeNo");

        /**   心跳/支付回调 标记  心跳:timePost   支付回调:up    **/
        String todo = request.getParameter("todo");
        if ("timePost".equals(todo)) {
            return syncCheck(merchNo,outChannel,request);
        }else if("up".equals(todo)){
            //回调验签
            if (verifySign(merchNo, outChannel, request)) {
                //业务逻辑处理
            	String msg = null;
				try {
					msg = URLDecoder.decode( request.getParameter("time"),"UTF-8") + URLDecoder.decode( request.getParameter("username"),"UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.error("参数解码失败，{}，{}","time","username");
				}
                payQrService.notifyQr(merchNo, outChannel, monAmount, businessNo, msg);
            }else{
            	logger.error("支付扫码通道后台通知 验签不通过");
            }
        }
        return null;
    }

    
    
    private JSONObject syncCheck(String merchNo,String outChannel,HttpServletRequest request) {
        String v = request.getParameter("v");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Date date = new Date();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("S", "0");
        jsonObj.put("msg", "timepost do nothing");
        jsonObj.put("T", sdf.format(date));
        jsonObj.put("V", v);
        RedisUtil.setQrGatewayLastSyncTime(merchNo,outChannel,date.getTime());
        return jsonObj;
    }

    /**
     * @param merchNo
     * @param outChanel
     * @param request
     * @return
     * @Description 验签
     */
    private boolean verifySign(String merchNo, String outChanel, HttpServletRequest request) {
    	try {
			PayQrConfigDO payQrConfigDO = payQrConfigService.get(outChanel, merchNo);//"4faf5ac89fe24e7dbf64dfa50c2fed7f";//根据商户号获取
			if (payQrConfigDO == null) {
				logger.error("没有对应的APIKEY({},{})",merchNo,outChanel);
				return false;
			}
			String apiKey = payQrConfigDO.getApiKey();
			if (StringUtils.isBlank(apiKey)) {
				logger.error("APIKEY为空({},{})",merchNo,outChanel);
				return false;
			}
			TreeMap<String, String> params = RequestUtils.getRequestParam(request);
			String tradeNo = params.get("tradeNo");
			String status = params.get("status");
			if(outChanel.equals(OutChannel.jfali.name())){
                tradeNo = URLDecoder.decode(tradeNo,"UTF-8");
                status = URLDecoder.decode(params.get("status"),"UTF-8");
            }
			String[] obj = {tradeNo,URLDecoder.decode(params.get("desc"),"UTF-8"),URLDecoder.decode(params.get("time"),"UTF-8"),URLDecoder.decode(params.get("username"),"UTF-8"),params.get("userid"),params.get("amount"),status,apiKey};
			String str = String.join("|", obj);
			logger.info(str);
			String sig = params.get("sig");
			String sign = Md5Util.MD5(str);
			logger.info("原签名："+sig+"；现签名："+sign);
			if(sign.equalsIgnoreCase(sig))
				return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return false;
    }

}
