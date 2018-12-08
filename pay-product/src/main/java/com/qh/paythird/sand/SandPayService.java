package com.qh.paythird.sand;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.JSONUtils;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.PayService;
import com.qh.paythird.sand.bean.req.GatewayOrderPayRequest;
import com.qh.paythird.sand.bean.req.GatewayOrderQueryRequest;
import com.qh.paythird.sand.bean.resp.GatewayOrderPayResponse;
import com.qh.paythird.sand.bean.resp.GatewayOrderQueryResponse;
import com.qh.paythird.sand.utils.*;
import com.qh.paythird.sand.utils.util.CertUtil;
import com.qh.paythird.sand.utils.util.CryptoUtil;
import com.qh.paythird.sand.utils.util.HttpUtil;
import com.qh.paythird.sand.utils.util.SDKUtil;
import com.qh.paythird.sand.utils.util.SandpayConstants;
import com.qh.paythird.ysb.utils.YinShengBaoConst;
import com.qh.redis.service.RedisUtil;


import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 衫德支付接口
 */
@Service
public class SandPayService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 发起支付
     *
     * @param order
     * @return
     */
    public R order(Order order) {

        logger.info("衫德支付 支付：{");
        
        
        String publicKeyPath = SandPayConst.getCert(SandPayConst.CERT);
        String privateKeyPath = SandPayConst.getCert(order.getPayMerch()+SandPayConst.PFX);
        String keyPassword = SandPayConst.getPassword(order.getPayMerch()+SandPayConst.PASSWORD);

        logger.info("加载衫德安全证书...");
        // 加载证书
        try {
            CertUtil.init(publicKeyPath, privateKeyPath, keyPassword,order.getPayMerch());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("加载衫德安全证书失败...");
        }
        
        String merchantCode = order.getMerchNo();
		String orderId = merchantCode + order.getOrderNo();

        // 组后台报文
        SandPayRequestHead head = new SandPayRequestHead();
        GatewayOrderPayRequest.GatewayOrderPayRequestBody body = new GatewayOrderPayRequest.GatewayOrderPayRequestBody();

        GatewayOrderPayRequest gwOrderPayReq = new GatewayOrderPayRequest();
        gwOrderPayReq.setHead(head);
        gwOrderPayReq.setBody(body);

        
        // 支付方式
        if (OutChannel.wy.name().equals(order.getOutChannel())){// 银行网关支付
        	head.setVersion(SandPayConst.getVersion()); // 版本号
        	head.setProductId(SandPayConst.getProductId());// 产品id
        	head.setMethod("sandpay.trade.pay");
        	head.setAccessType("1");
        	head.setMid(order.getPayMerch()); // 商户号
        	head.setChannelType("07");
        	head.setReqTime(SandAgencyPayConst.getCurrentTime());
        	
        	body.setOrderCode(orderId);
        	String money = order.getAmount().multiply(new BigDecimal(100)).toBigInteger().toString();
        	// 将 201 变成 000000000201
        	while (money.length() < 12) {
        		money = "0" + money;
        	}
        	body.setTotalAmount(String.valueOf(money));
        	body.setSubject(order.getTitle());
        	body.setBody(order.getMemo());
        	body.setClientIp(order.getReqIp()); // 请求ip地址
        	body.setNotifyUrl(PayService.commonNotifyUrl(order));// 支付回调地址
        	body.setFrontUrl(PayService.commonReturnUrl(order));// 前台通知地址
        	
        	Map<String, String> payMap  = new HashMap<>();
        	
            payMap.put("payType", "1"); // 1借记卡 2贷记卡 3混合支付
            payMap.put("bankCode", SandpayConstants.bankNumberMap.get(order.getBankCode()));// 银行编码
            body.setPayMode("bank_pc");
            body.setPayExtra(JSON.toJSONString(payMap));
            try{
            	//外网测试
            	logger.info("杉德支付 请求地址"+SandPayConst.getPayUrl());
                GatewayOrderPayResponse gwPayResponse = SandpayClient.execute(gwOrderPayReq, SandPayConst.getPayUrl()+"/order/pay",order.getPayMerch());

                logger.info("衫德支付 应答参数>>：{}"+JSON.toJSONString(gwPayResponse));

                SandPayResponseHead respHead = gwPayResponse.getHead();

                if (SandpayConstants.SUCCESS_RESP_CODE.equals(respHead.getRespCode())) {

                    GatewayOrderPayResponse.GatewayOrderPayResponseBody respBody = gwPayResponse.getBody();

                    // 支付凭证
                    String credential = respBody.getCredential();
                    JSONObject jso = JSON.parseObject(credential);
                    
                    String SubUrl = jso.getString("submitUrl"); 
                    String params = jso.getString("params");
                    Map<String,Object> paramMap = JSONUtils.jsonToMap(params);
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
        			jumpData.put(PayConstants.web_params, paramMap);
        			jumpData.put(PayConstants.web_form_url, 1);
        			jumpData.put(PayConstants.web_action, SubUrl);
        			order.setJumpData(jumpData);
        			
        			return R.okData(resultMap);
                } else {
                    logger.error("衫德支付 支付失败： respCode[{}],respMsg[{}]."+respHead.getRespCode(), respHead.getRespMsg());
                    return R.error(respHead.getRespMsg() + ":" + respHead.getRespCode());
                }
            }catch(Exception e){
            	 e.printStackTrace();
                 logger.error("衫德支付 支付异常>>:" + e.getMessage());
                 return R.error("支付异常");
            }
            
        }else if (OutChannel.q.name().equals(order.getOutChannel())){// 杉德一键快捷
        	//https://cashier.sandpay.com.cn/fastPay/quickPay/index
        	String url = RedisUtil.getPayCommonValue(SandPayConst.sd_quickPay_url);
        	//String url = "https://cashier.sandpay.com.cn/";
            
    		JSONObject head_q = new JSONObject();
            head_q.put("version",SandpayConstants.DEFAULT_VERSION);
            head_q.put("method", "sandPay.fastPay.quickPay.index");
           /* 00000016 一键快捷
            00000017 标准绑卡快捷
            00000018 后台绑卡快捷*/
            head_q.put("productId", "00000016");//一键绑卡快捷
            head_q.put("accessType","1");
            head_q.put("mid", order.getPayMerch());
            head_q.put("channelType", "07");
            head_q.put("reqTime", DateUtil.getCurrentNumStr());
            

            JSONObject body_q = new JSONObject();
            String userId = order.getUserId();
            if(userId.length() > 10)
            	userId = userId.substring(userId.length()-10,userId.length());
            body_q.put("userId",userId);
            //body_q.put("userId","00000001");
            /*0： T1（默认）
            1： T0
            2： D0*/
            body_q.put("clearCycle", "0");
            body_q.put("currencyCode", "156");
            body_q.put("frontUrl", PayService.commonReturnUrl(order));
            body_q.put("notifyUrl", PayService.commonNotifyUrl(order));
            body_q.put("orderCode", orderId);
            body_q.put("orderTime", DateUtil.getCurrentNumStr());
            String money = order.getAmount().multiply(new BigDecimal(100)).toBigInteger().toString();
        	// 将 201 变成 000000000201
        	while (money.length() < 12) {
        		money = "0" + money;
        	}
            body_q.put("totalAmount", money);
            body_q.put("body", order.getProduct());
            body_q.put("subject", order.getTitle());
           // body_q.put("extend", "");

            JSONObject data = new JSONObject();
            data.put("head", head_q);
            data.put("body", body_q);
            logger.info("杉德一键快捷支付 请求参数"+data.toJSONString());
            try {
                // 签名
                String reqSign = URLEncoder.encode(new String(
                        Base64.encodeBase64(CryptoUtil.digitalSign(JSON.toJSONString(data).getBytes("UTF-8"),
                                CertUtil.getPrivateKey(order.getPayMerch()), "SHA1WithRSA"))), "UTF-8");
                Map<String,String> map = new HashMap<String,String>();
                map.put("charset", "UTF-8");
                map.put("signType", "01");
                map.put("data", JSON.toJSONString(data));
                map.put("sign", reqSign);// 签名串
                //map.put("extend", "");
                /*<input type="text" name="charset" id="charset" value="UTF-8"/></br>
                <label>[signType]签名类型:</label>
                <input type="text" name="signType" id="signType" value="01"/></br>*/
                logger.info("杉德一键快捷支付 请求参数"+map);
                
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
    			jumpData.put(PayConstants.web_action, url+"fastPay/quickPay/index");
    			order.setJumpData(jumpData);
    			
    			return R.okData(resultMap);
            }catch (Exception e) {
                e.printStackTrace();
                logger.error("杉德一键快捷 签名异常 : {}" , e.getMessage());
                return R.error("杉德一键快捷 签名异常");
            }
        }else {
            logger.error("衫德支付 不支持的支付方式 : {}" , order.getOutChannel());
            return R.error("不支持的支付方式");
        }
        

//        body.setTxnTimeOut("");// 默认
//        body.setStoreId(SandPayConst.getStoreId());// 商户门店编号
//        body.setTerminalId(SandPayConst.getTerminalId());// 终端编号
//        body.setOperatorId(SandPayConst.getOperatorId()); // 操作员编号
//        body.setBizExtendParams("bizExtendParams"); // 业务扩展参数
//        body.setMerchExtendParams("merchExtendParams");// 商户扩展参数
//        body.setExtend("extend");// 扩展域

        //logger.info("衫德支付 支付参数：{}", JSON.toJSONString(gwOrderPayReq));

        /*try {
            //外网测试
        	logger.info("杉德支付 请求地址",SandPayConst.getPayUrl());
            GatewayOrderPayResponse gwPayResponse = SandpayClient.execute(gwOrderPayReq, SandPayConst.getPayUrl());

            logger.info("衫德支付 应答参数>>：{}", JSON.toJSONString(gwPayResponse));

            SandPayResponseHead respHead = gwPayResponse.getHead();

            if (SandpayConstants.SUCCESS_RESP_CODE.equals(respHead.getRespCode())) {

                GatewayOrderPayResponse.GatewayOrderPayResponseBody respBody = gwPayResponse.getBody();

                // 支付凭证
                String credential = respBody.getCredential();

                return R.ok("订单提交成");
            } else {
                logger.error("衫德支付 支付失败： respCode[{}],respMsg[{}].", respHead.getRespCode(), respHead.getRespMsg());
                return R.error(respHead.getRespMsg() + ":" + respHead.getRespCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("衫德支付 支付异常>>:" + e.getMessage());
            return R.error("支付异常");
        } finally {
            logger.info("衫德支付 支付：}");
        }*/
    }


    /**
     * 支付回调
     *
     * @param order
     * @param request
     * @return
     */
    public R notify(Order order, HttpServletRequest request,String requestBody) {
        //orderCode商户订单号  totalAmount订单金额 credential支付凭证 traceNo交易流水号 buyerPayAmount买家付款金额 discAmount优惠金额 payTime支付时间 clearDate清算日期
    	logger.info("杉德网关 支付 回调 开始-------------------------------------------------");
    	String msg = "";
    	try{
    		String reqbody = URLDecoder.decode(requestBody,"UTF-8");
    		Map<String,String> reqMap = SDKUtil.convertResultStringToMap(reqbody);
    		String data = reqMap.get("data");
    		JSONObject jsono = JSONObject.parseObject(data);
    		JSONObject head = (JSONObject) jsono.get("head");
    		JSONObject body = (JSONObject) jsono.get("body");
    		if(!"000000".equals(head.getString("respCode"))){
    			return R.error("回调状态非正常");
    		}
    	  //Map<String, String> params = RequestUtils.getAllRequestParamStream(request, "UTF-8");
          logger.info("衫德支付 支付回调返回参数：{}", jsono);
          order.setRealAmount(order.getAmount());
          if ("1".equals(body.getString("orderStatus"))) {
              logger.info("衫德支付 支付回调成功：订单支付成功");
              order.setOrderState(OrderState.succ.id());
              msg = "订单支付成功";
              return R.ok(msg);
          }
          order.setOrderState(OrderState.succ.id());
          msg = "订单支付失败";
          return R.ok("订单处理失败");
      }catch(Exception e){
    	  	logger.info("杉德网关 支付回调 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("杉德支付回调 异常：" + e.getMessage());
      }finally{
    	  logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
    	  logger.info("杉德网关 支付 回调 结束-------------------------------------------------");
      }
    	
    }

    /**
     * 查询订单
     *
     * @param order
     * @return
     */
    public R query(Order order) {
        logger.info("衫德支付 订单查询：{");
        
        String publicKeyPath = SandPayConst.getCert(SandPayConst.CERT);
        String privateKeyPath = SandPayConst.getCert(order.getPayMerch()+SandPayConst.PFX);
        String keyPassword = SandPayConst.getPassword(order.getPayMerch()+SandPayConst.PASSWORD);

        logger.info("加载衫德安全证书...");
        // 加载证书
        try {
            CertUtil.init(publicKeyPath, privateKeyPath, keyPassword,order.getPayMerch());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("加载衫德安全证书失败...");
        }
        
        SandPayRequestHead head = new SandPayRequestHead();
        GatewayOrderQueryRequest queryRequest = new GatewayOrderQueryRequest();
        GatewayOrderQueryRequest.GatewayOrderQueryRequestBody body = new GatewayOrderQueryRequest.GatewayOrderQueryRequestBody();
        String merchantCode = order.getMerchNo();
		String orderId = merchantCode + order.getOrderNo();
		
		head.setVersion(SandpayConstants.DEFAULT_VERSION);
		head.setMethod("sandpay.trade.query");
		head.setProductId("00000007");//00000007  网关支付
		head.setAccessType("1");//
		head.setMid(order.getPayMerch());//商户号
		head.setChannelType("07");//07互联网 08移动端
		head.setReqTime(SandAgencyPayConst.getCurrentTime());
		
        body.setOrderCode(orderId);
        body.setExtend("extend");
        queryRequest.setBody(body);
        queryRequest.setHead(head);
        try {

            logger.info("衫德支付 订单请求信息 >> ： {}", JSON.toJSONString(body));

            GatewayOrderQueryResponse response = SandpayClient.execute(queryRequest,SandPayConst.getPayUrl()+"/order/query",order.getPayMerch());

            GatewayOrderQueryResponse.GatewayOrderQueryResponseBody responseBody = response.getBody();
            SandPayResponseHead respHead = response.getHead();
            if(!"000000".equals(respHead.getRespCode())){
            	return R.error("杉德支付查询:"+respHead.getRespMsg());
            }
            
            String respBodyStr = JSON.toJSONString(responseBody);

            logger.info("衫德支付 订单返回信息 >> ： {}", respBodyStr);
            String orderStatus = responseBody.getOrderStatus();
            String msg = "";
            order.setRealAmount(order.getAmount());
            if ("00".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单处理成功}");
                order.setOrderState(OrderState.succ.id());
                msg = "订单处理成功";
            } else if ("01".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单处理中}");
                order.setOrderState(OrderState.ing.id());
                msg = "订单处理中";
            } else if ("02".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单处理失败}");
                order.setOrderState(OrderState.fail.id());
                msg = "订单处理失败";
            } else if ("03".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单已撤销}");
                order.setOrderState(OrderState.fail.id());
                msg = "订单已撤销";
            } else if ("04".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单已退货}");
                order.setOrderState(OrderState.ing.id());
                msg = "订单已退货";
            } else if ("05".equals(orderStatus)) {
                logger.info("衫德支付 订单查询返回结果 ： {订单退款处理中}");
                order.setOrderState(OrderState.ing.id());
                msg = "订单退款处理中";
            }
            
            return R.ok(msg);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("衫德支付 订单查询失败 >> " + e.getMessage());
            return R.error("订单查询失败");
        } finally {
            logger.info("衫德支付 订单查询结束：}");
        }
    }
    
    
    //URL=https://caspay.sandpay.com.cn/agent-main/openapi/
    /**
	 * 代付
	 * @param order
	 * @return
	 */
	public R order_acp(Order order){
		
		logger.info("杉德支付 代付：{");
		try {
			//请求地址
	        String url = RedisUtil.getPayCommonValue(SandPayConst.ACP_URL);//小额代付
	        url += "agentpay";
	        //请求参数
	        String merchantCode = order.getMerchNo();
	        //订单号最大长度30位  需做判断处理
			String orderId = merchantCode + order.getOrderNo();
			if(orderId.length() > 30)
				orderId = orderId.substring(orderId.length()-30, orderId.length());
			
			String merchId = order.getPayMerch();//商户号
			
	        String version = "01";//RedisUtil.getPayCommonValue(SandPayConst.VERSION);
	        JSONObject jsonObject = new JSONObject();
			jsonObject.put("orderCode", orderId);
			
			jsonObject.put("version", version);
			jsonObject.put("productId", "00000004");//00000004 代付对私 00000003代付对公
			jsonObject.put("accAttr","0");//0 对私 1 对公
			jsonObject.put("tranTime", DateUtil.getCurrentNumStr());

//			jsonObject.put("timeOut", "20161024120000");
			String money = order.getAmount().multiply(new BigDecimal(100)).toBigInteger().toString();
        	// 将 201 变成 000000000201
        	while (money.length() < 12) {
        		money = "0" + money;
        	}
			jsonObject.put("tranAmt", money);
			jsonObject.put("currencyCode", "156");
			

			jsonObject.put("accNo", order.getBankNo());
			
			jsonObject.put("accType", "4");
			jsonObject.put("accName", order.getAcctName());//收款人姓名
			
			
			
//			jsonObject.put("provNo", "sh");
//			jsonObject.put("cityNo", "sh");
			
			/*jsonObject.put("bankName","");//收款人姓名
			jsonObject.put("bankType","");//收款人姓名
*/			jsonObject.put("remark", "pay");
			/*jsonObject.put("payMode","1");//收款人姓名
			jsonObject.put("channelType","07");//收款人姓名
			jsonObject.put("phone","");//收款人姓名
		jsonObject.put("noticeUrl","http://61.129.71.103:15678/merPayReturn/");*/	
//			jsonObject.put("reqReserved", "请求方保留测试");
			String jsonstr =  jsonObject.toJSONString();
			logger.info("杉德代付  请求参数--"+jsonstr);
			
			//装载配置
			
			String transCode = "RTPM";
			
			String cerPath = SandPayConst.getCert(SandPayConst.CERT);
			String fixPath = SandPayConst.getCert(merchId+SandPayConst.PFX);
			String pas = SandPayConst.getPassword(merchId+SandPayConst.PASSWORD);
			logger.info("cerPath:"+cerPath+",fixPath:"+fixPath+":"+pas);
					//创建http辅助工具
			HttpUtil httpUtil= new HttpUtil(cerPath,fixPath,pas,merchId);
					//通过辅助工具发送交易请求，并获取响应报文
			String data = "";
			data = httpUtil.post(url, merchId, transCode, jsonstr);
			logger.info("杉德代付  请求参数--"+data);
			if(ParamUtil.isEmpty(data)){
				R.error("查询返回参数为空！");
			}
			
			JSONObject resData = JSONObject.parseObject(data);
			String respCode = resData.getString("respCode");
			String resultFlag = resData.getString("resultFlag");
			String resultMsg = resData.getString("respDesc");
			
			/**
			 * 1.交易成功： respCode=0000 且 resultFlag=0
			 *	2.交易处理中： respCode=0001或respCode=0002
			 *	3.交易失败：非1、非2
			 */
			order.setRealAmount(order.getAmount());
			if("0000".equals(respCode)&&"0".equals(resultFlag)){
				order.setOrderState(OrderState.succ.id());
			}else if("0001".equals(respCode)||"0002".equals(respCode)){
				order.setOrderState(OrderState.ing.id());
			}else{
				order.setOrderState(OrderState.fail.id());
			}
			
			return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resultMsg).put(PayConstants.acp_real_time, YesNoType.yes.id());
		} catch (Exception e) {
			logger.error("杉德支付 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("杉德支付 代付：}");
		}
	}
	

	/**
	 * @Description 代付查询
	 * @param order
	 * @return
	 */
	public R query_acp(Order order) {
		//https://Pay.heepay.com/API/PayTransit/QueryTransfer.aspx
		logger.info("杉德支付 代付 查询 开始------------------------------------------------------------");
		String msg = "";
		try {
			//请求地址
	        String url = RedisUtil.getPayCommonValue(SandPayConst.ACP_URL);//小额代付
	        url += "queryOrder";
	        //请求参数
	        String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			if(orderId.length() > 30)
				orderId = orderId.substring(orderId.length()-30, orderId.length());
			String merchId = order.getPayMerch();//商户号
			
	        String version = RedisUtil.getPayCommonValue(SandPayConst.VERSION);
	        JSONObject jsonObject = new JSONObject();
			jsonObject.put("orderCode", orderId);
			jsonObject.put("version", version);
			jsonObject.put("productId", "00000004");
			jsonObject.put("tranTime",DateUtil.getCurrentNumStr());	
			
			String jsonstr =  jsonObject.toJSONString();
			logger.info("杉德代付  请求参数--"+jsonstr);
			
			String transCode = "ODQU";
					//创建http辅助工具
			
			String cerPath = SandPayConst.getCert(SandPayConst.CERT);
			String fixPath = SandPayConst.getCert(merchId+SandPayConst.PFX);
			String pas = SandPayConst.getPassword(merchId+SandPayConst.PASSWORD);
			HttpUtil httpUtil= new HttpUtil(cerPath,fixPath,pas,merchId);
					//通过辅助工具发送交易请求，并获取响应报文
			String data = "";
			data = httpUtil.post(url, merchId, transCode, jsonstr);
			logger.info("杉德代付  请求参数--"+data);
			if(ParamUtil.isEmpty(data)){
				R.error("查询返回参数为空！");
			}
			
			JSONObject resData = JSONObject.parseObject(data);
			String respCode = resData.getString("respCode");
			String resultMsg = resData.getString("respDesc");
			String resultFlag = resData.getString("resultFlag");
			String origRespCode = resData.getString("origRespCode");
			//String origRespDesc = resData.getString("origRespDesc");
			
			/**
			 * 1.订单查询交易成功： respCode=0000
				2.订单查询交易失败: respCode!=0000
				3.原交易成功:origRespCode=0000且resultFlag=0
				4.原交易处理中： origRespCode=0001或 origRespCode=0002 或resultFlag=2
				5.原交易失败：非3、非4
			 */
			if(!"0000".equals(respCode)){
				return R.error(resultMsg);
			}
			
			order.setRealAmount(order.getAmount());
			if("0000".equals(origRespCode)&&"0".equals(resultFlag)){
				order.setOrderState(OrderState.succ.id());
				msg = "订单处理完成";
			}else if("0001".equals(origRespCode)||"0002".equals(origRespCode)||"2".equals(resultFlag)){
				order.setOrderState(OrderState.ing.id());
				msg = "订单处理中";
			}else{
				order.setOrderState(OrderState.fail.id());
				msg = "订单处理失败";
			}
			
			return R.ok(msg+order.getMerchNo() + "," + order.getOrderNo()+resultMsg);
		} catch (Exception e) {
			logger.info("杉德支付 代付 查询 异常："+e.getMessage());
			e.printStackTrace();
			return R.error("代付查询 异常："+e.getMessage());
		} finally {
			logger.info("杉德支付 代付 查询 结束------------------------------------------------------------");
		}
	}
	
}
