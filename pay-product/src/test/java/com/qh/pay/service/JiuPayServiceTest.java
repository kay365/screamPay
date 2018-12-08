package com.qh.pay.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.pay.api.constenum.CardSendType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;

/**
 * @ClassName JiuPayServiceTest
 * @Description 九派支付测试
 * @Date 2017年11月22日 下午5:42:56
 * @version 1.0.0
 */
public class JiuPayServiceTest extends PayBaseServiceTest{
	/**
	 * 
	 *网银订单支付测试
	 * @throws Exception 
	 */
	@Test
	public void order_test() throws Exception{
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		jsObj.put("merchNo", merchNo);
		//订单号
		jsObj.put("orderNo", reqTime + new Random().nextInt(10000));	
		//支付渠道
		jsObj.put("outChannel", OutChannel.wy.name());
		if(OutChannel.wy.name().equals(jsObj.get("outChannel"))){
			jsObj.put("bankCode", "CMB");
			jsObj.put("bankName", "招商银行");
		}
		//订单标题 
		jsObj.put("title", "商城网银下单");
		//产品名称
		jsObj.put("product", "产品名称");
		//支付金额 单位 元 
		jsObj.put("amount", String.valueOf(new Random().nextInt(10000)));
		//币种
		jsObj.put("currency", "CNY");
		//前端返回地址
		jsObj.put("returnUrl", "http://www.baidu.com");
		//后台通知地址
		jsObj.put("notifyUrl", "http://www.baidu.com");
		//请求时间
		jsObj.put("reqTime", reqTime);
		//userId
		jsObj.put("userId", "123456789");
		//对公
		jsObj.put("acctType", 1);
		logger.info("请求source:" + jsObj.toString());
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url, jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("网银支付链接地址：{}", jo.getString("code_url"));
				
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
	/*@Test
	public void save_orderSuccData(){
		String orderNo = "201712081629424391";
		Order order = RedisUtil.getOrder(merchNo,orderNo);
		order.setOrderState(OrderState.succ.id());
		order.setRealAmount(order.getAmount());
		order.setBusinessNo(orderNo);
		RedisUtil.setOrder(order);
		payService.orderDataMsg(merchNo,orderNo);
	}*/
	
	
	/**
	 * 
	 *快捷订单支付测试
	 * @throws Exception 
	 */
	@Test
	public void order_q_test() throws Exception{
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		jsObj.put("merchNo", merchNo);
		//订单号
		jsObj.put("orderNo", reqTime + new Random().nextInt(10000));	
		//支付渠道
		jsObj.put("outChannel", OutChannel.q.name());
		if(OutChannel.q.name().equals(jsObj.get("outChannel"))){
			jsObj.put("bankCode", "CMB");
			jsObj.put("bankName", "招商银行");
		}
		//订单标题 
		jsObj.put("title", "商城网银快捷下单");
		//产品名称
		jsObj.put("product", "产品名称");
		//支付金额 单位 元 
		jsObj.put("amount", String.valueOf(new Random().nextInt(1000000)));
		//币种
		jsObj.put("currency", "CNY");
		//前端返回地址
		jsObj.put("returnUrl", "http://www.baidu.com");
		//后台通知地址
		jsObj.put("notifyUrl", "http://www.baidu.com");
		//请求时间
		jsObj.put("reqTime", reqTime);
		//userId
		jsObj.put("userId", "123456789");
		//对公
		jsObj.put("acctType", 1);
		logger.info("请求source:" + jsObj.toString());
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url, jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("快捷链接地址：{}", jo.getString("code_url"));
				logger.info("快捷订单号：{}", jo.getString("orderNo"));
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
	/**
	 * 
	 * @Description 绑卡
	 */
	@Test
	public void bind_card(){
		String cardUrl = card_url + "/bind";
		Map<String,String> dataMap = new HashMap<>();
		//商户号
		dataMap.put("merchNo", merchNo);
		//商户号用户标识
		dataMap.put("userId", "123456789");
		//商户订单号
		dataMap.put("orderNo", "2017112714502128");
		//姓名
		dataMap.put("acctName", "张三");
		dataMap.put("phone", "18100000000");
		//1 为身份证号码
		dataMap.put("certType", "1");
		dataMap.put("acctType", "");
		//身份证号码
		dataMap.put("certNo", "510265790128303");
		//银行卡类型
		dataMap.put("cardType", "0");
		dataMap.put("bankNo", "6226090000000048");
		dataMap.put("validDate", "");
		dataMap.put("cvv2", "");
		String source = ParamUtil.buildAllParams(dataMap, true);
		String result = RequestUtils.doPost(cardUrl, source);
		logger.info("请求结果！{}",result);
		JSONObject jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			logger.info("协议号：{}", jo.getString("sign"));
		}
	}
	
	/**
	 * 
	 * 短信重发
	 */
	@Test
	public void card_msgResend(){
		String cardUrl = card_url + "/msgResend";
		Map<String,String> dataMap = new HashMap<>();
		//商户号
		dataMap.put("merchNo", merchNo);
		//订单号
		dataMap.put("orderNo","201712121632071771");
		//签约号
		dataMap.put("sign", "201712120000039000");
		//短信发送类型
		dataMap.put("sendType", String.valueOf(CardSendType.bind.id()));
		String source = ParamUtil.buildAllParams(dataMap, true);
		String result = RequestUtils.doPost(cardUrl, source);
		logger.info("请求结果！{}",result);
	}
	
	/**
	 * 
	 * @Description 绑卡确认
	 */
	@Test
	public void bind_card_comfirm(){
		String cardUrl = card_url + "/bind/confirm";
		Map<String,String> dataMap = new HashMap<>();
		//商户号
		dataMap.put("merchNo", merchNo);
		//订单号
		dataMap.put("orderNo","2017112714502128");
		//签约号
		dataMap.put("sign", "201711270000027627");
		//验证码
		dataMap.put("checkCode", "111111");
		
		String source = ParamUtil.buildAllParams(dataMap, true);
		String result = RequestUtils.doPost(cardUrl, source);
		logger.info("请求结果！{}",result);
		JSONObject jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			logger.info("协议号：{}", jo.getString("sign"));
		}
	}
	
	
	
