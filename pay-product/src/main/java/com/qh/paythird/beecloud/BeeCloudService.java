package com.qh.paythird.beecloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.pay.service.PayService;
import com.qh.paythird.beecloud.utils.BKUtil;
import com.qh.paythird.beecloud.utils.BeeCloudConst;
import com.qh.redis.service.RedisUtil;

import cn.beecloud.BCCache;
import cn.beecloud.BCPay;
import cn.beecloud.BeeCloud;
import cn.beecloud.BCEumeration.PAY_CHANNEL;
import cn.beecloud.bean.BCException;
import cn.beecloud.bean.BCOrder;
import cn.beecloud.bean.BCQueryParameter;

/**
 * 比可支付
 * @author Swell
 *
 */
@Service
public class BeeCloudService {

	private static final Logger logger = LoggerFactory.getLogger(BeeCloudService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("比可支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//京东快捷支付
				return order_q(order);
			} 
			
			logger.error("比可支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("比可支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 京东快捷支付
	 * @param order
	 * @return
	 */
	private R order_q(Order order){
		
		if (ParamUtil.isEmpty(order.getSign())) {
			logger.info("比可支付 京东快捷支付 跳转支付界面 开始--------------------------------------------");
			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, PayService.commonCardUrl(order));
			} catch (Exception e) {
				logger.error("比可支付 京东快捷支付 跳转支付界面 card加密异常");
				return R.error("加密异常");
			} finally {
				logger.info("比可支付 京东快捷支付 跳转支付界面 结束--------------------------------------------");
			}
			return R.okData(resultMap);
		}
		return cardPay(order);
	}
	
	/**
	 * 确认支付
	 * @param order
	 * @return
	 */
	private R cardPay(Order order){
		
		logger.info("比可支付 京东快捷支付 确认支付：{");
		try {
			JSONObject jo = new JSONObject();
			String payMerch = order.getPayMerch();
			String app_id = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_ID);
			jo.put("app_id", app_id);
			Long times = System.currentTimeMillis();
			jo.put("timestamp", times);
			String app_secret = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_SECRET);
			String app_sign = Md5Util.MD5(app_id+times+app_secret);
			jo.put("app_sign", app_sign);
			jo.put("bc_bill_id", order.getSign());
			jo.put("verify_code", order.getCheckCode());
			logger.info("比可支付 京东快捷支付 确认支付 上送的报文为："+ jo.toJSONString());
			String resString = RequestUtils.doPostJson(RedisUtil.getPayCommonValue(BeeCloudConst.CONFIRM_URL), jo.toJSONString());
			logger.info("比可支付 京东快捷支付 确认支付 返回参数：" + resString);
			JSONObject resJo = JSONObject.parseObject(resString);
			String result_code = resJo.getString("result_code");
			if(BeeCloudConst.RESULT_CODE_SUCC.equals(result_code)){
				// 如果支付成功，则以查询或者回调为主。。。
				if (OrderState.succ.id() == order.getOrderState()) {
					order.setOrderState(OrderState.ing.id());
				}
				Map<String, Object> map =new HashMap<String, Object>();
				map.put("realAmount", order.getAmount());
				return R.ok(map);
			}else{
				return R.error(resJo.getString("result_msg")+":"+resJo.getString("err_detail"));
			}
		} catch (Exception e) {
			logger.error("比可支付 京东快捷支付 确认支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("确认支付异常");
		} finally {
			logger.info("比可支付 京东快捷支付 确认支付：}");
		}
	}
	
	/**
	 * @Description 比可支付 京东快捷绑卡
	 * @param order
	 * @param userSign
	 */
	public R cardBind(Order order, MerchUserSignDO userSign) {
		
		logger.info("比可支付 京东快捷支付 绑卡：{");
		try {
			JSONObject jo = new JSONObject();
			String payMerch = order.getPayMerch();
			String app_id = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_ID);
			jo.put("app_id", app_id);
			Long times = System.currentTimeMillis();
			jo.put("timestamp", times);
			String app_secret = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_SECRET);
			String app_sign = Md5Util.MD5(app_id+times+app_secret);
			jo.put("app_sign", app_sign);
			jo.put("channel", BeeCloudConst.CHANNEL_BC_EXPRESS);
			String amount = ParamUtil.yuanToFen(order.getAmount());
			jo.put("total_fee", Integer.parseInt(amount));
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			jo.put("bill_no", orderId);
			jo.put("title", order.getProduct());
			jo.put("notify_url", PayService.commonNotifyUrl(order));
			jo.put("card_no", userSign.getBankNo());
			jo.put("bank", bankMap.get(userSign.getBankCode()));
			jo.put("id_holder", userSign.getAcctName());
			jo.put("id_no", userSign.getCertNo());
			Map<String, String> optional = new HashMap<String, String>();
			if (CardType.savings.id() == userSign.getCardType()) {
				jo.put("card_type", BeeCloudConst.CARD_TYPE_DEBIT);
			} else {
				jo.put("card_type", BeeCloudConst.CARD_TYPE_CREDIT);
				optional.put("pay_bank_expiry_date", userSign.getValidDate());
				optional.put("pay_bank_cvv2", userSign.getCvv2());
			}
			optional.put("user_mobile", userSign.getPhone());
			jo.put("optional", optional);
			logger.info("比可支付 京东快捷支付 上送的报文为："+ jo.toJSONString());
			String resString = RequestUtils.doPostJson(RedisUtil.getPayCommonValue(BeeCloudConst.REQ_URL), jo.toJSONString());
			logger.info("比可支付 京东快捷支付 返回参数：" + resString);
			JSONObject resJo = JSONObject.parseObject(resString);
			String result_code = resJo.getString("result_code");
			if(BeeCloudConst.RESULT_CODE_SUCC.equals(result_code)){
				String id = resJo.getString("id");
				userSign.setSign(id);
				order.setBusinessNo(id);
				Map<String, Object> data = new HashMap<>();
				data.put("sign", id);
				return R.ok(data);
			}else{
				return R.error(resJo.getString("result_msg")+":"+resJo.getString("err_detail"));
			}
		} catch (Exception e) {
			logger.error("比可支付 京东快捷支付 绑卡 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("绑卡异常");
		} finally {
			logger.info("比可支付 京东快捷支付 绑卡：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, String requestBody) {
		
		logger.info("比可支付 京东快捷支付 回调 开始----------------------------------------------------------------------------");
		String payMerch = order.getPayMerch();
		String masterSecret = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.MASTER_SECRET);
		String app_id = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_ID);
		String app_secret = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_SECRET);
		BeeCloud.registerApp(app_id, null,app_secret,masterSecret);
