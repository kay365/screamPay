package com.qh.pay.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.junit.Test;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;

/**
 * @ClassName PayServiceTest
 * @Description 支付测试
 * @Date 2017年10月31日 上午11:42:01
 * @version 1.0.0
 */
public class PayServiceTest extends PayBaseServiceTest{
	
	/**
	 * 
	 *订单支付测试
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
			jsObj.put("bankCode", "BOC");
			jsObj.put("bankName", "建设银行");
		}
		//用户标志
		jsObj.put("userId", "userId");
		
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
		//对公
		jsObj.put("acctType", 1);
		
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
	
	/**
	 * 
	 * @Description 支付订单查询
	 * @throws Exception
	 */
	@Test
	public void order_query() throws Exception{
		String orderNo = "201711151647288410";
		JSONObject jsObj = new JSONObject();
		//商户号
		jsObj.put("merchNo", merchNo);
		jsObj.put("orderNo", orderNo);
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
				logger.info("订单支付状态", jo.getString("orderState"));
				
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
		//订单号
		jsObj.put("orderNo", reqTime + new Random().nextInt(10000));	
		//支付渠道  ---acp
		jsObj.put("outChannel", OutChannel.acp.name());
		jsObj.put("bankCode", "BOC");
		jsObj.put("bankName", "建设银行");
		//订单标题 
		jsObj.put("title", "商城网银代付");
		//产品名称
		jsObj.put("product", "产品名称");
		//代付付金额 单位 元 
		jsObj.put("amount", String.valueOf(new Random().nextInt(10000)));
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
				logger.info("代付消息：{}", jo.getString("msg"));
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
}
