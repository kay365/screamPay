package com.qh.paythird.huiFuBao;


import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.service.PayService;
import com.qh.paythird.huiFuBao.utils.Des;
import com.qh.paythird.huiFuBao.utils.HttpUtil;
import com.qh.paythird.huiFuBao.utils.HuifubaoConst;
import com.qh.paythird.huiFuBao.utils.SmallTools;
import com.qh.paythird.huiFuBao.utils.XmlUtils;
import com.qh.redis.service.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 汇付宝
 * @author Swell
 *
 */
@Service
public class HuiFuBaoService {

	private static final Logger logger = LoggerFactory.getLogger(HuiFuBaoService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("汇付宝支付 开始------------------------------------------------------");
		try {
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return order_wy(order);
			} 
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return order_acp(order);
			}
			if (OutChannel.wap.name().equals(order.getOutChannel())) {
				//wap微信支付
				return order_wap(order);
			}
			logger.error("汇付宝支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("汇付宝支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 回调
	 * @param order
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request, String responseBody) {
		
		logger.info("汇付宝回调 开始------------------------------------------------------");
		try {
			
			
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return notify_wy(order,request);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return notify_acp(order,request,responseBody);
			}

			if (OutChannel.wap.name().equals(order.getOutChannel())) {
				//wap微信支付
				return  notify_wap(order, request);
			}
			logger.error("汇付宝回调 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("汇付宝回调 结束------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("汇付宝查询 开始------------------------------------------------------");
		try {
			
			if (OutChannel.wy.name().equals(order.getOutChannel())) {
				//网银网关支付
				return query_wy(order);
			} 
			
			if (OutChannel.acp.name().equals(order.getOutChannel())) {
				//代付
				return query_acp(order);
			}

			if (OutChannel.wap.name().equals(order.getOutChannel())) {
				//wap微信
				return query_wy(order);
			}

			logger.error("汇付宝查询 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("汇付宝查询 结束------------------------------------------------------");
		}
	}
	
	
	/**
	 * 网关支付
	 * @param order
	 * @return
	 */
	private R order_wy(Order order){
		
		logger.info("汇付宝网关 支付：{");
		try {
			
			//请求参数
	        String version = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_version);               //当前接口版本号
	        String is_phone = "";               //是否使用手机端支付，1=是，不传为pc端支付
	        String agent_id = order.getPayMerch();       //商户id
	        String pay_type = "20";              //支付类型 20
	        String pay_amt = order.getAmount().toString();             //订单总金额
	        String notify_url = PayService.commonNotifyUrl(order);      //异步通知地址
	        String return_url = PayService.commonReturnUrl(order);      //支付完成同步跳转地址
	        String user_ip = order.getReqIp().replaceAll("\\.", "_");   //用户IP
	        String bank_card_type = order.getCardType().toString();       //银行卡类型：未知=-1；储蓄卡=0；信用卡=1
	        String goods_name = order.getProduct();//商品名称
	        String goods_num = "1";             //商品数量
	        String pay_code = "0";              //银行编码
	        String remark = "''"; //商户自定义字段，异步通知时原样返回
	        String sign_type = "MD5";           //签名方式MD5
	        String key = RedisUtil.getPayCommonValue(agent_id+HuifubaoConst.hfb_key);//密钥
	        String sign = "";                   //MD5签名结果
	        
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			
			String notifyUrl = PayService.commonNotifyUrl(order);
			//String key = RedisUtil.getPayCommonValue(payMerch + HuifubaoConst.KEY);
			String returnUrl = PayService.commonReturnUrl(order);
			
			map.put("version",version);
			map.put("agent_id", agent_id);
			map.put("agent_bill_id", orderId);
			map.put("agent_bill_time",DateUtil.getCurrentNumStr());
			map.put("pay_type",pay_type);
			map.put("pay_amt",pay_amt);
			map.put("notify_url", notifyUrl);
			map.put("return_url", returnUrl);
			map.put("user_ip", user_ip);
			map.put("goods_name", goods_name);
			map.put("goods_num",goods_num);
			map.put("remark", remark);
			map.put("is_phone", is_phone);
			map.put("bank_card_type", bank_card_type);
			map.put("pay_code", pay_code);
			map.put("sign_type", sign_type);
			
			/**参与验签的字段*/
			sign = "version="+version+"&agent_id="+agent_id+"&agent_bill_id="+orderId
					+"&agent_bill_time="+map.get("agent_bill_time")+"&pay_type="+pay_type+"&pay_amt="+pay_amt
					+"&notify_url="+notify_url+"&return_url="+return_url+"&user_ip="+user_ip
					+"&bank_card_type="+bank_card_type+"&key="+key;
			
			logger.info("汇付宝网关 支付 签名的参数为："+sign);
			sign = SmallTools.MD5en(sign);
			map.put("sign", sign);
			logger.info("汇付宝网关 支付 签名为：sign="+sign);
			
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
			jumpData.put(PayConstants.web_action, RedisUtil.getPayCommonValue(HuifubaoConst.hfb_res_wyURL));
			order.setJumpData(jumpData);
			
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.error("汇付宝网关 支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("汇付宝网关 支付：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_wy(Order order, HttpServletRequest request) {
		
		logger.info("汇付宝网关 支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String result = request.getParameter("result");
			String pay_message = request.getParameter("pay_message");
			String agent_id = request.getParameter("agent_id");
			String jnet_bill_no = request.getParameter("jnet_bill_no");
			String agent_bill_id = request.getParameter("agent_bill_id");
			String pay_type = request.getParameter("pay_type");
			String pay_amt = request.getParameter("pay_amt");
			String remark = request.getParameter("remark");
			String sign = request.getParameter("sign") ;
			
			String payMerch = order.getPayMerch();
			String key = RedisUtil.getPayCommonValue(payMerch + HuifubaoConst.hfb_key);
			
			StringBuffer s = new StringBuffer(50);
			//拼成数据串
			s.append("result=").append(result);
			s.append("&agent_id=").append(agent_id);
			s.append("&jnet_bill_no=").append(jnet_bill_no);
			s.append("&agent_bill_id=").append(agent_bill_id);
			s.append("&pay_type=").append(pay_type);
			s.append("&pay_amt=").append(pay_amt);
			s.append("&remark=").append(remark);
			s.append("&key=").append(key);
			logger.info("汇付宝网关 回调 参数"+s.toString());
			String vsign = SmallTools.MD5en(s.toString());
			logger.info("汇付宝网关 回调参数加密结果："+vsign);
			if(vsign.equalsIgnoreCase(sign)){
				order.setRealAmount(order.getAmount());
				if("1".equals(result)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "处理失败";
					logger.info("汇付宝网关 支付回调 失败："+pay_message);
				}
				return R.ok(msg);
			}else{
				logger.info("汇付宝网关 支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("汇付宝网关 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("汇付宝支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("汇付宝网关 支付 回调 结束-------------------------------------------------");
		}
	}
	
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query_wy(Order order) {
		
		logger.info("汇付宝网关 支付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			//请求地址
	        String url = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_query_wyURL);
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
	        //请求参数
	        String version = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_version);                            //当前接口版本号
	        String agent_id = order.getPayMerch();                    //商户id
	        String agent_bill_id = orderId;  //商户订单号
	        String agent_bill_time = DateUtil.getCurrentNumStr();    //单据时间
	        String return_mode = "1";                       //查询结果返回类型
	        String remark = "hy";                           //商户自定义字段，原样返回
	        String sign_type = "MD5";                       //签名方式
	        String key = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_key);     //签名密钥
	        String sign = "";                               //签名结果
	     
	        //组织签名串
	        StringBuilder sign_sb = new StringBuilder();
	        sign_sb.append("version")            .append("=").append(version)             .append("&")
	                .append("agent_id")          .append("=").append(agent_id)            .append("&")
	                .append("agent_bill_id")    .append("=").append(agent_bill_id)       .append("&")
	                .append("agent_bill_time")  .append("=").append(agent_bill_time) 		.append("&")
	                .append("return_mode")       .append("=").append(return_mode)			.append("&")
	                .append("key")                .append("=").append(key);
	        logger.info("汇付宝网关 支付签名参数："+sign_sb.toString());
	        sign = SmallTools.MD5en(sign_sb.toString());
	        logger.info("汇付宝网关 支付 签名结果："+sign);

	        //请求参数
	        StringBuilder requestParams = new StringBuilder();
	        requestParams.append("version")      .append("=").append(version)            .append("&")
	                .append("agent_id")          .append("=").append(agent_id)           .append("&")
	                .append("agent_bill_id")    .append("=").append(agent_bill_id)      .append("&")
	                .append("agent_bill_time")  .append("=").append(agent_bill_time) 	   .append("&")
	                .append("return_mode")      .append("=").append(return_mode)		   .append("&")
	                .append("remark")            .append("=").append(remark)		       .append("&")
	                .append("sign_type")        .append("=").append(sign_type)		   .append("&")
	                .append("sign")              .append("=").append(sign);
	        logger.info("汇付宝网关 支付请求参数："+requestParams.toString());
	        logger.info("汇付宝网关 支付 查询 请求地址：" + url);
	        String res = HttpUtil.sendPost(url,requestParams.toString());
	        logger.info("汇付宝网关 支付 返回参数："+res);
	       
	        if(StringUtils.isBlank(res)){
	        	return R.error("查询返回参数为空！");
	        }

	        //返回参数验签
	        int i = res.lastIndexOf("|");
	        String ss = res.substring(0,i+1);
	        //签名参数
	        String md5_params = ss + "key=" + key;
	        //接收到的sign
	        String r_sign = res.substring(i+6,res.length());
	        //自己加密的sign
	        String md5_sign = SmallTools.MD5en(md5_params);
	        if(md5_sign.equals(r_sign)){
	        	order.setRealAmount(order.getAmount());
	        	if(res.contains(HuifubaoConst.tradeState_SUC)){
	        		order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
	        	}else if(res.contains(HuifubaoConst.tradeState_FAIL)){
	        		msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
	        	}else{
	        		msg = "订单处理中";
					order.setOrderState(OrderState.ing.id());
	        	}
	        	
	        	return R.ok(msg);
	        }else {
	        	logger.info("汇付宝网关 支付 查询 验签不通过");
	        	return R.error("验签不通过");
	        }
			
		} catch (Exception e) {
			logger.info("汇付宝网关 支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		} finally {
			logger.info("汇付宝网关 支付 查询 结束------------------------------------------------------------");
		}
	}
	
	
	
	/**
	 * 代付
	 * @param order
	 * @return
	 */
	private R order_acp(Order order){
		
		logger.info("汇付宝 代付：{");
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
	        logger.info("汇付宝代付 请求参数"+requestParams.toString());
	        String res = HttpUtil.sendPost(url,requestParams.toString());
	        logger.info("汇付宝代付 响应参数:[" + res + "]");
			
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
			logger.error("汇付宝 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("汇付宝 代付：}");
		}
	}
	
	/**
	 * @Description 代付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify_acp(Order order, HttpServletRequest request,String responseBody) {
		
		logger.info("汇付宝 代付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			//result=1&agent_id=1234567&jnet_bill_no=B20100225132210&agent_bill_id=20100225132210&
			String str = request.getQueryString();
			logger.info("汇付宝 代付 回调 回调参数："+str);
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
	        String hy_bill_no = request.getParameter("hy_bill_no").trim();//汇付宝订单号
	        String status = request.getParameter("status").trim();//-1=无效，0=未处理，1=成功
	        String batch_no = request.getParameter("batch_no").trim();//商户系统订单号
	        String batch_amt = request.getParameter("batch_amt").trim();//成功付款金额
	        String batch_num = request.getParameter("batch_num").trim();//成功付款数量
	        String ext_param1 = request.getParameter("ext_param1").trim();//商户自定义参数，透传参数
	        String sign = request.getParameter("sign").trim();//签名

	        String key = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_key);;               //签名密钥  D7887B4537A24A2A8B3EB718;
	        String DesKey = RedisUtil.getPayCommonValue(order.getPayMerch()+HuifubaoConst.hfb_df_3deskey);;               //签名密钥  D7887B4537A24A2A8B3EB718;
	        //解密detail_data
//	        detail_data = Des.Decrypt3Des(detail_data, DesKey,"ToHex16").trim();
	        logger.info("汇付宝 代付 回调 detail_data参数："+detail_data);
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
	        logger.info("汇付宝 代付 回调 验证参数："+b_sign);
	        b_sign = SmallTools.MD5en(b_sign.toLowerCase());
	        logger.info("汇付宝 代付 回调 生成签名："+b_sign+"，原签名："+sign);
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
				logger.info("汇付宝 代付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("汇付宝 代付 回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("汇付宝代付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("汇付宝 代付 回调 结束-------------------------------------------------");
		}
	}
	

	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R query_acp(Order order) {
		//https://Pay.heepay.com/API/PayTransit/QueryTransfer.aspx
		logger.info("汇付宝 代付 查询 开始------------------------------------------------------------");
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
	        
	        logger.info("汇付宝代付 支付签名参数："+sign_sb.toString());
	        sign = SmallTools.MD5en(sign_sb.toString().toLowerCase());
	        logger.info("汇付宝代付 支付 签名结果："+sign);

	        //请求参数
	        StringBuilder requestParams = new StringBuilder();
	        requestParams.append("version").append("=").append(version).append("&")
	                .append("agent_id").append("=").append(agent_id).append("&")
	                .append("batch_no").append("=").append(batch_no).append("&")
	                .append("sign")  .append("=").append(sign);
	        
	        logger.info("汇付宝代付 支付请求参数："+requestParams.toString());
	        logger.info("汇付宝代付 支付 查询 请求地址：" + url);
	        String res = HttpUtil.sendPost(url,requestParams.toString());
	        logger.info("汇付宝代付 支付 返回参数："+res);
	       
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
				logger.info("汇付宝代付 支付 返回参数 detail_data解密明文："+detail_data);
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
			logger.info("汇付宝 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询 异常："+e.getMessage());
		} finally {
			logger.info("汇付宝 代付 查询 结束------------------------------------------------------------");
		}
	}

	private R order_wap(Order order){
		String merchantCode = order.getMerchNo();
		String orderId = merchantCode + order.getOrderNo();
		try{
			logger.info("汇付宝WAP微信 支付 开始---------------------------------------------");
			String version = RedisUtil.getPayCommonValue(HuifubaoConst.hfb_version);//版本号
			String is_phone = "1";//是否是手机端
			String is_frame = "0";//是否是公众号
			String pay_type = "30";//支付类型
			String agent_id = order.getPayMerch();
			String agent_bill_id = orderId;//商户内部订单编号
			String pay_amt = String.valueOf(order.getAmount());//订单金额
			String return_url = order.getReturnUrl();
			String notify_url = order.getNotifyUrl();
			String user_ip = order.getReqIp().replaceAll(",","_");
			String agent_bill_time = SmallTools.getDate("yyyyMMddHHmmss");//提交单据时间
			String goods_name = URLEncoder.encode(order.getProduct(),"GBK");//商品名称
			String remark = "";
			String sign = "";//签名结果
			String key = RedisUtil.getPayCommonValue(agent_id+ HuifubaoConst.PAYMERCHAT_KEY);//商户密钥
			String meta_option = "{\"s\":\"WAP\",\"n\":\""+ RedisUtil.getPayCommonValue(HuifubaoConst.Merchant_REQ_WAP_NAME)+"\",\"id\":\""+ RedisUtil.getPayCommonValue(HuifubaoConst.Merchant_REQ_WAP_URL)+"\"}";//详见开发文档
			String goods_note = URLEncoder.encode("blue","GBK");//支付说明
			String sign_type = "MD5";
			String goods_num = "1";

			//meta_option参数先Base64加密，再URLencode
			meta_option = URLEncoder.encode(new BASE64Encoder().encode(meta_option.getBytes("gb2312")),"UTF-8");
			String sign1 = "version="+version+
					"&agent_id="+agent_id+
					"&agent_bill_id="+agent_bill_id+
					"&agent_bill_time="+agent_bill_time+
					"&pay_type="+pay_type+
					"&pay_amt="+pay_amt+
					"&notify_url="+notify_url+
					"&return_url="+return_url+
					"&user_ip="+user_ip+
					"&key="+key;

			logger.info("签名参数："+sign1);
			//对签名参数进行MD5加密得到sign
			sign = SmallTools.MD5en(sign1).toLowerCase();
			//拼接请求参数
			String parameter = "version="+version+
					"&is_phone="+is_phone+
					"&is_frame="+is_frame+
					"&pay_type="+pay_type+
					"&agent_id="+agent_id+
					"&agent_bill_id="+agent_bill_id+
					"&pay_amt="+pay_amt+
					"&return_url="+return_url+
					"&notify_url="+notify_url+
					"&user_ip="+user_ip+
					"&agent_bill_time="+agent_bill_time+
					"&goods_name="+goods_name+
					"&remark="+remark+
					"&sign_type="+sign_type+
					"&goods_num="+goods_num+
					"&goods_note="+goods_note+
					"&meta_option="+meta_option+
					"&sign="+sign;

			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, RedisUtil.getPayCommonValue(HuifubaoConst.REQ_WAP_URL)+"?"+parameter);
			} catch (Exception e) {
				logger.error("jump加密异常！！");
				return R.error("加密异常");
			}
			return R.okData(resultMap);
		}catch(Exception e){
			logger.info("汇付宝WAP微信支付异常:"+e.getMessage());
			return R.error("汇付宝WAP微信支付异常");
		}finally {
			logger.info("汇付宝WAP微信支付 结束-----------------------------------------");
		}
	}


	/**
	 * @param order
	 * @param request
	 * @return
	 * @Description 支付回调
	 */
	public R notify_wap(Order order, HttpServletRequest request) {
		logger.info("汇付宝WAP微信回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String status = request.getParameter("result");//支付结果，1=成功（没有其他情况，因为之后支付成功才会下发异步通知）
			String agent_id = request.getParameter("agent_id");//商户ID
			String jnet_bill_no = request.getParameter("jnet_bill_no");//汇付宝订单号
			String agent_bill_id = request.getParameter("agent_bill_id");//商户系统订单号
			String pay_type = request.getParameter("pay_type");//支付类型
			String pay_amt = request.getParameter("pay_amt");//实际支付金额
			String remark = request.getParameter("remark");//商户自定义参数，透传参数
			String sign = request.getParameter("sign");//签名
			String key = HuifubaoConst.PAYMERCHAT_KEY;
			//验签
			String b_sign = "result="+status+
					"&agent_id="+agent_id+
					"&jnet_bill_no="+jnet_bill_no+
					"&agent_bill_id="+agent_bill_id+
					"&pay_type="+pay_type+
					"&pay_amt="+pay_amt+
					"&remark="+remark+
					"&key="+key;
			b_sign = SmallTools.MD5en(b_sign);

			order.setRealAmount(order.getAmount());
			if(b_sign.equalsIgnoreCase(sign)){
				msg = "验签成功";
			}else{
				msg = "验签失败";
			}
			if("1".equals(status)){
				msg = "订单处理完成";
				order.setOrderState(OrderState.succ.id());
				return R.ok(msg);
			}else{
				msg = "处理处理失败";
				order.setOrderState(OrderState.fail.id());
				return R.error(msg);
			}
		} catch (Exception e) {
			logger.info("汇付宝WAP微信支付回调 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("汇付宝WAP微信支付回调 异常：" + e.getMessage());
		} finally {
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("汇付宝WAP微信支付回调 结束-------------------------------------------------");
		}
	}

//	/**
//	 * 查询
//	 */
//	public R query_wap(Order order) {
//		String msg = null;
//		try {
//			logger.info("汇付宝 查询 开始-------------------------------------------------");
//			String version = RedisUtil.getPayCommonValue(HuifubaoConst.VERSION_WAP);//版本号
//			String agent_id = order.getPayMerch();
//			String agent_bill_id = order.getOrderNo();//商户内部订单编号
//			String agent_bill_time = SmallTools.getDate("yyyyMMddHHmmss");//提交单据时间
//			String remark = "";
//			String return_mode = "1";
//			String key = RedisUtil.getPayCommonValue(order.getPayMerch() + HuifubaoConst.PAYMERCHAT_KEY);//商户密钥
//			String b_sign = "version=" + version +
//					"&agent_id=" + agent_id +
//					"&agent_bill_id=" + agent_bill_id +
//					"&agent_bill_time=" + agent_bill_time +
//					"&return_mode=" + return_mode +
//					"&key=" + key;
//			String sign = SmallTools.MD5en(b_sign);
//
//			String params = "version=" + version +
//					"&agent_id=" + agent_id +
//					"&agent_bill_id=" + agent_bill_id +
//					"&agent_bill_time=" + agent_bill_time +
//					"&remark=" + remark +
//					"&return_mode=" + return_mode +
//					"&sign=" + sign;
//			String responseResult = RequestUtils.sendGet(RedisUtil.getPayCommonValue(HuifubaoConst.REQ_QUERY_WAP_URL), params);
//
//			logger.info("返回的参数:"+responseResult);
//
//			if (ParamUtil.isEmpty(responseResult)) {
//				return R.error("返回参数为空");
//			}
//			Map<String, String> resultMap = SmallTools.URLRequest(responseResult);
//
//			//1验签
//            String c_sign = resultMap.get("sign");
//            if (!ParamUtil.isNotEmpty(c_sign)) {
//                if (!sign.equalsIgnoreCase(c_sign)) {
//                    return R.error("验证失败");
//                }
//            }
//			//2状态
//			String status = resultMap.get("result");
//			if ("1".equals(status)) {
//				order.setOrderState(OrderState.succ.id());
//				msg = "订单处理完成";
//			} else if ("-1".equals(status)) {
//				msg = "处理失败";
//				order.setOrderState(OrderState.fail.id());
//			} else if ("0".equals(status)) {
//				msg = "订单处理中";
//				order.setOrderState(OrderState.ing.id());
//			}
//			return R.ok(msg);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return R.error("汇付宝查询异常");
//		} finally {
//			logger.info("汇付宝 查询 结束-------------------------------------------------");
//		}
//	}
}
