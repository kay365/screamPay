package com.qh.paythird.wft;


import java.net.URLDecoder;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.PayService;
import com.qh.paythird.wft.utils.Base64;
import com.qh.paythird.wft.utils.HttpHelper;
import com.qh.paythird.wft.utils.HttpMethodType;
import com.qh.paythird.wft.utils.Md5;
import com.qh.paythird.wft.utils.RSAUtil;
import com.qh.redis.service.RedisUtil;

/**
 * 威富通
 * @author Swell
 *
 */
@Service
public class WeiFuTongService{

	private static final Logger logger = LoggerFactory.getLogger(WeiFuTongService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("威富通支付 开始------------------------------------------------------");
		try {
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//网银网关支付
				return order_ali(order);
			} 
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return order_acp(order);
			}
			logger.error("威富通支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("威富通支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 回调
	 * @param order
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request, String responseBody) {
		
		logger.info("威富通回调 开始------------------------------------------------------");
		try {
			
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//网银网关支付
				return notify_ali(order,request);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return notify_acp(order,request,responseBody);
			}

			logger.error("威富通回调 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("威富通回调 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("威富通查询 开始------------------------------------------------------");
		try {
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码支付
				return query_ali(order);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return query_acp(order);
			}

			logger.error("威富通查询 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("威富通查询 结束------------------------------------------------------");
		}
	}
	
	
	/**
	 * 预下单支付接口   支付宝
	 * @param order
	 * @return
	 */
	private R order_ali(Order order){
		try {
			String merchantCode = order.getMerchNo();
	        //订单号最大长度30位  需做判断处理
			String orderId = merchantCode + order.getOrderNo();
			String domain = RedisUtil.getPayCommonValue(WeiFuTongConst.wft_reqUrl);
			
			//业务参数
	        JSONObject json = new JSONObject();
	        json.put("orderId", orderId);
	        json.put("amount", ParamUtil.yuanToFen(order.getAmount()));

	        json.put("returnUrl", PayService.commonReturnUrl(order));
	        json.put("body", order.getProduct());
	        json.put("merchantCode", order.getPayMerch());
	        json.put("version", WeiFuTongConst.wft_version);
	        json.put("notifyUrl",PayService.commonNotifyUrl(order));
			json.put("terminalIp",order.getReqIp());

	        JSONObject res = pay_request(domain+WeiFuTongConst.payOrder, json,order);
	        logger.info("下单返回结果："+res);
	        if(res == null){
	        	return R.error("预下单接口返回参数为空");
	        }
	        String resMessage = res.getString("msg");
	        
	        if(WeiFuTongConst.F.equals(res.get("success").toString())){
	        	return R.error(resMessage);
	        }
	        //下单返回结果：{"tranId":"5c186836-e3b7-4ce9-b6fe-33699d0b2b79","success":true,"msg":"下单成功","orderId":"test20160725000005"}
	        //下单返回结果：{"success":false,"msg":"订单号重复"}

	        //以下二选一
	        //支付【跳转支付】
	       // String url = getLink(res.getString("tranId"));
	        //logger.info("支付链接：" + url);
	        //支付【获取支付二维码】
	        String imgCode = getCode(res.getString("tranId"),order);
	        logger.info("支付二维码：" + imgCode);
	        order.setBusinessNo(res.getString("tranId"));
	        Map<String,String> data = PayService.initRspData(order);
			data.put(PayConstants.web_qrcode_url, imgCode);
			return R.okData(data);
		}catch (Exception e) {
			logger.error("威富通支付 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} 
	}
	
	private R query_ali(Order order){
		String domain = RedisUtil.getPayCommonValue(WeiFuTongConst.wft_reqUrl);
		//业务参数
		String tranId = order.getBusinessNo();
		if(ParamUtil.isEmpty(tranId)){
			logger.info("交易号不存在！");
			return R.error("交易号不存在！");
		}
        JSONObject json = new JSONObject();
        try {
        	json.put("version", WeiFuTongConst.wft_version);
			json.put("tranId",tranId);
			JSONObject res = pay_request(domain+WeiFuTongConst.payQuery, json,order);
			//查询订单返回结果：{"status":0,"success":true,"msg":"查询成功","orderId":"test20160725000005"}
			logger.info("查询订单返回结果：" + res);
			if(res == null){
				return R.error("查询返回参数为空！");
			}
			String rescode = res.get("success").toString();
			String msg = res.getString("msg");
			String status = res.get("status").toString();
			
			if(WeiFuTongConst.F.equals(rescode)){
				return R.error(msg);
			}
			
			order.setRealAmount(order.getAmount());
			if("0".equals(status)){
				order.setOrderState(OrderState.ing.id());
				msg = "订单待支付";
			}else if("1".equals(status)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单支付成功";
			}else{
				order.setOrderState(OrderState.fail.id());
				msg = "订单支付失败";
			}
			
			return R.ok(msg);
		} catch (JSONException e) {
			logger.info("威富通 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("威富通查询 异常："+e.getMessage());
		}

	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	private R notify_ali(Order order, HttpServletRequest request) {
		
		logger.info("威富通 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String status = request.getParameter("status");
			String tranId = request.getParameter("tranId");
			String orderId = request.getParameter("orderId");
			String signature = request.getParameter("signature");
			String amount = request.getParameter("amount");
			
			String payMerch = order.getPayMerch();
			String key = RedisUtil.getPayCommonValue(payMerch +WeiFuTongConst.wft_key);
			
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append(status);
			s.append(orderId);
			s.append(tranId);
			s.append(amount);
			s.append(key);
			
			logger.info("威富通 回调 参数"+s.toString());
			String vsign = Md5Util.MD5(s.toString());
			logger.info("威富通 回调参数加密结果："+vsign);
			if(vsign.equalsIgnoreCase(signature)){
				
				order.setRealAmount(order.getAmount());
				if("0".equals(status)){
					order.setOrderState(OrderState.ing.id());
					msg = "待支付";
				}else if("1".equals(status)){
					order.setOrderState(OrderState.succ.id());
					msg = "支付成功";
				}else if("2".equals(status)){
					order.setOrderState(OrderState.fail.id());
					msg = "支付失败";
				}
				return R.ok(msg);
			}else{
				logger.info("威富通 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("威富通 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("威富通支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("威富通 支付 回调 结束-------------------------------------------------");
		}
	}
	
	
	
	
	/**
	 * 代付
	 * @param order
	 * @return
	 */
	
	private R order_acp(Order order){
		String publicKey1 = RedisUtil.getPayCommonValue(order.getPayMerch()+WeiFuTongConst.wft_publickey);
		String appid = RedisUtil.getPayCommonValue(order.getPayMerch()+WeiFuTongConst.wft_appId);
		String domain = RedisUtil.getPayCommonValue(WeiFuTongConst.wft_reqUrl);
		
		logger.info("威富通 代付开始：{");
		try {
			String merchantCode = order.getMerchNo();
	        //订单号最大长度30位  需做判断处理
			String orderId = merchantCode + order.getOrderNo();
			JSONObject jsonObject = new JSONObject();
	        jsonObject.put("orderNo",orderId);
	        jsonObject.put("isCompay","0"); //0 对私  1对公
	        // jsonObject.put("cnapsNo","18564652485"); 公户必传  联行号
	        jsonObject.put("city",order.getBankCity());
	        jsonObject.put("bankName",order.getBankName());
	        jsonObject.put("cardNumber",order.getBankNo());
	        jsonObject.put("accountName",order.getAcctName());
	        jsonObject.put("amount",ParamUtil.yuanToFen(order.getAmount()));
	        jsonObject.put("version", WeiFuTongConst.wft_version);
	        logger.info("代付请求参数："+jsonObject.toString());
	        logger.info("公钥："+publicKey1);
	        logger.info("请求地址："+domain+WeiFuTongConst.payAcp);
	        PublicKey publicKey = RSAUtil.loadPublicKey(publicKey1);
	        // 加密
	        byte[] encryptByte = RSAUtil.encryptData(jsonObject.toString().getBytes("utf-8"), publicKey);
	        String params = Base64.encode(encryptByte);
	        logger.info("加密后请求参数"+params);
	        Map<String,Object> map = new HashMap<String,Object>();
	        map.put("appid", appid);
	        map.put("params", params);
	        String result = new HttpHelper().sendPostHttp(domain+WeiFuTongConst.payAcp, map, false);
	        logger.info("代付返回参数："+result);
	        if(StringUtils.isEmpty(result)){
	        	return R.error("代付返回参数为空");
	        }
	        JSONObject jsonRs = new JSONObject(result);
	        String resCode = jsonRs.get("success").toString();
	        String resMessage = jsonRs.getString("msg");
	        if(WeiFuTongConst.F.equals(resCode)){
	        	return R.error(resMessage);
	        }
	        
	        order.setRealAmount(order.getAmount());
	        order.setOrderState(OrderState.ing.id());
	        return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resMessage);
		} catch (Exception e) {
			logger.error("威富通 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("威富通 代付结束：}");
		}
	}
	
	/**
	 * @Description 代付回调
	 * @param order
	 * @param request
	 * @return
	 */
	private R notify_acp(Order order, HttpServletRequest request,String responseBody) {
		
		logger.info("威富通 支付 回调 开始-------------------------------------------------");
		logger.info("回调参数"+responseBody);
		String msg ="";
		try {
			String status = request.getParameter("status");
			String orderId = request.getParameter("orderId");
			String signature = request.getParameter("signature");
			String amount = request.getParameter("amount");
			String message = request.getParameter("msg");
			
			String payMerch = order.getPayMerch();
			String key = RedisUtil.getPayCommonValue(payMerch +WeiFuTongConst.wft_key);
			
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append(status);
			s.append(orderId);
			if(message == null){
				s.append("");
			}
			s.append(amount);
			s.append(key);
			logger.info("回调signature："+signature);
			logger.info("威富通 回调 参数"+s.toString());
			String vsign = Md5Util.MD5(s.toString());
			logger.info("威富通 回调参数加密结果："+vsign);
			if(vsign.equalsIgnoreCase(signature)){
				
				order.setRealAmount(order.getAmount());
				if("0".equals(status)){
					order.setOrderState(OrderState.ing.id());
					msg = "待转账";
				}else if("3".equals(status)){
					order.setOrderState(OrderState.ing.id());
					msg = "转账中";
				}else if("1".equals(status)){
					order.setOrderState(OrderState.succ.id());
					msg = "成功";
				}else if("2".equals(status)){
					order.setOrderState(OrderState.fail.id());
					msg = "失败";
				}else if("-1".equals(status)){
					order.setOrderState(OrderState.fail.id());
					msg = "审核失败";
				}
				return R.ok(msg);
			}else{
				logger.info("威富通 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("威富通 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("威富通支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("威富通 支付 回调 结束-------------------------------------------------");
		}
	}
	

	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	private R query_acp(Order order) {
		String domain = RedisUtil.getPayCommonValue(WeiFuTongConst.wft_reqUrl);
		//https://Pay.heepay.com/API/PayTransit/QueryTransfer.aspx
		logger.info("威富通 代付 查询 开始------------------------------------------------------------");
		//业务参数
		String merchantCode = order.getMerchNo();
        //订单号最大长度30位  需做判断处理
		String orderId = merchantCode + order.getOrderNo();
				if(ParamUtil.isEmpty(orderId)){
					logger.info("订单号不存在！");
					return R.error("订单号不存在！");
				}
		        JSONObject json = new JSONObject();
		        try {
		        	json.put("type", WeiFuTongConst.wft_type_acp);
		        	json.put("version", WeiFuTongConst.wft_version);
					json.put("orderId",orderId);
					JSONObject res = pay_request(domain+WeiFuTongConst.payQuery, json,order);
					//查询订单返回结果：{"status":0,"success":true,"msg":"查询成功","orderId":"test20160725000005"}
					logger.info("查询订单返回结果：" + res);
					if(res == null){
						return R.error("查询返回参数为空！");
					}
					String rescode = res.get("success").toString();
					String msg = res.getString("msg");
					String status = res.get("status").toString();
					
					if(WeiFuTongConst.F.equals(rescode)){
						return R.error(msg);
					}
					
					order.setRealAmount(order.getAmount());
					if("0".equals(status)||"5".equals(status)||"2".equals(status)){
						order.setOrderState(OrderState.ing.id());
						msg = "订单处理中";
					}else if("1".equals(status)){
						order.setOrderState(OrderState.fail.id());
						msg = "订单审核失败";
					}else if("3".equals(status)){
						order.setOrderState(OrderState.succ.id());
						msg = "订单转账成功";
					}else if("4".equals(status)){
						order.setOrderState(OrderState.fail.id());
						msg = "订单转账失败";
					}else{
						order.setOrderState(OrderState.fail.id());
						msg = "订单号不存在";
					}
					
					return R.ok(msg);
				} catch (JSONException e) {
					logger.info("威富通 查询 异常："+e.getMessage());
					e.printStackTrace();
					return R.error("威富通查询 异常："+e.getMessage());
				}
	}
	
	private static JSONObject pay_request(String api, JSONObject json,Order order) throws RuntimeException, JSONException {
		String merchantId = order.getPayMerch();
		String key = RedisUtil.getPayCommonValue(merchantId+WeiFuTongConst.wft_key);
		String appid = RedisUtil.getPayCommonValue(merchantId+WeiFuTongConst.wft_appId);
        
		HttpHelper http = new HttpHelper();
        //业务参数加密
        Map<String, Object> map = new HashMap<String, Object>();
        String input = json.toString();
        String encoded = Base64.encode(input.getBytes());

        //验签加密
        String newstr = input+key;
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();
        //appid
        map.put("appid", appid);
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        logger.info(appid+","+encoded+","+return_bigstr);
        return http.getJSONFromHttp(api, map, HttpMethodType.POST);
    }
	
	
	/**
	 * 下单支付跳转链接
	 * @param tranId
	 * @return
	 * @throws JSONException
	 */
    /*public  String getLink(String tranId) throws JSONException{
        JSONObject json = new JSONObject();
        json.put("version","3");
        json.put("way","ali_h5");

        json.put("tranId", tranId);

        String input = json.toString();
        String encoded = Base64.encode(input.getBytes());

        //签名参数
        String newstr = input + URL.key;
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();

        //appid参数
        String appid = URL.appid;
        String url = domain+WeiFuTongConst.payJump+"?appid="+appid+"&params="+encoded+"&signs="+return_bigstr;
        return url;
    }*/
    /**
     * 二维码支付方式
     * @param tranId
     * @return
     * @throws JSONException
     */
    public String getCode(String tranId,Order order) throws JSONException{
    	String merchantId = order.getPayMerch();
    	String domain = RedisUtil.getPayCommonValue(WeiFuTongConst.wft_reqUrl);
    	String appid = RedisUtil.getPayCommonValue(merchantId+WeiFuTongConst.wft_appId);
    	
		String key = RedisUtil.getPayCommonValue(merchantId+WeiFuTongConst.wft_key);
		
        JSONObject json = new JSONObject();
        json.put("version",WeiFuTongConst.wft_version);
        json.put("way","ali_qr");

        json.put("tranId", tranId);

        String input = json.toString();
        logger.info("支付跳转请求参数："+json);
        String encoded = Base64.encode(input.getBytes());

        //签名参数
        String newstr = input + key;
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();

        //appid参数
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("appid", appid);
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        String res = new HttpHelper().sendHttp(domain+WeiFuTongConst.payQr,map, HttpMethodType.GET);
        logger.info("获取二维码接口返回数据"+res);
        JSONObject resJson = new JSONObject(res);
        return resJson.getString("codeUrl");
    }
    

}
