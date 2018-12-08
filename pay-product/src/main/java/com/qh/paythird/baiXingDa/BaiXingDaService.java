package com.qh.paythird.baiXingDa;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.service.PayService;
import com.qh.paythird.baiXingDa.utils.BaixindaConst;
import com.qh.paythird.baiXingDa.utils.Base64;
import com.qh.paythird.baiXingDa.utils.DF0001ReqBean;
import com.qh.paythird.baiXingDa.utils.DF0003ReqBean;
import com.qh.paythird.baiXingDa.utils.DF0004ReqBean;
import com.qh.paythird.baiXingDa.utils.DataReq;
import com.qh.paythird.baiXingDa.utils.HttpClientUtil;
import com.qh.paythird.baiXingDa.utils.PP1017ReqBean;
import com.qh.paythird.baiXingDa.utils.PP1024ReqBean;
import com.qh.paythird.baiXingDa.utils.RSAUtil;
import com.qh.paythird.baiXingDa.utils.SignUtils;
import com.qh.paythird.xinqianbao.utils.MD5Utils;
import com.qh.redis.service.RedisUtil;

/**
 * 百信达
 * @author Swell
 *
 */
@Service
public class BaiXingDaService {

	private static final Logger logger = LoggerFactory.getLogger(BaiXingDaService.class);
	String charset = "UTF-8";
	// 商户私钥
	/*String privateKey = "MzA4MjAyNzYwMjAxMDAzMDBEMDYwOTJBODY0ODg2RjcwRDAxMDEwMTA1MDAwNDgyMDI2MDMwODIwMjVDMDIwMTAwMDI4MTgxMDBBNUUwQjVGOUU0QTk3REU2OEJEQTlFMUJGQjFBMDIwNzEzOURDRTE5RUVEQThFOTUyMzc1QjY4MTU3QjA5NjlDMTI4NEMwQUUzOTQ1QzBBQjMyMDFENUQwNTNGQjgwMkEzMEEzMjk0RjEyMzdBMUEzOUFDNDRGMEUzQTlDNDhCNjRERDNFNEMzQUVGNDY4OEJFMzRGMjk1ODVGNDFDMDcwNTU0MzEzNDc4ODAwNEVCRUZFRENDOENCQkVCREUwQUQ2NkMzNEQzNzYzOTM5OTkzQzgzNkY2NDU5NTgxRkRDMTQ5NEU1M0JCRkY1NUVBMDBDN0JBRDU0QkVDNjY4MjlEMDIwMzAxMDAwMTAyODE4MDJBNjVEMDNEOEEyQkE0NDk3QzJGQTlCRkZGMjM3QkE3MzE2NTYxNUI5MTg4N0Q2RjMzM0MxMDI0RTkzM0YyOUFGQkM0QzBCNzA3NUU4M0NCN0UyMUE3RjNGMkIzNTJFM0Y1QzA4RTdCNkU2RDk3QkMwODdGRUFCMEY5NUMxRURENDY3QkU0QkY5MkI3QzcxNjNBRTJEN0JCNTZEMTU5Qjg2QTkyMzZFNzRDM0RCN0M1QzlGQUQ5ODU1MzY0ODQ0MTAwNzY4RDg4MjI1NTY0RDMzMTZEM0E5Njc5MjQ3REVDMUMyMkY1MUZCNEU5REI3MDE0NjgyMDUzMDE4ODYxMjUwMjQxMDBGQkI3MkVEQkFGRDE4RjlBMDE1RkEyMDQzOTg5M0FBQTFFMzZDRTU0MTQ2MDE2REE2MTM5NzEwMEQyRjY4MDA0QjEzNjE1MjExMEZEN0FGMTJDMTA4QkU3NTIyNDQ0N0M0RjZCRjMwNjRFNEMzODEzNEIzNTNGM0M3QzhFMkIxNzAyNDEwMEE4QjM4MDQ5OTk5M0QxRTY0MEJCQTI2RkY3RkMwODdEOTI3MUE1QjY4ODYxMjU4M0Y4RDFFQUI3QjI1RDNFQUQ3RjNBODI2NTc1NTRDNEJGQjQ0RTM2MUIyNTUyMjc1QjQ5QjQ2MzYzQUIzQ0I0MDVCQTIzNkUwMDlENjk4MDZCMDI0MDI0RjNENjcwMkZENjZFRTM2N0YyMzcyMUIxQTRBMEI1MUFBQzY3MEJENkQ1RTg2NEY0QzJFRjAwRjRGNzc1MDFCQjU2M0EzMUQ5ODFBQ0NCQkNGMTRDRTg4Rjk5N0Q2ODU2NkM3RDg3REU3NEI4MDJCNTE2QzMwQUM5MkE0MUNGMDI0MDExQTJBQTFGODc4OTc3NjBDRDk1OEZENjhBQTJGMzM0NDU1MUQyMTNGMUNGQ0RGRjJDQ0NBQ0VGQzUxQTkwNDlDQ0NBMEUwNTkzMkQ2ODVGRURGNjVCMUI5RDVDMjgzNzE3Q0U1RUIxNzU5RTIzRTc5MTVDRDBDQzA5QTg5NEJGMDI0MTAwQUE5RDQ4QTg5Nzg4Qzk0OTJCQzVERTZBNjBGMTlCMDBDNTRFMzQyQjU5RDEzQjg3NUI0NUE2MTgzRkI2QjUxNEMxQzc2OUY0QjAyOTM2RjAzMTZGREVDNTRCRTYwNjE1REUwQkM5ODVDNTQ1NThGRDRBOUYxMTNFQTQwQTQyQzQ=";
	String proxyMerid = "20001";//渠道商ID
	//渠道商消息秘钥
	String key = "RrJ5TYzf8NjlMbESCdWukOj5zEssXafFa2txRjM4LKe788XkCZxzbOsAeoRy9V6u";
	String reqTestURL = "http://47.93.80.210:8011/api/polypay/core/1.0";
	String reqDfURL = "http://47.93.80.210:8011/api/polypay/batchPay";
	String childNo ="8814114411482";//子商户编码
*/	//String payType = "21";
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("百信达支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝扫码 21
				return order_ali(order);
			} 
			
