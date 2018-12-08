package com.qh.paythird.hx;

import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.hx.utils.AcpUtils;
import com.qh.paythird.hx.utils.Verify;
import com.qh.paythird.hx.utils.XmlUtils;
import com.qh.paythird.hx.utils.XunHuanConst;
import com.qh.paythird.hx.wsclient.OrderQueryService_WSOrderQuerySoap_Client;
import com.qh.paythird.hx.wsclient.ScanService_WSScanSoap_Client;
import com.qh.paythird.hx.wsclient.TradeQueryService;
import com.qh.paythird.ysb.utils.MD5Utils;
import com.qh.paythird.ysb.utils.YinShengBaoConst;
import com.qh.redis.service.RedisUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XunHuanService {

    private static final Logger logger = LoggerFactory.getLogger(XunHuanService.class);

    /**
     * @param order
     * @return
     * @Description 支付发起
     */
    public R order(Order order) {

        logger.info("环迅支付 开始------------------------------------------------------");
        try {
            if (OutChannel.wy.name().equals(order.getOutChannel())) {
                //网银
                return order_wy(order);
            }
            
            if (OutChannel.q.name().equals(order.getOutChannel())) {
                //H5网关
                return order_wy(order);
            }
            
            if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝
                return order_wy(order);
            }
            
            if (OutChannel.wx.name().equals(order.getOutChannel())) {
                //微信
                return order_wy(order);
            }
            
            if (OutChannel.acp.name().equals(order.getOutChannel())) {
                //微信
                return order_acp(order);
            }

            logger.error("环迅支付 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("环迅支付 结束------------------------------------------------------");
        }
    }
    
    /**
     * 环迅支付查询
     */
    public R query(Order order) {
    	logger.info("环迅支付 查询 开始------------------------------------------------------");
    	try {
            if (OutChannel.wy.name().equals(order.getOutChannel())) {
                //网银
                return query_wy(order);
            }
            
            if (OutChannel.q.name().equals(order.getOutChannel())) {
                //H5网关
                return query_wy(order);
            }
            
            if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝
                return query_wy(order);
            }
            
            if (OutChannel.wx.name().equals(order.getOutChannel())) {
                //微信
                return query_wy(order);
            }
            
            if (OutChannel.acp.name().equals(order.getOutChannel())) {
                //代付
                return query_acp(order);
            }

            logger.error("环迅支付 查询 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("环迅支付 查询 结束------------------------------------------------------");
        }
    }

    /**
     * @param order
     * @return 网银支付
     */
    private R order_wy(Order order) {
    	String outChannel = order.getOutChannel();
        try {
            logger.info("环迅"+outChannel+" 支付 开始---------------------------------------------");
            Calendar cal = Calendar.getInstance();
            String merchantcode = order.getMerchNo();
            String orderId = merchantcode + order.getOrderNo();
            String url = "";
            //body部分
            SortedMap<String, String> bodyparams = new TreeMap<>();
            bodyparams.put("MerBillNo", orderId);
            bodyparams.put("Date", new SimpleDateFormat("yyyyMMdd").format(cal.getTime()));
            bodyparams.put("CurrencyType", "156");
            bodyparams.put("Amount", String.valueOf(order.getAmount()));
            bodyparams.put("RetEncodeType", "17");
            bodyparams.put("ServerUrl", PayService.commonNotifyUrl(order));
            bodyparams.put("GoodsName", order.getProduct());
            if (OutChannel.wy.name().equals(order.getOutChannel())) {
            	bodyparams.put("Merchanturl", PayService.commonReturnUrl(order));
            	bodyparams.put("GatewayType", "01");
            	bodyparams.put("RetType", "1");
            	bodyparams.put("OrderEncodeType", "5");
            	url = RedisUtil.getPayCommonValue(XunHuanConst.REQ_WY_URL);
            }else if (OutChannel.q.name().equals(order.getOutChannel())) {
            	bodyparams.put("Merchanturl", PayService.commonReturnUrl(order));
            	bodyparams.put("GatewayType", "01");
            	bodyparams.put("RetType", "1");
            	bodyparams.put("OrderEncodeType", "5");
            	url = RedisUtil.getPayCommonValue(XunHuanConst.REQ_WY_H5_URL);
            }else if (OutChannel.ali.name().equals(order.getOutChannel())) {
            	bodyparams.put("GatewayType", "11");
            }else if (OutChannel.wx.name().equals(order.getOutChannel())) {
            	bodyparams.put("GatewayType", "10");
            }
            String bodyXml = XmlUtils.parseXML(bodyparams);
            bodyXml = bodyXml.replaceAll("xml", "body").replaceAll("\n", "");
            //head部分
            String payMerch =  order.getPayMerch();
            SortedMap<String, String> headparams = new TreeMap<>();
            headparams.put("Version", RedisUtil.getPayCommonValue(XunHuanConst.VERSION_WY));
            headparams.put("MerCode", payMerch);
            headparams.put("Account", RedisUtil.getPayCommonValue(payMerch + XunHuanConst.Acc));
            headparams.put("ReqDate", DateUtil.getCurrentNumStr());
            headparams.put("MsgId", "MsgId123456");
            //签名
            String sign = DigestUtils.md5Hex(ParamUtil.getBytes(bodyXml.toString() + payMerch + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY),"UTF-8"));
            headparams.put("Signature", sign);
            String headXml = XmlUtils.parseXML(headparams);
            headXml = headXml.replaceAll("xml", "head");
            String xml = "<Ips>" +
                    "<GateWayReq>" +
                    headXml +
                    bodyXml +
                    "</GateWayReq>" +
                    "</Ips>";
            xml = xml.replaceAll("\n", "");
            logger.info("环迅"+outChannel+"支付 请求数据:"+xml);
            if("01".equals(bodyparams.get("GatewayType"))) {
	            //确认返回数据
	            Map<String, String> resultMap = PayService.initRspData(order);
	            try {
					resultMap.put(PayConstants.web_code_url, PayService.commonJumpUrl(order));
				} catch (Exception e) {
					logger.error("jump加密异常！！");
					return R.error("加密异常");
				}
	            order.setResultMap(resultMap);
	            Map<String, Object> map = new HashMap<>();
	            map.put("pGateWayReq", xml);
	            logger.info("环迅"+outChannel+"支付 数据封装:"+JSONObject.toJSONString(map));
	            Map<String, Object> jumpData = new HashMap<>();
	            jumpData.put(PayConstants.web_params, map);
				jumpData.put(PayConstants.web_form_url, 1);
	            jumpData.put(PayConstants.web_action, url);
	            order.setJumpData(jumpData);
	            return R.okData(resultMap);
            }else {
            	//扫码
            	String respXml = ScanService_WSScanSoap_Client.scanPay(xml);
            	Document document = DocumentHelper.parseText(respXml);
                Element head = document.getRootElement().element("GateWayRsp").element("head");
                Element body = document.getRootElement().element("GateWayRsp").element("body");
                String rspCode = head.elementText("RspCode");
                if("000000".equals(rspCode)) {
                	if (XmlUtils.checkSign(payMerch, RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY), respXml)) {
                		Map<String, String> resultMap = PayService.initRspData(order);
                		String qrCode = body.elementText("QrCode");
            			resultMap.put(PayConstants.web_qrcode_url, qrCode);
            			return R.okData(resultMap);
                    }else {
                    	return R.error("验签失败");
                    }
                }else {
                	return R.error(head.elementText("RspMsg"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("环迅"+outChannel+"支付异常:"+e.getMessage());
            return R.error(outChannel+"支付异常");
        }finally {
            logger.info("环迅"+outChannel+" 支付 结束-----------------------------------------");
        }
    }


    /**
     * @param order
     * @param request
     * @return
     * @Description 支付回调
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("环迅支付回调 开始-------------------------------------------------");
        String respXml = request.getParameter("paymentResult");
        logger.info("环迅支付回调  返回的xml-----------------:"+respXml);
        String msg = "";
        try {
            String payMerch = order.getPayMerch();
            if (XmlUtils.getRspCode(respXml).equals("000000")) {
				if (XmlUtils.checkSign(payMerch, RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY), respXml)) {
					//判断结果
		            String status = XmlUtils.getStatus(respXml);
		            if("Y".equals(status)){
		                order.setOrderState(OrderState.succ.id());
		                msg = "订单处理完成";
		            }else if("N".equals(status)){
		                msg = "处理失败";
		                order.setOrderState(OrderState.fail.id());
		            }else if("P".equals(status)){
		                msg = "订单处理中";
		                order.setOrderState(OrderState.ing.id());
		            }
		            String rspMsg = XmlUtils.getMsg(respXml);
		            rspMsg = rspMsg!=null?rspMsg.replaceAll("<!\\[CDATA\\[|!\\]\\]>", ""):"";
		            msg = msg+":"+rspMsg;
		            order.setRealAmount(order.getAmount());
		            logger.info("");
		            return R.ok(msg);
				}else {
					msg = "验签失败";
                    return R.error(msg);
				}
                
            } else {
                order.setOrderState(OrderState.fail.id());
                String rspMsg = XmlUtils.getRspMsg(respXml);
                rspMsg = rspMsg!=null?rspMsg.replaceAll("<!\\[CDATA\\[|!\\]\\]>", ""):"";
                msg = "订单失败"+":"+rspMsg;
                return R.error(msg);
            }
        } catch (Exception e) {
            logger.info("环迅支付回调 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("环迅支付回调 异常：" + e.getMessage());
        } finally {
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("环迅支付回调 结束-------------------------------------------------");
        }
    }

    /**
     * 环迅支付查询
     */
    public R query_wy(Order order) {
    	
    	logger.info("环迅 网银 查询 开始-------------------------------------------------");
        String msg = "";
        try{
            Calendar cal = Calendar.getInstance();
            String merchantcode = order.getMerchNo();
            String orderId = merchantcode + order.getOrderNo();
            //body部分
            SortedMap<String, String> bodyparams = new TreeMap<>();
            bodyparams.put("MerBillNo", orderId);
            bodyparams.put("Date", new SimpleDateFormat("yyyyMMdd").format(cal.getTime()));
            bodyparams.put("Amount", String.valueOf(order.getAmount()));
            String bodyXml = XmlUtils.parseXML(bodyparams);
            bodyXml = bodyXml.replaceAll("xml", "body").replaceAll("\n", "");
            //head部分
            String payMerch =  order.getPayMerch();
            SortedMap<String, String> headparams = new TreeMap<>();
            headparams.put("Version", RedisUtil.getPayCommonValue(XunHuanConst.VERSION_WY));
            headparams.put("MerCode", payMerch);
            headparams.put("Account", RedisUtil.getPayCommonValue(payMerch + XunHuanConst.Acc));
            headparams.put("ReqDate", DateUtil.getCurrentNumStr());
            //签名
            String sign = DigestUtils.md5Hex(ParamUtil.getBytes(bodyXml.toString() + payMerch + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY),"UTF-8"));
            headparams.put("Signature", sign);
            String headXml = XmlUtils.parseXML(headparams);
            headXml = headXml.replaceAll("xml", "head");
            String xml = "<Ips>" +
                    "<OrderQueryReq>" +
                    headXml +
                    bodyXml +
                    "</OrderQueryReq>" +
                    "</Ips>";
            xml = xml.replaceAll("\n", "");
            logger.info("环迅网银 查询 请求数据:"+xml);
            //调用ws接口  获取响应报文
            String resultXml = OrderQueryService_WSOrderQuerySoap_Client.getOrderByMerBillNo(xml.toString());
            logger.info("环迅网银 查询 返回数据: " + resultXml);
            if(ParamUtil.isEmpty(resultXml)){
                logger.error("环迅网银 查询 返回数据：" + resultXml);
                return R.error("返回数据为空");
            }
            //1、判断IPS返回状态码
            if (!XmlUtils.getRspCode(resultXml).equals("000000")) {
                String rspMsg = XmlUtils.getRspMsg(resultXml);
                rspMsg = rspMsg.replaceAll("<!\\[CDATA\\[|!\\]\\]>", "");
                logger.error("环迅网银 查询 失败：" + rspMsg);
                return R.error(rspMsg);
            }
            // 2、验签
            if (!XmlUtils.checkSign(payMerch, RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY), resultXml)) {
                logger.error("环迅网银 查询 验签失败");
                return R.error("验签失败");
            }
            //判断结果
            String status = XmlUtils.getStatus(resultXml);
            if("Y".equals(status)){
                order.setOrderState(OrderState.succ.id());
                msg = "订单处理完成";
            }else if("N".equals(status)){
                msg = "处理失败";
                order.setOrderState(OrderState.fail.id());
            }else if("P".equals(status)){
                msg = "订单处理中";
                order.setOrderState(OrderState.ing.id());
            }
            order.setRealAmount(order.getAmount());
            return R.ok(msg);
        }catch(Exception e){
            logger.info("环迅网银 查询 异常:"+e.getMessage());
            return R.error("查询失败");
        }finally {
            logger.info("环迅网银 查询 结束------------------------------------------------------------");
        }
    }
    
    
    /**
	 * 代付
	 * @param order
	 * @return
	 */
	private R order_acp(Order order){
		
		logger.info("环迅 代付 开始：-------------------------------------");
		try {
			String merchantCode = order.getMerchNo();
			String orderId = merchantCode + order.getOrderNo();
			Map<String,String> map = new HashMap<String,String>();
			String payMerch = order.getPayMerch();
			
			//（<Detail>为重复节点,可多次出现）
		    String detail=
		           "<MerBillNo>"+ orderId+"</MerBillNo>" +    
		           "<AccountName><![CDATA["+ order.getAcctName()+"]]></AccountName>" +   
		           "<AccountNumber>"+ order.getBankNo()+"</AccountNumber>" +    
		           "<BankName><![CDATA["+ order.getBankName()+"]]></BankName>" +    
		           "<BranchBankName><![CDATA["+ order.getBankBranch()+"]]></BranchBankName>" +    
		           "<BankCity><![CDATA["+ order.getBankCity()+"]]></BankCity>" +    
		           "<BankProvince><![CDATA["+ order.getBankProvince()+"]]></BankProvince>" +    
		           "<BillAmount>"+ order.getAmount().setScale(2).toString()+"</BillAmount>" +    
		           "<IdCard>"+ order.getCertNo()+"</IdCard>" +    
		           "<MobilePhone>"+ order.getMobile()+"</MobilePhone>"
		           + "<Remark><![CDATA[" + order.getProduct() + "]]></Remark>";
		    logger.info("环迅 代付 上送的报文detail为：" + detail);
		    String detailDes=null;
		    try {
		    	//对Detail内容用3des加密
				 detailDes=AcpUtils.encrypt3DES(detail, RedisUtil.getPayCommonValue(payMerch + XunHuanConst.DES_KEY), RedisUtil.getPayCommonValue(payMerch + XunHuanConst.DES_VECTOR));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		    
		    // body前部分
		 	String bodyXmlh=	"<Body>" + 
		 	    "<BizId>1</BizId>" +  
		 	    "<ChannelId>3</ChannelId>" +  
		 	    "<Currency>156</Currency>" +   
		 	    "<Date>"+ DateUtil.getCurrentNumStr()+"</Date>" +    
		 	    "<Attach><![CDATA["+ order.getProduct()+"]]></Attach>" +    
		 	   "<IssuedDetails>" +  "<Detail>";
		    
		 	//body尾部
		    String bodyXmlf= "</Detail>"+"</IssuedDetails>" + "</Body>" ;
			
		    //拼接完整<Body></Body>
		    String bodyXml=bodyXmlh+detailDes+bodyXmlf;
		    System.out.println("bodyXml:"+bodyXml);
		    String md5key = RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY);
	        // 利用body+数字证书做MD5签名
		    String sign = DigestUtils.md5Hex(Verify.getBytes(bodyXml  + md5key,"UTF-8"));
			System.out.println("sign:"+sign);
			
			// 拼接发送ips完整的xml报文
			String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String xml =
			"<Req>"+ 
			  "<Head>" + 
			    "<Version>" + RedisUtil.getPayCommonValue(XunHuanConst.VERSION_WY) + "</Version>" +  
			    "<MerCode>" + payMerch + "</MerCode>" +  
			    "<MerName>" + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MER_NAME) + "</MerName>" +  
			    "<Account>" + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.Acc) + "</Account>" +  
			    "<MsgId>"+System.currentTimeMillis()+"</MsgId>" +  
			    "<ReqDate>" + date + "</ReqDate>" +  
			    "<Signature>"+ sign + "</Signature>" + 
		  "</Head>"+
			bodyXml +
		"</Req>";
			logger.info("环迅 代付 上送的报文为：" + xml);
			
			//代理方式调用webservice
			JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
			//"https://merservice.ips.com.cn/pfas-merws/services/issued?wsdl"
	        Client client = dcf.createClient(RedisUtil.getPayCommonValue(XunHuanConst.ACP_URL));
	        Object[] result=null;
			try {
				result = client.invoke("issued", xml);//调用webservice
				//调用ws接口  获取响应报文
				logger.info("环迅 代付 返回的报文为：" + (String) result[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String resultXml=(String) result[0];
			
			// 1、返回报文验签
			if (!AcpUtils.checkSign(resultXml, md5key)) {
				return R.error("验签失败！");
			}
			//2、 验签通过，判断IPS返回状态码
			String resultMsg = AcpUtils.getErrorMsg(resultXml);
			resultMsg = resultMsg!=null?resultMsg.replaceAll("<!\\[CDATA\\[|!\\]\\]>", ""):"";
			String rspMsg = AcpUtils.getRspMsg(resultXml).replaceAll("<!\\[CDATA\\[|!\\]\\]>", "");
			if (!AcpUtils.getRspCode(resultXml).equals("000000")) {
				return R.error(rspMsg+":"+resultMsg);
			}
			
			//3 (报备记录为多条时，以下几步 应循环<Details>获取<Detail> 逐条判断状态，是否提交成功)
			//4获取
			String status = AcpUtils.getIssuedErrorCode(resultXml);
			if(StringUtils.isBlank(status)) {
				String batchStatus = AcpUtils.getBatchStatus(resultXml);
				if("9".equals(batchStatus)) {
					return R.error(AcpUtils.getBatchErrorMsg(resultXml)); 
				}
				resultMsg = "0".equals(batchStatus)?"未处理":("8".equals(batchStatus)?"待审核":"");
			}else if(!"000000".equals(status)) {
				return R.error(resultMsg); 
			}
			order.setBusinessNo(AcpUtils.getBatchBillno(resultXml));
			order.setOrderState(OrderState.ing.id());
			order.setRealAmount(order.getAmount());
			return R.ok(order.getMerchNo() + "," + order.getOrderNo()+resultMsg);
		} catch (Exception e) {
			logger.error("环迅 代付 异常：" + e.getMessage());
			e.printStackTrace();
			return R.error("代付异常");
		} finally {
			logger.info("环迅 代付 结束：--------------------------------------------------------");
		}
	}
	
	/**
     * 环迅代付查询
     */
    public R query_acp(Order order) {
    	
    	logger.info("环迅 代付 查询 开始-------------------------------------------------");
        String msg = "";
        try{
        	
        	
        	String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            // body （<Param>为重复节点）
            String merchantcode = order.getMerchNo();
            String orderId = merchantcode + order.getOrderNo();
            String bodyXml =
                    "<body>" +
                            "<BatchNo>" + order.getBusinessNo() + "</BatchNo>" +
                            "<MerBillNo>" + orderId + "</MerBillNo>" +
                            "</body>";
            String payMerch = order.getPayMerch();
            String md5key = RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MD5KEY);
            // MD5签名
            String sign = DigestUtils
                    .md5Hex(Verify.getBytes(bodyXml + payMerch + md5key,
                            "UTF-8"));
            
            // 发送给ipsxml
            String xml = "<Ips>" +
                    "<IssuedTradeReq>" +
                    "<head>" +
                    "<Version>" + RedisUtil.getPayCommonValue(XunHuanConst.VERSION_WY) + "</Version>" +
                    "<MerCode>" + payMerch + "</MerCode>" +
                    "<MerName>" + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.MER_NAME) + "</MerName>" +
                    "<Account>" + RedisUtil.getPayCommonValue(payMerch + XunHuanConst.Acc) + "</Account>" +
                    "<MsgId>" + "msg" + date + "</MsgId>" +
                    "<ReqDate>" + date + "</ReqDate>" +
                    "<Signature>" + sign + "</Signature>" +
                    "</head>" +
                    bodyXml +
                    "</IssuedTradeReq>" +
                    "</Ips>";
            logger.info("环迅 代付 查询 请求报文: " + xml);
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setAddress(RedisUtil.getPayCommonValue(XunHuanConst.ACP_QUERY_URL));
            factory.setServiceClass(TradeQueryService.class);
            TradeQueryService query = (TradeQueryService)factory.create();
            //获取webservice service
            //调用ws接口  获取响应报文
            String resultXml = null;
            try {
                resultXml = query.getIssuedByBillNo(xml);
                //调用ws接口  获取响应报文
                logger.info("环迅 代付 查询 返回报文：" + (String) resultXml);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 1、验签
            if (!AcpUtils.checkSign(resultXml, payMerch, md5key)) {
                //TODO重点
            	logger.info("验签失败");
                return R.error("验签失败");
            }

            //2、 验签通过，判断IPS返回状态码RspCode
            String rspMsg = AcpUtils.getRspMsg(resultXml);
    		if (!AcpUtils.getRspCode(resultXml).equals("000000")) {
    			logger.info(rspMsg);
    			return R.error(rspMsg);
    		}
        	String ordStatus = AcpUtils.getOrdStatus(resultXml);
        	String trdStatus = AcpUtils.getTrdStatus(resultXml);
        	String errorMsg = AcpUtils.getErrorMsg(resultXml);
        	errorMsg = errorMsg!=null?errorMsg.replaceAll("<!\\[CDATA\\[|!\\]\\]>", ""):"";
        	if("10".equals(ordStatus)) {
        		//成功
        		order.setOrderState(OrderState.succ.id());
                msg = "订单处理完成";
        	}else if("9".equals(ordStatus) || "4".equals(trdStatus)) {
        		//失败    退票 也做失败处理
        		msg = "处理失败";
                order.setOrderState(OrderState.fail.id());
        	}else {
        		//处理中  0  或者  8
        		msg = "订单处理中";
                order.setOrderState(OrderState.ing.id());
        	}
            order.setRealAmount(order.getAmount());
            return R.ok(msg+":"+errorMsg);
        }catch(Exception e){
            logger.info("环迅代付 查询 异常:"+e.getMessage());
            return R.error("查询失败");
        }finally {
            logger.info("环迅代付 查询 结束------------------------------------------------------------");
        }
    }
	
}
