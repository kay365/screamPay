package com.qh.paythird.impl;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.qh.paythird.hsy.HsyService;
import com.qh.paythird.huiFuBao.HuiFuBaoService;
import com.qh.paythird.hx.XunHuanService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.paythird.PayBaseService;
import com.qh.paythird.baiXingDa.BaiXingDaService;
import com.qh.paythird.beecloud.BeeCloudService;
import com.qh.paythird.bopay.BopayService;
import com.qh.paythird.dianxin.DianXinService;
import com.qh.paythird.jiupay.JiupayService;
import com.qh.paythird.mobao.MoBaoService;
import com.qh.paythird.sand.SandPayService;
import com.qh.paythird.wft.WeiFuTongService;
import com.qh.paythird.xiaotian.XiaoTianService;
import com.qh.paythird.xinfu.XinFuService;
import com.qh.paythird.xinqianbao.XinQianBaoService;
import com.qh.paythird.ysb.YinShengBaoService;

/**
 * @ClassName PayBaseServiceImpl
 * @Description 对接支付基础类
 * @Date 2017年11月8日 下午5:21:06
 * @version 1.0.0
 */
@Service
public class PayBaseServiceImpl implements PayBaseService{
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayBaseServiceImpl.class);

	@Autowired
	private BopayService bopayService;
	@Autowired
	private JiupayService jiupayService;
	@Autowired
	private XinFuService xinfuService;
	@Autowired
	private DianXinService dianxinService;
	@Autowired
	private XinQianBaoService xinQianBaoService;
	@Autowired
	private BeeCloudService beeCloudService;
	@Autowired
	private HsyService hsyService;
	@Autowired
	private XiaoTianService xiaotianService;
	@Autowired
	private MoBaoService moBaoService;
	@Autowired
	private YinShengBaoService yinShengBaoService;
	@Autowired
	private XunHuanService xunHuanService;
	@Autowired
	private BaiXingDaService baiXingDaService;
	@Autowired
	private HuiFuBaoService huiFuBaoService;
	@Autowired
	private SandPayService sandPayService;
	@Autowired
	private WeiFuTongService weiFuTongService;

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#order(com.qh.pay.domain.PayConfigCompanyDO, com.qh.pay.api.Order)
	 */
	@Override
	public R order(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		switch (company) {
			case ysb:
				result = yinShengBaoService.order(order);
				break;
			case bopay:
				result = bopayService.order(order);
				break;
			case jiupay:
				result  = jiupayService.order(order);
				break;
			case xinfu:
				result  = xinfuService.order(order);
				break;
			case dianxin:
				result  = dianxinService.order(order);
				break;
			case xinqianbao:
				result  = xinQianBaoService.order(order);
				break;
			case beecloud:
				result  = beeCloudService.order(order);
				break;
			case hsy:
				result = hsyService.order(order);
				break;
			case mobao:
				result = moBaoService.order(order);
				break;
			case hx:
				result = xunHuanService.order(order);
				break;
			case bxd:
				result = baiXingDaService.order(order);
				break;
			case hfb:
				result = huiFuBaoService.order(order);
				break;
			case sd:
				result = sandPayService.order(order);
				break;
			case wft:
				result = weiFuTongService.order(order);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#notify(com.qh.pay.api.Order, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notify(Order order, HttpServletRequest request, String requestBody) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		logger.info("requestBody:"+requestBody);
		switch (company) {
			case ysb:
				result = yinShengBaoService.notify(order,request,requestBody);
				break;
			case bopay:
				result = bopayService.notify(order, requestBody);
				break;
			case jiupay:
				result = jiupayService.notify(order, request);
				break;
			case xinfu:
				result  = xinfuService.notify(order, request);
				break;
			case dianxin:
				result  = dianxinService.notify(order, request);
				break;
			case xinqianbao:
				result  = xinQianBaoService.notify(order, request);
				break;
			case beecloud:
				result  = beeCloudService.notify(order, requestBody);
				break;
			case hsy:
				result = hsyService.notify(order,requestBody);
				break;
			case mobao:
				result = moBaoService.notify(order,request);
				break;
			case hx:
				result = xunHuanService.notify(order,request);
				break;
			case bxd:
				result = baiXingDaService.notify(order,request,requestBody);
				break;
			case hfb:
				result = huiFuBaoService.notify(order,request,requestBody);
				break;
			case sd:
				result = sandPayService.notify(order,request,requestBody);
				break;
			case wft:
				result = weiFuTongService.notify(order,request,requestBody);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#query(com.qh.pay.api.Order)
	 */
	@Override
	public R query(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		switch (company) {
			case ysb:
				result = yinShengBaoService.query(order);
				break;
			case bopay:
				result = bopayService.query(order);
				break;
			case jiupay:
				result = jiupayService.query(order);
				break;
			case xinfu:
				result  = xinfuService.query(order);
				break;
			case dianxin:
				result  = dianxinService.query(order);
				break;
			case xinqianbao:
				result  = xinQianBaoService.query(order);
				break;
			case beecloud:
				result  = beeCloudService.query(order);
				break;
			case hsy:
				result = hsyService.query(order);
				break;
			case mobao:
				result = moBaoService.query(order);
				break;
			case hx:
				result = xunHuanService.query(order);
				break;
			case bxd:
				result = baiXingDaService.query(order);
				break;
			case hfb:
				result = huiFuBaoService.query(order);
				break;
			case sd:
				result = sandPayService.query(order);
				break;
			case wft:
				result = weiFuTongService.query(order);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
		
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#orderAcp(com.qh.pay.api.Order)
	 */
	@Override
	public R orderAcp(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		switch (company) {
			case dianxin:
				result  = dianxinService.order(order);
				break;
			case hx:
				result = xunHuanService.order(order);
				break;
			case ysb:
				result = yinShengBaoService.order(order);
				break;
			case bopay:
				result = bopayService.orderAcp(order);
				break;
			case jiupay:
				result = jiupayService.orderAcp(order);
				break;
			case beecloud:
				result = beeCloudService.orderAcp(order);
				break;
			case xiaotian:
				result = xiaotianService.orderAcp(order);
				break;
			case bxd:
				result = baiXingDaService.orderAcp(order);
				break;
			case hfb:
				result = huiFuBaoService.order(order);
				break;
			case sd:
				result = sandPayService.order_acp(order);
				break;
			case wft:
				result = weiFuTongService.order(order);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#notifyAcp(com.qh.pay.api.Order, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notifyAcp(Order order, HttpServletRequest request, String requestBody) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		switch (company) {
			case dianxin:
				result  = dianxinService.notify_acp(order,request,requestBody);
				break;
			case ysb:
				result = yinShengBaoService.notify(order,request,requestBody);
				break;
			case bopay:
				result = bopayService.notifyAcp(order, requestBody);
				break;
			case jiupay:
				result = jiupayService.notifyAcp(order, request);
				break;
			case hfb:
				result = huiFuBaoService.notify(order,request,requestBody);
				break;
			case wft:
				result = weiFuTongService.notify(order,request,requestBody);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#acpQuery(com.qh.pay.api.Order)
	 */
	@Override
	public R acpQuery(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		String payMerch =order.getPayMerch();
		if(payMerch.contains("_")) {
			order.setPayMerch(payMerch.substring(0,payMerch.indexOf("_")));
		}
		switch (company) {
			case dianxin:
				result  = dianxinService.query_acp(order);
				break;
			case hx:
				result = xunHuanService.query(order);
				break;
			case ysb:
				result = yinShengBaoService.query(order);
				break;
			case bopay:
				result = bopayService.acpQuery(order);
				break;
			case jiupay:
				result = jiupayService.acpQuery(order);
				break;
			case xiaotian:
				result = xiaotianService.acpQuery(order);
				break;
			case bxd:
				result = baiXingDaService.acpQuery(order);
				break;
			case hfb:
				result = huiFuBaoService.query(order);
				break;
			case sd:
				result = sandPayService.query_acp(order);
				break;
			case wft:
				result = weiFuTongService.query(order);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		order.setPayMerch(payMerch);
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#refreshBanks(com.qh.pay.api.Order, java.util.List, java.util.List)
	 */
	@Override
	public void refreshBanks(Order order, List<String> bank_savings, List<String> bank_credits) {
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			return;
		}
		switch (company) {
			case bopay:
				// bopayService.refreshBanks(order);
				break;
			case jiupay:
				jiupayService.refreshBanks(order, bank_savings, bank_credits);
				break;
			case beecloud:
				beeCloudService.refreshBanks(order, bank_savings, bank_credits);
				break;
			default:
				logger.error("未找到支付公司！");
				break;
		}
		
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#cardBind(com.qh.pay.api.Order, com.qh.pay.domain.MerchUserSignDO)
	 */
	@Override
	public R cardBind(Order order, MerchUserSignDO userSign) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {
			case bopay:
				break;
			case jiupay:
				result = jiupayService.cardBind(order,userSign);
				break;
			case beecloud:
				result = beeCloudService.cardBind(order,userSign);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#cardBindConfirm(com.qh.pay.api.Order, com.qh.pay.domain.MerchUserSignDO)
	 */
	@Override
	public R cardBindConfirm(Order order, MerchUserSignDO userSign) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {
			case bopay:
				break;
			case jiupay:
				result = jiupayService.cardBindConfirm(order,userSign);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.paythird.PayBaseService#cardMsgResend(com.qh.pay.api.Order, java.lang.Integer)
	 */
	@Override
	public R cardMsgResend(Order order, Integer sendType) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {
			case bopay:
				break;
			case jiupay:
				result = jiupayService.cardMsgResend(order,sendType);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

}
