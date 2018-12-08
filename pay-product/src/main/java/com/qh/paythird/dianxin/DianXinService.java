package com.qh.paythird.dianxin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.PayService;
import com.qh.paythird.dianxin.utils.DianXinConst;
import com.qh.paythird.dianxin.utils.MD5Utils;
import com.qh.paythird.dianxin.utils.XmlUtils;
import com.qh.paythird.dianxin.utils.b.HttpHelper;
import com.qh.paythird.dianxin.utils.b.Md5;
import com.qh.paythird.dianxin.utils.b.RSAUtil;
import com.qh.paythird.dianxin.utils.b.RequestUtil;
import com.qh.redis.service.RedisUtil;



/**
 * 点芯支付
 * @author Swell
 *
 */
@Service
public class DianXinService {
	
	private static final Logger logger = LoggerFactory.getLogger(DianXinService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("点芯支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.qq.name().equals(order.getOutChannel())) {
				//qq扫码支付
				return order(order,"qq_qr");
			} 
			
			if (OutChannel.wx.name().equals(order.getOutChannel())) {
				//微信扫码支付
				return order(order,"wei_qr");
			} 
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码支付
				return order(order,"ali_qr");
			} 
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return order_acp(order);
			} 
			
			/*if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝支付
				return order_ali(order);
			} */
			
			logger.error("点芯支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("点芯支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 点芯扫码支付
	 * @param order
	 * @param way
	 * @return
	 */
	private R order(Order order,String way) {
		
		try {
			JSONObject json = new JSONObject();
			String merchantNo = order.getMerchNo();
	        String orderId = merchantNo + order.getOrderNo();
			json.put("orderId", orderId);
			 String amount = ParamUtil.yuanToFen(order.getAmount());
			json.put("amount", amount);

			String merchantCode = order.getPayMerch();
			json.put("returnUrl", PayService.commonReturnUrl(order));
			json.put("body", order.getProduct());
			json.put("merchantCode", merchantCode);
			json.put("version", RedisUtil.getPayCommonValue(DianXinConst.B_VERSION));
			json.put("notifyUrl",PayService.commonNotifyUrl(order));
			json.put("terminalIp","39.108.65.78");
			JSONObject res = RequestUtil.request(RedisUtil.getPayCommonValue(DianXinConst.B_ORDER_DOWN), json,merchantCode);
			logger.info("点芯"+way+"支付 预下单返回结果："+res.toString());
			if(!res.getBoolean("success")) {
				return R.error(res.getString("msg"));
			}
			JSONObject imgCodeRes = RequestUtil.getCode(res.getString("tranId"),merchantCode,way);
			if(!imgCodeRes.getBoolean("success")) {
				return R.error(imgCodeRes.getString("msg"));
			}
			Map<String, String> resultMap = PayService.initRspData(order);
			String qrcode = imgCodeRes.getString("codeUrl");
			resultMap.put(PayConstants.web_qrcode_url, qrcode);
			return R.okData(resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("点芯"+way+"支付异常：系统异常" + e.getMessage());
            return R.error("支付异常：系统异常");
		} finally {
			logger.info("点芯"+way+"支付 结束------------------------------------------------------");
		}
	}
	
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request) {
		
		logger.info("点芯支付回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String dxRealip = ParamUtil.getIpAddr(request);
			String status = request.getParameter("status");
			String tranId = request.getParameter("tranId");
			String orderId = request.getParameter("orderId");
			String signature = request.getParameter("signature");
			String amount = request.getParameter("amount");
			String resString = "status="+status+",tranId="+tranId+",orderId="+orderId+",amount="+amount+",signature="+signature;
			logger.info("点芯支付 后台异步回调通知接口 ip:" + dxRealip + "通知内容：" + resString);
			
			String sign = Md5.getMd5ofStr(status+orderId+tranId+amount+RedisUtil.getPayCommonValue(order.getPayMerch() + DianXinConst.B_KEY));
			if(!sign.equals(signature)) {
				logger.error("点芯异步通知错误，操作失败，原因：验证签名不通过");
				return R.error("签名不通过");
			}
			if("1".equals(status)) {
				//成功
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("0".equals(status)) {
				//待支付
				order.setOrderState(OrderState.ing.id());
				msg = "订单处理中";
			}else if("2".equals(status)) {
				//失败
				order.setOrderState(OrderState.fail.id());
				msg = "处理失败";
			}
			order.setBusinessNo(tranId);
			return R.ok(msg);
		} catch (Exception e) {
			logger.info("点芯支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("点芯支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("点芯支付回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		logger.info("点芯支付查询 开始-------------------------------------------------");
		String msg = "";
		try {
			String payMerch = order.getPayMerch();
			String merchantNo = order.getMerchNo();
	        String orderId = merchantNo + order.getOrderNo();
			JSONObject json = new JSONObject();
	        json.put("version", RedisUtil.getPayCommonValue(DianXinConst.B_VERSION));
	        json.put("orderId", orderId);
	        logger.info("点芯支付查询 请求参数：" + json.toString());
	        JSONObject res = RequestUtil.request(RedisUtil.getPayCommonValue(DianXinConst.B_QUERY_ORDER_STATUS), json,payMerch);
	        logger.info("点芯支付查询 返回参数：" + res.toString());
	        if(!res.getBoolean("success")) {
				return R.error(res.getString("msg"));
			}
	        int status = res.getInt("status");
	        String amount = res.get("amount").toString();
	        if (ParamUtil.isNotEmpty(amount)) {
				order.setRealAmount(ParamUtil.fenToYuan(amount));
			}else {
				order.setRealAmount(order.getAmount());
			}
			if(status == 1) {
				//成功
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if(status == 0) {
				//待支付
				order.setOrderState(OrderState.ing.id());
				msg = "订单处理中";
			}else if(status == 2) {
				//失败
				order.setOrderState(OrderState.fail.id());
				msg = "处理失败";
			}
			order.setBusinessNo(res.getString("tranId"));
            return R.ok(msg+":"+res.getString("msg"));
		} catch (Exception e) {
			logger.info("点芯支付查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("点芯支付查询 异常："+e.getMessage());
		}finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(),msg);
			logger.info("点芯支付查询 结束-------------------------------------------------");
		}
	}
	
	//--------------------------点芯代付----------------------------------
	public R order_acp(Order order) {
		logger.info("点芯代付 开始-------------------------------------------------");
		String msg = "";
		try {
			String payMerch = order.getPayMerch();
			String merchantNo = order.getMerchNo();
			String orderId = merchantNo + order.getOrderNo();
			String PUCLIC_KEY = RedisUtil.getPayCommonValue(payMerch + DianXinConst.B_RSA_PUBLIC_KEY);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("orderNo",orderId);
			jsonObject.put("isCompay","0"); //0 对私  1对公
			// jsonObject.put("cnapsNo","18564652485"); 公户必传  联行号
			jsonObject.put("city",order.getBankCity());
			jsonObject.put("bankName",order.getBankName());
			jsonObject.put("cardNumber",order.getBankNo());
			jsonObject.put("accountName",order.getAcctName());
			jsonObject.put("amount",ParamUtil.yuanToFen(order.getAmount()));
			jsonObject.put("version", RedisUtil.getPayCommonValue(DianXinConst.B_VERSION));
			logger.info("点芯代付 请求参数：" + jsonObject.toString());
			PublicKey publicKey = RSAUtil.loadPublicKey(PUCLIC_KEY);
			// 加密
			byte[] encryptByte = RSAUtil.encryptData(jsonObject.toString().getBytes("utf-8"), publicKey);
			String params = Base64.encodeBase64String(encryptByte);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("appid", RedisUtil.getPayCommonValue(payMerch+DianXinConst.B_APPID));
			map.put("params", params);
			logger.info("点芯代付 加密后参数：" + new JSONObject(map).toString());
			String result = new HttpHelper().sendPostHttp(RedisUtil.getPayCommonValue(DianXinConst.B_ACP_URL), map, false);
			logger.info("点芯代付 返回数据：" + result);
			JSONObject res = new JSONObject(result);
			String returnMessage = res.getString("msg");
			if(res.getBoolean("success")) {
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else {
				order.setOrderState(OrderState.fail.id());
				msg = "处理失败";
			}
			order.setRealAmount(order.getAmount());
			return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+returnMessage);
		} catch (Exception e) {
			logger.info("点芯代付 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("点芯代付 异常："+e.getMessage());
		} finally {
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(),msg);
			logger.info("点芯代付 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R query_acp(Order order) {
		logger.info("点芯 代付 查询 开始-------------------------------------------------");
		String msg = "";
		try {
			String payMerch = order.getPayMerch();
			String merchantNo = order.getMerchNo();
	        String orderId = merchantNo + order.getOrderNo();
	        JSONObject json = new JSONObject();
	    	json.put("type", "withdraw");
	    	json.put("orderId", orderId);
	    	json.put("version", RedisUtil.getPayCommonValue(DianXinConst.B_VERSION));
	        logger.info("点芯 代付 查询 请求参数：" + json.toString());
	        JSONObject res = RequestUtil.request(RedisUtil.getPayCommonValue(DianXinConst.B_QUERY_ORDER_STATUS), json,payMerch);
	        logger.info("点芯 代付 查询 返回参数：" + res.toString());
	        if(!res.getBoolean("success")) {
				return R.error(res.getString("msg"));
			}
	        int status = res.getInt("status");
			if(status == 3) {
				//成功
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if(status == 4 || status == 1 || status == -1) {
				//失败
				order.setOrderState(OrderState.fail.id());
				msg = "处理失败";
			}else {
				//待支付
				order.setOrderState(OrderState.ing.id());
				msg = "订单处理中";
			}
			String amount = res.isNull("amount")?"":res.get("amount").toString();
	        if (ParamUtil.isNotEmpty(amount)) {
				order.setRealAmount(ParamUtil.fenToYuan(amount));
			}else {
				order.setRealAmount(order.getAmount());
			}
			order.setBusinessNo(res.isNull("tranId")?"":res.getString("tranId"));
            return R.ok(msg+":"+res.getString("msg"));
		} catch (Exception e) {
			logger.info("点芯 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("点芯 代付 查询 异常："+e.getMessage());
		}finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(),msg);
			logger.info("点芯 代付 查询 结束-------------------------------------------------");
		}
	}
	
	
	/**
	 * @Description 代付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_acp(Order order, HttpServletRequest request,String responseBody) {
		
		logger.info("点芯代付回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String dxRealip = ParamUtil.getIpAddr(request);
			String status = request.getParameter("status");
			String tranId = request.getParameter("tranId");
			String orderId = request.getParameter("orderId");
			String signature = request.getParameter("signature");
			String amount = request.getParameter("amount");
			String returnMsg = request.getParameter("msg");
			if(StringUtils.isNotBlank(responseBody)) {
				JSONObject resultJsonObj = new JSONObject(responseBody);
				status = resultJsonObj.get("status").toString();
				orderId = resultJsonObj.getString("orderId");
				returnMsg = resultJsonObj.getString("msg");
				amount = resultJsonObj.getString("amount");
				signature = resultJsonObj.getString("signature");
			}
			String resString = "status="+status+",tranId="+tranId+",orderId="+orderId+",amount="+amount+",returnMsg="+returnMsg+",signature="+signature;
			logger.info("点芯代付 后台异步回调通知接口 ip:" + dxRealip + "通知内容：" + resString);
			
			String sign = Md5.getMd5ofStr(status+orderId+returnMsg+amount+RedisUtil.getPayCommonValue(order.getPayMerch() + DianXinConst.B_KEY));
			if(!sign.equals(signature)) {
				logger.error("点芯代付异步通知错误，操作失败，原因：验证签名不通过");
				return R.error("签名不通过");
			}
			if("1".equals(status)) {
				//成功
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("0".equals(status) || "3".equals(status) ) {
				//待支付
				order.setOrderState(OrderState.ing.id());
				msg = "订单处理中";
			}else if("2".equals(status) || "-1".equals(status)) {
				//失败
				order.setOrderState(OrderState.fail.id());
				msg = "处理失败";
			}
			order.setBusinessNo(tranId);
			return R.ok(msg);
		} catch (Exception e) {
			logger.info("点芯代付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("点芯代付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("点芯代付回调 结束-------------------------------------------------");
		}
	}
}