	/**
	 * 
	 * 快捷订单支付发起
	 * @throws Exception 
	 */
	@Test
	public void order_q_pay() throws Exception{
		String cardUrl = card_url + "/pay";
		Map<String,String> dataMap = new HashMap<>();
		//签约号
		dataMap.put("sign", "201711270000027627");
		//商户号
		dataMap.put("merchNo", merchNo);
		//订单号
		dataMap.put("orderNo", "2017112714502128");
		//银行卡类型
		dataMap.put("cardType", "0");
		logger.info("请求source:" + dataMap.toString());
		String source = ParamUtil.buildAllParams(dataMap, true);
		String result = RequestUtils.doPost(cardUrl, source);
		logger.info("请求结果！{}",result);
		JSONObject jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			logger.info("返回消息：{}", jo.getString("msg"));
		}
	}
	
	
	/**
	 * 
	 * 快捷订单支付确认
	 * @throws Exception 
	 */
	@Test
	public void order_q_pay_confirm() throws Exception{
		String cardUrl = card_url + "/pay/confirm";
		Map<String,String> dataMap = new HashMap<>();
		//商户号
		dataMap.put("merchNo", merchNo);
		//订单号
		dataMap.put("orderNo", "2017112714502128");
		//短信验证码
		dataMap.put("checkCode", "111111");
		logger.info("请求source:" + dataMap.toString());
		String source = ParamUtil.buildAllParams(dataMap, true);
		String result = RequestUtils.doPost(cardUrl, source);
		logger.info("请求结果！{}",result);
		JSONObject jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			logger.info("返回消息：{}", jo.getString("msg"));
		}
	}
	
	
	
	
	/**
	 * 
	 * @Description 支付订单查询
	 * @throws Exception
	 */
	@Test
	public void order_query() throws Exception{
		String orderNo = "1801051118786263110025001960";
		JSONObject jsObj = new JSONObject();
		//商户号
		jsObj.put("merchNo", merchNo);
		jsObj.put("orderNo", orderNo);
		
		//setOrderIng(orderNo);
		
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url + "/query", jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("订单支付状态:{}", jo.getString("orderState"));
				if("1".equals(jo.getString("orderState"))){
					//回调成功通知订单保存
					payService.orderDataMsg(merchNo, orderNo);
				}
			}else{
				logger.info("验签失败！{}");
			}
			
		}
		
	}

	/**
	 * 
	 * @Description 代付订单
	 * @throws Exception
	 */
	@Test
	public void order_acp() throws Exception{
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		jsObj.put("merchNo", merchNo);
		//商户号用户标识
		jsObj.put("userId", "123456789");
		//订单号
		jsObj.put("orderNo", reqTime + new Random().nextInt(10000));	
		//支付渠道  ---acp
		jsObj.put("outChannel", OutChannel.acp.name());
		jsObj.put("bankName", "招商银行");
		jsObj.put("bankCode", "CMB");
		jsObj.put("bankNo", "6226090000000048");
		jsObj.put("acctName", "张三");
		jsObj.put("certNo", "6226090000000048");
		//订单标题 
		jsObj.put("title", "商城jp代付");
		//产品名称
		jsObj.put("product", "产品名称");
		//代付付金额 单位 元 
		jsObj.put("amount", String.valueOf(new Random().nextInt(100)));
		//币种
		jsObj.put("currency", "CNY");
		//后台通知地址
		jsObj.put("notifyUrl", "http://www.baidu.com");
		//请求时间
		jsObj.put("reqTime", reqTime);
		
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url + "/acp", jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
	/**
	 * 
	 * @Description 代付订单查询
	 * @throws Exception
	 */
	@Test
	public void order_acp_query() throws Exception{
		JSONObject jsObj = new JSONObject();
		//商户号
		jsObj.put("merchNo", merchNo);
		//订单号
		jsObj.put("orderNo", "201711282103548");	
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url + "/acp/query", jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("订单代付付状态:{}", jo.getString("orderState"));
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
}
