package com.qh.paythird.ysb;

import java.io.BufferedReader;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.ysb.utils.MD5Utils;
import com.qh.paythird.ysb.utils.YinShengBaoConst;
import com.qh.redis.service.RedisUtil;

/**
 * 银生宝
 * @author Swell
 *
 */
@Service
public class YinShengBaoService {

	private static final Logger logger = LoggerFactory.getLogger(YinShengBaoService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("银生宝支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//快捷支付
				return order_q(order);
			} 
			
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return order_wy(order);
			} 
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码
				return order_sm(order,YinShengBaoConst.SM_ALI);
			} 
			
			if (OutChannel.wx.name().equals(order.getOutChannel())) {
				//微信扫码
				return order_sm(order,YinShengBaoConst.SM_WX);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return order_acp(order);
			} 
			
			
			logger.error("银生宝支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("银生宝支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 回调
	 * @param order
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request, String responseBody) {
		
		logger.info("银生宝回调 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//快捷支付
				return notify_q(order,request);
			} 
			
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return notify_wy(order,request);
			} 
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码
				return notify_sm(order,request,responseBody,YinShengBaoConst.SM_ALI);
			} 
			
			if (OutChannel.wx.name().equals(order.getOutChannel())) {
				//微信扫码
				return notify_sm(order,request,responseBody,YinShengBaoConst.SM_WX);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return notify_acp(order,request,responseBody);
			} 
			
			logger.error("银生宝回调 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("银生宝回调 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("银生宝查询 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//快捷支付
				return query_q(order);
			} 
			
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return query_wy(order);
			} 
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码
				return query_sm(order,YinShengBaoConst.SM_ALI);
			} 
			
			if (OutChannel.wx.name().equals(order.getOutChannel())) {
				//微信扫码
				return query_sm(order,YinShengBaoConst.SM_WX);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return query_acp(order);
			} 
			
			logger.error("银生宝查询 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("银生宝查询 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 快捷支付
	 * @param order
	 * @return
	 */
	private R order_q(Order order){
		
		logger.info("银生宝快捷 支付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			map.put("accountId",payMerch);
			map.put("customerId",order.getUserId());
			map.put("orderNo",orderId);
			map.put("commodityName",order.getProduct());
			map.put("amount", order.getAmount().toString());
			String responseUrl = PayService.commonNotifyUrl(order);
			map.put("responseUrl", responseUrl);
			String returnUrl = PayService.commonReturnUrl(order);
			map.put("pageResponseUrl", returnUrl);
			/**参与验签的字段*/
			String sign = "accountId="+payMerch+"&customerId="+order.getUserId()+"&orderNo="+orderId+"&commodityName="+order.getProduct()+"&amount="+order.getAmount().toString()+"&responseUrl="+responseUrl+"&pageResponseUrl="+returnUrl;
			logger.info("银生宝快捷 支付 签名的参数为："+sign);
			sign = MD5Utils.sign(sign, RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY));
			map.put("mac", sign.toUpperCase());
			
			// 确认返回数据
			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, PayService.commonJumpUrl(order));
			} catch (Exception e) {
				logger.error("jump加密异常！！");
				return R.error("加密异常");
			}
			order.setResultMap(resultMap);
			Map<String, Object> jumpData = new HashMap<>();
			jumpData.put(PayConstants.web_params, map);
			jumpData.put(PayConstants.web_form_url, 1);
			jumpData.put(PayConstants.web_action, RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_Q_URL));
			order.setJumpData(jumpData);
			
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.error("银生宝快捷 支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("银生宝快捷 支付：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_q(Order order, HttpServletRequest request) {
		
		logger.info("银生宝快捷 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String result_code = request.getParameter("result_code");
			String result_msg = request.getParameter("result_msg");
			String orderNo = request.getParameter("orderNo");
			String userId = request.getParameter("userId");
			String bankName = request.getParameter("bankName");
			String tailNo = request.getParameter("tailNo");
			String amount = request.getParameter("amount");
			String mac = request.getParameter("mac");
			String payMerch = order.getPayMerch();
			String signParam = "accountId="+payMerch+"&orderNo="+orderNo+"&userId="+userId+"&bankName="+bankName+"&tailNo="+tailNo+"&amount="+amount+"&result_code="+result_code+"&result_msg="+result_msg;
			logger.info("银生宝快捷 支付 回调 参数"+signParam);
			String vsign = MD5Utils.sign(signParam,RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY));
			if(vsign.equalsIgnoreCase(mac)){
				order.setRealAmount(order.getAmount());
				if("0000".equals(result_code)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败";
				}
				return R.ok(msg);
			}else{
				logger.info("银生宝快捷 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("银生宝快捷 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("银生宝快捷支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("银生宝快捷 支付 回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query_q(Order order) {
		
		logger.info("银生宝快捷 支付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			Map<String, String> map = new HashMap<String, String>();
			String payMerch = order.getPayMerch();
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			map.put("accountId",payMerch);
			map.put("orderNo", orderId);
			/** 参与验签的字段 */
			String sign = MD5Utils.getSignParam(map);
			logger.info("银生宝快捷 支付 查询 用于签名参数为：" + sign);
			sign = MD5Utils.sign(sign,RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY));
			map.put("mac", sign.toUpperCase());
			String baowen = MD5Utils.getSignParam(map);
			logger.info("银生宝快捷 支付 查询 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_Q_QUERY_URL);
			String sr = RequestUtils.sendPost(url, baowen);
			logger.info("银生宝快捷 支付 查询 请求地址：" + url);
			logger.info("银生宝快捷 支付 查询 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String respCode = jsonObject.getString("result_code");
			if(!"0000".equals(respCode)){
				return R.error(jsonObject.getString("result_msg"));
			}
			String status = jsonObject.getString("status");
			order.setRealAmount(order.getAmount());
			if("00".equals(status)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("20".equals(status)){
				msg = "处理失败";
				order.setOrderState(OrderState.fail.id());
			}else if("10".equals(status)){
				msg = "订单处理中";
				order.setOrderState(OrderState.ing.id());
			}
			return R.ok(msg);
		} catch (Exception e) {
			logger.info("银生宝快捷 支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		} finally {
			logger.info("银生宝快捷 支付 查询 结束------------------------------------------------------------");
		}
	}
	
	
	
	/**
	 * 网关支付
	 * @param order
	 * @return
	 */
	private R order_wy(Order order){
		
		logger.info("银生宝网关 支付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			
			String version = RedisUtil.getPayCommonValue(YinShengBaoConst.VERSION_WY);
			map.put("version",version);
			map.put("merchantId", payMerch);
			String responseUrl = PayService.commonNotifyUrl(order);
			map.put("merchantUrl", responseUrl);
			map.put("responseMode", "3");
			map.put("orderId",orderId);
			map.put("currencyType","CNY");
			map.put("amount", order.getAmount().toString());
			map.put("assuredPay", "false");
			Long crtDate = new Long(order.getCrtDate())*1000;
			map.put("time", DateUtil.getCurrentNumStr(new Date(crtDate)));
			map.put("remark", "");
			String key = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
//			map.put("merchantKey", key);
			map.put("commodity",order.getProduct());
			String returnUrl = PayService.commonReturnUrl(order);
			map.put("frontURL", returnUrl);
			
			/**参与验签的字段*/
			String sign = "merchantId="+payMerch+"&merchantUrl="+responseUrl+"&responseMode="+map.get("responseMode")+"&orderId="+orderId+"&currencyType="+map.get("currencyType")+"&amount="+map.get("amount")+"&assuredPay="+map.get("assuredPay")+"&time="+map.get("time")+"&remark="+map.get("remark")+"&merchantKey="+key;
			logger.info("银生宝网关 支付 签名的参数为："+sign);
			sign = MD5Utils.md5(sign);
			map.put("mac", sign.toUpperCase());
			logger.info("银生宝网关 支付 签名为：mac="+map.get("mac"));
			// 确认返回数据
			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, PayService.commonJumpUrl(order));
			} catch (Exception e) {
				logger.error("jump加密异常！！");
				return R.error("加密异常");
			}
			order.setResultMap(resultMap);
			Map<String, Object> jumpData = new HashMap<>();
			jumpData.put(PayConstants.web_params, map);
			jumpData.put(PayConstants.web_form_url, 1);
			jumpData.put(PayConstants.web_action, RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_WY_URL));
			order.setJumpData(jumpData);
			
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.error("银生宝网关 支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("银生宝网关 支付：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_wy(Order order, HttpServletRequest request) {
		
		logger.info("银生宝网关 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String merchantId = request.getParameter("merchantId");
			String responseMode = request.getParameter("responseMode");
			String orderId = request.getParameter("orderId");
			String currencyType = request.getParameter("currencyType");
			String amount = request.getParameter("amount");
			String returnCode = request.getParameter("returnCode");
			String returnMessage = request.getParameter("returnMessage");
			String mac = request.getParameter("mac") ;
			
			String payMerch = order.getPayMerch();
			String merchantKey = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
			
			boolean success = "0000".equals(returnCode);
			boolean paid = "0001".equals(returnCode);
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append("merchantId=").append(merchantId);
			s.append("&responseMode=").append(responseMode);
			s.append("&orderId=").append(orderId);
			s.append("&currencyType=").append(currencyType);
			s.append("&amount=").append(amount);
			s.append("&returnCode=").append(returnCode);
			s.append("&returnMessage=").append(returnMessage);
			s.append("&merchantKey=").append(merchantKey);
			logger.info("银生宝网关 回调 参数"+s.toString());
			String vsign = MD5Utils.md5(s.toString());
			if(vsign.equalsIgnoreCase(mac)){
				order.setRealAmount(order.getAmount());
				if(success || paid){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败";
				}
				return R.ok(msg);
			}else{
				logger.info("银生宝网关 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("银生宝网关 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("银生宝支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("银生宝网关 支付 回调 结束-------------------------------------------------");
		}
	}
	
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query_wy(Order order) {
		
		logger.info("银生宝网关 支付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			Map<String, String> map = new HashMap<String, String>();
			String payMerch = order.getPayMerch();
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			map.put("merchantId",payMerch);
			map.put("orderId", orderId);
			String key = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
			/** 参与验签的字段 */
			String sign = MD5Utils.getSignParam(map);
			logger.info("银生宝网关 支付 查询 用于签名参数为：" + sign);
			sign = MD5Utils.md5(sign+"&merchantKey="+key);
			map.put("mac", sign.toUpperCase());
			String baowen = MD5Utils.getSignParam(map);
			logger.info("银生宝网关 支付 查询 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_WY_QUERY_URL);
			String sr = RequestUtils.sendPost(url, baowen);
			logger.info("银生宝网关 支付 查询 请求地址：" + url);
			logger.info("银生宝网关 支付 查询 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			String vsignStr = "";
			String[] params = sr.split("\\|");
			for (int i = 0; i < params.length-2; i++) {
				vsignStr+= params[i]+"|";
			}
			vsignStr += key;
			String vsign = MD5Utils.md5(vsignStr);
			if(vsign.equalsIgnoreCase(params[6])) {
				String code = params[3];
				if(!"0000".equals(code)){
					return R.error(params[4]);
				}
				String status = params[5];
				order.setRealAmount(order.getAmount());
				if("3".equals(status)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else if("4".equals(status)){
					msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
				}else if("6".equals(status)){
					msg = "处理取消";
					order.setOrderState(OrderState.fail.id());
				}else if("0".equals(status) || "20".equals(status)){
					msg = "订单处理中";
					order.setOrderState(OrderState.ing.id());
				}
				return R.ok(msg);
			}else {
				return R.error("验签不通过");
			}
		} catch (Exception e) {
			logger.info("银生宝网关 支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		} finally {
			logger.info("银生宝网关 支付 查询 结束------------------------------------------------------------");
		}
	}
	
	/**
	 * 扫码支付
	 * @param order
	 * @return
	 */
	private R order_sm(Order order,String payType){
		
		logger.info("银生宝"+order.getOutChannel()+"扫码 支付：---------------------开始--------------------");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			map.put("accountId",payMerch);
			map.put("payType",payType);
			map.put("orderId",orderId);
			String commodity = URLEncoder.encode(order.getProduct(),"utf-8");
			map.put("commodity",commodity);
			map.put("amount", order.getAmount().toString());
			String responseUrl = PayService.commonNotifyUrl(order);
			map.put("responseUrl", responseUrl);
			map.put("ext", "");
			/**参与验签的字段*/
			String sign = "accountId="+payMerch+"&payType="+payType+"&orderId="+orderId+"&commodity="+commodity+"&amount="+order.getAmount().toString()+"&responseUrl="+responseUrl;
			logger.info("银生宝扫码 支付 签名的参数为："+sign);
			sign = MD5Utils.sign(sign, RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY1));
			map.put("mac", sign.toUpperCase());
			
			String baowen = JSONObject.toJSONString(map);
			logger.info("银生宝扫码 支付 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_SM_URL);
			String sr = RequestUtils.doPostJson(url, baowen);
			logger.info("银生宝 扫码 支付 请求地址：" + url);
			logger.info("银生宝 扫码 支付 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String result_code = jsonObject.getString("result_code");
			if(!"0000".equals(result_code)){
				return R.error(jsonObject.getString("result_msg"));
			}
			Map<String, String> resultMap = PayService.initRspData(order);
			String qrcode = jsonObject.getString("qrcode");
			resultMap.put(PayConstants.web_qrcode_url, qrcode);
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.error("银生宝扫码 支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("银生宝"+order.getOutChannel()+"扫码 支付：----------------结束-----------------");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_sm(Order order, HttpServletRequest request,String responseBody,String payType) {
		
		logger.info("银生宝"+order.getOutChannel()+"扫码 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			logger.info("银生宝"+order.getOutChannel()+"扫码 支付 回调 返回参数：" + responseBody);
			JSONObject jo = JSONObject.parseObject(responseBody);
			String orderId = jo.getString("orderId");
			String amount = jo.getString("amount");
			String returnCode = jo.getString("result_code");
			String returnMessage = jo.getString("result_msg");
			String mac = jo.getString("mac") ;
			
			String payMerch = order.getPayMerch();
			String merchantKey = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
			
			boolean success = "0000".equals(returnCode);
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append("accountId=").append(payMerch);
			s.append("&orderId=").append(orderId);
			s.append("&amount=").append(amount);
			s.append("&result_code=").append(returnCode);
			s.append("&result_msg=").append(returnMessage);
			s.append("&key=").append(merchantKey);
			logger.info("银生宝扫码 回调 参数"+s.toString());
			String vsign = MD5Utils.md5(s.toString());
			if(vsign.equalsIgnoreCase(mac)){
				order.setRealAmount(order.getAmount());
				if(success){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败";
				}
				return R.ok(msg+","+returnMessage);
			}else{
				logger.info("银生宝扫码 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("银生宝扫码 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("银生宝支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("银生宝"+order.getOutChannel()+"扫码 支付 回调 结束-------------------------------------------------");
		}
	}
	
	
	/**
	 * 扫码查询
	 * @param order
	 * @return
	 */
	private R query_sm(Order order,String payType){
		
		logger.info("银生宝"+order.getOutChannel()+"扫码 查询：---------------------开始--------------------");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			map.put("accountId",payMerch);
			map.put("orderId",orderId);
			/**参与验签的字段*/
			String sign = "accountId="+payMerch+"&orderId="+orderId;
			logger.info("银生宝扫码 查询 签名的参数为："+sign);
			sign = MD5Utils.sign(sign, RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY1));
			map.put("mac", sign.toUpperCase());
			
			String baowen = JSONObject.toJSONString(map);
			logger.info("银生宝扫码 查询 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_SM_QUERY_URL);
			String sr = RequestUtils.doPostJson(url, baowen);
			logger.info("银生宝 扫码 查询 请求地址：" + url);
			logger.info("银生宝 扫码 查询 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String result_code = jsonObject.getString("result_code");
			if(!"0000".equals(result_code)){
				return R.error(jsonObject.getString("result_msg"));
			}
			String msg = "";
			String status = jsonObject.getString("status");
			order.setRealAmount(order.getAmount());
			if("00".equals(status)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("20".equals(status)){
				msg = "处理失败";
				order.setOrderState(OrderState.fail.id());
			}else if("10".equals(status)){
				msg = "订单处理中";
				order.setOrderState(OrderState.ing.id());
			}
			order.setBusinessNo(jsonObject.getString("bankSerialNo"));
			String desc = jsonObject.getString("desc");
			return R.ok(msg+":"+desc);
		} catch (Exception e) {
			logger.error("银生宝扫码 查询 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("查询异常");
		} finally {
			logger.info("银生宝"+order.getOutChannel()+"扫码 查询：----------------结束-----------------");
		}
	}
	
	
	/**
	 * 代付
	 * @param order
	 * @return
	 */
	private R order_acp(Order order){
		
		logger.info("银生宝 代付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			
			map.put("accountId",payMerch);
			map.put("name",order.getAcctName());
			map.put("cardNo",order.getBankNo());
			map.put("orderId",orderId);
			map.put("purpose",order.getAcctName());
			map.put("amount", order.getAmount().setScale(2).toString());
			String responseUrl = PayService.commonAcpNotifyUrl(order);
			map.put("responseUrl", responseUrl);
			String key = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
			/**参与验签的字段*/
			String sign = "accountId="+payMerch+"&name="+map.get("name")+"&cardNo="+map.get("cardNo")+"&orderId="+orderId+"&purpose="+map.get("purpose")+"&amount="+map.get("amount")+"&responseUrl="+map.get("responseUrl")+"&key="+key;
			logger.info("银生宝 代付 签名的参数为："+sign);
			sign = MD5Utils.md5(sign);
			map.put("mac", sign.toUpperCase());
			String baowen = MD5Utils.getSignParam(map);
			logger.info("银生宝 代付 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_ACP_URL);
			String sr = RequestUtils.sendPost(url, baowen);
			logger.info("银生宝 代付 请求地址：" + url);
			logger.info("银生宝 代付 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String respCode = jsonObject.getString("result_code");
			String resultMsg = jsonObject.getString("result_msg");
			if(!"0000".equals(respCode)){
				return R.error(resultMsg);
			}
			order.setOrderState(OrderState.ing.id());
			order.setRealAmount(order.getAmount());
			return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resultMsg);
		} catch (Exception e) {
			logger.error("银生宝 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("银生宝 代付：}");
		}
	}
	
	/**
	 * @Description 代付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_acp(Order order, HttpServletRequest request,String responseBody) {
		
		logger.info("银生宝 代付 回调 开始-------------------------------------------------"+responseBody);
		String msg = "";
		try {
			String orderId = request.getParameter("orderId");
			String amount = request.getParameter("amount");
			String returnCode = request.getParameter("result_code");
			String returnMessage = request.getParameter("result_msg");
			String mac = request.getParameter("mac") ;
			
			String payMerch = order.getPayMerch();
			String merchantKey = RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY);
			
			boolean success = "0000".equals(returnCode);
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append("accountId=").append(payMerch);
			s.append("&orderId=").append(orderId);
			s.append("&amount=").append(amount);
			s.append("&result_code=").append(returnCode);
			s.append("&result_msg=").append(returnMessage);
			s.append("&key=").append(merchantKey);
			logger.info("银生宝 代付 回调 参数"+s.toString());
			String vsign = MD5Utils.md5(s.toString());
			if(vsign.equalsIgnoreCase(mac)){
				order.setRealAmount(order.getAmount());
				if(success){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败";
				}
				return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+returnMessage);
			}else{
				logger.info("银生宝 代付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("银生宝 代付 回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("银生宝代付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("银生宝 代付 回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R query_acp(Order order) {
		
		logger.info("银生宝 代付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			Map<String, String> map = new HashMap<String, String>();
			String payMerch = order.getPayMerch();
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			map.put("accountId",payMerch);
			map.put("orderId", orderId);
			/** 参与验签的字段 */
			String sign = MD5Utils.getSignParam(map);
			logger.info("银生宝 代付 查询 用于签名参数为：" + sign);
			sign = MD5Utils.sign(sign,RedisUtil.getPayCommonValue(payMerch + YinShengBaoConst.KEY));
			map.put("mac", sign.toUpperCase());
			String baowen = MD5Utils.getSignParam(map);
			logger.info("银生宝 代付 查询 上送的报文为：" + baowen);
			String url = RedisUtil.getPayCommonValue(YinShengBaoConst.REQ_ACP_QUERY_URL);
			String sr = RequestUtils.sendPost(url, baowen);
			logger.info("银生宝 代付 查询 请求地址：" + url);
			logger.info("银生宝 代付 查询 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String respCode = jsonObject.getString("result_code");
			if(!"0000".equals(respCode)){
				return R.error(jsonObject.getString("result_msg"));
			}
			String status = jsonObject.getString("status");
			order.setRealAmount(order.getAmount());
			if("00".equals(status)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("20".equals(status)){
				msg = "处理失败";
				order.setOrderState(OrderState.fail.id());
			}else if("10".equals(status)){
				msg = "订单处理中";
				order.setOrderState(OrderState.ing.id());
			}else{
				return R.error( "状态未知！");
			}
			String desc = jsonObject.getString("desc");
			return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+desc);
		} catch (Exception e) {
			logger.info("银生宝 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询 异常："+e.getMessage());
		} finally {
			logger.info("银生宝 代付 查询 结束------------------------------------------------------------");
		}
	}
}
