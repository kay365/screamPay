package com.qh.paythird.mobao;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
//import com.kspay.cert.CertVerify;
//import com.kspay.cert.LoadKeyFromPKCS12;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.pay.service.PayService;
import com.qh.paythird.PayBaseService;
import com.qh.paythird.mobao.utils.HttpUtil;
import com.qh.paythird.mobao.utils.MD5;
import com.qh.paythird.mobao.utils.MoBaoConst;
import com.qh.paythird.xinfu.utils.Md5;
import com.qh.paythird.xinfu.utils.XinFuConst;
import com.qh.redis.service.RedisUtil;

/**
 * 摩宝支付
 * @author Swell
 *
 */
@Service
public class MoBaoService{

	private static final Logger logger = LoggerFactory.getLogger(MoBaoService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info(" 摩宝支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//微信扫码支付
				return order_q(order);
			} 
			
			logger.error(" 摩宝支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info(" 摩宝支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 快捷支付支付
	 * @param order
	 * @return
	 */
	private R order_q(Order order){
		
		logger.info("摩宝支付 快捷支付 开始-----------------------");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> params=new LinkedHashMap<String,String>();
			params.put("versionId",RedisUtil.getPayCommonValue(MoBaoConst.VERSION));
	        params.put("businessType","1100");
	        params.put("insCode","");
	        String payMerch = order.getPayMerch();
	        params.put("merId",payMerch);
	        params.put("orderId",orderId);
	        params.put("transDate",DateUtil.getCurrentNumStr());
	        String amount = ParamUtil.yuanToFen(order.getAmount());
	        params.put("transAmount",ParamUtil.fenToYuan(amount).toString());
	        params.put("transCurrency","156");
	        params.put("transChanlName","UNIONPAY");  
	        params.put("openBankName","");
	        String notifyUrl = PayService.commonNotifyUrl(order);
	        params.put("pageNotifyUrl",notifyUrl);
	        String returnUrl = PayService.commonReturnUrl(order);
	        params.put("backNotifyUrl",returnUrl); 
	        params.put("orderDesc","buy");
	        params.put("dev","");
	        String signParams = ParamUtil.buildAllParams(params, false);
	        String key = RedisUtil.getPayCommonValue(payMerch+MoBaoConst.KEY);
	        String sign = MD5.md5(signParams+key);
	        params.put("signData",sign);
	        logger.info("摩宝支付 快捷支付 请求数据："+signParams+"&signData="+sign);
	        Map<String, String> resultMap = PayService.initRspData(order);
	        resultMap.put(PayConstants.web_code_url, PayService.commonJumpUrl(order));
	        order.setResultMap(resultMap);
	        Map<String, Object> jumpData = new HashMap<>();
			jumpData.put(PayConstants.web_params, params);
			jumpData.put(PayConstants.web_form_url, 1);
			jumpData.put(PayConstants.web_action, RedisUtil.getPayCommonValue(MoBaoConst.REQ));
			order.setJumpData(jumpData);
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.info("摩宝支付 快捷支付 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("摩宝支付 快捷支付 结束-----------------------");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request) {
		
		logger.info("摩宝支付 快捷支付 回调 开始-------------------------------------------------");
		String msg = "";
		try {
			TreeMap<String, String> map = RequestUtils.getRequestParam(request);
			logger.info("摩宝支付 快捷支付 回调参数：" + JSONObject.toJSONString(map));
			Map<String,String> param=new LinkedHashMap<String,String>();
			param.put("versionId",RedisUtil.getPayCommonValue(MoBaoConst.VERSION));
			param.put("businessType","1100");
			param.put("insCode",""); 
			param.put("merId",map.get("merId"));
			param.put("transDate",map.get("transDate"));
			param.put("transAmount",map.get("transAmount"));
			param.put("transCurrency",map.get("transCurrency"));
			param.put("transChanlName",map.get("transChanlName"));
			param.put("openBankName",map.get("openBankName"));
			param.put("orderId",map.get("orderId"));
			param.put("payStatus",map.get("payStatus"));
			param.put("payMsg",map.get("payMsg"));
			param.put("pageNotifyUrl",map.get("pageNotifyUrl"));
			param.put("backNotifyUrl",map.get("backNotifyUrl"));
			param.put("orderDesc",map.get("orderDesc"));
			param.put("dev",map.get("dev"));
			String signParams = ParamUtil.buildAllParams(param, false);
			logger.info("摩宝支付 快捷支付 回调 参与签名参数：" + signParams);
			String payMerch = order.getPayMerch();
	        String key = RedisUtil.getPayCommonValue(payMerch+MoBaoConst.KEY);
	        String sign = MD5.md5(signParams+key);
	        if(sign.equals(map.get("signtrue"))){
	        	String status = map.get("payStatus");
	        	BigDecimal realAmount = ParamUtil.fenToYuan(ParamUtil.yuanToFen(new BigDecimal(map.get("transAmount"))));
				order.setRealAmount(realAmount);
				if(MoBaoConst.ORDER_STATUS_SUCC.equals(status)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else if(MoBaoConst.ORDER_STATUS_FAIL.equals(status)){
					msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
				}else if(MoBaoConst.ORDER_STATUS_ING.equals(status)){
					msg = "订单处理中";
					order.setOrderState(OrderState.ing.id());
				}else{
					msg = "未知订单状态";
					order.setOrderState(OrderState.ing.id());
				}
				return R.ok(msg);
			}else{
				logger.info("摩宝支付 快捷支付 回调 验签失败！");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("摩宝支付 快捷支付 回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("摩宝支付 快捷支付 回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("摩宝支付 快捷支付 回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		logger.info("摩宝支付 快捷支付 查询 开始-------------------------------------------------");
		String msg = "";
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> params=new LinkedHashMap<String,String>();
			params.put("versionId",RedisUtil.getPayCommonValue(MoBaoConst.VERSION));
	        params.put("businessType","1300");
	        params.put("insCode","");
	        String payMerch = order.getPayMerch();
	        params.put("merId",payMerch);
	        params.put("transDate",DateUtil.getCurrentNumStr());
	        params.put("orderId",orderId);
	        String signParams = ParamUtil.buildAllParams(params, false);
	        String key = RedisUtil.getPayCommonValue(payMerch+MoBaoConst.KEY);
	        String sign = MD5.md5(signParams+key);
	        params.put("signType","MD5");
	        params.put("signData",sign);
	        logger.info("摩宝支付 快捷支付 查询 请求数据："+signParams+"&signData="+sign);
	        String content = HttpUtil.POSTReturnString(RedisUtil.getPayCommonValue(MoBaoConst.QUERY_URL),params,"GBK");
	        JSONObject jsonObject = JSONObject.parseObject(content);
	        if(jsonObject.containsKey("refCode")){
	        	return R.error(jsonObject.getString("refMsg")+"("+jsonObject.getString("refCode")+")");
	        }
	        if(!jsonObject.containsKey("payStatus")){
	        	return R.error("第三方未返回查询信息");
	        }
	        
	        String payStatus = jsonObject.getString("payStatus");
	        order.setRealAmount(order.getAmount());
	        if (MoBaoConst.ORDER_STATUS_SUCC.equals(payStatus)){
	        	order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
	        } else if (MoBaoConst.ORDER_STATUS_FAIL.equals(payStatus)){
	        	msg = "处理失败";
				order.setOrderState(OrderState.fail.id());
	        } else if (MoBaoConst.ORDER_STATUS_ING.equals(payStatus)){
	        	msg = "订单处理中";
				order.setOrderState(OrderState.ing.id());
	        }else{
				msg = "未知订单状态";
				order.setOrderState(OrderState.ing.id());
			}
			return R.ok(msg);
		} catch (Exception e) {
			logger.info("摩宝支付 快捷支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		}finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(),msg);
			logger.info("摩宝支付 快捷支付 查询 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 代付
	 * @param order
	 * @return
	 */
	public R orderAcp(Order order) {
		
		logger.info("摩宝支付 代付 开始--------------------------------------------------------------------");
		try {
			JSONObject obj = new JSONObject();           
			StringBuffer str = new StringBuffer();
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			JSONObject transData = new JSONObject(); 
			//交易
			transData.put("accName", order.getAcctName()); // 收款人姓名
			transData.put("accNo", order.getBankNo()); // 收款人账号  
			transData.put("orderId", orderId); // 订单号  
			transData.put("transAmount", order.getAmount().toString()); // 交易金额
			transData.put("transDate", DateUtil.getCurrentNumStr()); // 交易日期
			logger.info("摩宝支付 代付 transBody信息："+transData.toString());
			String pfxFileName = "私钥证书路径";
			String pfxPassword = "证书密码";
			PrivateKey privateKey = null;
//			privateKey = LoadKeyFromPKCS12.initPrivateKey(pfxFileName, pfxPassword);
			String transBody = "xxx";
//			String  transBody=LoadKeyFromPKCS12.PrivateSign(transData.toString(),privateKey);
     
			obj.put("transBody", transBody);
			obj.put("businessType", "470000"); // 业务类型
			String payMerch = order.getPayMerch();
			obj.put("merId", payMerch); // 商户号
			String versionId = RedisUtil.getPayCommonValue(MoBaoConst.VERSION);
			obj.put("versionId",versionId ); // 版本号 
			str.append("businessType" + "=470000").append("&merId" + "=" + payMerch)
			.append("&transBody" + "=" + transBody).append("&versionId" + "=" + versionId);
			String key = RedisUtil.getPayCommonValue(payMerch+MoBaoConst.KEY);
			String signData = MD5.md5(str.toString()+"&key="+key);
			
			obj.put("signData", signData); // 交易日期
			obj.put("signType", "MD5"); // 版本号       
			
			logger.info("摩宝支付 代付 请求数据："+obj.toString());
			
			String content = HttpUtil.POSTAcp(RedisUtil.getPayCommonValue(MoBaoConst.ACP_REQ_URL), obj.toString());
			logger.info("摩宝支付 代付 返回数据："+content);
			JSONObject contentJson = JSONObject.parseObject(content);
			String status = contentJson.getString("status");
			if (MoBaoConst.ORDER_ACP_STATUS_SUCC.equals(status)){
				if(contentJson.containsKey("resBody")){
					String resBody = contentJson.getString("resBody");
					//公钥证书解密
					String cerFileName = "公钥路径";
					PublicKey publicKey = null;		
//					byte[]signByte=LoadKeyFromPKCS12.encryptBASE64(resBody);
//					publicKey = CertVerify.initPublicKey(cerFileName);
//					byte[] str1=CertVerify.publicKeyDecrypt(signByte,publicKey);
					byte[] str1= "aa".getBytes();

					String string = new  String(str1);
					logger.info("摩宝支付 代付 resBody信息："+string);
					JSONObject  resBodyObject = JSONObject.parseObject(string);
					String refCode = resBodyObject.getString("refCode");
					if(MoBaoConst.ORDER_ACP_STATUS_SUCC.equals(refCode)){
						order.setRealAmount(order.getAmount());
						/*String bfbSequenceNo = resJo.getString("id");
						if (ParamUtil.isNotEmpty(bfbSequenceNo)) {
							order.setBusinessNo(bfbSequenceNo);
						}*/
						order.setOrderState(OrderState.succ.id());
						return R.ok(refCode +","+ order.getMerchNo() + "," + order.getOrderNo());
					}else{
						return R.error(resBodyObject.getString("refCode")+":"+resBodyObject.getString("refMsg"));
					}
				}else{
					return R.error("返回数据不存在resBody");
				}
			}else{
				return R.error(MoBaoConst.ORDER_ACP_STATUS_FAIL.equals(status)?"失败":(MoBaoConst.ORDER_ACP_STATUS_ERROR.equals(status)?"系统错误":"未知"));
			}
		} catch (Exception e) {
			logger.info("摩宝支付 代付 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付异常："+e.getMessage());
		}finally{
			logger.info("{},{}", order.getMerchNo(), order.getOrderNo());
			logger.info("摩宝支付 代付 结束-------------------------------------------------");
		}
	}
	
	public R acpQuery(Order order) {
		
		logger.info("摩宝支付 代付 查询 开始--------------------------------------------------------------------");
		try {
			JSONObject obj = new JSONObject();           
			StringBuffer str = new StringBuffer();
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			JSONObject transData = new JSONObject(); 
			//交易
			transData.put("orderId", orderId); // 订单号  
			transData.put("transDate", DateUtil.getCurrentNumStr()); // 交易日期
			logger.info("摩宝支付 代付 查询 transBody信息："+transData.toString());
			String pfxFileName = "私钥证书路径";
			String pfxPassword = "证书密码";
			PrivateKey privateKey = null;
//			privateKey = LoadKeyFromPKCS12.initPrivateKey(pfxFileName, pfxPassword);
//			String  transBody=LoadKeyFromPKCS12.PrivateSign(transData.toString(),privateKey);
     		String transBody = "xx";

			obj.put("transBody", transBody);
			obj.put("businessType", "460000"); // 业务类型
			String payMerch = order.getPayMerch();
			obj.put("merId", payMerch); // 商户号
			String versionId = RedisUtil.getPayCommonValue(MoBaoConst.VERSION);
			obj.put("versionId",versionId ); // 版本号 
			str.append("businessType" + "=460000").append("&merId" + "=" + payMerch)
			.append("&transBody" + "=" + transBody).append("&versionId" + "=" + versionId);
			String key = RedisUtil.getPayCommonValue(payMerch+MoBaoConst.KEY);
			String signData = MD5.md5(str.toString()+"&key="+key);
			
			obj.put("signData", signData); // 交易日期
			obj.put("signType", "MD5"); // 版本号       
			
			logger.info("摩宝支付 代付 查询 请求数据："+obj.toString());
			
			String content = HttpUtil.POSTAcp(RedisUtil.getPayCommonValue(MoBaoConst.ACP_QUERY_URL), obj.toString());
			logger.info("摩宝支付 代付 查询 返回数据："+content);
			JSONObject contentJson = JSONObject.parseObject(content);
			String status = contentJson.getString("status");
			if (MoBaoConst.ORDER_ACP_STATUS_SUCC.equals(status)){
				if(contentJson.containsKey("resBody")){
					String resBody = contentJson.getString("resBody");
					//公钥证书解密
					String cerFileName = "公钥路径";
					PublicKey publicKey = null;		
//					byte[]signByte=LoadKeyFromPKCS12.encryptBASE64(resBody);
//					publicKey = CertVerify.initPublicKey(cerFileName);
//					byte[] str1=CertVerify.publicKeyDecrypt(signByte,publicKey);

					byte[] str1 = "xx".getBytes();
			
					String string = new  String(str1);
					logger.info("摩宝支付 代付 查询 resBody信息："+string);
					JSONObject  resBodyObject = JSONObject.parseObject(string);
					String refCode = resBodyObject.getString("refCode");
					if(MoBaoConst.ORDER_ACP_QUERY_STATUS_SUCC.equals(refCode)){
						order.setOrderState(OrderState.succ.id());
					}else if(MoBaoConst.ORDER_ACP_QUERY_STATUS_FAIL.equals(refCode)){
						order.setOrderState(OrderState.fail.id());
					}else if(MoBaoConst.ORDER_ACP_QUERY_STATUS_UNKNOWN.equals(refCode)){
						order.setOrderState(OrderState.ing.id());
					}else if(MoBaoConst.ORDER_ACP_QUERY_STATUS_NO.equals(refCode)){
						order.setOrderState(OrderState.fail.id());
					}else{
						return R.error( "状态未知！");
					}
					return R.ok(refCode+","+ resBodyObject.getString("refMsg")+","+ order.getMerchNo() + "," + order.getOrderNo());
				}else{
					return R.error("返回数据不存在resBody");
				}
			}else{
				return R.error(MoBaoConst.ORDER_ACP_STATUS_FAIL.equals(status)?"失败":(MoBaoConst.ORDER_ACP_STATUS_ERROR.equals(status)?"系统错误":"未知"));
			}
		} catch (Exception e) {
			logger.info("摩宝支付 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询异常："+e.getMessage());
		}finally{
			logger.info("{},{}", order.getMerchNo(), order.getOrderNo());
			logger.info("摩宝支付 代付 查询 结束-------------------------------------------------");
		}
	}
}
