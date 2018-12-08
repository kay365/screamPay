package com.qh.pay.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.ClearState;
import com.qh.pay.api.constenum.FeeType;
import com.qh.pay.api.constenum.OrderParamKey;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OrderType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.dao.MerchChargeDao;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.dao.PayOrderLoseDao;
import com.qh.pay.dao.RecordFoundAcctDao;
import com.qh.pay.dao.RecordFoundAvailAcctDao;
import com.qh.pay.dao.RecordMerchAvailBalDao;
import com.qh.pay.dao.RecordMerchBalDao;
import com.qh.pay.domain.MerchCharge;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayQrConfigDO;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayHandlerService;
import com.qh.pay.service.PayQrConfigService;
import com.qh.pay.service.PayQrService;
import com.qh.pay.service.PayService;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;
import com.qh.redis.service.RedissonLockUtil;
import com.qh.sms.service.SMSUtils;

/**
 * @version 1.0.0
 * @ClassName PayQrServiceImpl
 * @Description 扫码通道实现类
 * @Date 2017年12月19日 上午10:27:30
 */
@Service
public class PayQrServiceImpl implements PayQrService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayQrServiceImpl.class);
    @Autowired
    private PayQrConfigService payQrConfigService;
    @Autowired
	private PayOrderDao payOrderDao;
    @Autowired
    private PayHandlerService payHandlerService;
    @Autowired
	private RecordFoundAcctDao rdFoundAcctDao;
    @Autowired
	private RecordFoundAvailAcctDao rdFoundAvailAcctDao;
    @Autowired
    private MerchChargeDao merchChargeDao;
    @Autowired
    private RecordMerchBalDao rdMerchBalDao;
    @Autowired
    private RecordMerchAvailBalDao rdMerchAvailBalDao;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private PayOrderLoseDao payOrderLoseDao;
    
    /* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrService#getChargeMon(java.lang.String, java.lang.String)
	 */
	@Override
	public R getChargeMon(String monAmount, String merchNo,String outChannel) {
		PayAcctBal pab = RedisUtil.getPayFoundBal();
		String pabFoundNo = pab.getUsername();
		PayQrConfigDO payQrCfg = payQrConfigService.get(outChannel, pabFoundNo);
        if (payQrCfg == null) {
            return R.error(pabFoundNo + "未配置相应的资金账号扫码通道");
        }
        if (!checkGatewayIsAlive(pab.getUsername(), outChannel)) {
            SMSUtils.sendMessageNotify(payQrCfg.getAccountPhone(), OutChannel.jfDesc().get(outChannel));
            return R.error("通道断开连接");
        }
        //资金账户下的金额 放置对应的充值商户号
        String newMonAmount = findCloseMonAmount(monAmount,merchNo, payQrCfg);
        if (ParamUtil.isEmpty(newMonAmount)) {
            return R.error(merchNo + "," + monAmount + "支付金额被占用或不存在");
        }
		
		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("amount", newMonAmount);
        if(payQrCfg != null && payQrCfg.getQrs().containsKey(newMonAmount)){
            resultMap.put("qr_url", "/files/" + pabFoundNo + "/" + outChannel + "/" + newMonAmount.replace(".", "p") + ".jpg?r=" + ParamUtil.generateCode6());
        }else{
            resultMap.put("qr_url", "/files/" + pabFoundNo + "/" + outChannel + "/0.jpg?r=" + ParamUtil.generateCode6());
        }
        int remainSec = RedisUtil.getMonAmountOccupyValidTime(pabFoundNo, outChannel, newMonAmount);
		resultMap.put("remainSec", String.valueOf(remainSec));
		return R.okData(resultMap);
	}
    
    /* (非 Javadoc)
     * Description:
     * @see com.qh.pay.service.PayQrService#qrOrder(com.qh.pay.api.Order)
     */
    @Override
    public R qrOrder(Order order, Merchant merchant) {
        String outChannel = order.getOutChannel();
        String merchNo = order.getMerchNo();
        PayQrConfigDO payQrCfg = payQrConfigService.get(outChannel, merchNo);
        if (payQrCfg == null) {
            return R.error(merchNo + "未配置相应的扫码通道");
        }
        BigDecimal amount = order.getAmount();

        String monAmount = amount.toPlainString();

        if (payQrCfg.getQrs() == null) {
            return R.error("请配置相应的支付扫码金额");
        }
        if (!checkGatewayIsAlive(merchNo, outChannel)) {
            SMSUtils.sendMessageNotify(payQrCfg.getAccountPhone(), OutChannel.jfDesc().get(outChannel));
            return R.error("通道断开连接");
        }
        String newMonAmount = findCloseMonAmount(monAmount,order.getOrderNo(), payQrCfg);
        if (ParamUtil.isEmpty(newMonAmount)) {
            return R.error(merchNo + "," + monAmount + "支付金额被占用或不存在");
        }
        order.setRealAmount(new BigDecimal(newMonAmount));
        // 聚富手续金额
        BigDecimal jfRate = null;
		/*if(merchant.getHandRate() != null && (jfRate=merchant.getHandRate().get(order.getOutChannel())) != null){
			jfRate = merchant.getHandRate().get(order.getOutChannel());
		}*/
		if(jfRate == null){
			jfRate =  payQrCfg.getJfRate();
		}
		if(jfRate != null){
			order.setQhAmount(ParamUtil.multBig(amount, jfRate));
		}else{
			order.setQhAmount(BigDecimal.ZERO);
		}
        //判断商户的可用余额
        RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
        try {
            merchLock.lock();
            PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
            if (pab == null || pab.getAvailBal().compareTo(order.getQhAmount()) < 0) {
                return R.error("商户号 " + merchNo + ",可用余额不足！");
            }
        } finally {
            merchLock.unlock();
        }
        if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
            order.setMsg(order.getMsg().substring(0, 50));
        }
        order.setCrtDate(DateUtil.getCurrentTimeInt());
        Map<String, String> resultMap = PayService.initRspData(order);
        try {
            //支付扫码通道
            resultMap.put(PayConstants.web_code_url, PayService.commonQrUrl(order));
            //返回真正的金额
            resultMap.put(OrderParamKey.amount.name(), order.getRealAmount().toPlainString());
        } catch (Exception e) {
            logger.error("支付扫码url加密异常");
            return R.error("加密异常");
        }
        return R.okData(resultMap);
    }


    /**
     * @Description 与网关通讯间隔时间超出时间 320S 则返回false
     * @Author chensi
     * @Time 2017/12/20 19:29
     */
    private boolean checkGatewayIsAlive(String merchNo, String outChannel) {
        Long oldTime = (Long) RedisUtil.getQrGatewayLastSyncTime(merchNo, outChannel);
        if(oldTime == null){
        	return true;
        }
        long nowTime = new Date().getTime();
        long timeDif = nowTime - oldTime;
        if (timeDif > 1000 * 320 || timeDif <= 0) {
            return false;
        }
        return true;
    }

    /**
     * @Description 找到最接近的金额数值
     * @param monAmount
     * @param orderNo
     * @param payQrCfg
     * @return
     */
    private String findCloseMonAmount(String monAmount, String orderNo, PayQrConfigDO payQrCfg) {
        int count = 100;
        boolean findFlag = false;
        monAmount = ParamUtil.subZeroAndDot(monAmount);
        for (int i = 0; i < count; i++) {
            RLock monAmountLock = RedissonLockUtil.getMonAmountLock(payQrCfg.getMerchNo(), payQrCfg.getOutChannel(), monAmount);
            try {
                monAmountLock.lock();
                if (!RedisUtil.ifMonAmountOccupy(payQrCfg.getMerchNo(), payQrCfg.getOutChannel(), monAmount)) {
                    findFlag = true;
                    RedisUtil.setMonAmountOccupy(payQrCfg.getMerchNo(), payQrCfg.getOutChannel(), monAmount);
                    RedisUtil.setMonAmountOrderNo(payQrCfg.getMerchNo(), payQrCfg.getOutChannel(),monAmount,orderNo);
                }
            } finally {
                monAmountLock.unlock();
            }
            if (findFlag) {
                return monAmount;
            }
            monAmount = ParamUtil.addMinMonAmount(monAmount);
        }
        return null;
    }

    /* (非 Javadoc)
     * Description:
     * @see com.qh.pay.service.PayQrService#releaseMonAmount(com.qh.pay.api.Order, com.qh.pay.domain.Merchant)
     */
    @Override
    public void releaseMonAmount(Order order) {
        String monAmount = order.getRealAmount().toPlainString();
        RLock monAmountLock = RedissonLockUtil.getMonAmountLock(order.getMerchNo(), order.getOutChannel(), monAmount);
        try {
            monAmountLock.lock();
            RedisUtil.delMonAmountOccupy(order.getMerchNo(), order.getOutChannel(), monAmount);
            RedisUtil.delMonAmountOrderNo(order.getMerchNo(), order.getOutChannel(), monAmount);
        } finally {
            monAmountLock.unlock();
        }
    }

    /* (非 Javadoc)
     * Description:
     * @see com.qh.pay.service.PayQrService#notifyQr(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void notifyQr(String merchNo, String outChannel, String monAmount, String businessNo,String msg) {
        monAmount = ParamUtil.subZeroAndDot(monAmount);
        RLock monAmountLock = RedissonLockUtil.getMonAmountLock(merchNo, outChannel, monAmount);
        try {
            monAmountLock.lock();
            if(!RedisUtil.ifQrBusinessNo(merchNo, outChannel, businessNo)){//该支付业务单号已经处理
                logger.warn("该支付业务单号已经处理,{}", businessNo);
                return ;
            }
            String orderNo = RedisUtil.getMonAmountOrderNo(merchNo, outChannel, monAmount);
            if(ParamUtil.isEmpty(orderNo)){
                logger.error("该支付订单号不存在,{}", orderNo);
                this.saveQrOrderLoseData(merchNo, outChannel, monAmount,businessNo,msg);
                return ;
            }
            Order order = RedisUtil.getOrder(merchNo, orderNo);
            if(ParamUtil.isEmpty(order)){
                logger.error("该支付订单不存在,{},{}", merchNo,orderNo);
                this.saveQrOrderLoseData(merchNo, outChannel, monAmount,businessNo,msg);
                return ;
            }
            if(order.getRealAmount().compareTo(new BigDecimal(monAmount)) != 0){
                logger.error("金额不一致,订单金额：{},回调金额：{}", order.getRealAmount().toPlainString(),monAmount);
                return ;
            }
            //更新支付信息
            order.setBusinessNo(businessNo);
            order.setOrderState(OrderState.succ.id());
            if(ParamUtil.isNotEmpty(msg) && msg.length() > 50){
            	order.setMsg(msg.substring(0, 50));
            }else{
            	order.setMsg(msg);
            }
            
            RedisUtil.setOrder(order);
            RedisMsg.orderNotifyMsg(merchNo, orderNo);
            RedisUtil.setQrBusinessNo(merchNo, outChannel, businessNo);
            RedisUtil.delMonAmountOccupy(merchNo, outChannel, monAmount);
            RedisUtil.delMonAmountOrderNo(merchNo, outChannel, monAmount);
        } finally {
            monAmountLock.unlock();
        }
    }

	/**
	 * @Description 保存掉单数据
	 * @param merchNo
	 * @param outChannel
	 * @param monAmount
	 * @param businessNo
	 * @param msg
	 */
	private void saveQrOrderLoseData(String merchNo, String outChannel, String monAmount, String businessNo,
			String msg) {
		Order order = new Order();
		order.setOrderNo("0");
		order.setMerchNo(merchNo);
		order.setOutChannel(outChannel);
		order.setAmount(new BigDecimal(monAmount));
		order.setQhAmount(BigDecimal.ZERO);
		order.setBusinessNo(businessNo);
		 if(ParamUtil.isNotEmpty(msg) && msg.length() > 50){
         	order.setMsg(msg.substring(0, 50));
         }else{
         	order.setMsg(msg);
         }
		order.setOrderState(OrderState.init.id());
		order.setOrderType(OrderType.pay.id());
		order.setPayCompany(PayCompany.jf.name());
		order.setCrtDate(DateUtil.getCurrentTimeInt());
		payOrderLoseDao.save(order);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrService#notifyChargeQr(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void notifyChargeQr(String merchNo, String outChannel, String monAmount, String businessNo,String msg) {
		monAmount = ParamUtil.subZeroAndDot(monAmount);
        RLock monAmountLock = RedissonLockUtil.getMonAmountLock(merchNo, outChannel, monAmount);
        try {
            monAmountLock.lock();
            if(!RedisUtil.ifQrBusinessNo(merchNo, outChannel, businessNo)){//该支付业务单号已经处理
                logger.warn("该充值业务单号已经处理,{}", businessNo);
                return ;
            }
            String chargeMerchNo = RedisUtil.getMonAmountOrderNo(merchNo, outChannel, monAmount);
            if(ParamUtil.isEmpty(chargeMerchNo)){
                logger.error("该充值商户不存在,{}", chargeMerchNo);
                return ;
            }
            //更新充值信息
            MerchCharge merchCharge = PayQrService.initMerchCharge(chargeMerchNo,outChannel,monAmount,businessNo);
            merchCharge.setClearState(ClearState.succ.id());
            merchCharge.setOrderState(OrderState.succ.id());
            
            if(ParamUtil.isNotEmpty(msg) && msg.length() > 50){
            	merchCharge.setMsg(msg.substring(0, 50));
            }else{
            	merchCharge.setMsg(msg);
            }
            RedisUtil.setMerchCharge(merchCharge);
            RedisMsg.chargeDataMsg(chargeMerchNo, businessNo);
            
            RedisUtil.setQrBusinessNo(merchNo, outChannel, businessNo);
            RedisUtil.delMonAmountOccupy(merchNo, outChannel, monAmount);
            RedisUtil.delMonAmountOrderNo(merchNo, outChannel, monAmount);
        } finally {
            monAmountLock.unlock();
        }
		
	}

	/* (非 Javadoc)
	 * Description:商户充值无手续费，无代理费率
	 * @see com.qh.pay.service.PayQrService#chargeDataMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public void chargeDataMsg(String merchNo, String businessNo) {
		RLock lock = RedissonLockUtil.getChargeLock(merchNo, businessNo);
		if (lock.tryLock()) {
			try {
				MerchCharge merchCharge = RedisUtil.getMerchCharge(merchNo, businessNo);
				if(merchCharge == null){
					logger.info("商户充值保存失败，充值订单不存在，{}，{}",merchNo,businessNo);
					return;
				}
				if(this.saveChargeData(merchCharge)){
					RedisUtil.delMerchCharge(merchCharge);
					RedisUtil.delQrBusinessNo(RedisUtil.getPayFoundBal().getUsername(), merchCharge.getOutChannel(), businessNo);
					logger.info("商户充值保存成功，{}，{}",merchNo,businessNo);
				}
			} finally {
				lock.unlock();
			}
		}
	}

	/**
	 * @Description 保存商户充值以及增加相应的流水
	 * @param merchCharge
	 * @return
	 */
	@Transactional
	private boolean saveChargeData(MerchCharge merchCharge) {
		
		merchCharge.setCrtDate(DateUtil.getCurrentTimeInt());
		//保存数据
		
		merchChargeDao.save(merchCharge);
		//增加商户余额以及可用余额流水
		RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchChargeAdd(merchCharge, merchCharge.getAmount(),FeeType.merchCharge.id(), OrderType.charge.id());
		rdMerchBal.setCrtDate(merchCharge.getCrtDate());
		rdMerchBalDao.save(rdMerchBal);
		
		RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchChargeAdd(merchCharge, merchCharge.getAmount(),FeeType.merchCharge.id(), OrderType.charge.id());
		rdMerchAvailBal.setCrtDate(merchCharge.getCrtDate());
		rdMerchAvailBalDao.save(rdMerchAvailBal);
		
		return true;
	}

	/**
	 * @Description 保存扫码订单数据
	 * @param order
	 * @return
	 */
	@Transactional
	public boolean saveQrOrderData(Order order) {
		String merchNo = order.getMerchNo();
		// 商户信息
		Merchant merchant = merchantService.get(merchNo);
		// 支付通道信息
		PayQrConfigDO  payQrConfig = payQrConfigService.get(order.getOutChannel(), merchNo);
		
		BigDecimal amount = order.getRealAmount();
		// 成本金额
		if(payQrConfig.getCostRate() != null){
			order.setCostAmount(ParamUtil.multBig(amount, payQrConfig.getCostRate()));
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		// 聚富代理金额
		BigDecimal jfRate = null;
		/*if(merchant.getHandRate() != null && (jfRate=merchant.getHandRate().get(order.getOutChannel())) != null){
			jfRate = merchant.getHandRate().get(order.getOutChannel());
		}*/
		if(jfRate == null){
			jfRate = payQrConfig.getJfRate();
		}
		if(jfRate != null){
			order.setQhAmount(ParamUtil.multBig(amount, jfRate));
		}else{
			order.setQhAmount(BigDecimal.ZERO);
		}
		// 商户代理金额
		BigDecimal feeRate = null;
		/*if (ParamUtil.isNotEmpty(merchant.getFeeRate())) {
			feeRate = merchant.getFeeRate().get(order.getOutChannel());
		}*/
		if (feeRate != null) {
			order.setAgentAmount(ParamUtil.multSmall(amount, feeRate));
		} else {
			order.setAgentAmount(BigDecimal.ZERO);
		}
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
		//设置清算状态
		order.setClearState(ClearState.succ.id());
		payOrderDao.save(order);
		//如果没有手续费用，直接返回
		if(order.getQhAmount().compareTo(BigDecimal.ZERO) == 0 ){
			return true;
		}
		// 扣除商户余额以及可用余额流水
		RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchSub(order, order.getQhAmount(),
				FeeType.merchPreHand.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchBalDao.save(rdMerchBal);
		
		RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchSubForQr(order, order.getQhAmount(),
				FeeType.merchPreHand.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchAvailBalDao.save(rdMerchAvailBal);
		
		
		// 增加平台资金账户余额以及可用余额流水
		RecordFoundAcctDO rdFoundAcct = payHandlerService.balForPlatAdd(order,order.getQhAmount().subtract(order.getAgentAmount()), 
				FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAcctDao.save(rdFoundAcct);
		rdFoundAcct = payHandlerService.availBalForPlatAdd(order, order.getQhAmount().subtract(order.getAgentAmount()), FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAvailAcctDao.save(rdFoundAcct);

		// 增加商户代理余额以及可用余额流水
		if (order.getAgentAmount().compareTo(BigDecimal.ZERO) != 0) {
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getAgentAmount(),merchant.getParentAgent(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order, order.getQhAmount().subtract(order.getAgentAmount()), merchant.getParentAgent(), FeeType.platIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
		}
		return true;
	}


    /* (非 Javadoc)
     * Description:
     * @see com.qh.pay.service.PayQrService#superChargeQr(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public R superChargeQr(String merchNo, String outChannel, String monAmount, String businessNo) {
        Merchant merchant =  merchantService.get(merchNo);
        if(merchant == null){
            return R.error("请检查商户号");
        }

        MerchCharge merchCharge = PayQrService.initMerchCharge(merchNo,outChannel,monAmount,businessNo);
        merchCharge.setClearState(ClearState.succ.id());
        merchCharge.setOrderState(OrderState.succ.id());
        merchCharge.setCrtDate(DateUtil.getCurrentTimeInt());

        RLock lock = RedissonLockUtil.getChargeLock(merchNo, businessNo);
        if (lock.tryLock()) {
            try {
                if(this.saveChargeData(merchCharge)){
                    return R.ok("充值成功");
                }
            } finally {
                lock.unlock();
            }
        }
        return R.error("充值失败！");

    }

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrService#syncOrder(com.qh.pay.api.Order, java.lang.String)
	 */
	@Override
	public R syncOrder(Merchant merchant,Order order, String businessNo) {
		order.setBusinessNo(businessNo);
		Order payOrderLose = payOrderLoseDao.getByBusinessNo(businessNo, order.getMerchNo(), order.getOutChannel());
		if(payOrderLose == null){
			logger.error("未找到掉单记录，{}，{}，{}",businessNo,order.getMerchNo(),order.getOutChannel());
			return R.error("未找到掉单记录");
		}else{
			payOrderLose.setOrderNo(order.getOrderNo());
			payOrderLose.setOrderState(OrderState.succ.id());
			payOrderLose.setMsg("手动同步");
			if(payOrderLose.getAmount().compareTo(order.getRealAmount()) != 0){
				logger.error("金额不一致，{}，{}，订单实际金额：{}，掉单金额：{}",businessNo,order.getMerchNo(),
						order.getRealAmount(),payOrderLose.getAmount());
				return R.error("金额不一致");
			}
			if(Math.abs(payOrderLose.getCrtDate() - order.getCrtDate()) < 60 * 60 * 24){
				logger.error("时间相差过大，订单创建时间：{}，掉单创建时间：{}",order.getCrtDate(),payOrderLose.getCrtDate());
				return R.error("时间相差过大");
			}
			if(ParamUtil.isNotEmpty(payOrderLose.getOrderNo()) && !payOrderLose.getOrderNo().equals(order.getOrderNo())){
				logger.error("同步的订单号不一致，订单订单号：{}，掉单订单号：{}",order.getOrderNo(),payOrderLose.getOrderNo());
				return R.error("同步的订单号不一致");
			}
			BigDecimal jfRate = null;
			/*if(merchant.getHandRate() != null && (jfRate=merchant.getHandRate().get(order.getOutChannel())) != null){
				jfRate = merchant.getHandRate().get(order.getOutChannel());
			}*/
			if(jfRate == null){
				PayQrConfigDO payQrConfig = payQrConfigService.get(order.getOutChannel(), order.getMerchNo());
				if(payQrConfig != null){
					jfRate = payQrConfig.getJfRate();
				}
			}
			if(jfRate != null){
				payOrderLose.setQhAmount(ParamUtil.multBig(payOrderLose.getAmount(), jfRate));
			}else{
				payOrderLose.setQhAmount(BigDecimal.ZERO);
			}
			payOrderLoseDao.update(payOrderLose);
			order.setOrderState(payOrderLose.getOrderState());
		}
		return R.ok(Constant.result_msg_ok);
	}

}
