package com.qh.paythird.xinfu;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
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
import com.qh.paythird.xinfu.utils.HttpHelper;
import com.qh.paythird.xinfu.utils.HttpMethodType;
import com.qh.paythird.xinfu.utils.Md5;
import com.qh.paythird.xinfu.utils.XinFuConst;
import com.qh.redis.service.RedisUtil;

/**
 * 芯付支付 服务类
 * @author Swell
 *
 */
@Service
public class XinFuService {

	private static final Logger logger = LoggerFactory.getLogger(XinFuService.class);
	
	/**
	 * @Description 支付发起
	 * @param order
	 * @return
	 */
	public R order(Order order) {
		
		logger.info("芯付支付 开始------------------------------------------------------");
		try {
			
			if (OutChannel.wx.name().equals(order.getOutChannel())) {
				//微信扫码支付
				return order_wx(order);
			} 
			
			if (OutChannel.qq.name().equals(order.getOutChannel())) {
				//QQ扫码支付
				return order_qq(order);
			} 
			
			if (OutChannel.q.name().equals(order.getOutChannel())) {
				//京东快捷支付
				return order_q(order);
			} 
			
			/*if (OutChannel.ali.name().equals(order.getOutChannel())) {
				//支付宝支付
				return order_ali(order);
			} */
			
			logger.error("芯付支付 不支持的支付渠道：{}", order.getOutChannel());
			return R.error("不支持的支付渠道");
		} finally {
			logger.info("芯付支付 结束------------------------------------------------------");
		}
	}
	
	/**
	 * 微信扫码支付
	 * @param order
	 * @return
	 */
	private R order_wx(Order order){
		
		logger.info("芯付支付 微信扫码支付：{");
		try {
			return pay(order, XinFuConst.WAY_WX);
		} finally {
			logger.info("芯付支付 微信扫码支付：}");
		}
	}
	
	/**
	 * QQ扫码支付
	 * @param order
	 * @return
	 */
	private R order_qq(Order order){
		
		logger.info("芯付支付 QQ扫码支付：{");
		try {
			return pay(order, XinFuConst.WAY_QQ);
		} finally {
			logger.info("芯付支付 QQ扫码支付：}");
		}
	}
	
	/**
	 * 京东快捷支付
	 * @param order
	 * @return
	 */
	private R order_q(Order order){
		
		logger.info("芯付支付 京东快捷支付：{");
		try {
			return pay(order, XinFuConst.WAY_JD);
		} finally {
			logger.info("芯付支付 京东快捷支付：}");
		}
	}
	
