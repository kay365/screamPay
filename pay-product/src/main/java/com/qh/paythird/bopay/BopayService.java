package com.qh.paythird.bopay;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.PayProKeyConst;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.Currency;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.redis.service.RedisUtil;

import net.sf.json.JSONObject;

/**
 * @ClassName BopayService
 * @Description bopay支付服务类
 * @Date 2017年11月8日 下午5:24:10
 * @version 1.0.0
 */
@Service
public class BopayService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BopayService.class);

	/**
	 * @Description bopay支付
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		String outChannel = order.getOutChannel();
		/* ======== businessHead ===================== */
		JSONObject businessHead = new JSONObject();
		// 字符集 00表示UTF-8，暂时只支持UTF-8
		businessHead.put("charset", "00");
		// 版本号
		businessHead.put("version", "V1.0");
		// 商户号
		businessHead.put("merchantNumber", order.getPayMerch());
		// 请求时间
		businessHead.put("requestTime", order.getReqTime());
		// 签名类型
		businessHead.put("signType", "RSA");
		// 请求服务类型 各接口自定义
		if (outChannel.equals(OutChannel.wy.name())) {
			businessHead.put("tradeType", "bankPayment");
		} else if (outChannel.equals(OutChannel.qq.name())) {
			businessHead.put("tradeType", "qqPayApi");
		}

		/* ======== businessContext ===================== */
		JSONObject businessContext = new JSONObject();
		businessContext.put("orderNumber", order.getMerchNo() + order.getOrderNo());
		businessContext.put("amount", ParamUtil.yuanToFen(order.getAmount()));
		businessContext.put("currency", Currency.CNY.name());
		businessContext.put("commodityName", order.getTitle());
		businessContext.put("commodityDesc", order.getProduct());
		businessContext.put("commodityRemark", order.getMemo());
		if (outChannel.equals(OutChannel.wy.name())) {
			if (CardType.savings.name().equals(order.getCardType())) {
				businessContext.put("cardType", BopayConst.cardType_SAVINGS);
				if (AcctType.pri.name().equals(order.getAcctType())) {
					businessContext.put("payType", BopayConst.payType_UNION_B2C_SAVINGS);
				} else {
					businessContext.put("payType", BopayConst.payType_UNION_B2B);
				}
			} else {
				businessContext.put("cardType", BopayConst.cardType_CREDIT);
				if (AcctType.pri.name().equals(order.getAcctType())) {
					businessContext.put("payType", BopayConst.payType_UNION_B2C_CREDIT);
				} else {
					businessContext.put("payType", BopayConst.payType_UNION_B2B);
				}
			}
			businessContext.put("bankNumber", BopayConst.bankNumberMap.get(order.getBankName()));
			businessContext.put("returnUrl", PayService.commonReturnUrl(order));
		} else if (outChannel.equals(OutChannel.qq.name())) {
			businessContext.put("payType", BopayConst.payType_QQ_SCAN);
			businessContext.put("orderCreateIp", order.getReqIp());
		} else if (outChannel.equals(OutChannel.q.name())) {

		}
		businessContext.put("notifyUrl", PayService.commonNotifyUrl(order));
		logger.info("九派支付 businessContext : " + businessContext);
		logger.info("九派支付 businessHead : " + businessHead);
		String payMerch = order.getPayMerch();
		try {
			String context = BoPayRSAUtils.verifyAndEncryptionToString(businessContext, businessHead,
					RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key),
					RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
			logger.info("九派支付签名之后 context :" + context);
			if (outChannel.equals(OutChannel.wy.name())) {
				String url = RedisUtil.getPayCommonValue(PayProKeyConst.bopay_url)
						+ RedisUtil.getPayCommonValue(PayProKeyConst.bopay_cardPayUrl)
						+ URLEncoder.encode(context, Constant.ec_utf_8);
				logger.info("九派请求url：" + url);
				Map<String, String> data = PayService.initRspData(order);
				data.put(PayConstants.web_code_url, url);
				return R.okData(data);
			} else if (outChannel.equals(OutChannel.qq.name())) {
				String url = RedisUtil.getPayCommonValue(PayProKeyConst.bopay_url)
						+ RedisUtil.getPayCommonValue(PayProKeyConst.bopay_qqPayUrl);
				logger.info("九派请求url：" + url);
				net.sf.json.JSONObject jsonParam = new net.sf.json.JSONObject();
				jsonParam.put("context", context);
				String responseData = RequestUtils.doPostJson(url, jsonParam.toString(), Constant.ec_utf_8);
				logger.info("九派支付QQ请求结果：" + responseData);
				com.alibaba.fastjson.JSONObject jsonResult = JSON.parseObject(responseData);
				String resultContext = jsonResult.getString("context");
				String decrypt = null;
				try {
					decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
							RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
				} catch (Exception e) {
					logger.info("=========bopayQQ支付返回结果解密失败 :" + e.getMessage());
				}
				logger.info("====bopay QQ支付解密后结果:" + decrypt);
				if (BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key))) {
					net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
					Map<String, String> data = PayService.initRspData(order);
					String qrcode = null;
					if (respResult != null && respResult.getJSONObject("businessContext") != null) {
						qrcode = respResult.getJSONObject("businessContext").getString("qrcode");
					}
					if (qrcode != null) {
						data.put(PayConstants.web_qrcode_url, qrcode);
						return R.okData(data);
					} else {
						return R.error("请求失败！");
					}
				}
			} else {
				return R.error("暂不支持通道：" + outChannel);
			}
		} catch (Exception e) {
			logger.error("九派支付发起支付请求失败！" + e.getMessage());
		}
		return R.error("支付请求异常");
	}

	/**
	 * @Description bopay 回调处理
	 * @param order
	 * @param request
	 * @param requestBody
	 * @return
	 */
	public R notify(Order order, String requestBody) {
		logger.info("接收到bopay支付后台异步回调！");
		logger.info("接收到bopay支付后台回调原始参数:" + requestBody);
		net.sf.json.JSONObject jsonResult = net.sf.json.JSONObject.fromObject(requestBody);
		String payMerch = order.getPayMerch();
		if (ParamUtil.isNotEmpty(jsonResult)) {
			logger.info("bopay支付返回参数：" + jsonResult.toString());
			String resultContext = jsonResult.getString("context");
			String decrypt = null;
			try {
				decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
						RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
				logger.error("bopay支付解密结果:" + decrypt);
			} catch (Exception e) {
				logger.error("bopay支付解密失败：" + e.getMessage());
				return R.error("解密失败！" + order.getMerchNo() + "," + order.getOrderNo());
			}
			boolean isVerify = false;
			try {
				isVerify = BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
			} catch (Exception e) {
				logger.error("bopay支付验签失败：" + e.getMessage());
				return R.error("验签失败！" + order.getMerchNo() + "," + order.getOrderNo());
			}
			if (isVerify) {
				net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
				net.sf.json.JSONObject businessContext = (net.sf.json.JSONObject) respResult.get("businessContext");
				order.setRealAmount(ParamUtil.fenToYuan(businessContext.getString("amount")));
				order.setBusinessNo(businessContext.getString("bopayOrderNumber"));
				String tradeState = businessContext.getString("tradeState");
				String errMsg = businessContext.getString("stateExplain");
				return handOrderState(order, tradeState, errMsg);
			} else {
				logger.error("bopay后台通知验签失败！");
			}
		} else {
			logger.error("bopay支付返回参数为空或失败");
		}
		return R.error("支付处理失败！" + order.getMerchNo() + "," + order.getOrderNo());
	}

	/**
	 * @Description 交易状态处理
	 * @param order
	 * @param tradeState
	 * @param errMsg
	 * @return
	 */
	private R handOrderState(Order order, String tradeState, String msg) {
		if (BopayConst.tradeState_SUC.equals(tradeState)) {
			order.setOrderState(OrderState.succ.id());
			msg = "订单处理成功！" + order.getMerchNo() + "," + order.getOrderNo();
		} else if (BopayConst.tradeState_WAIT.equals(tradeState)) {
			order.setOrderState(OrderState.init.id());
			msg = "订单未处理！" + order.getMerchNo() + "," + order.getOrderNo() + msg;
		} else if (BopayConst.tradeState_HANDLE.equals(tradeState)) {
			order.setOrderState(OrderState.ing.id());
			msg = "订单处理中！" + order.getMerchNo() + "," + order.getOrderNo() + msg;
		} else if (BopayConst.tradeState_FAIL.equals(tradeState)) {
			order.setOrderState(OrderState.fail.id());
			msg = "订单处理失败！" + order.getMerchNo() + "," + order.getOrderNo() + msg;
		} else if (BopayConst.tradeState_CLOSE.equals(tradeState)) {
			order.setOrderState(OrderState.close.id());
			msg = "订单交易关闭！" + order.getMerchNo() + "," + order.getOrderNo() + msg;
		} else {
			msg = "订单交易状态未名！" + order.getMerchNo() + "," + order.getOrderNo() + msg;
		}
		logger.info(msg);
		return R.ok(msg);
	}

	/**
	 * @Description 支付订单查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		net.sf.json.JSONObject businessHead = new net.sf.json.JSONObject();
		String payMerch = order.getPayMerch();
		businessHead.put("charset", "00");
		// 版本号
		businessHead.put("version", "V1.0");
		// 商户号
		businessHead.put("merchantNumber", payMerch);
		// 请求时间
		String tranTime = DateUtil.getCurrentNumStr();
		businessHead.put("requestTime", tranTime);
		// 签名类型
		businessHead.put("signType", "RSA");
		String url = RedisUtil.getPayCommonValue(PayProKeyConst.bopay_url);
		net.sf.json.JSONObject businessContext = new net.sf.json.JSONObject();
		// 网银支付
		if (OutChannel.wy.name().equals(order.getOutChannel())) {
			businessHead.put("tradeType", "rpmbankQuery");
			businessContext.put("merchantserialNo", payMerch + order.getMerchNo() + order.getOrderNo());
			businessContext.put("payType", BopayConst.payType_UNION_B2C_SAVINGS);
			url = url + RedisUtil.getPayCommonValue(PayProKeyConst.bopay_queryCardPayUr);
			// qq支付
		} else if (OutChannel.qq.name().equals(order.getOutChannel())) {
			businessHead.put("tradeType", "qqPayQuery");
			businessContext.put("orderNumber", payMerch + order.getMerchNo() + order.getOrderNo());
			businessContext.put("payType", BopayConst.payType_QQ_SCAN);
			url = url + RedisUtil.getPayCommonValue(PayProKeyConst.bopay_qqQueryUrl);
		}

		logger.info("=========bopay支付查询加密前businessContext " + businessContext);
		logger.info("=========bopay支付查询加密前businessHead：" + businessHead);

		String context = null;
		try {
			context = BoPayRSAUtils.verifyAndEncryptionToString(businessContext, businessHead,
					RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key),
					RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
		} catch (Exception e) {
			logger.info("=========bopay支付查询加密错误：" + e.getMessage());
		}
		logger.info("=========bopay支付查询签名加密后context :" + context);

		net.sf.json.JSONObject jsonParam = new net.sf.json.JSONObject();
		jsonParam.put("context", context);

		logger.info("====bopay支付查询URL:" + url);
		String respesult = RequestUtils.doPostJson(url, jsonParam.toString(), Constant.ec_utf_8);
		logger.info("====bopay支付查询Post后结果:" + respesult);
		if (ParamUtil.isNotEmpty(respesult)) {
			com.alibaba.fastjson.JSONObject jsonResult = JSON.parseObject(respesult);
			if (jsonResult.getBoolean("success")) {
				String resultContext = jsonResult.getString("context");
				String decrypt = null;
				try {
					decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
							RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
				} catch (Exception e) {
					logger.error("=========bopay支付查询返回结果解密失败 :" + e.getMessage());
				}
				logger.info("====bopay支付解密后结果:" + decrypt);
				if (BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key))) {
					net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
					businessContext = (net.sf.json.JSONObject) respResult.get("businessContext");
					if(order.getRealAmount() == null || order.getRealAmount().compareTo(BigDecimal.ZERO) == 0){
						order.setRealAmount(ParamUtil.fenToYuan(businessContext.getString("amount")));
					}
					String bopayOrderNumber = null;
					if (OutChannel.wy.name().equals(order.getOutChannel())) {
						bopayOrderNumber = businessContext.getString("bopaySerialNo");
					} else if (OutChannel.qq.name().equals(order.getOutChannel())) {
						bopayOrderNumber = businessContext.getString("bopayOrderNumber");
					}
					if (ParamUtil.isNotEmpty(bopayOrderNumber)) {
						order.setBusinessNo(bopayOrderNumber);
					}
					String tradeState = businessContext.getString("tradeState");
					String errMsg = businessContext.getString("stateExplain");
					return handOrderState(order, tradeState, errMsg);
				} else {
					logger.error("bopay支付查询返回验签失败！");
				}
			} else {
				logger.error("bopay支付查询请求结果异常！");
			}
		} else {
			logger.error("bopay支付查询请求结果异常！");
		}
		return R.error("支付订单查询异常！" + order.getMerchNo() + "," + order.getOrderNo());
	}

	/**
	 * @Description bopay代付
	 * @param order
	 * @return
	 */
	public R orderAcp(Order order) {
		String payMerch = order.getPayMerch();
		net.sf.json.JSONObject businessHead = new net.sf.json.JSONObject();
		// 字符集 00表示UTF-8，暂时只支持UTF-8
		businessHead.put("charset", "00");
		// 版本号
		businessHead.put("version", "V1.0");
		// 商户号
		businessHead.put("merchantNumber", payMerch);
		// 请求服务类型 各接口自定义，如该文档接口为：网关支付
		businessHead.put("tradeType", "bankPayment");
		// 请求时间
		String tranTime = DateUtil.getCurrentNumStr();
		businessHead.put("requestTime", tranTime);
		// 签名类型
		businessHead.put("signType", "RSA");
		net.sf.json.JSONObject businessContext = new net.sf.json.JSONObject();
		businessContext.put("amount", order.getAmount());
		businessContext.put("currency", order.getCurrency());
		// 账户户名（下发卡持有人姓名
		businessContext.put("accName", order.getAcctName());
		// 银行卡号（下发的卡号
		businessContext.put("cardNo", order.getBankNo());
		// 手机号（长度11 开户卡手机号
		businessContext.put("cellPhone", order.getMobile());
		// 收款人开户行号（对公必填
		businessContext.put("bankCode", BopayConst.bankNumberMap.get(order.getBankName()));
		// 收款人开户行名称（对公必填
		businessContext.put("bankName", order.getBankName());
		// 支行名称（可空）
		businessContext.put("bankBranchName", "");
		// SAVINGS-储蓄卡（只支持）、CREDIT-信用卡（不支持)
		if (CardType.savings.name().equals(order.getCardType())) {
			businessContext.put("cardType", BopayConst.cardType_SAVINGS);
			// cvv2 信用卡必传
			businessContext.put("cvv2", "");
			// 信用有效期格式 YYMM 信用卡必传
			businessContext.put("validPeriod", "");
		} else {
			businessContext.put("cardType", BopayConst.cardType_CREDIT);
			// cvv2 信用卡必传
			businessContext.put("cvv2", "");
			// 信用有效期格式 YYMM 信用卡必传
			businessContext.put("validPeriod", "");
		}
		if (AcctType.pri.equals(order.getAcctType())) {
			// 账户类型
			businessContext.put("accType", BopayConst.accType_PRI);
		} else {
			businessContext.put("accType", BopayConst.accType_PUB);
		}
		// 商户单号（商户号+下发类型+商户单号全局唯一 ）
		businessContext.put("orderNumber", order.getOrderNo());
		// 订单备注
		businessContext.put("remark", order.getTitle());
		// 银行附言
		businessContext.put("bankRemark", order.getMemo());
		logger.info("=========bopay代付加密前businessContext " + businessContext);

		logger.info("=========bopay代付加密前businessHead：" + businessHead);

		String context = null;
		try {
			context = BoPayRSAUtils.verifyAndEncryptionToString(businessContext, businessHead,
					RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key),
					RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
		} catch (Exception e) {
			logger.info("=========bopay代付加密错误：" + e.getMessage());
			return R.error("代付查询加密错误");
		}
		logger.info("=========bopay代付签名加密后context :" + context);

		net.sf.json.JSONObject jsonParam = new net.sf.json.JSONObject();
		jsonParam.put("context", context);
		String url = RedisUtil.getPayCommonValue(PayProKeyConst.bopay_url)
				+ RedisUtil.getPayCommonValue(PayProKeyConst.bopay_paySingleUrl);
		String respesult = RequestUtils.doPostJson(url, jsonParam.toString(), Constant.ec_utf_8);
		logger.info("====bopay代付Post后结果:" + respesult);
		if (ParamUtil.isNotEmpty(respesult)) {
			com.alibaba.fastjson.JSONObject jsonResult = com.alibaba.fastjson.JSONObject.parseObject(respesult);
			if (jsonResult.getBoolean("success")) {
				String resultContext = jsonResult.getString("context");
				String decrypt = null;
				try {
					decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
							RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
				} catch (Exception e) {
					logger.info("=========bopay代付返回结果解密失败 :" + e.getMessage());
				}
				logger.info("====bopay代付解密后结果:" + decrypt);
				if (BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key))) {
					net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
					businessContext = (net.sf.json.JSONObject) respResult.get("businessContext");
					String tradeState = businessContext.getString("tradeState");
					order.setRealAmount(ParamUtil.fenToYuan(businessContext.getString("amount")));
					order.setBusinessNo(businessContext.getString("bopaySerialNo"));
					String errMsg = businessContext.getString("stateExplain");
					return handOrderState(order, tradeState, errMsg);
				} else {
					logger.error("bopay代付返回验签失败！");
				}
			} else {
				logger.error("bopay代付请求结果异常！");
			}
		} else {
			logger.error("bopay代付请求结果异常！");
		}
		return R.error("代付请求结果异常");
	}

	/**
	 * @Description 代付订单回调
	 * @param order
	 * @param requestBody
	 * @return
	 */
	public R notifyAcp(Order order, String requestBody) {
		logger.info("接收到bopay代付后台异步回调！");
		logger.info("接收到bopay代付后台回调原始参数:" + requestBody);
		net.sf.json.JSONObject jsonResult = net.sf.json.JSONObject.fromObject(requestBody);
		String payMerch = order.getPayMerch();
		if (ParamUtil.isNotEmpty(jsonResult)) {
			logger.info("bopay支付返回参数：" + jsonResult.toString());
			String resultContext = jsonResult.getString("context");
			String decrypt = null;
			try {
				decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
						RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
				logger.error("bopay代付解密结果:" + decrypt);
			} catch (Exception e) {
				logger.error("bopay代付解密失败：" + e.getMessage());
				return R.error("解密失败！" + order.getMerchNo() + "," + order.getOrderNo());
			}
			boolean isVerify = false;
			try {
				isVerify = BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
			} catch (Exception e) {
				logger.error("bopay代付验签失败：" + e.getMessage());
				return R.error("验签失败！" + order.getMerchNo() + "," + order.getOrderNo());
			}
			if (isVerify) {
				net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
				net.sf.json.JSONObject businessContext = (net.sf.json.JSONObject) respResult.get("businessContext");
				if(order.getRealAmount() == null || order.getRealAmount().compareTo(BigDecimal.ZERO) == 0){
					order.setRealAmount(ParamUtil.fenToYuan(businessContext.getString("amount")));
				}
				if(ParamUtil.isEmpty(order.getBusinessNo())){
					order.setBusinessNo(businessContext.getString("bopayOrderNumber"));
				}
				String tradeState = businessContext.getString("tradeState");
				String errMsg = businessContext.getString("stateExplain");
				return handOrderState(order, tradeState, errMsg);
			} else {
				logger.error("bopay后台通知验签失败！");
			}
		} else {
			logger.error("bopay代付返回参数为空或失败");
		}
		return R.error("代付处理失败！" + order.getMerchNo() + "," + order.getOrderNo());
	}

	/**
	 * @Description 代付订单查询
	 * @param order
	 * @return
	 */
	public R acpQuery(Order order) {
		net.sf.json.JSONObject businessHead = new net.sf.json.JSONObject();
		String payMerch = order.getPayMerch();
		businessHead.put("charset", "00");
		// 版本号
		businessHead.put("version", "V1.0");
		// 商户号
		businessHead.put("merchantNumber", payMerch);
		// 请求服务类型 各接口自定义，如该文档接口为：网关支付
		businessHead.put("tradeType", "bankPayment");
		// 请求时间
		String tranTime = DateUtil.getCurrentNumStr();
		businessHead.put("requestTime", tranTime);
		// 签名类型
		businessHead.put("signType", "RSA");

		net.sf.json.JSONObject businessContext = new net.sf.json.JSONObject();
		businessContext.put("bopaySerialNo", order.getBusinessNo());
		logger.info("=========bopay代付查询加密前businessContext " + businessContext);
		logger.info("=========bopay代付查询加密前businessHead：" + businessHead);

		String context = null;
		try {
			context = BoPayRSAUtils.verifyAndEncryptionToString(businessContext, businessHead,
					RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key),
					RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key));
		} catch (Exception e) {
			logger.info("=========bopay支付查询加密错误：" + e.getMessage());
			return R.error("代付查询加密错误");
		}

		logger.info("=========bopay代付查询签名加密后context :" + context);
		net.sf.json.JSONObject jsonParam = new net.sf.json.JSONObject();
		jsonParam.put("context", context);
		String url = RedisUtil.getPayCommonValue(PayProKeyConst.bopay_url)
				+RedisUtil.getPayCommonValue(PayProKeyConst.bopay_queryIssuOrder);
		String respesult = RequestUtils.doPostJson(url, jsonParam.toString(), Constant.ec_utf_8);
		logger.info("====bopay代付查询Post后结果:" + respesult);
		if (ParamUtil.isEmpty(respesult)) {
			logger.error("bopay代付查询请求结果异常！");
			return R.error("代付查询请求结果异常");
		}

		com.alibaba.fastjson.JSONObject jsonResult = com.alibaba.fastjson.JSONObject.parseObject(respesult);
		if (!jsonResult.getBoolean("success")) {
			logger.error("代付查询失败！");
			return R.error("代付查询失败");
		}

		String resultContext = jsonResult.getString("context");
		String decrypt = null;
		try {
			decrypt = BoPayRSAUtils.decryptByPrivateKey(resultContext,
					RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.bopay_mc_private_key));
		} catch (Exception e) {
			logger.error("=========bopay代付付查询返回结果解密失败 :" + e.getMessage());
			return R.error("代付查询解密失败");
		}
		
		logger.info("====bopay代付解密后结果:" + decrypt);
		if (BoPayRSAUtils.verify(decrypt, RedisUtil.getPayCommonValue(PayProKeyConst.bopay_public_key))) {
			net.sf.json.JSONObject respResult = net.sf.json.JSONObject.fromObject(decrypt);
			businessContext = (net.sf.json.JSONObject) respResult.get("businessContext");
			if(order.getRealAmount() == null || order.getRealAmount().compareTo(BigDecimal.ZERO) <= 0){
				order.setRealAmount(ParamUtil.fenToYuan(businessContext.getString("amount")));
			}
			String tradeState = businessContext.getString("tradeState");
			String errMsg = businessContext.getString("stateExplain");
			return handOrderState(order, tradeState, errMsg);
		} else {
			logger.error("bopay代付查询返回验签失败！");
			return R.error("代付查询结果验签失败");
		}
        
	}

}
