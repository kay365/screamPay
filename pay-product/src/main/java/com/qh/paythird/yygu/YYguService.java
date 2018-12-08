package com.qh.paythird.yygu;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.huiFuBao.utils.Des;
import com.qh.paythird.huiFuBao.utils.HttpUtil;
import com.qh.paythird.huiFuBao.utils.HuifubaoConst;
import com.qh.paythird.huiFuBao.utils.SmallTools;
import com.qh.paythird.huiFuBao.utils.XmlUtils;
import com.qh.redis.service.RedisUtil;

/**
 * 吖吖谷
 * @author Swell
 *
 */
@Service
public class YYguService {

	private static final Logger logger = LoggerFactory.getLogger(YYguService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("吖吖谷支付 开始------------------------------------------------------");
		try {
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//网银网关支付
				return order_ali(order);
			} 
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return order_acp(order);
			}
			logger.error("吖吖谷支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("吖吖谷支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 回调
	 * @param order
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request, String responseBody) {
		
		logger.info("吖吖谷回调 开始------------------------------------------------------");
		try {
			
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//网银网关支付
				return notify_ali(order,request);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return notify_acp(order,request,responseBody);
			}

			logger.error("吖吖谷回调 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("吖吖谷回调 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("吖吖谷查询 开始------------------------------------------------------");
		try {
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//网银网关支付
				return query_ali(order);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return query_acp(order);
			}

			logger.error("吖吖谷查询 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("吖吖谷查询 结束------------------------------------------------------");
		}
	}
	
	
	/**
	 * 预下单支付接口   支付宝
	 * @param order
	 * @return
	 */
	private R order_ali(Order order){
		String privateKey = "19c4c4723f4311e5aa3cd89d672b90b8";
		String reqUrl = "http://api.zhangyoo.cn/order/";
		logger.info("吖吖谷查询 开始------------------------------------------------------");
		try {
			//请求参数
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			
			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("clientId", "123");
			paramMap.put("merchantId", "1");
			paramMap.put("outOrderId", orderId);
			paramMap.put("orderType", 1);//1 ali  4微信
			//paramMap.put("tradeType", "NATIVE");//JSAPI 或 NATIVE二传一（微信时必传）
			//paramMap.put("openid", "");//为JSAPI时必传
			/*paramMap.put("bankNumber", "");
			paramMap.put("bankcardtype", "");*/
			paramMap.put("inputCharset",YYguConst.charset);
			paramMap.put("signType", YYguConst.signType);
			paramMap.put("notifyUrl", PayService.commonNotifyUrl(order));
			paramMap.put("totalFee", ParamUtil.yuanToFen(order.getAmount()));
	       
	       
	        //组织签名串  inputCharset=utf-8&merchantId=123&orderId=13551486463&outOrderId=2088101568338364&signType=MD5
	        StringBuilder sign_sb = new StringBuilder();
	        sign_sb.append("clientId").append("=").append(paramMap.get("clientId")).append("&");
	        sign_sb.append("inputCharset").append("=").append(paramMap.get("inputCharset")).append("&")
	        		.append("merchantId").append("=").append(paramMap.get("merchantId")).append("&")
	        		.append("notifyUrl").append("=").append(paramMap.get("notifyUrl")).append("&");
	        /*if("JSAPI".equals(paramMap.get("tradeType"))){
	        	sign_sb.append("openid").append("=").append(paramMap.get("openid")).append("&");
	        }*/
	        sign_sb	.append("orderType").append("=").append(paramMap.get("orderType")).append("&")
	        		.append("outOrderId").append("=").append(paramMap.get("outOrderId")).append("&")
	        		.append("signType").append("=").append(paramMap.get("signType")).append("&")
	        		.append("totalFee").append("=").append(paramMap.get("totalFee"));
	                //.append("tradeType").append("=").append(paramMap.get("tradeType"));
	                
	        logger.info("吖吖谷查询签名参数："+sign_sb.toString());
	        String signature = Md5Util.MD5(sign_sb.toString()+privateKey);//商品名称
	        logger.info("吖吖谷查询签名结果："+signature);
	        
	        //paramMap.put("signature", signature);
	        //String paramStr = JSONUtils.beanToJson(paramMap);
	        sign_sb.append("&").append("signature").append("=").append(signature);
	        logger.info("吖吖谷查询请求参数："+sign_sb.toString());
	       // logger.info("吖吖谷查询请求参数："+paramStr);
	        String resp = RequestUtils.doPost(reqUrl+"preCreate",sign_sb.toString());
	        logger.info("吖吖谷查询返回参数为："+resp);
	        if(StringUtils.isBlank(resp)){
				return R.error("支付返回参数为空");
			}
			
			JSONObject jsonObject = JSONObject.parseObject(resp);
			String resCode = jsonObject.getString("result");
			String message = jsonObject.getString("message");
			if(!"result".equals(resCode)){
				return R.error(message);
			}
			JSONObject dataJson = (JSONObject) jsonObject.get("data");
			String qrurl = dataJson.getString("qrCode");
			// 确认返回数据
			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, qrurl);
			} catch (Exception e) {
				logger.error("jump加密异常！！");
				return R.error("加密异常");
			}
			order.setResultMap(resultMap);
			
			return R.okData(resultMap);
		}catch (Exception e) {
			logger.error("吖吖谷查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("吖吖谷查询 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_ali(Order order, HttpServletRequest request) {
		
		logger.info("吖吖谷 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String result = request.getParameter("result");
			String message = request.getParameter("message");
			String totalFee = request.getParameter("totalFee");
			String outOrderId = request.getParameter("outOrderId");
			String payCode = request.getParameter("payCode");
			String orderType = request.getParameter("orderType");
			String orderState = request.getParameter("orderState");
			String orderId = request.getParameter("orderId");
			String thirdOrderId = request.getParameter("thirdOrderId");
			String thirdLoginId = request.getParameter("thirdLoginId");
			String openid = request.getParameter("openid");
			String signature = request.getParameter("signature") ;
			
			String payMerch = order.getPayMerch();
			String key = RedisUtil.getPayCommonValue(payMerch + HuifubaoConst.hfb_key);
			
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append("message=").append(message);
			s.append("&openid=").append(openid);
			s.append("&orderId=").append(orderId);
			s.append("&orderState=").append(orderState);
			s.append("&orderType=").append(orderType);
			s.append("&outOrderId=").append(outOrderId);
			s.append("&payCode=").append(payCode);
			s.append("&result=").append(result);
			s.append("&thirdLoginId=").append(thirdLoginId);
			s.append("&thirdOrderId=").append(thirdOrderId);
			s.append("&totalFee=").append(totalFee);
			s.append(key);
			
			logger.info("吖吖谷 回调 参数"+s.toString());
			String vsign = Md5Util.MD5(s.toString());
			logger.info("吖吖谷 回调参数加密结果："+vsign);
			if(vsign.equalsIgnoreCase(signature)){
				if(!"result".equals(result)){
					return R.error(message);
				}
				
				order.setRealAmount(order.getAmount());
				if("7".equals(result)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else if("10".equals(result)){
					order.setOrderState(OrderState.fail.id());
					msg = "已退款";
				}else if("4".equals(result)){
					order.setOrderState(OrderState.close.id());
					msg = "已取消";
				}
				return R.ok(msg);
			}else{
				logger.info("吖吖谷 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("吖吖谷 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("吖吖谷支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("吖吖谷 支付 回调 结束-------------------------------------------------");
		}
	}
	
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query_ali(Order order) {
		String privateKey = "19c4c4723f4311e5aa3cd89d672b90b8";
		String reqUrl = "http://api.zhangyoo.cn/order/";
		logger.info("吖吖谷查询 开始------------------------------------------------------");
		try {
			//请求参数
			String merchantId = "1";
			String orderId = "201805071704184125129";//平台订单号
			String inputCharset = "UTF-8";
			String signType = "MD5";
			
	       
	        //组织签名串  inputCharset=utf-8&merchantId=123&orderId=13551486463&outOrderId=2088101568338364&signType=MD5
	        StringBuilder sign_sb = new StringBuilder();
	        sign_sb.append("inputCharset").append("=").append(inputCharset).append("&");
	        sign_sb.append("merchantId").append("=").append(merchantId).append("&")
	        		.append("orderId").append("=").append(orderId).append("&")
	        		.append("signType").append("=").append(signType);
	       
	                
	        logger.info("吖吖谷查询签名参数："+sign_sb.toString());
	        String signature = Md5Util.MD5(sign_sb.toString()+privateKey);//商品名称
	        logger.info("吖吖谷查询签名结果："+signature);
	       
	        //paramMap.put("signature", signature);
	        //String paramStr = JSONUtils.beanToJson(paramMap);
	        sign_sb.append("&").append("signature").append("=").append(signature);
	        logger.info("吖吖谷查询请求参数："+sign_sb.toString());
	       // logger.info("吖吖谷查询请求参数："+paramStr);
	        String resp = RequestUtils.doPost(reqUrl+"query",sign_sb.toString());
	        logger.info("吖吖谷查询返回参数为："+resp);
	        if(StringUtils.isBlank(resp)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(resp);
	        String resCode = jsonObject.getString("result");
			String resMessage = jsonObject.getString("message");
			String orderState = jsonObject.getString("orderState");
			String msg = "";
			
			if(!"result".equals(resCode)){
				return R.error(resMessage);
			}
			
				order.setRealAmount(order.getAmount());
				if("1".equals(orderState)){
					order.setOrderState(OrderState.ing.id());
					msg = "待付款";
				}else if("4".equals(orderState)){
					order.setOrderState(OrderState.close.id());
					msg = "已取消";
				}else if("7".equals(orderState)){
					order.setOrderState(OrderState.succ.id());
					msg = "已完成";
				}else if("10".equals(orderState)){
					order.setOrderState(OrderState.fail.id());
					msg = "已退款";
				}else {
					order.setOrderState(OrderState.ing.id());
					msg = "未支付";
				}
				return R.ok(msg);
			
		}catch (Exception e) {
			logger.error("吖吖谷查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("吖吖谷查询 异常：" + e.getMessage());
		} finally {
			logger.info("吖吖谷查询 结束------------------------------------------------------");
		}
	}
	
	
	
	/**
	 * 代付
	 * @param order
	 * @return
	 */
	private R order_acp(Order order){
		
		logger.info("吖吖谷 代付：{");
		try {
			//请求地址
	        String url = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_req_DfURL);//小额代付
	       // String xiaourl ="https://Pay.heepay.com/API/PayTransit/PayTransferWithSmallAll.aspx";
	       // String daurl ="https://Pay.heepay.com/API/PayTransit/PayTransferWithLargeWork.aspx";
	        //String url = "https://Pay.heepay.com/API/PayTransit/PayTransferWithSmallAll.aspx";  //小额
	        //String url = "https://Pay.heepay.com/API/PayTransit/PayTransferWithLargeWork.aspx";   //大额
	        //请求参数
	        String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			
	        String version = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_version);               //当前接口版本号
	        String agent_id = order.getPayMerch();    //商户ID 
	        String batch_no = orderId;    //批量付款订单号
	        String batch_amt = order.getAmount().toString();       //付款总金额
	        String batch_num = "1";   //该次付款总笔数
	        
	        String merNo = UUID.randomUUID().toString().substring(0, 20);;//商户流水号
	        String acctType = HuifubaoConst.accType_PUB;//对私
	        String bankNo = order.getBankNo();//银行卡号
	        String name = order.getAcctName();//收款人
	        String payReason = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_df_reason);
	        String province = order.getBankProvince();
	        String city = order.getBankCity();
	        String bankName = order.getBankBranch();
	        
	        StringBuffer sb = new StringBuffer();
	        sb.append(merNo); sb.append("^");
	        sb.append(HuifubaoConst.bankNumberMap.get(order.getBankCode())); sb.append("^");
	        sb.append(acctType); sb.append("^");
	        sb.append(bankNo); sb.append("^");
	        sb.append(name); sb.append("^");
	        sb.append(batch_amt); sb.append("^");
	        sb.append(payReason); sb.append("^");
	        sb.append(province); sb.append("^");
	        sb.append(city); sb.append("^");
	        sb.append(bankName); 
	       
	        //String detail_data = "A123456^2^0^6217000010100164865^张三^0.01^商户付款^北京^北京市^建设银行";           //批付到银行帐户格式
	        //商户流水号^银行编号^对公对私^收款人帐号^收款人姓名^付款金额^付款理由^省份^城市^收款支行名称
	        String detail_data = sb.toString();
	        String notify_url = PayService.commonAcpNotifyUrl(order);                //异步通知地址
	        String ext_param1 = "123";                //商户自定义
	        String DesKey = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_3deskey);
	        String key = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_key);;               //签名密钥  D7887B4537A24A2A8B3EB718
	        String sign = "";                   //签名结果
	        //组织签名串
	        StringBuilder sign_sb = new StringBuilder();
	        sign_sb.append("agent_id")       .append("=").append(agent_id)    .append("&")
	                .append("batch_amt")     .append("=").append(batch_amt)   .append("&")
	                .append("batch_no")      .append("=").append(batch_no)    .append("&")
	                .append("batch_num")     .append("=").append(batch_num)   .append("&")
	                .append("detail_data")   .append("=").append(detail_data) .append("&")
	                .append("ext_param1")    .append("=").append(ext_param1)  .append("&")
	                .append("key")            .append("=").append(key)         .append("&")
	                .append("notify_url")    .append("=").append(notify_url)  .append("&")
	                .append("version")       .append("=").append(version);
	        System.out.println("签名参数："+sign_sb.toString().toLowerCase());
	        sign = SmallTools.MD5en(sign_sb.toString().toLowerCase());
	        System.out.println("签名结果："+sign);
	        //3DES加密detail_data
	        detail_data = Des.Encrypt3Des(detail_data, DesKey,"ToHex16");
	        //请求参数
	        StringBuilder requestParams = new StringBuilder();
	        requestParams.append("version") .append("=").append(version)      .append("&")
	                .append("agent_id")     .append("=").append(agent_id)     .append("&")
	                .append("batch_no")     .append("=").append(batch_no)     .append("&")
	                .append("batch_amt")    .append("=").append(batch_amt)    .append("&")
	                .append("batch_num")    .append("=").append(batch_num)	.append("&")
	                .append("detail_data")  .append("=").append(detail_data)	.append("&")
	                .append("notify_url")   .append("=").append(notify_url)	.append("&")
	                .append("ext_param1")   .append("=").append(ext_param1)	.append("&")
	                .append("sign")          .append("=").append(sign);
	        logger.info("吖吖谷代付 请求参数"+requestParams.toString());
	        String res = HttpUtil.sendPost(url,requestParams.toString());
	        logger.info("吖吖谷代付 响应参数:[" + res + "]");
			
			if(StringUtils.isBlank(res)){
				return R.error("返回参数为空！");
			}
			
			Map<String,Object> resmap = XmlUtils.toMap(res);
			
			String respCode = resmap.get("ret_code").toString();
			String resultMsg = resmap.get("ret_msg").toString();
			if(!"0000".equals(respCode)){
				return R.error(resultMsg);
			}
			order.setOrderState(OrderState.ing.id());
			order.setRealAmount(order.getAmount());
			return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resultMsg);
		} catch (Exception e) {
			logger.error("吖吖谷 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("吖吖谷 代付：}");
		}
	}
	
	/**
	 * @Description 代付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_acp(Order order, HttpServletRequest request,String responseBody) {
		
		logger.info("吖吖谷 代付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			//result=1&agent_id=1234567&jnet_bill_no=B20100225132210&agent_bill_id=20100225132210&
			String str = request.getQueryString();
			logger.info("吖吖谷 代付 回调 回调参数："+str);
			String ret_msg = "";
			String detail_data = "";
			String[] s = str.split("&");
			for (String string : s) {
				String[] kv = string.split("=");
				if(kv[0].equals("ret_msg")) {
					ret_msg = URLDecoder.decode(kv[1],"GBK");
				}
				if(kv[0].equals("detail_data")) {
					detail_data = URLDecoder.decode(kv[1],"GBK");
				}
			}
	        String ret_code = request.getParameter("ret_code").trim();//返回码值0000 表示查询成功
	        String agent_id = request.getParameter("agent_id").trim();//商户ID
	        String hy_bill_no = request.getParameter("hy_bill_no").trim();//吖吖谷订单号
	        String status = request.getParameter("status").trim();//-1=无效，0=未处理，1=成功
	        String batch_no = request.getParameter("batch_no").trim();//商户系统订单号
	        String batch_amt = request.getParameter("batch_amt").trim();//成功付款金额
	        String batch_num = request.getParameter("batch_num").trim();//成功付款数量
	        String ext_param1 = request.getParameter("ext_param1").trim();//商户自定义参数，透传参数
	        String sign = request.getParameter("sign").trim();//签名

	        String key = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_key);;               //签名密钥  D7887B4537A24A2A8B3EB718;
	        //解密detail_data
//	        detail_data = Des.Decrypt3Des(detail_data, DesKey,"ToHex16").trim();
	        logger.info("吖吖谷 代付 回调 detail_data参数："+detail_data);
	        //验签
	        String b_sign = "ret_code="+ret_code+
	                "&ret_msg="+ret_msg+
	                "&agent_id="+agent_id+
	                "&hy_bill_no="+hy_bill_no+
	                "&status="+status+
	                "&batch_no="+batch_no+
	                "&batch_amt="+batch_amt+
	                "&batch_num="+batch_num+
	                "&detail_data="+detail_data+
	                "&ext_param1="+ext_param1+
	                "&key="+key;
	        logger.info("吖吖谷 代付 回调 验证参数："+b_sign);
	        b_sign = SmallTools.MD5en(b_sign.toLowerCase());
	        logger.info("吖吖谷 代付 回调 生成签名："+b_sign+"，原签名："+sign);
			if(b_sign.equalsIgnoreCase(sign)){
				order.setRealAmount(order.getAmount());
				if("0000".equals(ret_code)){
					String[] detail = detail_data.split("\\^");
					if("S".equals(detail[4])){
						order.setOrderState(OrderState.succ.id());
						msg = "订单处理完成";
					}else if("F".equals(detail[4])){
						order.setOrderState(OrderState.fail.id());
						msg = "处理失败";
					}
				}else{
					order.setOrderState(OrderState.ing.id());
					msg = "处理中";
				}
				return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+ret_msg);
			}else{
				logger.info("吖吖谷 代付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("吖吖谷 代付 回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("吖吖谷代付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("吖吖谷 代付 回调 结束-------------------------------------------------");
		}
	}
	

	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R query_acp(Order order) {
		//https://Pay.heepay.com/API/PayTransit/QueryTransfer.aspx
		logger.info("吖吖谷 代付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			//请求地址
	        String url = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_query_DfURL);
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
	        //请求参数
	        String version = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_version);                            //当前接口版本号
	        String agent_id = order.getPayMerch();                    //商户id
	        String batch_no = orderId;  //商户订单号
	        String sign = "";                               //签名结果
	        
	        String key = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_key);     //签名密钥
	     
	        //组织签名串
	        StringBuilder sign_sb = new StringBuilder();
	        
	        sign_sb.append("agent_id").append("=").append(agent_id).append("&")
	                .append("batch_no").append("=").append(batch_no).append("&")
	                .append("key")  .append("=").append(key).append("&")
	                .append("version").append("=").append(version);
	        
	        logger.info("吖吖谷代付 支付签名参数："+sign_sb.toString());
	        sign = SmallTools.MD5en(sign_sb.toString().toLowerCase());
	        logger.info("吖吖谷代付 支付 签名结果："+sign);

	        //请求参数
	        StringBuilder requestParams = new StringBuilder();
	        requestParams.append("version").append("=").append(version).append("&")
	                .append("agent_id").append("=").append(agent_id).append("&")
	                .append("batch_no").append("=").append(batch_no).append("&")
	                .append("sign")  .append("=").append(sign);
	        
	        logger.info("吖吖谷代付 支付请求参数："+requestParams.toString());
	        logger.info("吖吖谷代付 支付 查询 请求地址：" + url);
	        String res = HttpUtil.sendPost(url,requestParams.toString());
	        logger.info("吖吖谷代付 支付 返回参数："+res);
	       
	        if(StringUtils.isBlank(res)){
	        	return R.error("查询返回参数为空！");
	        }
	        
			Map<String,Object> resmap = XmlUtils.toMap(res);
			
			String respCode = resmap.get("ret_code").toString();
			String resultMsg = resmap.get("ret_msg").toString();
			if(!"0000".equals(respCode)){
				return R.error(resultMsg);
			}
			String DesKey = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_3deskey);
			if(resmap.containsKey("detail_data")) {
				String detail_data = resmap.get("detail_data").toString();
				detail_data = Des.Decrypt3Des(detail_data, DesKey,"ToHex16");
				logger.info("吖吖谷代付 支付 返回参数 detail_data解密明文："+detail_data);
				String[] detail = detail_data.split("\\^");
				if(detail.length > 4) {
					String state = detail[4];
					if("S".equals(state)){
						order.setOrderState(OrderState.succ.id());
						msg = "订单处理完成";
					}else if("F".equals(state)){
						order.setOrderState(OrderState.fail.id());
						msg = "处理失败";
					}
				}
			}
			order.setRealAmount(order.getAmount());
			return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+resultMsg+msg);
		} catch (Exception e) {
			logger.info("吖吖谷 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询 异常："+e.getMessage());
		} finally {
			logger.info("吖吖谷 代付 查询 结束------------------------------------------------------------");
		}
	}

}