			if (OutChannel.yl.name().equals(order.getOutChannel())) {
				//银联二维码 51
				return order_ali(order);
			}
			
			if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
				//支付宝wap
				return order_wap(order);
			} 
			
			logger.error("百信达支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("百信达支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 支付宝扫码支付
	 * @param order
	 * @return
	 */
	private R order_ali(Order order){
		    
		logger.info("百信达支付 支付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			DataReq dataReqBean = new DataReq();
			String merchNo = order.getPayMerch();
			//PP1017 收款下单   
	        PP1017ReqBean reqBean = new PP1017ReqBean(BaixindaConst.tradeCode_pay,merchNo,orderId);//交易码  子商户号
	       // reqBean.setAuthCode("134531981059932006");
	       // reqBean.setOpenId("130070946807255368");
	        reqBean.setOrderAmount(order.getAmount().toString());//订单总金额
	       // reqBean.setOrderAmount("0.1");//订单总金额
	        String url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_resURL);
	        if(OutChannel.yl.name().equals(order.getOutChannel())){
	        	reqBean.setPayType("51");
	        	url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_reqYl_url);
	        	reqBean.setRemark("银联扫码");
	        }else{
	        	reqBean.setPayType("21");
	        	reqBean.setRemark("支付宝主扫");
	        }
	        logger.info("百信达支付 请求地址:[" + url + "]");
	       // reqBean.setCardNo("6214850101857412");
	        reqBean.setBackNoticeUrl(PayService.commonNotifyUrl(order));
	        /*List<PP1017ReqBean.ProductInfos.ProductInfo> productInfos = new ArrayList<PP1017ReqBean.ProductInfos.ProductInfo>();
	        PP1017ReqBean.ProductInfos.ProductInfo productInfo = new PP1017ReqBean.ProductInfos.ProductInfo();
	        productInfo.setProductName("测试商品");
	        productInfo.setProductNumbers("1");
	        productInfo.setProductPrice("0.1");
	        productInfos.add(productInfo);
	        PP1017ReqBean.ProductInfos productInfos1 = new PP1017ReqBean.ProductInfos();
	        productInfos1.setProductInfo(productInfos);
	        reqBean.setProductInfos(productInfos1);*/
	        dataReqBean.setData(reqBean);
	        String req = JSON.toJSONString(dataReqBean);
	        logger.info("百信达支付 发送数据为:[" + req + "]");
	        String res = HttpClientUtil.doPost(url, sign(req,RedisUtil.getPayCommonValue(merchNo+BaixindaConst.bxd_channl_no),merchNo), req, charset);
			logger.info("百信达支付 请求返回参数："+res);
			if(StringUtils.isBlank(res)){
				return R.error("支付返回参数为空");
			}
			
			JSONObject jsonObject = JSONObject.parseObject(res);
			JSONObject headjo = (JSONObject) jsonObject.get("head");
	        String resCode = headjo.getString("resCode");
			String resMessage = headjo.getString("resMessage");
			if(!"000000".equals(resCode)){
				return R.error(resMessage);
			}
			Map<String,String> data = PayService.initRspData(order);
			String qrcodeStr = jsonObject.getString("qrCodeStr");
			//String qrCode = qrcodeStr.split("uuid=")[1];
			logger.info("支付宝qrcodeStr为"+qrcodeStr);
			data.put(PayConstants.web_qrcode_url, qrcodeStr);
			return R.okData(data);
		} catch (Exception e) {
			logger.error("百信达支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("百信达支付 支付：}");
		}
	}
	/**
	 * 支付宝扫码支付
	 * @param order
	 * @return
	 */
	private R order_wap(Order order){
		
		logger.info("百信达支付 支付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			DataReq dataReqBean = new DataReq();
			String merchNo = order.getPayMerch();
			//PP1017 收款下单   
			PP1017ReqBean reqBean = new PP1017ReqBean(BaixindaConst.tradeCode_pay,merchNo,orderId);//交易码  子商户号
			reqBean.setOrderAmount(order.getAmount().toString());//订单总金额
			reqBean.setPayType("23");
			reqBean.setRemark("支付宝wap");
			// reqBean.setCardNo("6214850101857412");
			reqBean.setBackNoticeUrl(PayService.commonNotifyUrl(order));
			
			dataReqBean.setData(reqBean);
			String req = JSON.toJSONString(dataReqBean);
			logger.info("百信达支付 发送数据为:[" + req + "]");
			String proxyMerid = RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_channl_no);
			String res = HttpClientUtil.doPost(RedisUtil.getPayCommonValue(BaixindaConst.bxd_resH5_url), sign(req,proxyMerid,merchNo), req, charset);
			logger.info("百信达支付 请求返回参数："+res);
			if(StringUtils.isBlank(res)){
				return R.error("支付返回参数为空");
			}
			
			JSONObject jsonObject = JSONObject.parseObject(res);
			JSONObject headjo = (JSONObject) jsonObject.get("head");
			String resCode = headjo.getString("resCode");
			String resMessage = headjo.getString("resMessage");
			if(!"000000".equals(resCode)){
				return R.error(resMessage);
			}
			// 确认返回数据
			String qrurl = jsonObject.getString("qrCodeStr");
			Map<String, String> resultMap = PayService.initRspData(order);
			try {
				resultMap.put(PayConstants.web_code_url, qrurl);
			} catch (Exception e) {
				logger.error("jump加密异常！！");
				return R.error("加密异常");
			}
			order.setResultMap(resultMap);
			/*Map<String, Object> jumpData = new HashMap<>();
			jumpData.put(PayConstants.web_params, null);
			jumpData.put(PayConstants.web_form_url, 1);
			jumpData.put(PayConstants.web_action, qrurl);
			order.setJumpData(jumpData);*/
			
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.error("百信达支付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally {
			logger.info("百信达支付 支付：}");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request,String requestBody) {
		
		logger.info("百信达支付回调 开始-------------------------------------------------");
		String msg = "";
		try {
//			TreeMap<String, String> params = RequestUtils.getRequestParam(request);
			logger.info("百信达支付 回调 参数："+ requestBody);
			JSONObject json = JSONObject.parseObject(requestBody);
			String orderNo = json.getString("orderNo");
			String orderStatus = json.getString("orderStatus");
			String orderAmount = json.getString("orderAmount");
			String serialNo = json.getString("serialNo");
			String thridOrderNo = json.getString("thridOrderNo");
			String orderTime = json.getString("orderTime");
			String payType = json.getString("payType");
			
			String sign = json.getString("md5Info");
			String payMerch = order.getPayMerch();
			//String key = RedisUtil.getPayCommonValue(payMerch + XinQianBaoConst.KEY);
			
			String stringSignTemp = "orderAmount="+orderAmount
					+"&orderNo="+orderNo
					+"&orderStatus="+orderStatus
					+"&orderTime="+orderTime
					+"&payType="+payType
					+"&serialNo="+serialNo
					+"&thridOrderNo="+thridOrderNo;
			logger.info("百信达支付 回调验签原串参数"+stringSignTemp);
			String vsign = MD5Utils.ecodeByMD5(stringSignTemp+RedisUtil.getPayCommonValue(payMerch+BaixindaConst.bxd_key));
			logger.info("百信达key:"+RedisUtil.getPayCommonValue(payMerch+BaixindaConst.bxd_key));
			logger.info("百信达返回sign：----" + sign);
			logger.info("百信达支付本地验签结果：----" + vsign);
			if(vsign.equalsIgnoreCase(sign)){
				order.setRealAmount(order.getAmount());
				order.setBusinessNo(thridOrderNo);
				/*校验订单金额*/
				if (new BigDecimal(orderAmount).compareTo(order.getAmount()) != 0) {
					msg = "处理失败,订单金额" +order.getAmount()+ "不等:" + orderAmount;
					return R.error(msg);
				}
				
				if("1".equals(orderStatus)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
					return R.ok(msg);
				}else{
					order.setOrderState(OrderState.fail.id());
					msg = "订单处理失败:";
					return R.error(msg);
				}
			}else{
				logger.info("百信达支付 回调 验证签名不通过");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("百信达支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("百信达支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("百信达支付回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		
		logger.info("百信达支付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			DataReq dataReqBean = new DataReq();
			//PP1017 收款下单   
			PP1024ReqBean reqBean = new PP1024ReqBean(BaixindaConst.tradeCode_pay_query,order.getPayMerch(),orderId);//交易码  子商户号
	       // reqBean.setAuthCode("134531981059932006");
	        reqBean.setOrderNo(orderId);
	        
	        dataReqBean.setData(reqBean);
	        String req = JSON.toJSONString(dataReqBean);
	        logger.info("百信达支付 发送数据为:[" + req + "]");
	        String url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_resURL);
	      //  String url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_reqYl_url);
	        if(OutChannel.aliwap.name().equals(order.getOutChannel())){
	        	url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_resH5_url);
	        }else if(OutChannel.yl.name().equals(order.getOutChannel())){
	        	url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_reqYl_url);
	        }
	        String res = HttpClientUtil.doPost(url, sign(req,RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_channl_no),order.getPayMerch()), req, charset);
			logger.info("百信达支付 查询 请求后返回参数：" + res);
			if(StringUtils.isBlank(res)){
				return R.error("查询返回参数为空！");
			}
			JSONObject jsonObject = JSONObject.parseObject(res);
			JSONObject headjo = (JSONObject) jsonObject.get("head");
	        String resCode = headjo.getString("resCode");
			String resMessage = headjo.getString("resMessage");
			if(!"000000".equals(resCode)){
				return R.error(resMessage);
			} else {
				String ordreStatus = jsonObject.getString("orderStatus");
				order.setRealAmount(order.getAmount());
				if("1".equals(ordreStatus)){
					
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else {
					order.setOrderState(OrderState.ing.id());
					msg = "支付中";
				}
				String thridOrderNo = jsonObject.getString("thridOrderNo");
				order.setBusinessNo(thridOrderNo);
				return R.ok(msg);
			}
		} catch (Exception e) {
			logger.info("百信达支付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("百信达支付查询 异常："+e.getMessage());
		} finally {
			logger.info("百信达支付 查询 结束------------------------------------------------------------");
		}
	}
	
	/**
	 * 代付
	 * @param order
	 * @return
	 */
	public R orderAcp(Order order){
		
		logger.info("百信达 代付：{");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			DF0003ReqBean df0003ReqBean = new DF0003ReqBean();
	        DF0001ReqBean.BatchPayHeadReq batchPayHeadReq = new DF0001ReqBean.BatchPayHeadReq();
	        batchPayHeadReq.setTranCode(BaixindaConst.tradeCode_acp);
	        batchPayHeadReq.setChannelVersion("1.0");
	        batchPayHeadReq.setApiVersion("1.0");
	        batchPayHeadReq.setChannelDate(DateUtil.getCurrentDateStr());//"20171014"
	        batchPayHeadReq.setChannelTime(DateUtil.parseTimeSecStr());//115658
	        batchPayHeadReq.setChannelSerial(orderId);//唯一  订单号
	        batchPayHeadReq.setProxyID(RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_channl_no));
	        df0003ReqBean.setHead(batchPayHeadReq);

	        df0003ReqBean.setCardByName(order.getAcctName());
	        df0003ReqBean.setCardByNo(order.getBankNo());
	        String bankCode = order.getBankCode();
	        if(bankCode.equals(BankCode.BCOM.name())) {
	        	bankCode = "COMM";
	        }
	        if(bankCode.equals(BankCode.GDB.name())) {
	        	bankCode = "CGB";
	        }
	        df0003ReqBean.setBankCode(order.getBankCode());//CIB



	        df0003ReqBean.setTradeTime(DateUtil.getCurrentNumStr());
	        //df0003ReqBean.setBankMobile("");
	        df0003ReqBean.setOrderId(orderId.replaceAll("[a-zA-Z]", ""));
	        df0003ReqBean.setAmount(order.getAmount().toString());
	        df0003ReqBean.setAccType(BaixindaConst.accType_yhk);

	        //df0003ReqBean.setBankCity(order.getBankCity());
	        //df0003ReqBean.setBankOpenName(order.getBankName());
	        //df0003ReqBean.setBankProvcince(order.getBankProvince());

	        df0003ReqBean.getHead().setSign(SignUtils.sign(JSONObject.toJSONString(df0003ReqBean), RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_privateKey)));
	        
	        String url = RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_acp_url);
		      //  String url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_reqYl_url);
		        /*if(OutChannel.yl.name().equals(order.getOutChannel())){
		        	url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_acpYl_url);
		        }*/
	        logger.info("百信达代付 请求参数:[" + JSONObject.toJSONString(df0003ReqBean) + "]");
	        String rsp = HttpClientUtil.doPost(url, null, JSONObject.toJSONString(df0003ReqBean), "utf-8");
	        logger.info("百信达代付 响应参数:[" + rsp + "]");
			if(StringUtils.isBlank(rsp)){
				return R.error("百信达代付 返回参数为空！");
			}
			
			JSONObject jsonObject = JSONObject.parseObject(rsp);
			JSONObject headjo = (JSONObject) jsonObject.get("head");
	        String resCode = headjo.getString("resCode");
			String resMessage = headjo.getString("resMessage");
			
			if(!BaixindaConst.tradeState_SUC.equals(resCode)){
				return R.error(resMessage);
			}
			
			order.setOrderState(OrderState.ing.id());
			order.setRealAmount(order.getAmount());
			return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resMessage);
		} catch (Exception e) {
			logger.error("百信达 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("百信达 代付：}");
		}
	}
	
	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R acpQuery(Order order) {
		
		logger.info("百信达 代付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			DF0004ReqBean df0004ReqBean = new DF0004ReqBean();
	        DF0001ReqBean.BatchPayHeadReq batchPayHeadReq = new DF0001ReqBean.BatchPayHeadReq();
	        batchPayHeadReq.setTranCode(BaixindaConst.tradeCode_acp_query);
	        batchPayHeadReq.setChannelVersion("1.0");
	        batchPayHeadReq.setApiVersion("1.0");
	        batchPayHeadReq.setChannelDate(DateUtil.getCurrentDateStr());
	        batchPayHeadReq.setChannelTime(DateUtil.parseTimeSecStr());
	        batchPayHeadReq.setChannelSerial(orderId);
	        batchPayHeadReq.setProxyID(RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_channl_no));
	        df0004ReqBean.setHead(batchPayHeadReq);

	        df0004ReqBean.setOrderId(orderId.replaceAll("[a-zA-Z]", ""));//1509680138551
	        df0004ReqBean.setTradeDate(DateUtil.getCurrentDateStr());


	        df0004ReqBean.getHead().setSign(SignUtils.sign(JSONObject.toJSONString(df0004ReqBean), RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_privateKey)));
	        
	        String url = RedisUtil.getPayCommonValue(order.getPayMerch()+BaixindaConst.bxd_acp_url);
		      //  String url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_reqYl_url);
		        /*if(OutChannel.yl.name().equals(order.getOutChannel())){
		        	url = RedisUtil.getPayCommonValue(BaixindaConst.bxd_acpYl_url);
		        }*/
	        logger.info("百信达代付请求参数:[" + JSONObject.toJSONString(df0004ReqBean) + "]");
	        String rsp = HttpClientUtil.doPost(url, null, JSONObject.toJSONString(df0004ReqBean), "utf-8");
	        logger.info("百信达代付响应参数:[" + rsp + "]");
	        JSONObject jsonObject = JSONObject.parseObject(rsp);
	        JSONObject headjo = (JSONObject) jsonObject.get("head");
	        String resCode = headjo.getString("resCode");
			String resMessage = headjo.getString("resMessage");
	        
	        if(!BaixindaConst.tradeState_SUC.equals(resCode)){
				return R.error(resMessage);
			} else {
				String status = jsonObject.getString("status");
				if("4".equals(status)){ //出账成功
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else if("3".equals(status)){
					msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
				}else{
					msg = "订单处理中";
					order.setOrderState(OrderState.ing.id());
				}
				order.setRealAmount(order.getAmount());
				return R.ok(msg +","+ order.getMerchNo() + "," + order.getOrderNo()+resMessage);
			}
	       
		} catch (Exception e) {
			logger.info("百信达 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询 异常："+e.getMessage());
		} finally {
			logger.info("百信达 代付 查询 结束------------------------------------------------------------");
		}
	}
	
	 /**
     * 签名方法
     *
     * @param reqMsg 请求消息
     * @return
     * @throws UnsupportedEncodingException
     */
    private Map<String, String> sign(String reqMsg,String proxyMerid,String merchNo)
            throws UnsupportedEncodingException {
        Map<String, String> reqHead = new HashMap<String, String>();
        String digestStr = DigestUtils.sha1Hex(reqMsg.getBytes(charset));
        String signValue = RSAUtil.encodeByPrivateKey(digestStr, new String(
                new Base64().decode(RedisUtil.getPayCommonValue(merchNo+BaixindaConst.bxd_privateKey))));
        reqHead.put("mercId", proxyMerid);//渠道商ID
        reqHead.put("sign", signValue);
        logger.info("百信达支付数据签名："+signValue);
        return reqHead;
    }
}