//		StringBuffer json = new StringBuffer();
//		String line = null;
		String msg = "";
		try { 
			logger.info("比可支付 京东快捷支付 回调 返回参数：" + requestBody);
			JSONObject jsonObj = JSONObject.parseObject(requestBody);

			String signature = jsonObj.getString("signature");
			String transactionId = jsonObj.getString("transaction_id");
			String transactionType = jsonObj.getString("transaction_type");
			String channelType = jsonObj.getString("channel_type");
			String transactionFee = jsonObj.get("transaction_fee").toString();
			boolean trade_success =  jsonObj.getBooleanValue("trade_success");
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("out_trade_no", transactionId);
			params.put("channel_type", channelType);
			params.put("transaction_fee", transactionFee);
			params.put("trade_success",trade_success);
			JSONObject message_detail = jsonObj.getJSONObject("message_detail");
			params.put("message_detail", message_detail);

			StringBuffer toSign = new StringBuffer();
			toSign.append(BCCache.getAppID()).append(transactionId).append(transactionType).append(channelType)
					.append(transactionFee);
			boolean status = BKUtil.verifySign(toSign.toString(), masterSecret, signature);
			if (status) {
				order.setRealAmount(ParamUtil.fenToYuan(transactionFee));
				if(trade_success){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else{
					msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
				}
				return R.ok(msg);
			} else {
				logger.info("比可支付 京东快捷支付 回调 验签失败！");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.error("比可支付 京东快捷支付 回调 返回出错！" + e.getMessage());
			return R.error("比可支付回调 异常：" + e.getMessage());
		} finally {
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("比可支付 京东快捷支付 回调 结束----------------------------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		/*2017/12/26 20:30:28
		L`Swell 2017/12/26 20:30:28
		查询不行哦。  是不是不能查询？
		大烟囱 2017/12/26 20:31:02
		没有查询接口
		大烟囱 2017/12/26 20:31:10
		有对账

		L`Swell 2017/12/26 20:31:29
		哦哦。   好的。 
		大烟囱 2017/12/26 20:31:43
		你们对哪笔订单有异议，可以在beecloud后台对账*/
		boolean isa = true;
		if(isa)return R.error("暂未提供查询接口");
		logger.info("比可支付 京东快捷支付 查询 开始-------------------------------------------------");
		String msg = "";
		String querytype = "BC_EXPRESS";
        BCQueryParameter param = new BCQueryParameter();
        if (querytype != null && querytype != "") {
            try {
                PAY_CHANNEL channel = PAY_CHANNEL.valueOf(querytype);
                param.setChannel(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        String merchantCode = order.getMerchNo();
		String orderId = merchantCode + order.getOrderNo();
        param.setNeedDetail(true);
        param.setBillNo(orderId);
        logger.info("比可支付 京东快捷支付 查询 请求参数：" +JSONObject.toJSONString(param));
        try {
            List<BCOrder> bcOrders = BCPay.startQueryBill(param);
            if(bcOrders!=null){
            	if(bcOrders.size()>=1){
            		BCOrder bcOrder = bcOrders.get(0);
            		String bcOrderStr = JSONObject.toJSONString(bcOrder);
            		logger.info("比可支付订单查询返回参数！"+bcOrderStr);
            		String messageDetail = bcOrder.getMessageDetail();
            		if(bcOrder.isResult()){
            			if(messageDetail!=null && !"".equals(messageDetail)){
//            				JSONObject jsonObject= JSONObject.parseObject(messageDetail);
//            				String result_code = jsonObject.get("result_code").toString();
            				return R.error("");
            			}else{
                    		return R.error("查询没有渠道返回的详细信息！");
                    	}
            		}else{
            			return R.error("支付失败！"+bcOrder.isResult());
            		}
            	}else{
            		return R.error("查询没有结果！");
            	}
            }else{
        		return R.error("查询没有结果！");
        	}
        } catch (BCException e) {
        	 logger.error("比可支付 京东快捷支付 查询 请求异常！"+e.getMessage());
        	 return R.error("支付查询 异常："+e.getMessage());
        } finally {
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("比可支付 京东快捷支付 查询 结束----------------------------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 比可京东快捷单笔代付发起（须在比可官方绑定IP：http://help.beecloud.cn/hc/kb/article/173489/）
	 * @param order
	 * @return
	 */
	public R orderAcp(Order order) {
		
		logger.info("比可支付 京东快捷 代付 开始--------------------------------------------------------------------");
		try {
			JSONObject jo = new JSONObject();
			String payMerch = order.getPayMerch();
			String app_id = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_ID);
			jo.put("app_id", app_id);
			Long times = System.currentTimeMillis();
			jo.put("timestamp", times);
			String app_secret = RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_SECRET);
			String app_sign = Md5Util.MD5(app_id+times+app_secret);
			jo.put("app_sign", app_sign);
			String amount = ParamUtil.yuanToFen(order.getAmount());
			jo.put("total_fee", Integer.parseInt(amount));
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			jo.put("bill_no", orderId);
			jo.put("title", order.getProduct()==null?"商家下发":order.getProduct());
			jo.put("account_type", "P");
			jo.put("bank_code", order.getUnionpayNo());
			jo.put("account_no", order.getBankNo());
			jo.put("account_name", order.getAcctName());
			jo.put("bank_point", order.getBankBranch());
			Map<String, String> optional = new HashMap<String, String>();
			jo.put("optional", optional);
			logger.info("比可支付 京东快捷 代付 上送的报文为："+ jo.toJSONString());
			String resString = RequestUtils.doPostJson(RedisUtil.getPayCommonValue(BeeCloudConst.REQ_DF_URL), jo.toJSONString());
			logger.info("比可支付 京东快捷 代付 返回参数：" + resString);
			JSONObject resJo = JSONObject.parseObject(resString);
			String result_code = resJo.getString("result_code");
			if(BeeCloudConst.RESULT_CODE_SUCC.equals(result_code)){
				order.setRealAmount(ParamUtil.fenToYuan(amount));
				String bfbSequenceNo = resJo.getString("id");
				if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
					order.setBusinessNo(bfbSequenceNo);
				}
				order.setOrderState(OrderState.succ.id());
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(PayConstants.acp_real_time, YesNoType.yes.id());
				return R.ok(map);
			}else{
				return R.error(resJo.getString("result_msg")+":"+resJo.getString("err_detail"));
			}
		} catch (Exception e) {
			logger.error("比可支付 京东快捷支付 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("{},{}", order.getMerchNo(), order.getOrderNo());
			logger.info("比可支付 京东快捷 代付 结束--------------------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 比可京东快捷单笔代付异步通知
	 * @param order
	 * @param request
	 * @return
	 */
	public R notifyAcp(Order order, HttpServletRequest request) {
		/*2018/1/5 16:27:09
		L`Swell 2018/1/5 16:27:09

		回调地址都不需要传的啊。  
		2018/1/5 16:28:28
		大烟囱 2018/1/5 16:28:28
		这个打款是同步的
		大烟囱 2018/1/5 16:28:32
		没有异步通知*/

		return R.error("暂未提供回调接口");
	}
	
	/**
	 * @Description 比可京东快捷单笔代付查询
	 * @param order
	 * @return
	 */
	public R acpQuery(Order order) {
		/*2017/12/26 20:30:28
		L`Swell 2017/12/26 20:30:28
		查询不行哦。  是不是不能查询？
		大烟囱 2017/12/26 20:31:02
		没有查询接口
		大烟囱 2017/12/26 20:31:10
		有对账

		L`Swell 2017/12/26 20:31:29
		哦哦。   好的。 
		大烟囱 2017/12/26 20:31:43
		你们对哪笔订单有异议，可以在beecloud后台对账*/
		return R.error("暂未提供查询接口");
	}
	
	/**
	 * @Description 更新银行卡列表
	 * @param order
	 * @param bank_savings
	 * @param bank_credits
	 */
	public void refreshBanks(Order order, List<String> bank_savings, List<String> bank_credits) {
		
		List<String> c = new ArrayList<String>(bankMap.keySet());
		bank_savings.addAll(c);
		bank_credits.addAll(c);
	}
	
	private static final Map<String, String> bankMap = new HashMap<String, String>(32);
	static {
		bankMap.put(BankCode.ICBC.name(), "中国工商银行");
		bankMap.put(BankCode.ABC.name(), "中国农业银行");
		bankMap.put(BankCode.BOC.name(), "中国银行");
		bankMap.put(BankCode.CCB.name(), "中国建设银行");
		bankMap.put(BankCode.BCOM.name(), "交通银行");
		bankMap.put(BankCode.CMB.name(), "招商银行");
		bankMap.put(BankCode.GDB.name(), "广发银行");
		bankMap.put(BankCode.CITIC.name(), "中信银行");
		bankMap.put(BankCode.CMBC.name(), "中国民生银行");
		bankMap.put(BankCode.CEB.name(), "中国光大银行");
		bankMap.put(BankCode.PABC.name(), "平安银行");
		bankMap.put(BankCode.SPDB.name(), "浦发银行");
		bankMap.put(BankCode.PSBC.name(), "中国邮政储蓄银行");
		bankMap.put(BankCode.HXB.name(), "华夏银行");
		bankMap.put(BankCode.CIB.name(), "兴业银行");
		bankMap.put(BankCode.BOB.name(), "北京银行");
		bankMap.put(BankCode.BOS.name(), "上海银行");
		bankMap.put(BankCode.BRCB.name(), "北京农商银行");
	}
	
	
	/*测试
	public static void main(String[] args) {
		BeeCloudService b = new BeeCloudService();
		b.orderAcp(null);
	}
	public R orderAcp(Order order) {
		
		logger.info("比可支付 京东快捷 代付 开始--------------------------------------------------------------------");
		try {
			JSONObject jo = new JSONObject();
//			String payMerch = order.getPayMerch();
			String app_id = "22ca3d33-cbcd-4729-a5d3-1eb0220de9e7";//RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_ID);
			jo.put("app_id", app_id);
			Long times = System.currentTimeMillis();
			jo.put("timestamp", times);
			String app_secret = "614225a3-01e6-4c93-8c41-abee0998d070";//RedisUtil.getPayCommonValue(payMerch + BeeCloudConst.APP_SECRET);
			String app_sign = Md5Util.MD5(app_id+times+app_secret);
			jo.put("app_sign", app_sign);
			String amount = "100";//ParamUtil.yuanToFen(order.getAmount());
			jo.put("total_fee", Integer.parseInt(amount));
			String merchantCode = times+"";//order.getMerchNo();
			String orderId = merchantCode ;//+ order.getOrderNo();
			jo.put("bill_no", orderId);
			jo.put("title", "测试商品");//order.getProduct());
			jo.put("account_type", "P");
			jo.put("bank_code", "105551001065");//order.getUnionpayNo());
			jo.put("account_no", "6236682920005566672");//order.getBankNo());
			jo.put("account_name", "罗四维");//order.getAcctName());
			jo.put("bank_point", "中国建设银行股份有限公司长沙芙蓉南路支行");//order.getBankBranch());
			Map<String, String> optional = new HashMap<String, String>();
			jo.put("optional", optional);
			logger.info("比可支付 京东快捷 代付 上送的报文为："+ jo.toJSONString());
			String reqUrl = "https://api.beecloud.cn/2/rest/qs/transfer";//RedisUtil.getPayCommonValue(BeeCloudConst.REQ_DF_URL);
			String resString = RequestUtils.doPostJson(reqUrl, jo.toJSONString());
			logger.info("比可支付 京东快捷 代付 返回参数：" + resString);
			JSONObject resJo = JSONObject.parseObject(resString);
			String result_code = resJo.getString("result_code");
			if(BeeCloudConst.RESULT_CODE_SUCC.equals(result_code)){
				order.setRealAmount(ParamUtil.fenToYuan(amount));
				String bfbSequenceNo = resJo.getString("id");
				if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
					order.setBusinessNo(bfbSequenceNo);
				}
				return R.ok("订单处理完成");
			}else{
				return R.error(resJo.getString("result_msg")+":"+resJo.getString("err_detail"));
			}
		} catch (Exception e) {
			logger.error("比可支付 京东快捷支付 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("比可支付 京东快捷 代付 结束--------------------------------------------------------------------");
		}
	}*/
}
