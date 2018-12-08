package com.qh.pay.service;

import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.redis.service.RedisUtil;

/**
 * @ClassName PayBaseServiceTest
 * @Description 支付基础类测试
 * @Date 2017年11月22日 下午5:48:22
 * @version 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ComponentScan("com.qh.pay")
public class PayBaseServiceTest {
	
	@Autowired
	public PayService payService; 
	
	public static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayBaseServiceTest.class);
	/****
	 * 商户号
	 */
	public final static String merchNo = "QH0000";
	/**
	 * 公钥 --聚富
	 */
	public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCMlNqJh3JG6shlMJ0OJ42QnuG9OVUiBlcpbUXbaaprUjF1XTqDaUJZLvk5fkRDAgZAC/CbyYOOoZBpp8y3CnnCSPtJ8oKoLuQOcN1hW4snE0VP+J2wKMQQyjmzFK4MiRRDE6oxD2nWFe517zl8IOJYZWK3egTIXezoidLG0bucZwIDAQAB";
	/**
	 * 公钥 --商户
	 */
	public final static String mcPublicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFr5fSL3N0qa+tpIRFn/ApDfeuOMrvhz3Cb3T94by7KigO57ppkMadAOG2wLV5S6QA5WeN5oZWHzNUnYZbn6cFE38cV8LX0ABMl0A0x5O00NCMTCkxxUZ/5IlrK6SYEjk75vSiimtlAI9ZW/F8RKqzVoOr5pHZJ4tRSXaR5VHO0wIDAQAB";
	/**
	 * 私钥---商户
	 */
	public final static String mcPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIWvl9Ivc3Spr62khEWf8CkN9644yu+HPcJ"
			+ "vdP3hvLsqKA7nummQxp0A4bbAtXlLpADlZ43mhlYfM1SdhlufpwUTfxxXwtfQAEyXQDTHk7TQ0IxMKTHFRn/kiWsrpJgSOTvm9KKKa2"
			+ "UAj1lb8XxEqrNWg6vmkdkni1FJdpHlUc7TAgMBAAECgYBNlfoDtxxHoc9edHN7wPXtrbiIOVe1qgSy2mLIkYEqEq5K8Dvk1mweZIuat"
			+ "77alYaqKnluBlMCmnr86as3c7HHTQlh8tlOOSnmwLzacVF453FvKAjvH9ti1nSf6dk9yCoDcsgulOYnqqRbAvVg+evBmmWuIVqZxvwe"
			+ "CxNERo98CQJBANFPtCSJdzMDk0uiE0r9nwiESYyX1n0NCozHKc6kSuGilx30xrrcedMbZyKDTCYgogp+d+QEzYddqq0Gj67jtXcCQQ"
			+ "CjgXQjgaFfUEBsFQ2menYGQgawCGnxYCJ7oUlBUScJrFpFhosHcBaoq69acQyGkC6kOu/jjuODjAAzjUVn4biFAkEAlu9tzOcgALZ0U"
			+ "hb26J3JP5/9VZfsgNKVp/y6phuNL/ZKGLz5TahNZTEehyG9GMVxdDXMiK3588JUoF7Z39iucwJBAJYsUyA9cprZWaIroBL0zSwoPn41"
			+ "7CBPPLyyQVclkyZWT78luNIHCDi5H2CBDpEVIlGi9CvcVGjBEHpI2aN09QUCQCg0j5IEsCeinYje4Pjs6v8y6GdiW6qUl8p2pol1LBt"
			+ "R/ycMcYkJWSUN1Ffgz84cRCkxuLS6oxyyyLporbj4kig=";
	/***
	 * 支付域名
	 */
	public final static String url = "http://localhost:8888/pay/order";
	
	/****
	 * 绑卡域名
	 */
	public final static String card_url = "http://localhost:8888/pay/card";
	
	/**
	 * @Description 设置订单支付中 用于查询
	 * @param orderNo
	 */
	public void setOrderIng(String orderNo) {
		Order order = RedisUtil.getOrder(merchNo, orderNo);
		if(order == null){
			logger.info("订单不存在：{},{}",merchNo,orderNo);
		}
		order.setOrderState(OrderState.ing.id());
		RedisUtil.setOrder(order);
	}
	
	
}
