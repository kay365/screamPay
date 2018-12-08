package com.qh.redis.config;

import java.util.concurrent.CountDownLatch;

import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.service.PayService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;

/**
 * @ClassName MessageNotifyRedis
 * @Description 通知消息
 * @Date 2017年11月30日 下午2:39:36
 * @version 1.0.0
 */
public class MessageNotifyRedis implements MessageListener{
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MessageNotifyRedis.class);

	private CountDownLatch latch;
	
	private PayService payService;
	
	/**
	 * @Description T
	 * @param latch
	 */
	public MessageNotifyRedis(CountDownLatch latch, PayService payService) {
		this.latch = latch;
		this.payService = payService;
	}
	

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String channel = new String(message.getChannel());
		String msgKey = new String(message.getBody());
		msgKey = msgKey.replaceAll("\"", "");
		int index = msgKey.indexOf(RedisConstants.link_symbol);
		if(index <= 0){
			return;
		}
		String merchNo = msgKey.substring(0,index);
    	String orderNo = msgKey.substring(index+1);
		logger.info("MessageNotifyRedis"+ ":" + channel + ":" + merchNo + ":" + orderNo);
		if(RedisConstants.channel_keyevent_expired.equals(channel)){
			if(merchNo.startsWith(RedisConstants.cache_keyevent_ord)){
				merchNo = merchNo.substring(RedisConstants.cache_keyevent_ord.length());
				String result = payService.eventOrderNotifyMsg(merchNo,orderNo);
				if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
//					RedisMsg.orderDataMsg(merchNo,orderNo);
				}else{
					/*if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
						RedisMsg.orderDataMsg(merchNo,orderNo);
					}*/
					RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
				}
			}else if(merchNo.startsWith(RedisConstants.cache_keyevent_acp)){
				merchNo = merchNo.substring(RedisConstants.cache_keyevent_acp.length());
				String result = payService.eventOrderAcpNotifyMsg(merchNo,orderNo);
				if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
//					RedisMsg.orderAcpDataMsg(merchNo,orderNo);
				}else{
					/*if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo)){
						RedisMsg.orderAcpDataMsg(merchNo,orderNo);
					}*/
					RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo);
				}
			}else if(merchNo.startsWith(RedisConstants.cache_keyevent_not_pay_ord)){
				merchNo = merchNo.substring(RedisConstants.cache_keyevent_not_pay_ord.length());
				Order order = RedisUtil.getOrder(merchNo, orderNo);
				if(order!=null) {
					if(order.getOrderState().equals(OrderState.init.id()) || order.getOrderState().equals(OrderState.ing.id())) {
						R r = payService.syncOrder(merchNo, orderNo,null);
						logger.info("MessageNotifyRedis:订单过期自动同步返回信息："+r.getMsg());
						
						order = RedisUtil.getOrder(merchNo, orderNo);
						if(!order.getOrderState().equals(OrderState.succ.id())){
							if(order.getOrderState().equals(OrderState.init.id()) || order.getOrderState().equals(OrderState.ing.id())) {
								if(!RedisUtil.setKeyEventExpiredByAutoSync(RedisConstants.cache_keyevent_not_pay_ord,merchNo,orderNo)) {
									order.setOrderState(OrderState.close.id());
									order.setMsg(r.getMsg()+":订单过期自动关闭");
									RedisUtil.setOrder(order);
								}
							}
							if(!order.getOrderState().equals(OrderState.init.id()) && !order.getOrderState().equals(OrderState.ing.id())) {
								String result = payService.eventOrderNotifyMsg(merchNo,orderNo);
								if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
	//								RedisMsg.orderDataMsg(merchNo,orderNo);
								}else{
									/*if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
										RedisMsg.orderDataMsg(merchNo,orderNo);
									}*/
									RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
								}
								RedisMsg.orderDataMsg(merchNo,orderNo);
							}
						}
					}else {
						logger.info("MessageNotifyRedis:{},{},{},订单未通知到商户或商户通知返回状态不对",merchNo,orderNo,order.getOrderState());
					}
				}else {
					logger.info("MessageNotifyRedis:{},{},订单已处理完成",merchNo,orderNo);
				}
			}
		}
		latch.countDown();
	}
}