	/**
	 * 支付
	 * @param order
	 * @return
	 */
	private R pay(Order order, String way){
		
		try {
			String merchantCode = order.getMerchNo();
			JSONObject json = new JSONObject();
			String orderId = merchantCode + order.getOrderNo();
			json.put("orderId", orderId);
			String amount = ParamUtil.yuanToFen(order.getAmount());
			json.put("amount", amount);
//			String returnUrl = PayService.commonReturnUrl(order);
			json.put("returnUrl", "");//暂时没用
			String notifyUrl = PayService.commonNotifyUrl(order);
			json.put("notifyUrl",notifyUrl);
			String payMerch = order.getPayMerch();
			json.put("body", order.getProduct());
			json.put("merchantCode", payMerch);
			json.put("version", RedisUtil.getPayCommonValue(XinFuConst.VERSION));
			logger.info("芯付支付 请求数据："+json.toString());
			JSONObject res = request(RedisUtil.getPayCommonValue(XinFuConst.DOWN_ORDER), json,payMerch);
			logger.info("芯付支付 下单返回结果："+res);
			if("false".equals(res.get("success").toString())){
				return R.error(res.getString("msg"));
			}
			Map<String, String> resultMap = PayService.initRspData(order);
			//支付【获取支付二维码】
			JSONObject queryRes = getCode(res.getString("tranId"),way,payMerch);
			logger.info("芯付支付 获取二维码返回结果：" + queryRes);
			if("false".equals(queryRes.get("success").toString())){
				return R.error(queryRes.getString("msg"));
			}
			order.setBusinessNo(res.getString("tranId"));
			String imgCode = queryRes.getString("codeUrl");
			if(XinFuConst.WAY_JD.equals(way)){
				resultMap.put(PayConstants.web_code_url, imgCode);
			}else
				resultMap.put(PayConstants.web_qrcode_url, imgCode);
			return R.okData(resultMap);
		} catch (Exception e) {
			logger.info("芯付支付 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付异常");
		} finally{
			logger.info("芯付支付 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付回调
	 * @param order
	 * @param request
	 * @return
	 */
	public R notify(Order order, HttpServletRequest request) {
		
		logger.info("芯付支付回调 开始-------------------------------------------------");
		String msg = "";
		try {
			String payMerch = order.getPayMerch();
			String status = request.getParameter("status");
			String tranId = request.getParameter("tranId");
			String orderId = request.getParameter("orderId");
			String amount = request.getParameter("amount");
			String signature = request.getParameter("signature");
			logger.info("芯付支付回调 回调参数：status："+status+",orderId："+orderId+",tranId："+tranId+",amount："+amount+",signature："+signature);
			if(Md5.getMd5ofStr(status+orderId+tranId+amount+RedisUtil.getPayCommonValue(payMerch + XinFuConst.KEY)).equals(signature)){
				order.setRealAmount(ParamUtil.fenToYuan(amount));
				if(XinFuConst.ORDER_STATUS_SUCC.equals(status)){
					order.setOrderState(OrderState.succ.id());
					msg = "订单处理完成";
				}else if(XinFuConst.ORDER_STATUS_ERROR.equals(status)){
					msg = "处理失败";
					order.setOrderState(OrderState.fail.id());
				}else if(XinFuConst.ORDER_STATUS_WAIT.equals(status)){
					msg = "订单处理中";
					order.setOrderState(OrderState.ing.id());
				}else{
					msg = "未知订单状态";
					order.setOrderState(OrderState.ing.id());
				}
				return R.ok(msg);
			}else{
				logger.info("芯付支付回调 验签失败！");
				return R.error("验签失败！");
			}
		} catch (Exception e) {
			logger.info("芯付支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("芯付支付回调 异常：" + e.getMessage());
		} finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
			logger.info("芯付支付回调 结束-------------------------------------------------");
		}
	}
	
	/**
	 * @Description 支付查询
	 * @param order
	 * @return
	 */
	public R query(Order order) {
		logger.info("芯付支付查询 开始-------------------------------------------------");
		String msg = "";
		try {
			String payMerch = order.getPayMerch();
			JSONObject json = new JSONObject();
			json.put("version", RedisUtil.getPayCommonValue(XinFuConst.VERSION));
			json.put("tranId", order.getBusinessNo());
			logger.info("芯付支付 查询 请求数据："+json.toString());
			JSONObject res = request(RedisUtil.getPayCommonValue(XinFuConst.QUERY_STATUS), json,payMerch);
			logger.info("芯付支付查询 返回结果："+res.toString());
			if("false".equals(res.get("success").toString())){
				return R.error(res.getString("msg"));
			}
			String status = res.get("status").toString();
//			Object amount = res.get("amount");
//			if (ParamUtil.isNotEmpty(amount)) {
//				order.setRealAmount(ParamUtil.fenToYuan(amount.toString()));
//			}else{
				order.setRealAmount(order.getAmount());
//			}
			if(XinFuConst.ORDER_STATUS_SUCC.equals(status)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if(XinFuConst.ORDER_STATUS_ERROR.equals(status)){
				msg = "处理失败";
				order.setOrderState(OrderState.fail.id());
			}else if(XinFuConst.ORDER_STATUS_WAIT.equals(status)){
				msg = "订单处理中";
				order.setOrderState(OrderState.ing.id());
			}else{
				msg = "未知订单状态";
				order.setOrderState(OrderState.ing.id());
			}
			return R.ok(msg);
		} catch (Exception e) {
			logger.info("芯付支付查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("支付查询 异常："+e.getMessage());
		}finally{
			logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(),msg);
			logger.info("芯付支付查询 结束-------------------------------------------------");
		}
	}
	
	/**
	 * ---------------------------------------------------------------------------------------------------
	 */
	
	private JSONObject request(String api, JSONObject json,String mid) throws Exception{
        HttpHelper http = new HttpHelper();
        //业务参数加密
        Map<String, Object> map = new HashMap<String, Object>();
        String input = json.toString();
        String encoded = Base64.encodeBase64String(input.getBytes());

        //验签加密
        String newstr = input+RedisUtil.getPayCommonValue(mid + XinFuConst.KEY);
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();
        //appid
        map.put("appid", RedisUtil.getPayCommonValue(mid + XinFuConst.APPID));
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        return http.getJSONFromHttp(api, map, HttpMethodType.POST);
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private  JSONObject getCode(String tranId,String way,String mid) throws Exception{
		
        JSONObject json = new JSONObject();
        json.put("version",RedisUtil.getPayCommonValue(XinFuConst.VERSION));
        json.put("way",way);

        json.put("tranId", tranId);

        String input = json.toString();
        String encoded = Base64.encodeBase64String(input.getBytes());

        //签名参数
        String newstr = input + RedisUtil.getPayCommonValue(mid + XinFuConst.KEY);
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();

        //appid参数
        Map map = new HashMap();
        map.put("appid", RedisUtil.getPayCommonValue(mid + XinFuConst.APPID));
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        String res = new HttpHelper().sendHttp(RedisUtil.getPayCommonValue(XinFuConst.GET_CODE),map, HttpMethodType.GET);
        return new JSONObject(res);
    }
}
