package com.qh.paythird.xiaotian;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.paythird.xiaotian.utils.XiaoTianConst;
import com.qh.paythird.xinqianbao.utils.MD5Utils;
import com.qh.redis.service.RedisUtil;

/**
 * 小天
 * @author Swell
 *
 */
@Service
public class XiaoTianService {

	private static final Logger logger = LoggerFactory.getLogger(XiaoTianService.class);
	
	/**
	 * 小天代付
	 * @param order
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public R orderAcp(Order order) {
		
		logger.info("小天支付 代付 开始--------------------------------------------------------------------");
		try {
			Map<String, String> paramMap = new HashMap<String, String>();
			String payMerch = order.getPayMerch();
			paramMap.put("mer_id", payMerch);
			paramMap.put("timestamp", DateUtil.getCurrentStr());
			paramMap.put("terminal", "PC");
			String version = RedisUtil.getPayCommonValue(XiaoTianConst.VERSION);
			paramMap.put("version", version);
			String amount = ParamUtil.yuanToFen(order.getAmount());
			paramMap.put("amount", amount);
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			paramMap.put("businessnumber", orderId);
			paramMap.put("bankcardnumber", order.getBankNo());
			paramMap.put("bankcardname", order.getAcctName());
			paramMap.put("bankname", order.getBankName());
			/**参与验签的字段*/
			String sign = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付 参与签名的参数为："+sign);
			sign = MD5Utils.getKeyedDigest(sign, RedisUtil.getPayCommonValue(payMerch + XiaoTianConst.KEY));
			paramMap.put("sign", sign);
			paramMap.put("sign_type", "md5");
			
			String baowen = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付  上送的报文为："+baowen);
			String sr = RequestUtils.sendPost(RedisUtil.getPayCommonValue(XiaoTianConst.ACP_REQ_URL), baowen);
			logger.info("小天支付 代付 请求返回参数："+sr);
			if(StringUtils.isBlank(sr)){
				return R.error("支付返回参数为空");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String result = jsonObject.getString("result");
			if(!XiaoTianConst.RESULT_SUCCESS.equals(result)){
				return R.error( jsonObject.getString("code")+":"+jsonObject.getString("msg"));
			}
			Map<String, String> dataMap = jsonObject.getObject("data", Map.class);
			String orgSign = dataMap.remove("sign");
			dataMap.remove("sign_type");
			String newSign = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付 返回参数 参与验签的参数为："+newSign);
			newSign = MD5Utils.getKeyedDigest(sign, RedisUtil.getPayCommonValue(payMerch + XiaoTianConst.KEY));
			logger.info("小天支付 代付 返回参数 原签名："+orgSign+"；现签名："+newSign);
			if(!newSign.equals(orgSign)){
				return R.error( "验签失败");
			}
			String status = dataMap.get("status");
			if(XiaoTianConst.STATUS_SUCCESS.equals(status)){
				order.setOrderState(OrderState.succ.id());
			}else if(XiaoTianConst.STATUS_ING.equals(status)){
				order.setOrderState(OrderState.ing.id());
			}else if(XiaoTianConst.STATUS_FAIL.equals(status)){
				order.setOrderState(OrderState.fail.id());
			}else{
				return R.error( "状态未知！");
			}
			amount = dataMap.get("amount");
			order.setRealAmount(ParamUtil.fenToYuan(amount));
			String bfbSequenceNo = dataMap.get("businessnumber");
			if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
				order.setBusinessNo(bfbSequenceNo);
			}
			return R.ok(status +","+ order.getMerchNo() + "," + order.getOrderNo());
		} catch (Exception e) {
			logger.error("小天支付 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("{},{}", order.getMerchNo(), order.getOrderNo());
			logger.info("小天支付 代付 结束--------------------------------------------------------------------");
		}
	}
	
	/**
	 * @Description 小天代付订单查询
	 * @param order
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public R acpQuery(Order order) {
		
		logger.info("小天支付 代付 查询 开始--------------------------------------------------------------------");
		try {
			Map<String, String> paramMap = new HashMap<String, String>();
			String payMerch = order.getPayMerch();
			paramMap.put("mer_id", payMerch);
			paramMap.put("timestamp", DateUtil.getCurrentStr());
			paramMap.put("terminal", "PC");
			String version = RedisUtil.getPayCommonValue(XiaoTianConst.VERSION);
			paramMap.put("version", version);
			String amount = ParamUtil.yuanToFen(order.getAmount());
			paramMap.put("amount", amount);
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			paramMap.put("businessnumber", orderId);
			/**参与验签的字段*/
			String sign = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付 查询 参与签名的参数为："+sign);
			sign = MD5Utils.getKeyedDigest(sign, RedisUtil.getPayCommonValue(payMerch + XiaoTianConst.KEY));
			paramMap.put("sign", sign);
			paramMap.put("sign_type", "md5");
			
			String baowen = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付 查询 上送的报文为："+baowen);
			String sr = RequestUtils.sendPost(RedisUtil.getPayCommonValue(XiaoTianConst.QUERY_ACP_REQ_URL), baowen);
			logger.info("小天支付 代付 查询 请求返回参数："+sr);
			if(StringUtils.isBlank(sr)){
				return R.error("查询返回参数为空");
			}
			JSONObject jsonObject = JSONObject.parseObject(sr);
			String result = jsonObject.getString("result");
			if(!XiaoTianConst.RESULT_SUCCESS.equals(result)){
				return R.error( jsonObject.getString("code")+":"+jsonObject.getString("msg"));
			}
			Map<String, String> dataMap = jsonObject.getObject("data", Map.class);
			String orgSign = dataMap.remove("sign");
			dataMap.remove("sign_type");
			String newSign = MD5Utils.getSignParam(paramMap);
			logger.info("小天支付 代付 查询 返回参数 参与验签的参数为："+newSign);
			newSign = MD5Utils.getKeyedDigest(sign, RedisUtil.getPayCommonValue(payMerch + XiaoTianConst.KEY));
			logger.info("小天支付 代付 查询 返回参数 原签名："+orgSign+"；现签名："+newSign);
			if(!newSign.equals(orgSign)){
				return R.error( "验签失败");
			}
			String status = dataMap.get("status");
			if(XiaoTianConst.STATUS_SUCCESS.equals(status)){
				order.setOrderState(OrderState.succ.id());
			}else if(XiaoTianConst.STATUS_ING.equals(status)){
				order.setOrderState(OrderState.ing.id());
			}else if(XiaoTianConst.STATUS_FAIL.equals(status)){
				order.setOrderState(OrderState.fail.id());
			}else{
				return R.error( "状态未知！");
			}
			/*amount = dataMap.get("amount");
			order.setRealAmount(ParamUtil.fenToYuan(amount));*/
			String bfbSequenceNo = dataMap.get("businessnumber");
			if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
				order.setBusinessNo(bfbSequenceNo);
			}
			return R.ok(status +","+ order.getMerchNo() + "," + order.getOrderNo());
		} catch (Exception e) {
			logger.error("小天支付 代付  查询 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付查询异常");
		} finally {
			logger.info("{},{}", order.getMerchNo(), order.getOrderNo());
			logger.info("小天支付 代付 查询 结束--------------------------------------------------------------------");
		}
	}
}
