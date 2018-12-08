package com.qh.paythird.xinqianbao;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.xinqianbao.utils.MD5Utils;
import com.qh.paythird.xinqianbao.utils.XinQianBaoConst;
import com.qh.redis.service.RedisUtil;

/**
 * 芯钱包
 * @author Swell
 *
 */
@Service
public class XinQianBaoService {

	private static final Logger logger = LoggerFactory.getLogger(XinQianBaoService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("芯钱包支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//快捷支付（收银台）
				return order_q(order);
			} 
			
			logger.error("芯钱包支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("芯钱包支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 快捷支付（收银台）
	 * @param order
	 * @return
	 */
	private R order_q(Order order){
		
		logger.info("芯钱包支付 支付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			String payMerch = order.getPayMerch();
			TreeMap<String,String> params = new TreeMap<String,String>();
			params.put("userid", payMerch);
			String txnTime = DateUtil.getCurrentNumStr();
			params.put("txnTime", txnTime);
			params.put("merId", RedisUtil.getPayCommonValue(XinQianBaoConst.AGENCY_NO));
			params.put("merKey", RedisUtil.getPayValue(XinQianBaoConst.AGENCY_KEY));
			params.put("amount", order.getAmount().toString());
			params.put("notifyUrl", PayService.commonNotifyUrl(order));
			params.put("pageNotifyUrl", PayService.commonReturnUrl(order));
			params.put("body", order.getProduct());
			logger.info("=====" + params);

			/**参与验签的字段*/
			String sign = MD5Utils.getSignParam(params);
			logger.info("芯钱包支付 签名的参数为："+sign);
			sign = MD5Utils.getKeyedDigest(sign, RedisUtil.getPayCommonValue(payMerch + XinQianBaoConst.KEY));

			/** 不参与签名的字段 **/
			params.put("orderCode", "an_newConsume");//银联在线
			params.put("sign", sign);
			params.put("pay_number", orderId);
			
			String baowen = MD5Utils.getSignParam(params);
			logger.info("芯钱包支付 上送的报文为："+baowen);
			String sr = RequestUtils.sendPost(RedisUtil.getPayCommonValue(XinQianBaoConst.REQ_URL), baowen);
			logger.info("芯钱包支付 请求返回参数："+sr);
			if(StringUtils.isBlank(sr)){
				return R.error("支付返回参数为空");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String respCode = jsonObject.getString("respCode");
			if(!"1001".equals(respCode)){
				return R.error(jsonObject.getString("respInfo"));
			}
			Map<String,String> data = new HashMap<>();
			data.put(PayConstants.web_code_url, jsonObject.getString("payUrl"));
			order.setBusinessNo(jsonObject.getString("orderId"));
			return R.okData(data);
		} catch (Exception e) {
			logger.error("芯钱包支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("芯钱包支付 支付：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request) {
		
		logger.info("芯钱包支付回调 开始-------------------------------------------------");
		String msg = "";
		try {
			TreeMap<String, String> params = RequestUtils.getRequestParam(request);
			logger.info("芯钱包支付 回调 参数："+ JSON.toJSONString(params));
			String pay_number = request.getParameter("pay_number");
			String orderId = request.getParameter("orderId");
			String respCode = request.getParameter("respCode");
			String respInfo = request.getParameter("respInfo");
			String amount = request.getParameter("amount");
			String sign = request.getParameter("sign");
			String payMerch = order.getPayMerch();
			String key = RedisUtil.getPayCommonValue(payMerch + XinQianBaoConst.KEY);
			String signParam = "amount=" +amount+ "&orderId=" +orderId+ "&pay_number=" +pay_number+ "&key=" +key;
			logger.info("芯钱包支付 回调验签原串参数"+signParam);
			String vsign = MD5Utils.ecodeByMD5(signParam);
			logger.info("芯钱包支付 本地验签结果：" + vsign);
			if(vsign.equalsIgnoreCase(sign)){
				order.setRealAmount(order.getAmount());
				/*校验订单金额*/
				if (!amount.equalsIgnoreCase(order.getAmount().toString())) {
					msg = "处理失败,订单金额" +order.getAmount()+ "不等:" + amount;
					return R.error(msg);
				}
				
				if(XinQianBaoConst.ORDER_STATUS_SUCC.equals(respCode)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
					return R.ok(msg);
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败:" + respInfo;
					return R.error(msg);
				}
			}else{
				logger.info("芯钱包支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("芯钱包支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("芯钱包支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("芯钱包支付回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("芯钱包支付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			TreeMap<String, String> map = new TreeMap<String, String>();
			String payMerch = order.getPayMerch();
			map.put("merId", RedisUtil.getPayCommonValue(XinQianBaoConst.AGENCY_NO));
			map.put("merKey", RedisUtil.getPayValue(XinQianBaoConst.AGENCY_KEY));
			map.put("userid",payMerch);
			/** 参与验签的字段 */
			String sign = MD5Utils.getSignParam(map);
			logger.info("芯钱包支付 查询 用于签名参数为：" + sign);
			sign = MD5Utils.getKeyedDigest(sign,RedisUtil.getPayCommonValue(payMerch + XinQianBaoConst.KEY));
			map.put("orderCode", "an_newConsumeQuery");
			map.put("sign", sign);
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			map.put("pay_number", orderId);
			String baowen = MD5Utils.getSignParam(map);
			logger.info("芯钱包支付 查询 上送的报文为：" + baowen);
			String sr = RequestUtils.sendPost(RedisUtil.getPayCommonValue(XinQianBaoConst.REQ_URL), baowen);
			logger.info("芯钱包支付 查询 请求后返回参数：" + sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String respCode = jsonObject.getString("respCode");
			if(!XinQianBaoConst.ORDER_STATUS_SUCC.equals(respCode)){
				return R.error(jsonObject.getString("respInfo"));
			} else {
				order.setRealAmount(order.getAmount());
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
				return R.ok(msg);
			}
		} catch (Exception e) {
			logger.info("芯钱包支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		} finally {
			logger.info("芯钱包支付 查询 结束------------------------------------------------------------");
		}
	}
}
