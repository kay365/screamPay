package com.qh.paythird.jiupay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.PayProKeyConst;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.CardSendType;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.CertType;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.Base64Utils;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.pay.service.PayService;
import com.qh.redis.service.RedisUtil;

/**
 * @ClassName JiupayService
 * @Description 九派支付服务类
 * @Date 2017年11月22日 下午5:54:41
 * @version 1.0.0
 */
@Service
public class JiupayService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JiupayService.class);

	/**
	 * @Description 获取针对 九派的用户Id
	 * @param order
	 * @return
	 */
	private String getMemberId(Order order) {
		return order.getUserId(); 
	}

	/**
	 * @Description 更新订单状态
	 * @param order
	 * @param string
	 */
	private String handOrderState(Order order, String orderSts) {
		String msg = "";
		if (ParamUtil.isEmpty(orderSts)) {
			msg = "订单状态不存在";
		}
		if (JiupayConst.orderSts_PD.equals(orderSts) || JiupayConst.orderSts_RF.equals(orderSts)
				|| JiupayConst.orderSts_RP.equals(orderSts)) {
			msg = "订单处理完成";
			order.setOrderState(OrderState.succ.id());
		} else if (JiupayConst.orderSts_WP.equals(orderSts) || JiupayConst.orderSts_PP.equals(orderSts)
				|| JiupayConst.orderSts_B2.equals(orderSts) || JiupayConst.orderSts_RQ.equals(orderSts)
				|| JiupayConst.orderSts_R1.equals(orderSts) || JiupayConst.orderSts_RE.equals(orderSts)) {
			msg = "订单处理中";
			order.setOrderState(OrderState.ing.id());
		} else if (JiupayConst.orderSts_EX.equals(orderSts) || JiupayConst.orderSts_CZ.equals(orderSts)
				|| JiupayConst.orderSts_CA.equals(orderSts)) {
			msg = "订单关闭";
			order.setOrderState(OrderState.close.id());
		} else if (JiupayConst.orderSts_RK.equals(orderSts) || JiupayConst.orderSts_NE.equals(orderSts)) {
			msg = "处理失败";
			order.setOrderState(OrderState.fail.id());
		}
		logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
		return msg;
	}

	/**
	 * @Description 更新代付订单状态
	 * @param order
	 * @param string
	 */
	private String handOrderAcpState(Order order, String orderSts) {
		String msg = "";
		if (ParamUtil.isEmpty(orderSts)) {
			msg = "订单状态不存在";
		}
		if (JiupayConst.orderSts_acp_S.equals(orderSts)) {
			msg = "订单处理完成";
			order.setOrderState(OrderState.succ.id());
		} else if (JiupayConst.orderSts_acp_P.equals(orderSts) || JiupayConst.orderSts_acp_N.equals(orderSts)) {
			msg = "订单处理中";
			order.setOrderState(OrderState.ing.id());
		} else if (JiupayConst.orderSts_acp_F.equals(orderSts) || JiupayConst.orderSts_acp_R.equals(orderSts)) {
			msg = "处理失败";
			order.setOrderState(OrderState.fail.id());
		} else if(JiupayConst.orderSts_acp_U.equals(orderSts)){
			msg = "初始化";
			order.setOrderState(OrderState.init.id());
		}
		logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
		return msg;
	}
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		// 快捷支付
		if (OutChannel.q.name().equals(order.getOutChannel())) {
			if (ParamUtil.isEmpty(order.getSign())) {
				Map<String, String> resultMap = PayService.initRspData(order);
				try {
					resultMap.put(PayConstants.web_code_url, PayService.commonCardUrl(order));
				} catch (Exception e) {
					logger.error("card加密异常");
					return R.error("加密异常");
				}
				return R.okData(resultMap);
			}
			// 快捷支付
			return order_q(order);
		} else if (OutChannel.wy.name().equals(order.getOutChannel())) {
			return order_wy(order);
		} else {
			logger.error("请确认支付渠道：{}", order.getOutChannel());
			return R.error("请确认支付渠道");
		}

	}

	/**
	 * @Description 网银支付
	 * @param order
	 * @return
	 */
	private R order_wy(Order order) {
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		String orderNo = order.getOrderNo();
		String service = JiupayConst.service_rpmBankPayment;
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		dataMap.put("pageReturnUrl", PayService.commonReturnUrl(order));
		dataMap.put("notifyUrl", PayService.commonNotifyUrl(order));
		dataMap.put("merchantName", order.getMerchNo());
		// 二级商户号
		// dataMap.put("subMerchantId", "");
		dataMap.put("memberId", getMemberId(order));
		dataMap.put("orderTime", order.getReqTime());
		dataMap.put("orderId", order.getMerchNo() + orderNo);
		dataMap.put("totalAmount", ParamUtil.yuanToFen(order.getAmount()));
		dataMap.put("currency", order.getCurrency());
		// 银行卡简称
		dataMap.put("bankAbbr", order.getBankCode());
		// 卡号类型
		dataMap.put("cardType", order.getCardType());
		// 支付类型
		String requestUrl = null;
		if (AcctType.pub.id() == order.getAcctType()) {
			dataMap.put("payType", JiupayConst.payType_B2B);
			requestUrl = RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_b2b_requestUrl);
		} else {
			dataMap.put("payType", JiupayConst.payType_B2C);
			requestUrl = RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl);
		}
		// 订单有效期
		dataMap.put("validUnit", JiupayConst.validUnit_min);
		dataMap.put("validNum", JiupayConst.validNum);
		dataMap.put("showUrl", "");
		dataMap.put("goodsName", order.getProduct());
		dataMap.put("goodsId", "");
		dataMap.put("goodsDesc", order.getMemo());

		ParamUtil.trimValue(dataMap);

		// 商户签名
		if (merchantSign(service, payMerch, dataMap) == null) {
			return R.error("商户签名异常");
		}
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
		jumpData.put(PayConstants.web_params, dataMap);
		jumpData.put(PayConstants.web_form_url, 1);
		jumpData.put(PayConstants.web_action, requestUrl);
		order.setJumpData(jumpData);
		return R.okData(resultMap);
	}

	/**
	 * @Description 快捷支付
	 * @param order
	 * @return
	 */
	private R order_q(Order order) {
		String payMerch = order.getPayMerch();
		String service = JiupayConst.service_rpmQuickPayInit;
		if (ParamUtil.isNotEmpty(order.getCheckCode())) {
			service = JiupayConst.service_rpmQuickPayCommit;
		}
		String requestTime = DateUtil.getCurrentNumStr();
		String orderNo = order.getOrderNo();

		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		dataMap.put("checkCode", order.getCheckCode());
		dataMap.put("contractId", order.getSign());
		dataMap.put("memberId", getMemberId(order));
		dataMap.put("orderId", order.getMerchNo() + orderNo);
		if (CardType.savings.id() == order.getCardType()) {
			dataMap.put("payType", JiupayConst.cardType_DQP);
		} else {
			dataMap.put("payType", JiupayConst.cardType_CQP);
		}
		dataMap.put("amount", ParamUtil.yuanToFen(order.getAmount()));
		dataMap.put("currency", order.getCurrency());
		dataMap.put("orderTime", order.getReqTime());
		dataMap.put("clientIP", order.getReqIp());
		// 订单有效期
		dataMap.put("validUnit", JiupayConst.validUnit_min);
		dataMap.put("validNum", JiupayConst.validNum);
		dataMap.put("offlineNotifyUrl", PayService.commonNotifyUrl(order));
		dataMap.put("goodsName", order.getProduct());
		dataMap.put("goodsDesc", order.getMemo());
		// 二级商户号
		// dataMap.put("subMerchantId", "");
		ParamUtil.trimValue(dataMap);
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			return R.error("签名异常");
		}
		logger.info("请求报文：{}", buf);
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			return R.error("支付请求异常！");
		}
		logger.info("九派快捷支付请求结果：service:{},result:{}", service, res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);

		ParamUtil.trimValue(respMap);

		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派快捷支付异常：service:{},msg:{}", service, respMap.get("rspMessage"));
			return R.error("验签失败！");
		}
		String code = (String) respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派快捷验签通过！");
			logger.error("九派快捷支付异常：service:{},code:{},msg:{}", service, code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		if (ParamUtil.isNotEmpty(order.getCheckCode())) {
			// 更新订单信息
			order.setBusinessNo(respMap.get("payOrderId"));
			// 更新订单状态
			String msg = handOrderState(order, respMap.get("orderSts"));
			// 如果支付成功，则以查询或者回调为主。。。
			if (OrderState.succ.id() == order.getOrderState()) {
				order.setOrderState(OrderState.ing.id());
			}
			return R.ok(msg);
		}
		return R.ok();
	}

	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request) {
		Map<String, String> respMap = RequestUtils.getAllRequestParamStream(request);
		logger.info("九派支付回调结果：{}", respMap);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派支付回调验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("支付回调验签返回失败");
		}
		String code = respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派支付回调验签通过！");
			logger.error("九派支付回调异常：,code:{},msg:{}", code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		String amount = respMap.get("amount");
		if (ParamUtil.isNotEmpty(amount)) {
			order.setRealAmount(ParamUtil.fenToYuan(amount));
		}
		String orderSts = respMap.get("orderSts");
		return R.ok(handOrderState(order, orderSts));

	}

	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		String service = JiupayConst.service_rpmPayQuery;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		dataMap.put("orderId", order.getMerchNo() + order.getOrderNo());
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派支付查询签名异常！{},{},{}", payMerch, order.getMerchNo(), order.getOrderNo());
			return R.error("支付查询签名有误");
		}
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("九派支付查询请求结果为空！{}", payMerch);
			return R.error("支付查询请求结果为空");
		}
		logger.info("九派支付查询请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派支付查询验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("支付查询验签返回失败");
		}
		String code = respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派快捷验签通过！");
			logger.error("九派快捷支付异常：service:{},code:{},msg:{}", service, code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		String amount = respMap.get("amount");
		if (ParamUtil.isNotEmpty(amount)) {
			order.setRealAmount(ParamUtil.fenToYuan(amount));
		}
		String payOrderId = respMap.get("payOrderId");
		if (ParamUtil.isNotEmpty(payOrderId)) {
			order.setBusinessNo(payOrderId);
		}
		String payResult = respMap.get("payResult");
		return R.ok(handOrderState(order, payResult));
	}

	/**
	 * @Description 九派单笔代付发起
	 * @param order
	 * @return
	 */
	public R orderAcp(Order order) {
		String service = JiupayConst.service_capSingleTransfer;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		// 后台异步通知地址
		dataMap.put("callBackUrl", PayService.commonAcpNotifyUrl(order));
		if(order.getReqTime() == null){
			order.setReqTime(requestTime);
		}
		// 商户交易流水
		dataMap.put("mcSequenceNo", order.getMerchNo() + order.getOrderNo());
		dataMap.put("mcTransDateTime", order.getReqTime());
		dataMap.put("orderNo", order.getMerchNo() + order.getOrderNo());
		dataMap.put("amount", ParamUtil.yuanToFen(order.getAmount()));
		//卡号加密
		dataMap.put("cardNo", HiDesUtils.desEnCode(order.getBankNo()));
		dataMap.put("accName", order.getAcctName());
		// 账户性质
		if (String.valueOf(AcctType.pub.id()).equals(String.valueOf(order.getAcctType()))) {
			dataMap.put("accType", JiupayConst.accType_pub);
			// 收款人开户行号
			dataMap.put("lBnkNo", "");
			// 收款人开户行名称
			dataMap.put("lBnkNam", "");
		} else {
			dataMap.put("accType", JiupayConst.accType_card);
		}
		// 卡号类型
		if (String.valueOf(CardType.credit.id()).equals(String.valueOf(order.getCardType()))) {
			dataMap.put("crdType", JiupayConst.cardType_credit);
			dataMap.put("validPeriod", "");
			dataMap.put("cvv2", "");
		} else {
			dataMap.put("crdType", JiupayConst.cardType_savings);
		}
		dataMap.put("cellPhone", order.getMobile());
		dataMap.put("remark", order.getTitle());
		dataMap.put("bnkRsv", order.getMemo());
		// 资金用途
		dataMap.put("capUse", JiupayConst.capUser_default);
		ParamUtil.trimValue(dataMap);
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派单笔代付签名异常！{},{},{}", payMerch, order.getMerchNo(), order.getOrderNo());
			return R.error("单笔代付签名有误");
		}
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("九派单笔代付请求结果为空！{}", payMerch);
			return R.error("单笔代付请求结果为空");
		}
		logger.info("九派单笔代付请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派单笔代付验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("单笔代付验签返回失败");
		}
		String code = respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派单笔代付验签通过！");
			logger.error("九派单笔代付异常：service:{},code:{},msg:{}", service, code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		String amount = respMap.get("amount");
		if (ParamUtil.isNotEmpty(amount)) {
			order.setRealAmount(ParamUtil.fenToYuan(amount));
		}
		String bfbSequenceNo = respMap.get("bfbSequenceNo");
		if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
			order.setBusinessNo(bfbSequenceNo);
		}
		String orderSts = respMap.get("orderSts");
		return R.ok(handOrderAcpState(order, orderSts));
	}

	/**
	 * @Description 九派单笔代付异步通知
	 * @param order
	 * @param request
	 * @return
	 */
	public R notifyAcp(Order order, HttpServletRequest request) {
		Map<String, String> respMap = RequestUtils.getAllRequestParamStream(request);
		logger.info("九派代付通知结果：{}", respMap);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派代付通知验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("代付通知验签返回失败");
		}
		String code = respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派代付通知验签通过！");
			logger.error("九派代付通知异常：,code:{},msg:{}", code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		String amount = respMap.get("amount");
		if (ParamUtil.isNotEmpty(amount)) {
			order.setRealAmount(ParamUtil.fenToYuan(amount));
		}
		String orderSts = respMap.get("orderSts");
		return R.ok(handOrderState(order, orderSts));
	}
	/**
	 * @Description 九派单笔代付查询
	 * @param order
	 * @return
	 */
	public R acpQuery(Order order) {
		String service = JiupayConst.service_capOrderQuery;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		//业务部分
		dataMap.put("mcSequenceNo", order.getMerchNo() + order.getOrderNo());
		dataMap.put("mcTransDateTime", order.getReqTime());
		dataMap.put("orderNo", order.getMerchNo() + order.getOrderNo());
		dataMap.put("amount", ParamUtil.yuanToFen(order.getAmount()));
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派单笔代付查询签名异常！{},{},{}", payMerch, order.getMerchNo(), order.getOrderNo());
			return R.error("单笔代付查询签名有误");
		}
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("九派单笔代付查询请求结果为空！{}", payMerch);
			return R.error("单笔代付查询请求结果为空");
		}
		logger.info("九派单笔代付查询请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派单笔代付查询验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("单笔代付查询验签返回失败");
		}
		String code = respMap.get("rspCode");
		if (!code.equals(JiupayConst.rspCode_succ)) {
			logger.info("九派单笔代付查询验签通过！");
			logger.error("九派单笔代付查询异常：service:{},code:{},msg:{}", service, code, respMap.get("rspMessage"));
			return R.error(respMap.get("rspMessage"));
		}
		String amount = respMap.get("amount");
		if (ParamUtil.isNotEmpty(amount)) {
			order.setRealAmount(ParamUtil.fenToYuan(amount));
		}
		String bfbSequenceNo = respMap.get("bfbSequenceNo");
		if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
			order.setBusinessNo(bfbSequenceNo);
		}
		
		String ordsts = respMap.get("ordsts");
		return R.ok(handOrderAcpState(order, ordsts));
	}
	/**
	 * @Description 更新银行卡列表
	 * @param order
	 * @param bank_savings
	 * @param bank_credits
	 */
	public void refreshBanks(Order order, List<String> bank_savings, List<String> bank_credits) {
		String service = JiupayConst.service_rpmBankList;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			return;
		}
		logger.info("请求报文：{}", buf);
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("请求结果为空！{}", payMerch);
			return;
		}
		logger.info("九派获取银行卡列表请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (verify(respMap, Constant.ec_utf_8) && respMap.get("rspCode").indexOf(JiupayConst.rspCode_five_zero) > 0) {
			// 储蓄卡
			String cardJson;
			JSONObject jo;
			try {
				cardJson = new String(Base64Utils.decode(respMap.get("bankList")));
				logger.info("储蓄卡列表：{}", cardJson);
				JSONArray jsonArray = JSON.parseArray(cardJson);
				for (int i = 0; i < jsonArray.size(); i++) {
					jo = (JSONObject) (jsonArray.get(i));
					bank_savings.add(jo.getString("bankAbbr"));
				}
			} catch (Exception e) {
				logger.error("解析储蓄卡列表失败！");
			}
			// 信用卡
			try {
				cardJson = new String(Base64Utils.decode(respMap.get("creditBankList")));
				logger.info("信用卡列表：{}", cardJson);
				JSONArray jsonArray = JSON.parseArray(cardJson);
				for (int i = 0; i < jsonArray.size(); i++) {
					jo = (JSONObject) (jsonArray.get(i));
					bank_credits.add(jo.getString("bankAbbr"));
				}
			} catch (Exception e) {
				logger.error("解析信用卡列表失败！");
			}
		} else {
			logger.error("九派获取银行卡列表验签失败：{}", respMap.get("rspMessage"));
			return;
		}

	}

	/**
	 * @Description 九派快捷绑卡
	 * @param order
	 * @param userSign
	 */
	public R cardBind(Order order, MerchUserSignDO userSign) {
		String service = JiupayConst.service_rpmBindCardInit;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		// 用户标志
		dataMap.put("memberId", getMemberId(order));
		// 商户订单号
		dataMap.put("orderId", order.getMerchNo() + order.getUserId() + requestTime);
		// 身份证号码
		if (CertType.identity.id() == userSign.getCertType()) {
			dataMap.put("idType", JiupayConst.idType_identity);
		}
		dataMap.put("idNo", HiDesUtils.desEnCode(userSign.getCertNo()));
		dataMap.put("userName", userSign.getAcctName());
		dataMap.put("phone", userSign.getPhone());
		dataMap.put("cardNo", HiDesUtils.desEnCode(userSign.getBankNo()));
		dataMap.put("cardType", String.valueOf(userSign.getCardType()));
		dataMap.put("expireDate", userSign.getValidDate());
		dataMap.put("cvn2", userSign.getCvv2());

		ParamUtil.trimValue(dataMap);
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派快捷绑卡 签名异常！{}", payMerch);
			return R.error("签名有误");
		}
		logger.info("请求报文：{}", buf);
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("请求结果为空！{}", payMerch);
			return R.error("请求结果为空!");
		}
		logger.info("九派快捷绑卡请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派快捷绑卡验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("绑卡验签返回失败"+ respMap.get("rspMessage"));
		}
		if (respMap.get("rspCode").indexOf(JiupayConst.rspCode_five_zero) > 0) {
			// 储蓄卡
			userSign.setSign(respMap.get("contractId"));
			Map<String, Object> data = new HashMap<>();
			data.put("sign", respMap.get("contractId"));
			return R.ok(data);
		} else {
			logger.error("九派快捷绑卡返回失败：{}", respMap.get("rspMessage"));
			return R.error("绑卡返回失败"+ respMap.get("rspMessage"));
		}
	}

	/**
	 * @Description 绑卡确认
	 * @param order
	 * @param userSign
	 * @return
	 */
	public R cardBindConfirm(Order order, MerchUserSignDO userSign) {
		String service = JiupayConst.service_rpmBindCardCommit;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		dataMap.put("contractId", userSign.getSign());
		dataMap.put("checkCode", userSign.getCheckCode());
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派快捷绑卡确认 签名异常！{}", payMerch);
			return R.error("签名有误");
		}
		logger.info("请求报文：{}", buf);
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("九派快捷请求结果为空！{}", payMerch);
			return R.error("请求结果为空!");
		}
		logger.info("九派快捷绑卡确认请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派快捷绑卡确认验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("绑卡验签返回失败");
		}
		if (respMap.get("rspCode").indexOf(JiupayConst.rspCode_five_zero) > 0) {
			// 储蓄卡
			String contractId = respMap.get("contractId");
			if (userSign.getSign().equals(contractId) && JiupayConst.cardSts_effect.equals(respMap.get("cardSts"))) {
				return R.ok("快捷绑卡成功");
			}
			logger.error("九派快捷绑卡返回失败：contractId：{},cardSts:{}", contractId, respMap.get("cardSts"));
			return R.error("绑卡确认返回失败"+ respMap.get("rspMessage"));
		} else {
			logger.error("九派快捷绑卡返回失败：{}", respMap.get("rspMessage"));
			return R.error("绑卡返回失败"+ respMap.get("rspMessage"));
		}
	}

	/**
	 * @Description 短信重发
	 * @param order
	 * @param sendType
	 * @return
	 */
	public R cardMsgResend(Order order, Integer sendType) {
		String service = JiupayConst.service_rpmQuickPaySms;
		String payMerch = order.getPayMerch();
		String requestTime = DateUtil.getCurrentNumStr();
		// -- 初始化请求头参数
		Map<String, Object> dataMap = initReqHeadData(order, service, payMerch, requestTime);
		dataMap.put("version", JiupayConst.sdk_version1);
		dataMap.put("contractId", order.getSign());
		dataMap.put("memberId", getMemberId(order));
		if(CardSendType.pay.id() == sendType){
			dataMap.put("orderId", order.getMerchNo() + order.getOrderNo());
		}
		// 签名数据
		String buf = merchantSign(service, payMerch, dataMap);
		if (buf == null) {
			logger.error("九派短信重发 签名异常！{}", payMerch);
			return R.error("签名有误");
		}
		logger.info("请求报文：{}", buf);
		String res = RequestUtils.doPostStream(RedisUtil.getPayCommonValue(PayProKeyConst.jiupay_requestUrl), buf);
		if (ParamUtil.isEmpty(res)) {
			logger.error("九派快捷短信重发请求结果为空！{}", payMerch);
			return R.error("请求结果为空!");
		}
		logger.info("九派快捷短信重发请求结果：{}", res);
		Map<String, String> respMap = RequestUtils.coverString2Map(res);
		ParamUtil.trimValue(respMap);
		// 验签
		if (!verify(respMap, Constant.ec_utf_8)) {
			logger.error("九派快捷短信重发验签返回失败：{}", respMap.get("rspMessage"));
			return R.error("短信重发验签返回失败" + respMap.get("rspMessage"));
		}
		if (respMap.get("rspCode").indexOf(JiupayConst.rspCode_five_zero) > 0) {
			logger.info("短信重发返回下发手机号：{}", respMap.get("phone"));
			return R.ok("短信重发成功");
		} else {
			logger.error("九派快捷短信重发返回失败：{}", respMap.get("rspMessage"));
			return R.error("短信重发返回失败" + respMap.get("rspMessage"));
		}
	}
	
	/**
	 * @Description 签名验证
	 * @param respMap
	 * @param ecUtf8
	 * @return
	 */
	private boolean verify(Map<String, String> respMap, String encoding) {
		RSASignUtil rsautil = new RSASignUtil(RedisUtil.getPayFilePathValue(PayProKeyConst.jiupay_rootCertPath));
		try {
			return rsautil.verify(RSASignUtil.coverMap2String(respMap), respMap.get(JiupayConst.param_serverSign),
					respMap.get(JiupayConst.param_serverCert), encoding);
		} catch (Exception e) {
			logger.error("验签失败！" + e.getMessage());
		}
		return false;
	}

	/**
	 * @Description 商户签名结果
	 * @param service
	 * @param payMerch
	 * @param dataMap
	 */
	private String merchantSign(String service, String payMerch, Map<String, Object> dataMap) {
		RSASignUtil util = new RSASignUtil(
				RedisUtil.getPayFilePathValue(payMerch + PayProKeyConst.jiupay_merchantCertPath),
				RedisUtil.getPayCommonValue(payMerch + PayProKeyConst.jiupay_merchantCertPass));
		String reqData = RSASignUtil.coverMap2String(dataMap);
		util.setService(service);
		String merchantSign;
		try {
			merchantSign = util.sign(reqData, Constant.ec_utf_8);
		} catch (Exception e) {
			logger.error("签名异常！service:{},payMerch:{}", service, payMerch);
			return null;
		}
		String merchantCert = util.getCertInfo();
		dataMap.put(JiupayConst.param_merchantSign, merchantSign);
		dataMap.put(JiupayConst.param_merchantCert, merchantCert);
		return reqData + "&merchantSign=" + merchantSign + "&merchantCert=" + merchantCert;
	}

	/**
	 * @Description 初始化请求信息
	 * @param order
	 * @param service
	 * @param payMerch
	 * @param requestTime
	 * @return
	 */
	private Map<String, Object> initReqHeadData(Order order, String service, String payMerch, String requestTime) {
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("charset", JiupayConst.sdk_charset);
		dataMap.put("version", JiupayConst.sdk_version);
		dataMap.put("service", service);
		dataMap.put("signType", JiupayConst.sdk_signType_rsa);
		dataMap.put("merchantId", payMerch);
		dataMap.put("requestTime", requestTime);
		dataMap.put("requestId", order.getMerchNo() + order.getUserId() + requestTime);
		return dataMap;
	}




}
