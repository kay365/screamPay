package com.qh.pay.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.service.UserBankService;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.AcctType;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.api.constenum.AuditResult;
import com.qh.pay.api.constenum.AuditType;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.CertType;
import com.qh.pay.api.constenum.ClearState;
import com.qh.pay.api.constenum.Currency;
import com.qh.pay.api.constenum.EncryptType;
import com.qh.pay.api.constenum.FeeType;
import com.qh.pay.api.constenum.OrderParamKey;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OrderType;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayChannelType;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.api.constenum.PaymentRateUnit;
import com.qh.pay.api.constenum.ProfitLoss;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.dao.PayAuditDao;
import com.qh.pay.dao.PayOrderAcpDao;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.dao.RecordFoundAcctDao;
import com.qh.pay.dao.RecordFoundAvailAcctDao;
import com.qh.pay.dao.RecordMerchAvailBalDao;
import com.qh.pay.dao.RecordMerchBalDao;
import com.qh.pay.dao.RecordPayMerchAvailBalDao;
import com.qh.pay.dao.RecordPayMerchBalDao;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayAuditDO;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;
import com.qh.pay.domain.RecordPayMerchBalDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayAuditService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.pay.service.PayHandlerService;
import com.qh.pay.service.PayQrService;
import com.qh.pay.service.PayService;
import com.qh.paythird.PayBaseService;
import com.qh.redis.RedisConstants;
import com.qh.redis.constenum.ConfigParent;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;
import com.qh.redis.service.RedissonLockUtil;

/**
 * @ClassName PayServiceImpl
 * @Description 支付实现类
 * @Date 2017年11月6日 下午2:48:20
 * @version 1.0.0
 */
@Service
public class PayServiceImpl implements PayService {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);
	@Autowired
	private PayConfigCompanyService payCfgCompService;
	@Autowired
	private PayBaseService payBaseService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private PayHandlerService payHandlerService;
	@Autowired
	private PayOrderDao payOrderDao;
	@Autowired
	private RecordFoundAcctDao rdFoundAcctDao;
	@Autowired
	private RecordMerchBalDao rdMerchBalDao;
	@Autowired
	private PayAuditDao payAuditDao;
	@Autowired
	private PayOrderAcpDao payOrderAcpDao;
	@Autowired
	private RecordMerchAvailBalDao rdMerchAvailBalDao;
	@Autowired
	private RecordFoundAvailAcctDao rdFoundAvailAcctDao;
	@Autowired
	private RecordPayMerchAvailBalDao rdPayMerchAvailBalDao;
	@Autowired
	private RecordPayMerchBalDao rdPayMerchBalDao;
	@Autowired
	private PayQrService payQrService;
	@Autowired
	private UserBankService userBankService;
	@Autowired
	private PayAuditService payAuditService;
	/**
	 * 发起支付
	 */
	@Override
	public Object order(Merchant merchant, JSONObject jo) {
		
		logger.info("=============================0===================================="+DateUtil.getCurrentNumStr());
		String merchNo = merchant.getMerchNo();
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				Order order = new Order();
				order.setOrderType(OrderType.pay.id());
				// 渠道编码
				order.setOutChannel(jo.getString(OrderParamKey.outChannel.name()));
				String initResult = null;
				if (OutChannel.jfDesc().containsKey(order.getOutChannel())) {
					//初始化扫码通道订单
					initResult = payHandlerService.initQrOrder(order, jo);
				}else{
					// 初始化订单信息
					initResult = payHandlerService.initOrder(order, jo);
				}
				if (ParamUtil.isNotEmpty(initResult)) {
					logger.error(initResult);
					return R.error(initResult);
				}
				//userId不能为商户号
				if(ParamUtil.isNotEmpty(order.getUserId()) && merchNo.equals(order.getUserId())){
					return R.error("userId不能与商户号一致");
				}
				
				String omteKey = CfgKeyConst.ORDER_MONEY_THREE_SECONDS+merchNo+"_"+order.getOutChannel()+"_"+order.getUserId()+"_"+order.getAmount().setScale(2);
				Long omteKeyExpireTime = RedisUtil.getRedisTemplate().getExpire(omteKey);
				if(omteKeyExpireTime > 0) {
					return R.error("请勿频繁下单!");
				}else {
					RedisUtil.setValue(omteKey, "true");
	        		RedisUtil.getRedisTemplate().expire(omteKey, 3, TimeUnit.SECONDS);
				}
				logger.info("==============================1==================================="+DateUtil.getCurrentNumStr());
				if (RedisUtil.getOrder(merchNo, orderNo) != null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}else if(payOrderDao.get(orderNo, merchNo)!= null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}
				logger.info("=============================2===================================="+DateUtil.getCurrentNumStr());
				Integer dayLimit = merchant.getDayLimit();
				
				String key = CfgKeyConst.MERCHANT_DAY_LIMIT+merchNo;
				Long expireTime = RedisUtil.getRedisTemplate().getExpire(key);
	        	if(expireTime > 0) {
	        		if(dayLimit!=null && dayLimit >0) {
		        		BigDecimal amount = (BigDecimal)RedisUtil.getValue(key);
		        		amount = amount.add(order.getAmount());
		        		
		        		if(new BigDecimal(dayLimit).compareTo(amount) == -1) {
		        			return R.error("超出当日限额"); 
		        		}
	        		}
	        	}else {
	        		if(dayLimit!=null && dayLimit >0) {
	        			if(new BigDecimal(dayLimit).compareTo(order.getAmount()) == -1) {
		        			return R.error("超出当日限额"); 
		        		}
	        		}
	        		RedisUtil.setValue(key, BigDecimal.ZERO);
	        		RedisUtil.getRedisTemplate().expire(key, DateUtil.getDayLeftSeconds(), TimeUnit.SECONDS);
	        	}
				Integer monthLimit = merchant.getMonthLimit();
				
				key = CfgKeyConst.MERCHANT_MONTH_LIMIT+merchNo;
				expireTime = RedisUtil.getRedisTemplate().getExpire(key);
				if(expireTime > 0) {
					if(monthLimit!=null && monthLimit >0) {
						BigDecimal amount = (BigDecimal)RedisUtil.getValue(key);
		        		amount = amount.add(order.getAmount());
		        		
		        		if(new BigDecimal(monthLimit).compareTo(amount) == -1) {
		        			return R.error("超出当月限额"); 
		        		}
					}
					
	        	}else {
	        		if(monthLimit!=null && monthLimit >0) {
	        			if(new BigDecimal(monthLimit).compareTo(order.getAmount()) == -1) {
		        			return R.error("超出当月限额"); 
		        		}
	        		}
	        		RedisUtil.setValue(key, BigDecimal.ZERO);
	        		RedisUtil.getRedisTemplate().expire(key, DateUtil.getMonthLeftSeconds(), TimeUnit.SECONDS);
	        	}
				logger.info("============================3====================================="+DateUtil.getCurrentNumStr());
				R r;
				//个人通道校验
				if(merchant.getChannelSwitch() == null || !merchant.getChannelSwitch().containsKey(order.getOutChannel())){
					return R.error(merchNo + "," + order.getOutChannel() + "通道关闭！");
				}
				if (OutChannel.jfDesc().containsKey(order.getOutChannel())) {
					//扫码通道订单处理
					r = payQrService.qrOrder(order,merchant);
					if(R.ifError(r) && order.getRealAmount() != null && order.getRealAmount().compareTo(BigDecimal.ZERO) > 0 ){
						payQrService.releaseMonAmount(order);
					}
					order.setPayCompany(PayCompany.jf.name());
					order.setPayMerch(merchNo);
				}else{
					//检查支付规则
					r = this.checkCfgComp(order);
					logger.info("===================================4=============================="+DateUtil.getCurrentNumStr());
					if(R.ifError(r)){
						return r;
					}
					r = (R) payBaseService.order(order);
					logger.info("====================================5============================="+DateUtil.getCurrentNumStr());
				}
				
				//返回处理
				if (R.ifSucc(r)) {
					@SuppressWarnings("unchecked")
					Map<String, String> data = (Map<String, String>) r.get(Constant.result_data);
					order.setResultMap(data);
					RedisUtil.setOrder(order);
					
					RedisUtil.setKeyEventExpiredByAutoSync(RedisConstants.cache_keyevent_not_pay_ord,merchNo,orderNo);
					//getRedisNotifyTemplate().opsForValue().set(RedisConstants.cache_keyevent_not_pay_ord + merchNo + RedisConstants.link_symbol +  orderNo, RedisConstants.keyevent_5, RedisConstants.keyevent_5, TimeUnit.MINUTES);
					
					
					lock.unlock();
					//先通知保存
					RedisMsg.orderDataMsg(merchNo,orderNo);
					
					return decryptAndSign(data, merchant.getPublicKey()).put(Constant.result_msg,
							r.get(Constant.result_msg));
				}
				return r;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
				logger.info("=====================================6============================"+DateUtil.getCurrentNumStr());
			}
		} else {
			return R.error(merchNo + "," + orderNo + "下单失败！");
		}
	}


	/**
	 * @Description 支付通道 选择
	 * @param order
	 * @return
	 */
	private R checkCfgComp(Order order) {
		
		Merchant merchant = merchantService.get(order.getMerchNo());
		Integer mPayChannelType = merchant.getPayChannelType();
		//判断商户是否有分配  通道分类   没有分配不支持支付
		if(mPayChannelType == null || !PayChannelType.desc().containsKey(mPayChannelType)) {
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未分配支付通道");
		}
		
		List<Object> payCfgComps = payCfgCompService.getPayCfgCompByOutChannel(order.getOutChannel());
		if (payCfgComps == null || payCfgComps.size() == 0) {
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道配置错误！");
		}
		
		List<PayConfigCompanyDO> pccList = new ArrayList<PayConfigCompanyDO>();
		PayConfigCompanyDO payCfgComp = null;
		for (Object object : payCfgComps) {
			payCfgComp = (PayConfigCompanyDO) object;
			if(payCfgComp!=null && payCfgComp.getIfClose() == 0){
				Integer cPayChannelType = payCfgComp.getPayChannelType();
				Integer paymentMethod = payCfgComp.getPaymentMethod();
				if(cPayChannelType != null && cPayChannelType.equals(mPayChannelType) && paymentMethod!=null && paymentMethod.equals(merchant.getPaymentMethod())) {
					//将与商户  同类的通道 找出来
					pccList.add(payCfgComp);
				}
			}
		}
		
		if(pccList.size() <= 0) {
			//当前商户没有 同类通道
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
		}
		
		//同类 有多个通道，则按规则获取
		if (pccList.size() >= 2) {
			String key = order.getOutChannel()+"_"+mPayChannelType+"_"+merchant.getPaymentMethod();
			payCfgComp = morePayChannelChoose(order, pccList, payCfgComp,key);
		}else {
			//同类 通道只有一个
			payCfgComp = pccList.get(0);
		}
		if(payCfgComp == null){
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
		}
		Integer maxPayAmt = payCfgComp.getMaxPayAmt();
		Integer minPayAmt = payCfgComp.getMinPayAmt();
		if(maxPayAmt != null && new BigDecimal(maxPayAmt).compareTo(order.getAmount()) == -1){
			return R.error("单笔最高限额:"+maxPayAmt+"元");
		}
		if(minPayAmt != null && new BigDecimal(minPayAmt).compareTo(order.getAmount()) == 1){
			return R.error("单笔最低限额为:"+minPayAmt+"元");
		}
		order.setPayCompany(payCfgComp.getCompany());
		order.setPayMerch(payCfgComp.getPayMerch());
		order.setCallbackDomain(payCfgComp.getCallbackDomain());
		return R.ok();
	}

	/**
	 * 多支付通道  选择规则方法
	 * @param order
	 * @param pccList
	 * @param payCfgComp
	 * @return
	 */
	private PayConfigCompanyDO morePayChannelChoose(Order order, List<PayConfigCompanyDO> pccList,
			PayConfigCompanyDO payCfgComp,String key) {
		
		String singlePollMoney = RedisUtil.getConfigValue(CfgKeyConst.COMPANY_SIGLE_POLL_MONEY, ConfigParent.outChannelConfig.name());
		if(StringUtils.isBlank(singlePollMoney) || new BigDecimal(singlePollMoney).compareTo(BigDecimal.ZERO) <= 0) {
			Integer useIndex = (Integer)RedisUtil.getHashValue(CfgKeyConst.COMPANY_USE_INDEX, key);
			if(useIndex == null || useIndex >= pccList.size()-1) {
				useIndex = 0;
			}else {
				useIndex++;
			}
			payCfgComp = pccList.get(useIndex);
			RedisUtil.setHashValue(CfgKeyConst.COMPANY_USE_INDEX, key, useIndex);
			return payCfgComp;
		}else
			singlePollMoney = new BigDecimal(singlePollMoney).divide(new BigDecimal(100)).toString();
		
		// 按成本费率升序排列   费率最低的通道排在最前面
		/*pccList.sort(new Comparator<PayConfigCompanyDO>() {
			@Override
			public int compare(PayConfigCompanyDO o1, PayConfigCompanyDO o2) {
				return o1.getCostRate().compareTo(o2.getCostRate());
			}
		});*/
		
		String date = DateUtil.getCurrentDateStr();
		int i = 1;
		
		for (PayConfigCompanyDO payConfigCompanyDO : pccList) {
			String company = payConfigCompanyDO.getCompany();
			String payMerch = payConfigCompanyDO.getPayMerch();
			
			String cacheDate = (String)RedisUtil.getRedisTemplate().opsForValue().get(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company + "_date");
			boolean dateChange = date.equals(cacheDate);
			//记录时间
			if(!dateChange)
				RedisUtil.getRedisTemplate().opsForValue().set(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company + "_date", date);
			
			//获取当日当前支付公司下单个商户  已交易的金额总数  以及单次轮询的目标金额         curMoney:100[交易金额]  curTargetMoney:100000[最大限额(资金池1000000)*10%]
			JSONObject useCapitalPoolJo = (JSONObject)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company, payMerch);
			//当日单个商户的交易最大限额(资金池)
			Integer capitalPool = payConfigCompanyDO.getCapitalPool();
			if(useCapitalPoolJo == null || !dateChange) {
				//如果不存在记录  或者  日期发生了变化   则创建
				useCapitalPoolJo = new JSONObject();
				//设置起始交易金额为 0
				useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_CUR_MONEY, new BigDecimal(0));
				//设置起始目标金额为 资金池的10%
				useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_CUR_TARGET_MONEY, new BigDecimal(capitalPool).multiply(new BigDecimal(singlePollMoney)));
				//是否跳过当前支付公司
				useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_IF_SKIP, YesNoType.not.id());
				//记录到redis
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company,payMerch, useCapitalPoolJo);
				//直接选择当前通道
				payCfgComp = payConfigCompanyDO;
				
				break;
			}else {
				int ifSkip = useCapitalPoolJo.getIntValue(RedisConstants.COMPANY_MERCHANT_IF_SKIP);
				if(ifSkip == YesNoType.yes.id()) {
					//跳过这一条通道
					continue;
				}
				//获取 当日商户交易金额
				BigDecimal curMoney = useCapitalPoolJo.getBigDecimal(RedisConstants.COMPANY_MERCHANT_CUR_MONEY);
				//当日商户交易金额  + 当前订单金额
				curMoney = curMoney.add(order.getAmount());
				//获取 目标金额
				BigDecimal curTargetMoney = useCapitalPoolJo.getBigDecimal(RedisConstants.COMPANY_MERCHANT_CUR_TARGET_MONEY);
				if (curMoney.compareTo(curTargetMoney) == -1) {
					//当日商户交易金额 不满足目标金额,则选择该通道
					payCfgComp = payConfigCompanyDO;
					break;
				} else {
					//当日商户交易金额大于等于目标金额
					if(i == pccList.size()) {
						//如果当前通道为 分类下面最后一个通道 则对该类下面所有通道调整目标金额
						payCfgComp = null;
						for (PayConfigCompanyDO payConfigCompany : pccList) {
							company = payConfigCompany.getCompany();
							payMerch = payConfigCompany.getPayMerch();
							//获取商户 交易金额记录
							useCapitalPoolJo = (JSONObject)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company, payMerch);
							//获取商户 目标金额
							curTargetMoney = useCapitalPoolJo.getBigDecimal(RedisConstants.COMPANY_MERCHANT_CUR_TARGET_MONEY);
							
							if(curTargetMoney.compareTo(new BigDecimal(capitalPool)) >= 0) {
								//如果目标金额已经到达该支付公司最大限额  则跳过当前支付公司
								useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_IF_SKIP, YesNoType.yes.id());
							}else {
								if(payCfgComp == null && curTargetMoney.compareTo(new BigDecimal(capitalPool)) == -1) {
									//如果还没有选择通道  并且 当前通道目标金额 小于  该通道的最大限额(资金池)  
									//则选择该通道
									payCfgComp = payConfigCompany;
								}
								
								//调整目标金额 = 商户当日最大限额 * 10% + 当前目标金额
								curTargetMoney = new BigDecimal(capitalPool).multiply(new BigDecimal(singlePollMoney)).add(curTargetMoney);
								if(curTargetMoney.compareTo(new BigDecimal(capitalPool)) >= 0) {
									//如果调整后目标金额 >= 商户当日最大限额   则直接等于限额
									curTargetMoney = new BigDecimal(capitalPool);
								}
								//记录调整后的目标金额
								useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_CUR_TARGET_MONEY, curTargetMoney);
							}
							//保存在redis
							RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + company,payMerch, useCapitalPoolJo);
						}
					}
				}
			}
			i++;
		}
		return payCfgComp;
	}
	
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#notify(java.lang.String,
	 * java.lang.String, java.lang.String,
	 * javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notify(String merchNo, String orderNo, HttpServletRequest request, String requestBody) {
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		try {
			lock.lock();
			Order order = RedisUtil.getOrder(merchNo, orderNo);
			if (order != null &&(order.getLastLockTime() == null || DateUtil.getCurrentTimeInt() - order.getLastLockTime() > 20)) {
				R r = payBaseService.notify(order, request, requestBody);
				order.setMsg((String) r.get(Constant.result_msg));
				order.setLastLockTime(DateUtil.getCurrentTimeInt());
				RedisUtil.setOrder(order);
				if(OrderState.ing.id() != order.getOrderState() && OrderState.init.id() != order.getOrderState()) {
					lock.unlock();
					RedisMsg.orderNotifyMsg(merchNo, orderNo);
				}
				return r;
			}
			
		} finally {
			if(lock.isHeldByCurrentThread())
				lock.unlock();
		}
		return R.error("无效的结果");
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderNotifyMsg(java.lang.String)
	 */
	@Override
	public String orderNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getOrderLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("orderNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#eventOrderNotifyMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public String eventOrderNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventOrderLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("eventOrderNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	
	/**
	 * 同步 带回调地址通知
	 * @param merchNo
	 * @param orderNo
	 * @param notifyUrl
	 * @return
	 */
	@Override
	public String syncOrderNotifyMsg(String merchNo, String orderNo,String notifyUrl) {
		RLock lock = RedissonLockUtil.getEventOrderLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrder(merchNo,orderNo);
				if(order==null)
					order = payOrderDao.get(orderNo, merchNo);
				String result = orderNotify(order,notifyUrl);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("syncOrderNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	
	/**
	 * @Description 通知保存
	 * @param merchNo
	 * @param orderNo
	 * @return
	 */
	private String orderNotify(String merchNo, String orderNo) {
		Order order = RedisUtil.getOrder(merchNo,orderNo);
		if(order==null)
			order = payOrderDao.get(orderNo, merchNo);
		return orderNotify(order);
	}

	/**
	 * @Description 订单通知
	 * @param order
	 * @return
	 */
	private String orderNotify(Order order) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		logger.info("发送通知请求：{},{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(order.getNotifyUrl(),
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if((result.contains("ok") || result.contains("success")) && result.length() < 30) {
				result = Constant.result_msg_ok;
			}
			if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
				upateOrderNoticeState(order);
			}
		}
		return result;
	}
	
	/**
	 * @Description 订单通知
	 * @param order
	 * @return
	 */
	private String orderNotify(Order order,String notifyUrl) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		if(StringUtils.isBlank(notifyUrl))
			notifyUrl = order.getNotifyUrl();
		logger.info("发送通知请求：{},{},{},{}", notifyUrl,order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount().toString());
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(notifyUrl,
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if((result.contains("ok") || result.contains("success")) && result.length() < 30) {
				result = Constant.result_msg_ok;
			}
			if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
				upateOrderNoticeState(order);
			}
		}
		return result;
	}
	
	@Transactional
	private boolean upateOrderNoticeState(Order order) {
		order.setNoticeState(YesNoType.yes.id());
		int count = payOrderDao.updateNoticeState(order);
		if(count>0) {
			return true;
		}else {
			logger.info("修改支付订单通知状态失败，{}，{}",order.getMerchNo(),order.getOrderNo());
		}
		return false;
	}
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrder(merchNo,orderNo);
				if (order == null) {
					logger.info("支付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return;
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.succ.id() || orderState == OrderState.fail.id()
						|| orderState == OrderState.close.id()) {
					boolean saveFlag = false;
					if(OutChannel.jfDesc().containsKey(order.getOutChannel())){
						saveFlag = payQrService.saveQrOrderData(order);
					}else{
						saveFlag = this.saveOrderData(order);
					}
					
					if (saveFlag) {
						RedisUtil.removeOrder(merchNo,orderNo);
						logger.info("支付订单数据保存成功，{}，{}",merchNo,orderNo);
						lock.unlock();
						RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
						RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_not_pay_ord, merchNo, orderNo);
					}
				}else if(orderState == OrderState.init.id()) {
					saveOrder(order);
				}else {
					logger.info("支付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
				}
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}

		}else {
			logger.info("支付订单数据保存失败，未获取到锁，不能保存条件，{}，{}",merchNo,orderNo);
		}
	}

	@Transactional
	private boolean saveOrder(Order order) {
		if(payOrderDao.get(order.getOrderNo(), order.getMerchNo())== null) {
			order.setOrderState(OrderState.ing.id());
			int count = payOrderDao.save(order);
			if(count>0) {
				return true;
			}else {
				logger.info("初始化支付订单数据保存失败，{}，{}",order.getMerchNo(),order.getOrderNo());
			}
		}else {
			logger.info("初始化支付订单数据保存失败，数据库已存在该订单，{}，{}",order.getMerchNo(),order.getOrderNo());
		}
		return false;
	}
	
	/**
	 * @Description 保存订单数据
	 * @param order
	 */
	@Transactional
	private boolean saveOrderData(Order order) {
		// 商户信息
		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();
		//费率单位
		Integer costRateUnit = payCfgComp.getCostRateUnit();
		// 成本金额
		if(payCfgComp.getCostRate() != null){
			if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());
			}
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		
		// 商户金额
		BigDecimal jfRate = null;
		Integer jfUnit = null;
		
		Integer paymentMethod = merchant.getPaymentMethod();
		Map<String,Map<String,BigDecimal>> rate = merchant.getdZero().get(paymentMethod.toString());
		if(rate==null || rate.size() <= 0) {
			logger.info("未找到商户结算费率{},{},{}",merchant.getMerchantsName(),paymentMethod,rate);
		}
		Map<String,Object> rateMap = new HashMap<>(rate.get(order.getOutChannel()));
		jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
		
		order.setPaymentMethod(paymentMethod);
		/*if(merchant.getHandRate() != null && (jfRate=merchant.getHandRate().get(order.getOutChannel())) != null){
			jfRate = merchant.getHandRate().get(order.getOutChannel());
		}
		if(jfRate == null){
			jfRate = payCfgComp.getQhRate();
		}*/
		BigDecimal qhAmount = BigDecimal.ZERO;
		if(jfRate != null){
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				jfRate = jfRate.divide(new BigDecimal(100));
				qhAmount = ParamUtil.multBig(amount, jfRate);
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				qhAmount = jfRate;
			}
		}
		BigDecimal maxFee = payCfgComp.getMaxFee();
		BigDecimal minFee = payCfgComp.getMinFee();
		if(minFee != null && minFee.compareTo(qhAmount) == 1) {
			qhAmount = minFee;
		}else if(maxFee != null && maxFee.compareTo(qhAmount) == -1) {
			qhAmount = maxFee;
		}
		order.setQhAmount(qhAmount);
		// 代理信息
		Agent agent = agentService.get(merchant.getParentAgent());
		rate = agent.getdZero().get(paymentMethod.toString());
		if(rate==null || rate.size() <= 0) {
			logger.info("未找到代理结算费率{},{},{}",agent.getMerchantsName(),paymentMethod,rate);
		}
		
		rateMap = new HashMap<>(rate.get(order.getOutChannel()));
		jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
		/*if (ParamUtil.isNotEmpty(merchant.getFeeRate())) {
			feeRate = merchant.getFeeRate().get(order.getOutChannel());
		}*/
		
		BigDecimal agentAmount = BigDecimal.ZERO;
		if (jfRate != null) {
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				jfRate = jfRate.divide(new BigDecimal(100));
				agentAmount = amount.multiply(jfRate);//ParamUtil.multSmall(amount, jfRate);   不做舍去,否则在后续算手续费时可能给代理多算 如：654行
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				agentAmount = jfRate;
			}
		}
		
		if(agentAmount.compareTo(qhAmount) == 1) {
			agentAmount = qhAmount;
		}
		
		String parentAgentNumber = agent.getParentAgent();
		//上级代理
		if(agent.getLevel() == AgentLevel.two.id() ) {
			
			order.setSubAgentAmount(ParamUtil.subSmall(order.getQhAmount(), agentAmount));//qhAmount=3.51  agentAmount=2.6499   在644行：不舍去99等于0.8601  舍去99等于0.87
			
			Agent paramAgent = agentService.get(parentAgentNumber);
			rate = paramAgent.getdZero().get(paymentMethod.toString());
			if(rate==null || rate.size() <= 0) {
				logger.info("未找到代理结算费率{},{},{}",paramAgent.getMerchantsName(),paymentMethod,rate);
			}
			rateMap = new HashMap<>(rate.get(order.getOutChannel()));
			jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
			jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
			/*if (ParamUtil.isNotEmpty(merchant.getFeeRate())) {
				feeRate = merchant.getFeeRate().get(order.getOutChannel());
			}*/
			if (jfRate != null) {
				BigDecimal parentAgentAmount = BigDecimal.ZERO;
				if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
					jfRate = jfRate.divide(new BigDecimal(100));
					parentAgentAmount = amount.multiply(jfRate);//ParamUtil.multSmall(amount, jfRate);  不做舍去,否则在后续算手续费时可能给代理多算
				}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
					parentAgentAmount = jfRate;
				}
				
				if(parentAgentAmount.compareTo(agentAmount) == 1) {
					parentAgentAmount = agentAmount;
				}
				
				order.setAgentAmount(ParamUtil.subSmall(agentAmount, parentAgentAmount));
				agentAmount = parentAgentAmount;
			}
		}else {
			order.setAgentAmount(ParamUtil.subSmall(order.getQhAmount(), agentAmount));
		}
		
		
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
//		order.setClearState(ClearState.succ.id());
		payOrderDao.update(order);
		int orderState = order.getOrderState();
		if (orderState != OrderState.succ.id()) {
			return true;
		}
		
		// 增加商户余额以及流水
		RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),
				FeeType.merchIn.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchBalDao.save(rdMerchBal);
		//减少商户手续费
		rdMerchBal = payHandlerService.balForMerchSub(order, order.getQhAmount(),
				FeeType.merchHandFee.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchBalDao.save(rdMerchBal);

		RecordFoundAcctDO rdFoundAcct = null;
		// 增加代理余额以及流水
		if (agent.getLevel() == AgentLevel.two.id()) {
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getSubAgentAmount(),agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			
			// 增加上级代理余额以及流水
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getAgentAmount(),parentAgentNumber, FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			
		}else {
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getAgentAmount(),agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
		}
		BigDecimal subAgentAmount = order.getSubAgentAmount();
		subAgentAmount =  subAgentAmount == null?BigDecimal.ZERO:subAgentAmount;
		BigDecimal platMoney = order.getQhAmount().subtract(order.getCostAmount().add(order.getAgentAmount()).add(subAgentAmount));
		platMoney = platMoney.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : platMoney;
		// 增加平台资金账户余额以及流水
		rdFoundAcct = payHandlerService.balForPlatAdd(order,platMoney, 
				FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAcctDao.save(rdFoundAcct);
		
		// 增加第三方支付公司资金账户余额以及流水
		RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.balForPayMerchAdd(order,order.getAmount(), 
				FeeType.payMerchTrade.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchBalDao.save(rdPayMerchAcct);
		
		// 减去第三方支付公司资金账户余额以及流水    减手续费
		rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getCostAmount(), 
				FeeType.payMerchTradeHand.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchBalDao.save(rdPayMerchAcct);
		
		
		if(paymentMethod.equals(PaymentMethod.D0.id()) || (paymentMethod.equals(PaymentMethod.T0.id()) && DateUtil.ifWorkingDays())) {
			try {
//				dZeroSettlement(order, agent, parentAgentNumber, crtDate, ptAmount);
				List<Order> updateOrders = new ArrayList<Order>();
				//清算比例
				BigDecimal clearRatio = payCfgComp.getClearRatio();
				if(clearRatio != null && clearRatio.compareTo(BigDecimal.ZERO) == 1 && clearRatio.compareTo(BigDecimal.ONE) == -1) {
					order.setClearState(ClearState.ing.id());
					BigDecimal clearAmount = ParamUtil.multSmall(order.getAmount(), clearRatio);
					order.setClearAmount(clearAmount);
				}else {
					order.setClearState(ClearState.succ.id());
					order.setClearAmount(order.getAmount());
				}
				updateOrders.add(order);
				if(payHandlerService.updateClearStateBatch(payCfgComp.getCompany(), updateOrders) > 0){
					payHandlerService.availBalForOrderClearSucc(updateOrders);
				}
			} catch (Exception e) {
				logger.debug("D0清算异常(保存订单数据)");
				e.printStackTrace();
			}
		}
		
		//记录支付公司单个商户的当日交易金额
		payHandlerService.companyMerchCapitalPoolRecord(order);
		
		payHandlerService.merchLimitRecord(order);
		return true;
	}

	/**
	 * D0 清算
	 * @param order
	 * @param paymentMethod
	 * @param agent
	 * @param parentAgentNumber
	 * @param crtDate
	 * @param ptAmount
	 */
	@Transactional
	private void dZeroSettlement(Order order, Agent agent, String parentAgentNumber, int crtDate,
			BigDecimal ptAmount) {
		RecordMerchBalDO rdMerchBal = null;
		RecordFoundAcctDO rdFoundAcct = null;
		
		//D0结算,实时结算
		// 增加商户可用余额以及流水
		rdMerchBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),
				FeeType.merchIn.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchAvailBalDao.save(rdMerchBal);
		// 减少商户可用余额以及流水 手续费
		rdMerchBal = payHandlerService.availBalForMerchSub(order, order.getQhAmount(),
				FeeType.merchHandFee.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchAvailBalDao.save(rdMerchBal);
		
		if (agent.getLevel() == AgentLevel.two.id()) {
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getQhAmount().subtract(order.getSubAgentAmount()), agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			
			// 增加上级代理余额以及流水
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getSubAgentAmount().subtract(order.getAgentAmount()), parentAgentNumber, FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			ptAmount = order.getAgentAmount();
		}else {
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getQhAmount().subtract(order.getAgentAmount()), agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
		}
		ptAmount = order.getAgentAmount();
		// 增加平台资金账户可用余额以及流水
		rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
				ptAmount.subtract(order.getCostAmount()),
				FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAvailAcctDao.save(rdFoundAcct);
		
		// 增加第三方支付公司资金账户可用余额以及流水
		RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.availBalForPayMerchAdd(order,order.getAmount(), 
				FeeType.payMerchTrade.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchAvailBalDao.save(rdPayMerchAcct);
		
		// 减去第三方支付公司资金账户可用余额以及流水    减手续费
		rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getCostAmount(), 
				FeeType.payMerchTradeHand.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchAvailBalDao.save(rdPayMerchAcct);
		
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#query(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R query(Merchant merchant, JSONObject jo) {
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		if (ParamUtil.isEmpty(orderNo)) {
			return R.error("查询支付订单号为空！");
		}
 		RLock lock = RedissonLockUtil.getOrderLock(merchant.getMerchNo() + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrder(merchant.getMerchNo(), orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderDao.get(orderNo, merchant.getMerchNo());
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchant.getMerchNo() + "," + orderNo + "支付订单不存在！");
				}
				if(OrderState.close.id() != order.getOrderState()) {
					// 无支付成功结果去第三方查询
					if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
						R r = payBaseService.query(order);
						if (R.ifError(r)) {
							return r;
						}
						msg = (String) r.get(Constant.result_msg);
						order.setMsg(msg);
						RedisUtil.setOrder(order);
						if(OrderState.succ.id() == order.getOrderState()){
							Order dateOrder = payOrderDao.get(orderNo, order.getMerchNo());
							Integer orderState = dateOrder.getOrderState();
							if (orderState == OrderState.init.id() || orderState == OrderState.ing.id()) {
								//支付成功。 同步保存
								lock.unlock();
								RedisMsg.orderDataMsg(merchant.getMerchNo(),orderNo);
							}else {
								logger.info("订单接口查询，订单已存在，不发通知保存.{},{}",merchant.getMerchNo(),orderNo);
							}
						}
					}
				}else {
					if(!dataFlag) {
						lock.unlock();
						RedisMsg.orderDataMsg(order.getMerchNo(),orderNo);
					}
				}
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), String.valueOf(order.getRealAmount()));
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			return decryptAndSign(data, merchant.getPublicKey(), msg);
		} else {
			return R.error("查询过于繁忙，请稍后再试！");
		}
	}

	/**
	 * @Description 公钥加密，私钥签名
	 * @param data
	 * @return
	 */
	private R decryptAndSign(Map<String, ?> data, String publicKey) {
//		try {
			/*byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(data), publicKey);
			String sign = RSAUtil.sign(context, QhPayUtil.getQhPrivateKey());
			R r = R.ok().put("sign", sign).put("context", context);
			if(data.containsKey("merchNo"))
				r.put("merchNo", data.get("merchNo"));*/
			return decryptAndSign(data,publicKey,"");
		/*} catch (Exception e) {
			logger.error("返回数据 公钥加密，私钥签名 失败！");
		}
		return R.error("返回数据 公钥加密，私钥签名 失败！");*/
	}

	/**
	 * @Description 公钥加密，私钥签名
	 * @param data
	 * @return
	 */
	private R decryptAndSign(Map<String, ?> data, String publicKey, String msg) {
		try {
			logger.info("回调商户 返回明文数据:"+JSON.toJSONString(data));
			String merchNo = data.get("merchNo").toString();
			R r = R.ok();
			if(EncryptType.isMD5ByMId(merchNo)) {
				Merchant merchant = merchantService.get(merchNo);
				byte[] context = JSON.toJSONBytes(data);
				String sign = Md5Util.sign(new String(context,"UTF-8"), merchant.getPublicKey(), "UTF-8");
				r.put("sign", sign).put("context", context);
			}else {
				byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(data), publicKey);
				String sign = RSAUtil.sign(context, QhPayUtil.getQhPrivateKey());
				r.put("sign", sign).put("context", context);
			}
			r.put(Constant.result_msg, msg).put("merchNo", merchNo).put("orderNo", data.get("orderNo"));
			logger.info("回调商户 返回加密数据:"+JSON.toJSONString(r));
			return r;
		} catch (Exception e) {
			logger.error("返回数据 公钥加密，私钥签名 失败！");
		}
		return R.error("返回数据 公钥加密，私钥签名 失败！");
	}

	

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcp(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R orderAcp(Merchant merchant, JSONObject jo) {
		String merchNo = merchant.getMerchNo();
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				Order order = new Order();
				// 初始化订单信息
				String initResult = payHandlerService.initOrder(order, jo);
				order.setOrderType(OrderType.acp.id());
				if (ParamUtil.isNotEmpty(initResult)) {
					logger.error(initResult);
					return R.error(initResult);
				}
				//userId不能为商户号
				if(ParamUtil.isNotEmpty(order.getUserId()) && merchNo.equals(order.getUserId())){
					return R.error("userId不能与商户号一致");
				}
				if (RedisUtil.getOrderAcp(merchNo, orderNo) != null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}else if(payOrderAcpDao.get(orderNo, merchNo)!= null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}
				
				BigDecimal amount = order.getAmount();
				//计算出  商户代付  手续费费率
				Map<String,Object> paidMap = new HashMap<>(merchant.getPaid());
				BigDecimal jfRate = new BigDecimal(paidMap.get(PayConstants.PAYMENT_RATE).toString());
				Integer jfUnit = Integer.valueOf(paidMap.get(PayConstants.PAYMENT_UNIT).toString());
				if(jfRate != null){
					if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
						jfRate = jfRate.divide(new BigDecimal(100));
						order.setQhAmount(ParamUtil.multBig(amount, jfRate));
					}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
						order.setQhAmount(jfRate);
					}
				}
				
				amount = amount.add(order.getQhAmount());
				
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
				RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchAcpOut.id(),
						OrderType.acp.id(), ProfitLoss.loss.id());
				rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
				// 检查商户余额 -----》商户可用余额
				RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
				try {
					merchLock.lock();
					
					PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
					if (pab == null || pab.getAvailBal().compareTo(amount) < 0) {
						return R.error("商户号 " + merchNo + ",可用余额不足！");
					}
					
					R r = checkAcpCfgComp(order);
					if(R.ifError(r)){
						return r;
					}
					
					
					rdMerchAvailBal.setTranAmt(order.getAmount());
					payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
					
					//扣除商户代付手续费
					rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchAcpHandFee.id(),
							OrderType.acp.id(), ProfitLoss.loss.id());
					rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
					rdMerchAvailBal.setTranAmt(order.getQhAmount());
					payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
					RedisUtil.setMerchBal(pab);
					
					rdMerchAvailBalList.add(rdMerchAvailBal);
				} finally {
					merchLock.unlock();
				}
				

				PayAuditDO payAudit = PayService.initPayAudit(order, AuditType.order_acp.id());
				int count = 0;
				try {
					payAudit.setCrtTime(rdMerchAvailBal.getCrtDate());
					count = payAuditDao.save(payAudit);
				} catch (Exception e) {
					count = 0;
				}
				try {
					//保存可用余额流水
					rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
				} catch (Exception e) {
					logger.error("代付保存可用余额流水记录失败！{}，{}",merchNo,orderNo);
				}
				if (count == 1) {
					RedisUtil.setOrderAcp(order);
					//如果成功，商户余额减少
					try {
						rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getAmount(), FeeType.merchAcpOut.id(),	OrderType.acp.id()));
						rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getQhAmount(), FeeType.merchAcpHandFee.id(),	OrderType.acp.id()));
					} catch (Exception e) {
						logger.error("代付保存余额流水记录失败！{}，{}",merchNo,orderNo);
					}
					
					//自动审核通过
					String pollMoneyValue = RedisUtil.getConfigValue(CfgKeyConst.PAY_AUDIT_AUTO_ACP, ConfigParent.payAuditConfig.name());
					if(pollMoneyValue != null && YesNoType.yes.id() == Integer.parseInt(pollMoneyValue)) {
						//保存代付订单
						if(payAuditService.audit(orderNo,merchNo,AuditType.order_acp.id(),AuditResult.pass.id(),null)>0){
							lock.unlock();
							RedisMsg.orderAcpMsg(merchNo, orderNo);
						}else {
							logger.info("代付自动审核失败：{},{}",merchNo,orderNo);
						}
					}
					
					Map<String, String> data = PayService.initRspData(order);
					data.put(OrderParamKey.orderState.name(), String.valueOf(OrderState.init.id()));
					return decryptAndSign(data, merchant.getPublicKey(), "代付订单已受理!");
				} else {// 出错了返还可用余额
					rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(), FeeType.merchAcpFail.id(), OrderType.acp.id());
					rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
					rdMerchAvailBalDao.save(rdMerchAvailBal);
					
					//返回手续费
					rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(), FeeType.merchAcpHandFee.id(), OrderType.acp.id());
					rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
					rdMerchAvailBalDao.save(rdMerchAvailBal);
					
					return R.error("代付异常！");
				}
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		} else {
			return R.error(merchNo + "," + orderNo + "下单失败！");
		}
	}

	/*
	 * (非 Javadoc) Description: 审核未通过 可用余额/余额返还
	 * 
	 * @see
	 * com.qh.pay.service.PayService#orderAcpNopassDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderAcpNopassDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order != null) {
					//提现与代付区别
					Integer orderType = order.getOrderType();
					Integer feeType = FeeType.merchAcpFail.id();
					if(orderType == null){
						orderType = OrderType.acp.id();
					}
					if(orderType == OrderType.withdraw.id()){
						feeType = FeeType.withdrawFail.id();
					}
					Integer userType = order.getUserType();
					if(userType != null && (UserType.agent.id() == userType || UserType.subAgent.id() == userType)) {
						List<RecordFoundAcctDO> rdFoundAvailBalList = new ArrayList<RecordFoundAcctDO>();
						List<RecordFoundAcctDO> rdFoundBalList = new ArrayList<RecordFoundAcctDO>();
						int crtTime = DateUtil.getCurrentTimeInt();
						RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),order.getMerchNo(), feeType, orderType);
						rdAgentAvailBal.setCrtDate(crtTime);
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(), orderType);
						rdAgentAvailBal.setCrtDate(crtTime);
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						RecordFoundAcctDO rdAgentBal = payHandlerService.balForAgentAdd(order, order.getAmount(),order.getMerchNo(), feeType,orderType);
						rdAgentBal.setCrtDate(crtTime);
						rdFoundBalList.add(rdAgentBal);
						rdAgentBal = payHandlerService.balForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(),	orderType);
						rdAgentBal.setCrtDate(crtTime);
						rdFoundBalList.add(rdAgentBal);
						
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						
						rdFoundAvailAcctDao.saveBatch(rdFoundAvailBalList);
						rdFoundAcctDao.save(rdFoundBalList.get(0));
						rdFoundAcctDao.save(rdFoundBalList.get(1));
						
						
					}else {
						List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
						List<RecordMerchBalDO> rdMerchBalList = new ArrayList<RecordMerchBalDO>();
						//返还可用余额
						RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),feeType,orderType);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),orderType);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						//返还余额
						RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),feeType,orderType);
						rdMerchBalList.add(rdMerchBal);
						rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),orderType);
						rdMerchBalList.add(rdMerchBal);
						
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						
						rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
						rdMerchBalDao.save(rdMerchBalList.get(0));
						rdMerchBalDao.save(rdMerchBalList.get(1));
					}
					
					//如果是提现 不需要发送
					if(orderType != OrderType.withdraw.id()){
						logger.error("发送代付审核不通过请求：{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo());
						RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcp(java.lang.String,java.lang.String)
	 */
	@Override
	public R orderAcp(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if(order == null){
					return R.error("代付订单不存在");
				}
				R r = payBaseService.orderAcp(order);
				order.setMsg((String) r.get(Constant.result_msg));
				if(R.ifError(r)) {
					order.setOrderState(OrderState.fail.id());
				}
				RedisUtil.setOrderAcp(order);
				
				payOrderAcpDao.save(order);
				
				lock.unlock();
				//如果成功，并且订单状态成功，并且是实时返回结果则直接发起回调
				if(R.ifSucc(r) && Integer.valueOf(OrderState.succ.id()).equals(order.getOrderState()) && 
						Integer.valueOf(YesNoType.yes.id()).equals(r.get(PayConstants.acp_real_time))){
					RedisMsg.orderAcpNotifyMsg(merchNo,orderNo);
				}
				if(Integer.valueOf(OrderState.fail.id()).equals(order.getOrderState())) {
					r = R.error(order.getMsg());
				}
				return r;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("orderAcp，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcpNotifyMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public String orderAcpNotifyMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderAcpNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("orderAcpNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#eventOrderAcpNotifyMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public String eventOrderAcpNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderAcpNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("eventOrderAcpNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	/**
	 * @Description 代付订单通知发起
	 * @param merchNo
	 * @param orderNo
	 * @return
	 */
	private String orderAcpNotify(String merchNo, String orderNo) {
		Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
		if(order==null)
			return null;
		if(Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
			return Constant.result_msg_ok;
		}
		return orderAcpNotify(order);
	}


	/**
	 * 代付同步 带回调地址通知
	 * @param merchNo
	 * @param orderNo
	 * @param notifyUrl
	 * @return
	 */
	@Override
	public String syncOrderAcpNotifyMsg(String merchNo, String orderNo,String notifyUrl) {
		RLock lock = RedissonLockUtil.getEventOrderAcpLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if(order==null)
					order = payOrderAcpDao.get(orderNo, merchNo);
				String result = orderAcpNotify(order,notifyUrl);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("syncOrderAcpNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	
	/**
	 * @Description 代付通知
	 * @param order
	 * @return
	 */
	private String orderAcpNotify(Order order,String notifyUrl) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		if(StringUtils.isBlank(notifyUrl))
			notifyUrl = order.getNotifyUrl();
		logger.info("发送代付通知请求：{},{},{},{}", notifyUrl,order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//result = RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo());
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			Merchant merchant = merchantService.get(order.getMerchNo());
			
			result = RequestUtils.doPostJson(notifyUrl,
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if((result.contains("ok") || result.contains("success")) && result.length() < 30) {
				result = Constant.result_msg_ok;
			}
			
			if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
				upateOrderAcpNoticeState(order);
			}
		}
		return result;
	}

	/**
	 * @Description 代付通知
	 * @param order
	 * @return
	 */
	private String orderAcpNotify(Order order) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		logger.info("发送代付通知请求：{},{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//result = RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo());
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(order.getNotifyUrl(),
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if((result.contains("ok") || result.contains("success")) && result.length() < 30) {
				result = Constant.result_msg_ok;
			}
			
			if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
				upateOrderAcpNoticeState(order);
			}
		}
		return result;
	}
	
	@Transactional
	private boolean upateOrderAcpNoticeState(Order order) {
		order.setNoticeState(YesNoType.yes.id());
		int count = payOrderAcpDao.updateNoticeState(order);
		if(count>0) {
			return true;
		}else {
			logger.info("修改代付订单通知状态失败，{}，{}",order.getMerchNo(),order.getOrderNo());
		}
		return false;
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcpDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderAcpDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order == null) {
					logger.info("代付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return;
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.succ.id() || orderState == OrderState.fail.id()
						|| orderState == OrderState.close.id()) {
					Integer orderType = order.getOrderType();
					order.setClearState(ClearState.succ.id());
					if(OrderType.withdraw.id() == orderType) {
						if (this.saveOrderWithdrawData(order)) {
							RedisUtil.removeOrderAcp(merchNo,orderNo);
							logger.info("提现订单数据保存成功，{}，{}",merchNo,orderNo);
							lock.unlock();
							RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo);
						}
					}else {
						if (this.saveOrderAcpData(order)) {
							RedisUtil.removeOrderAcp(merchNo,orderNo);
							logger.info("代付订单数据保存成功，{}，{}",merchNo,orderNo);
							lock.unlock();
							RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo);
						}
					}
				/*} else if (orderState == OrderState.init.id()) {
					saveOrderAcp(order);*/
				}else{
					logger.info("代付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
				}
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("orderAcpDataMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}

	}

	@Transactional
	private boolean saveOrderAcp(Order order) {
		if(payOrderAcpDao.get(order.getOrderNo(), order.getMerchNo())== null) {
			int count = payOrderAcpDao.save(order);
			if(count>0) {
				return true;
			}else {
				logger.info("初始化代付订单数据保存失败，{}，{}",order.getMerchNo(),order.getOrderNo());
			}
		}else {
			logger.info("初始化代付订单数据保存失败，数据库已存在该订单，{}，{}",order.getMerchNo(),order.getOrderNo());
		}
		return false;
	}
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#notifyAcp(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notifyAcp(String merchNo, String orderNo, HttpServletRequest request, String requestBody) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		try {
			lock.lock();
			Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
			if (order != null &&(order.getLastLockTime() == null || DateUtil.getCurrentTimeInt() - order.getLastLockTime() > 20)) {
				R r = payBaseService.notifyAcp(order, request, requestBody);
				order.setMsg((String) r.get(Constant.result_msg));
				RedisUtil.setOrderAcp(order);
				if(OrderState.ing.id() != order.getOrderState() && OrderState.init.id() != order.getOrderState()) {
					lock.unlock();
					RedisMsg.orderAcpNotifyMsg(merchNo, orderNo);
				}
				return r;
			}
		} finally {
			lock.unlock();
		}
		return R.error("无效的结果");
	}

	
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#acpQuery(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R acpQuery(Merchant merchant, JSONObject jo) {
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		if (ParamUtil.isEmpty(orderNo)) {
			return R.error("查询代付订单号为空！");
		}
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchant.getMerchNo() + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			Integer orderState = null;
			String msg = null;
			try {
				order = RedisUtil.getOrderAcp(merchant.getMerchNo(), orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderAcpDao.get(orderNo, merchant.getMerchNo());
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchant.getMerchNo() + "," + orderNo + "代付订单不存在！");
				}
				orderState = order.getOrderState();
				// 无支付结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r = payBaseService.acpQuery(order);
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					orderState = order.getOrderState();
					order.setMsg(msg);
					RedisUtil.setOrderAcp(order);
					
					if(OrderState.succ.id() == order.getOrderState()){
						Order dateOrder = payOrderAcpDao.get(orderNo, order.getMerchNo());
						Integer orderStateA = dateOrder.getOrderState();
						if (orderStateA == OrderState.init.id() || orderStateA == OrderState.ing.id()) {
							lock.unlock();
							RedisMsg.orderAcpDataMsg(merchant.getMerchNo(),orderNo);
						}else {
							logger.info("代付接口查询，订单已存在，不发通知保存.{},{}",merchant.getMerchNo(),orderNo);
						}
					}
				}
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(orderState));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount().toString());
			return decryptAndSign(data, merchant.getPublicKey(), msg);
		} else {
			return R.error("查询过于繁忙，请稍后再试！");
		}
	}

	/**
	 * @Description 保存提现订单数据
	 * @param order
	 */
	@Transactional
	private boolean saveOrderWithdrawData(Order order){
		
		Integer orderState = order.getOrderState();
		// 商户信息
//		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();
		
		// 成本金额
		if(payCfgComp != null && payCfgComp.getCostRate() != null){
			//费率单位
			Integer costRateUnit = payCfgComp.getCostRateUnit();
			if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());
			}
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
		payOrderAcpDao.update(order);
		
		Integer userType = order.getUserType();
		if(OrderState.succ.id() == orderState){
			
			RecordFoundAcctDO rdFoundAcct = null;
			
			BigDecimal dfAmount = order.getQhAmount();
			if(UserType.user.id() != userType){
				// 增加平台资金账户可用余额以及流水
				rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
						dfAmount.subtract(order.getCostAmount()),
						FeeType.platWithdrawIn.id(),OrderType.withdraw.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				// 增加平台资金账户余额以及流水
				rdFoundAcct = payHandlerService.balForPlatAdd(order,dfAmount.subtract(order.getCostAmount()),
						FeeType.platWithdrawIn.id(),OrderType.withdraw.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
			}
			
			// 减去第三方支付公司资金账户可用余额以及流水
			RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getAmount(), 
					FeeType.payMerchAcp.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户可用余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getCostAmount(), 
					FeeType.payMerchAcpHand.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
		
			// 减去第三方支付公司资金账户余额以及流水
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getAmount(), 
					FeeType.payMerchAcp.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getCostAmount(), 
					FeeType.payMerchAcpHand.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			return true;
		}else{
			
			if(UserType.merch.id() == userType){
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<>();
				//返还可用余额
				RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),FeeType.withdrawFail.id(),OrderType.withdraw.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBalList.add(rdMerchAvailBal);
				
				rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),OrderType.withdraw.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBalList.add(rdMerchAvailBal);
				
				rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
				
				//返还余额
				RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),FeeType.withdrawFail.id(),OrderType.withdraw.id());
				rdMerchBal.setCrtDate(crtDate);
				rdMerchBalDao.save(rdMerchBal);
				
				rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),OrderType.withdraw.id());
				rdMerchBal.setCrtDate(crtDate);
				rdMerchBalDao.save(rdMerchBal);
				
			} else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
				
				RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),order.getMerchNo(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);
				
				rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);
				
				rdFoundAcctDao.save(payHandlerService.balForAgentAdd(order, order.getAmount(),order.getMerchNo(), FeeType.withdrawFail.id(),	OrderType.withdraw.id()));
				rdFoundAcctDao.save(payHandlerService.balForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(),	OrderType.withdraw.id()));

			} else if(UserType.user.id() == userType){
				RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForPlatAdd(order, order.getAmount(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);
				
				rdAgentAvailBal = payHandlerService.availBalForPlatAdd(order, order.getQhAmount(), FeeType.platWithDrawHandFee.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);
				
				rdFoundAcctDao.save(payHandlerService.balForPlatAdd(order, order.getAmount(),FeeType.withdrawFail.id(),	OrderType.withdraw.id()));
				rdFoundAcctDao.save(payHandlerService.balForPlatAdd(order, order.getQhAmount(), FeeType.platWithDrawHandFee.id(),	OrderType.withdraw.id()));

			}
			return true;
		}
		
	}
	
	/**
	 * @Description 保存代付订单数据
	 * @param order
	 */
	@Transactional
	private boolean saveOrderAcpData(Order order) {
		Integer orderState = order.getOrderState();
		// 商户信息
		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();
		//费率单位
		Integer costRateUnit = payCfgComp.getCostRateUnit();
		// 成本金额
		if(payCfgComp.getCostRate() != null){
			if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());
			}
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		
		// 商户金额
		Integer jfUnit = null;
		// 代理金额
		BigDecimal feeRate = null;
		Agent agent = agentService.get(merchant.getParentAgent());
		
		BigDecimal agentAmount = BigDecimal.ZERO;
		
		Map<String,Object> paidAcpMap = new HashMap<>(agent.getPaid());
		feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
		if (feeRate != null) {
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				feeRate = feeRate.divide(new BigDecimal(100));
				agentAmount = ParamUtil.multSmall(amount, feeRate);
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				agentAmount = feeRate;
			}
		}
		//上级代理
		String parentAgentNumber = agent.getParentAgent();
		if(agent.getLevel() ==AgentLevel.two.id() ) {
			order.setSubAgentAmount(order.getQhAmount().subtract(agentAmount));
			
			feeRate = null;
			Agent paramAgent = agentService.get(parentAgentNumber);
			paidAcpMap = new HashMap<>(paramAgent.getPaid());
			feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
			jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
			if (feeRate != null) {
				BigDecimal parentAgentAmount = BigDecimal.ZERO;
				if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
					feeRate = feeRate.divide(new BigDecimal(100));
					parentAgentAmount = ParamUtil.multSmall(amount, feeRate);
				}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
					parentAgentAmount = feeRate;
				}
				order.setAgentAmount(agentAmount.subtract(parentAgentAmount));
				agentAmount = parentAgentAmount;
			}
		}else {
			order.setAgentAmount(order.getQhAmount().subtract(agentAmount));
		}
		
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
		payOrderAcpDao.update(order);
		if(OrderState.succ.id() == orderState){
			
			RecordFoundAcctDO rdFoundAcct = null;
			// 增加代理可用/余额以及流水
			if(agent.getLevel() ==AgentLevel.two.id() ) {
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getSubAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getSubAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
				
				//上级代理
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getAgentAmount(), parentAgentNumber, FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getAgentAmount(), parentAgentNumber, FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
			}else {
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
			}
			
			// 增加平台资金账户可用余额以及流水
			rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
					agentAmount.subtract(order.getCostAmount()),
					FeeType.platAcpIn.id(),OrderType.acp.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			
			// 增加平台资金账户余额以及流水
			rdFoundAcct = payHandlerService.balForPlatAdd(order,agentAmount.subtract(order.getCostAmount()),
					FeeType.platAcpIn.id(),OrderType.acp.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			
			// 减去第三方支付公司资金账户余额以及流水
			RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getAmount(), 
					FeeType.payMerchAcp.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getCostAmount(), 
					FeeType.payMerchAcpHand.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户可用余额以及流水
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getAmount(), 
					FeeType.payMerchAcp.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户可用余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getCostAmount(), 
					FeeType.payMerchAcpHand.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
			return true;
		}else{
			
			List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<>();
			//返还可用余额
			RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),FeeType.merchAcpFail.id(),OrderType.acp.id());
			rdMerchAvailBal.setCrtDate(crtDate);
			rdMerchAvailBalList.add(rdMerchAvailBal);
			
			rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchAcpHandFee.id(),OrderType.acp.id());
			rdMerchAvailBal.setCrtDate(crtDate);
			rdMerchAvailBalList.add(rdMerchAvailBal);
			
			//返还余额
			RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),FeeType.merchAcpFail.id(),OrderType.acp.id());
			rdMerchBal.setCrtDate(crtDate);
			rdMerchBalDao.save(rdMerchBal);
			
			rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchAcpHandFee.id(),OrderType.acp.id());
			rdMerchBal.setCrtDate(crtDate);
			rdMerchBalDao.save(rdMerchBal);
			
			rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
			
			return true;
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#chargeDataMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public void chargeDataMsg(String merchNo, String orderNo) {
		payQrService.chargeDataMsg(merchNo,orderNo);
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#syncOrder(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R syncOrder(String merchNo, String orderNo, String businessNo) {
		/*
		if(merchant == null){
			return R.error("商户不存在 " + merchNo);
		}*/
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrder(merchNo, orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderDao.get(orderNo, merchNo);
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchNo + "," + orderNo + "支付订单不存在！");
				}
				/*if(OrderState.succ.id() == order.getOrderState()){
					return R.error(merchNo + "," + orderNo + "订单已同步！");
				}*/
				if(OrderState.close.id() != order.getOrderState()) {
					// 无支付成功结果去第三方查询
					if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
						R r;
						if(OutChannel.jfDesc().containsKey(order.getOutChannel())){
							Merchant merchant = merchantService.get(merchNo);
							r = payQrService.syncOrder(merchant,order,businessNo);
						}else {
							r = payBaseService.query(order);
						}
						if (R.ifError(r)) {
							return r;
						}
						msg = (String) r.get(Constant.result_msg);
						order.setMsg(msg);
						RedisUtil.setOrder(order);
					}
					
					if(!dataFlag && OrderState.succ.id() == order.getOrderState()){
						String result = orderNotify(order,businessNo);
						Order dateOrder = payOrderDao.get(orderNo, merchNo);
						Integer orderState = dateOrder.getOrderState();
						if (orderState == OrderState.init.id() || orderState == OrderState.ing.id()) {
							if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
								/*lock.unlock();
								RedisMsg.orderDataMsg(merchNo,orderNo);*/
							}else{
								/*if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
									lock.unlock();
									RedisMsg.orderDataMsg(merchNo,orderNo);
								}*/
								RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
							}
							lock.unlock();
							RedisMsg.orderDataMsg(merchNo,orderNo);
						}else {
							logger.info("手动同步，订单已存在，不发通知保存.{},{},通知结果：{}",merchNo,orderNo,result);
						}
					}
				}else {
					if(!dataFlag) {
						lock.unlock();
						RedisMsg.orderDataMsg(merchNo,orderNo);
					}
				}
				return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		} else {
			return R.error("同步过于繁忙，请稍后再试！");
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#syncOrderAcp(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R syncOrderAcp(String merchNo, String orderNo, String businessNo) {
		if(!merchNo.equals("admin")) {
			Merchant merchant = merchantService.get(merchNo);
			if(merchant == null){
				return R.error("商户不存在 " + merchNo);
			}
		}
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrderAcp(merchNo, orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderAcpDao.get(orderNo, merchNo);
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchNo + "," + orderNo + "支付订单不存在！");
				}
				// 无支付成功结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r = payBaseService.acpQuery(order);
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					order.setMsg(msg);
					RedisUtil.setOrderAcp(order);
				}
				//提现类型的订单直接 手动同步返回
				if(Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
					lock.unlock();
					RedisMsg.orderAcpDataMsg(merchNo,orderNo);
					return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
				}
				if(!dataFlag && OrderState.succ.id() == order.getOrderState()){
					String result = orderAcpNotify(order,businessNo);
					Order dateOrder = payOrderAcpDao.get(orderNo, merchNo);
					Integer orderState = dateOrder.getOrderState();
					if (orderState == OrderState.init.id() || orderState == OrderState.ing.id()) {
						if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
							/*lock.unlock();
							RedisMsg.orderAcpDataMsg(merchNo,orderNo);*/
						}else{
							/*if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
								lock.unlock();
								RedisMsg.orderAcpDataMsg(merchNo,orderNo);
							}*/
							RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
						}
						lock.unlock();
						RedisMsg.orderAcpDataMsg(merchNo,orderNo);
					}else {
						logger.info("手动同步，订单已存在，不发通知保存.{},{},通知结果：{}",merchNo,orderNo,result);
					}
				}
				return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		} else {
			return R.error("同步过于繁忙，请稍后再试！");
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#withdraw(com.qh.pay.api.Order)
	 */
	@Override
	public R withdraw(Order order) {
		String merchNo = order.getMerchNo();
		String orderNo = ParamUtil.getOrderId();
		order.setOrderNo(orderNo);
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				if(RedisUtil.getOrderAcp(merchNo, orderNo) != null){
					return R.error("系统繁忙，请稍后再试");
				}
				// 检查提现订单信息
				String checkResult = payHandlerService.checkUserWithDrawOrder(order);
				order.setOrderType(OrderType.withdraw.id());
				if (ParamUtil.isNotEmpty(checkResult)) {
					logger.error(checkResult);
					return R.error(checkResult);
				}
				/*R r = checkAcpCfgComp(order);
				if(R.ifError(r)){
					return r;
				}*/
				//保存银联信息
				userBankService.save(order.getMobile(),merchNo, order);
				
				order.setUserId(merchNo);
				Integer userType = order.getUserType();
				BigDecimal amount = order.getAmount();
				PayAcctBal pab = null;
				Integer crtDate = null;
				Agent agent = null;
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
				List<RecordFoundAcctDO> rdFoundAvailBalList = new ArrayList<RecordFoundAcctDO>();
				if(UserType.merch.id() == userType){
					
					// 检查商户余额 -----》商户可用余额
					RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
					try {
						merchLock.lock();
						// 商户信息
						Merchant merchant = merchantService.get(merchNo);
						//计算出  商户代付  手续费费率
						Map<String,Object> paidMap = new HashMap<>(merchant.getPaid());
						BigDecimal jfRate = new BigDecimal(paidMap.get(PayConstants.PAYMENT_RATE).toString());
						Integer jfUnit = Integer.valueOf(paidMap.get(PayConstants.PAYMENT_UNIT).toString());
						if(jfRate != null){
							if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
								jfRate = jfRate.divide(new BigDecimal(100));
								order.setQhAmount(ParamUtil.multBig(amount, jfRate));
							}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
								order.setQhAmount(jfRate);
							}
						}
						RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.withdrawOut.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						pab = RedisUtil.getMerchBal(merchNo);
						if (pab == null || pab.getAvailBal().compareTo(order.getAmount()) < 0) {
							return R.error("商户号 " + merchNo + ",可用余额不足！");
						}
						rdMerchAvailBal.setTranAmt(order.getAmount().subtract(order.getQhAmount()));
						if(rdMerchAvailBal.getTranAmt().compareTo(BigDecimal.ZERO) < 0) {
							return R.error("商户号 " + merchNo + ",提现金额不能小于：" + order.getQhAmount());
						}
						
						payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
						RedisUtil.setMerchBal(pab);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						
						//扣除商户提现手续费
						rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchWithDrawHandFee.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						rdMerchAvailBal.setTranAmt(order.getQhAmount());
						payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
						RedisUtil.setMerchBal(pab);
						
						rdMerchAvailBalList.add(rdMerchAvailBal);
						
						crtDate = rdMerchAvailBal.getCrtDate();
					} finally {
						merchLock.unlock();
					}
				}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
					RLock merchLock = RedissonLockUtil.getBalFoundAcctLock(merchNo);
					try {
						merchLock.lock();
						agent = agentService.get(merchNo);
						Map<String,Object> paidAcpMap = new HashMap<>(agent.getPaid());
						BigDecimal feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
						Integer jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
						if (feeRate != null) {
							if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
								feeRate = feeRate.divide(new BigDecimal(100));
								order.setQhAmount(ParamUtil.multSmall(amount, feeRate));
							}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
								order.setQhAmount(feeRate);
							}
						}
						
						RecordFoundAcctDO rdAgentAvailBal = PayService.initRdFoundAcct(order, FeeType.withdrawOut.id(), OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdAgentAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						pab = RedisUtil.getAgentBal(agent.getAgentNumber());
						if (pab == null || pab.getAvailBal().compareTo(order.getAmount()) < 0) {
							return R.error("代理 " + merchNo + ",可用余额不足！");
						}
						rdAgentAvailBal.setTranAmt(order.getAmount().subtract(order.getQhAmount()));
						if(rdAgentAvailBal.getTranAmt().compareTo(BigDecimal.ZERO) < 0) {
							return R.error("商户号 " + merchNo + ",提现金额不能小于：" + order.getQhAmount());
						}
						rdAgentAvailBal.setUsername(agent.getAgentNumber());
						
						payHandlerService.availBalForAgentSub(order, rdAgentAvailBal, pab);
						RedisUtil.setAgentBal(pab);;
						
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						//扣除商户提现手续费
						rdAgentAvailBal = PayService.initRdFoundAcct(order, FeeType.agentWithDrawHandFee.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdAgentAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						rdAgentAvailBal.setTranAmt(order.getQhAmount());
						payHandlerService.availBalForAgentSub(order, rdAgentAvailBal, pab);
						RedisUtil.setAgentBal(pab);;
						
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						crtDate = rdAgentAvailBal.getCrtDate();
					} finally {
						merchLock.unlock();
					}
				}else if(UserType.user.id() == userType) {
					RLock merchLock = RedissonLockUtil.getBalFoundAcctLock();
					try {
						merchLock.lock();
						
						PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
								order.getOutChannel());
						order.setCallbackDomain(payCfgComp.getCallbackDomain());
						// 成本金额
						if(payCfgComp != null && payCfgComp.getCostRate() != null){
							//费率单位
							Integer costRateUnit = payCfgComp.getCostRateUnit();
							if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
								order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
							} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
								order.setCostAmount(payCfgComp.getCostRate());
							}
						}else{
							order.setCostAmount(BigDecimal.ZERO);
						}
						order.setQhAmount(order.getCostAmount());
						
						RecordFoundAcctDO rdFoundAcct = PayService.initRdFoundAcct(order, FeeType.withdrawOut.id(), OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdFoundAcct.setCrtDate(DateUtil.getCurrentTimeInt());
						
						pab = RedisUtil.getPayFoundBal();
						if (pab == null || pab.getAvailBal().compareTo(order.getAmount()) < 0) {
							return R.error("平台可用余额不足！");
						}
						rdFoundAcct.setTranAmt(order.getAmount().subtract(order.getQhAmount()));
						if(rdFoundAcct.getTranAmt().compareTo(BigDecimal.ZERO) < 0) {
							return R.error("平台提现金额不能小于：" + order.getQhAmount());
						}
						rdFoundAcct.setUsername(order.getOrderNo());
						
						payHandlerService.availBalForPlatSub(order, rdFoundAcct, pab);
						RedisUtil.setPayFoundBal(pab);
						
						rdFoundAvailBalList.add(rdFoundAcct);
						
						//扣除商户提现手续费
						rdFoundAcct = PayService.initRdFoundAcct(order, FeeType.platWithDrawHandFee.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdFoundAcct.setCrtDate(DateUtil.getCurrentTimeInt());
						rdFoundAcct.setTranAmt(order.getQhAmount());
						payHandlerService.availBalForPlatSub(order, rdFoundAcct, pab);
						RedisUtil.setPayFoundBal(pab);;
						
						rdFoundAvailBalList.add(rdFoundAcct);
						
						crtDate = rdFoundAcct.getCrtDate();
					} finally {
						merchLock.unlock();
					}
				}
				order.setAmount(order.getAmount().subtract(order.getQhAmount()));
				PayAuditDO payAudit = PayService.initPayAudit(order, AuditType.order_withdraw.id());
				int count = 0;
				try {
					payAudit.setCrtTime(crtDate);
					count = payAuditDao.save(payAudit);
				} catch (Exception e) {
					count = 0;
				}
				try {
					if(rdMerchAvailBalList.size() > 0 ) {
						//保存商户可用余额流水
						rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
					}
					if(rdFoundAvailBalList.size() > 0 ) {
						//保存代理可用余额流水
						rdFoundAvailAcctDao.saveBatch(rdFoundAvailBalList);
					}
					
				} catch (Exception e) {
					logger.error("提现保存可用余额流水记录失败！{}，{}",merchNo,orderNo);
				}
				if (count == 1) {
					RedisUtil.setOrderAcp(order);
					//如果成功，商户余额减少
					try {
						if(UserType.merch.id() == userType){
							rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getAmount(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
							rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getQhAmount(), FeeType.merchWithDrawHandFee.id(),	OrderType.withdraw.id()));
						}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
							rdFoundAcctDao.save(payHandlerService.balForAgentSub(order, order.getAmount(),agent.getAgentNumber(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
							rdFoundAcctDao.save(payHandlerService.balForAgentSub(order, order.getQhAmount(),agent.getAgentNumber(), FeeType.agentWithDrawHandFee.id(),	OrderType.withdraw.id()));
						}else if(UserType.user.id() == userType){
							rdFoundAcctDao.save(payHandlerService.balForPlatSub(order, order.getAmount(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
							rdFoundAcctDao.save(payHandlerService.balForPlatSub(order, order.getQhAmount(), FeeType.platWithDrawHandFee.id(),	OrderType.withdraw.id()));
							
							if(payAuditService.audit(orderNo,merchNo,AuditType.order_withdraw.id(),AuditResult.pass.id(),null)>0){
								lock.unlock();
								RedisMsg.orderAcpMsg(merchNo, orderNo);
							}
						}
					} catch (Exception e) {
						logger.error("代付保存余额流水记录失败！{}，{}",merchNo,orderNo);
					}
					Map<String, String> data = PayService.initRspData(order);
					data.put(OrderParamKey.orderState.name(), String.valueOf(OrderState.init.id()));
					return R.okData(data).put(Constant.result_msg, "提现请求已受理!");
				} else {// 出错了返还可用余额
					if(UserType.merch.id() == userType){
						RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
						rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
						rdMerchAvailBalDao.save(rdMerchAvailBal);
						
						rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(), FeeType.merchWithDrawHandFee.id(), OrderType.withdraw.id());
						rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
						rdMerchAvailBalDao.save(rdMerchAvailBal);
					}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
						RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),agent.getAgentNumber(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
						
						rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),agent.getAgentNumber(), FeeType.agentWithDrawHandFee.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
					}else if(UserType.user.id() == userType){
						RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForPlatAdd(order, order.getAmount(),FeeType.withdrawFail.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
						
						rdAgentAvailBal = payHandlerService.availBalForPlatAdd(order, order.getQhAmount(),FeeType.agentWithDrawHandFee.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
					}
					
					return R.error("代付异常！");
				}
			} finally {
				lock.unlock();
			}
		} else {
			return R.error("系统繁忙，请稍后再试");
		}
	}


	/**
	 * @Description 
	 * @param order
	 */
	private R checkAcpCfgComp(Order order) {
		R r = checkCfgCompDF(order);
		if(R.ifError(r)){
			return r;
		}
		//如果满足需要行号的支付公司则需要传递银联行号参数
		if(Integer.valueOf(YesNoType.yes.id()).equals(PayCompany.companyUnionPay(order.getPayCompany())) &&(
				ParamUtil.isEmpty(order.getUnionpayNo()) || ParamUtil.isEmpty(order.getBankBranch()))){
			return R.error("未找到银联行号信息参数");
		}
		return R.ok();
	}
	
	/**
	 * @Description 代付通道 选择
	 * @param order
	 * @return
	 */
	private R checkCfgCompDF(Order order) {
		
		String payCompany = "";
		String payMerch = "";
		String callbackDomain = "";
		Merchant merchant = merchantService.get(order.getMerchNo());
		Map<String,String> paidChannelMap = merchant.getPaidChannel();
		//如果为商户指定了通道，则直接使用指定通道
		payCompany = paidChannelMap.get("payCompany");
		payMerch = paidChannelMap.get("payMerch");
		if(StringUtils.isNotBlank(payMerch) && StringUtils.isNotBlank(payCompany)){
			PayConfigCompanyDO payCfgComp = payCfgCompService.get(payCompany, payMerch, order.getOutChannel());
			if(payCfgComp!=null) {
				if(payCfgComp.getIfClose()!= 0) {
					return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道关闭！");
				}
				callbackDomain = payCfgComp.getCallbackDomain();
			}else {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道不存在！");
			}
				
		}else {
			payCompany = "";
			payMerch = "";
			Integer mPayChannelType = merchant.getPayChannelType();
			//判断商户是否有分配  通道分类   没有分配不支持支付
			if(mPayChannelType == null || !PayChannelType.desc().containsKey(mPayChannelType)) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未分配支付通道");
			}
			
			List<Object> payCfgComps = payCfgCompService.getPayCfgCompByOutChannel(order.getOutChannel());
			if (payCfgComps == null || payCfgComps.size() == 0) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道配置错误！");
			}
			
			List<PayConfigCompanyDO> pccList = new ArrayList<PayConfigCompanyDO>();
			PayConfigCompanyDO payCfgComp = null;
			for (Object object : payCfgComps) {
				payCfgComp = (PayConfigCompanyDO) object;
				if(payCfgComp.getIfClose() == 0){
					Integer cPayChannelType = payCfgComp.getPayChannelType();
					if(cPayChannelType != null && cPayChannelType.equals(mPayChannelType)) {
						//将与商户  同类的通道 找出来
						pccList.add(payCfgComp);
					}
				}
			}
			
			if(pccList.size() <= 0) {
				//当前商户没有 同类通道
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
			}
			payCfgComp = null;
			BigDecimal amount = order.getAmount();
			BigDecimal qhAmount = order.getQhAmount();
			amount = amount.add(qhAmount);
			PayAcctBal pab = RedisUtil.getMerchBal(order.getMerchNo());
			Map<String, BigDecimal> companyPayAvailBal = pab.getCompanyPayAvailBal();
			if(companyPayAvailBal!=null) {
				for (PayConfigCompanyDO payConfigCompanyDO : pccList) {
					String payCompanyName = payConfigCompanyDO.getCompany();
					BigDecimal singleAvailBal = companyPayAvailBal.get(payCompanyName + RedisConstants.link_symbol +  payConfigCompanyDO.getPayMerch());
					/*BigDecimal maxFee = payConfigCompanyDO.getMaxFee();
					BigDecimal minFee = payConfigCompanyDO.getMinFee();
					if(minFee != null && minFee.compareTo(qhAmount) == 1) {
						qhAmount = minFee;
					}else if(maxFee != null && maxFee.compareTo(qhAmount) == -1) {
						qhAmount = maxFee;
					}else if(PayConstants.MIN_FEE.compareTo(qhAmount) == 1){
						qhAmount = PayConstants.MIN_FEE;
					}*/
//					amount = amount.add(qhAmount);
					logger.info("{}商户在支付公司{}下,商户号为{}的可用余额剩余:{},商户此次支出金额为:{}",order.getMerchNo(),payCompanyName,payConfigCompanyDO.getPayMerch(),singleAvailBal,amount);
					if(singleAvailBal != null && amount.compareTo(singleAvailBal) <= 0) {
						//如果订单金额小于当前商户在当前支付公司交易的可用余额， 则使用此通道
						payCfgComp = payConfigCompanyDO;
						break;
					}
				}
			}
			if(payCfgComp == null){
				return R.error(order.getMerchNo() +  "未找到相应的支付通道或者可用余额不足");
			}
			Integer maxPayAmt = payCfgComp.getMaxPayAmt();
			Integer minPayAmt = payCfgComp.getMinPayAmt();
			if(maxPayAmt != null && new BigDecimal(maxPayAmt).compareTo(order.getAmount()) == -1){
				return R.error("单笔最高限额:"+maxPayAmt+"元");
			}
			if(minPayAmt != null && new BigDecimal(minPayAmt).compareTo(order.getAmount()) == 1){
				return R.error("单笔最低限额为:"+minPayAmt+"元");
			}
			payCompany = payCfgComp.getCompany();
			payMerch = payCfgComp.getPayMerch();
			callbackDomain = payCfgComp.getCallbackDomain();
		}
		order.setPayCompany(payCompany);
		order.setPayMerch(payMerch);
		order.setCallbackDomain(callbackDomain);
		return R.ok();
		
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#offlineTransfer(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public R offlineTransfer(String orderNo, String merchNo, Integer auditType) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order == null) {
					logger.info("代付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return R.error("订单不存在");
				}
				if(!Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
					return R.error("只有提现类订单才支持线下转账");
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.init.id()) {
					order.setOrderState(OrderState.succ.id());
					order.setRealAmount(order.getAmount());
					
					saveOrderAcp(order);
					
					if (this.saveOrderWithdrawData(order)) {
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						logger.info("代付订单数据保存成功，{}，{}",merchNo,orderNo);
						return R.ok("线下转账成功");
					}
					return R.ok("线下转账失败");
				}else{
					logger.info("代付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
					return R.ok("线下转账失败，订单状态不满足条件");
				}
			} finally {
				lock.unlock();
			}
		}else{
			return R.error("系统繁忙，请稍后再试");
		}
	}


	@Override
	@Transactional
	public R transfer(String merchNo,BigDecimal money,Integer isplate) {
		
		Merchant merchant = merchantService.get(merchNo);
		if(merchant==null) {
			return R.error(merchNo+" 商户不存在");
		}
		
		String orderNo = ParamUtil.getOrderId();
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Integer crtDate = DateUtil.getCurrentTimeInt();
			try {
				Order order = new Order();
				order.setOrderNo(orderNo);
				order.setMerchNo(merchNo);
				order.setOrderState(OrderState.init.id());
				order.setOutChannel(OutChannel.acp.name());
				order.setCardType(CardType.savings.id());
				order.setAcctType(AcctType.pri.id());
				order.setCertType(CertType.identity.id());
				order.setTitle("已结算扣减");
				order.setProduct("已结算扣减");
				order.setAmount(money);
				order.setCurrency(Currency.CNY.name());
				order.setReqTime(DateUtil.getCurrentNumStr());
				order.setCrtDate(crtDate);
				order.setMemo("已结算扣减");
				order.setOrderType(OrderType.withdraw.id());
				order.setUserId(merchNo);
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
				RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
				try {
					merchLock.lock();
					
					PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
					if (pab == null || pab.getAvailBal().compareTo(money) < 0) {
						return R.error("商户号 " + merchNo + ",可用余额不足！");
					}
					
					RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.withdrawOut.id(),
							OrderType.withdraw.id(), ProfitLoss.loss.id());
					rdMerchAvailBal.setCrtDate(crtDate);
					rdMerchAvailBal.setTranAmt(order.getAmount());
					payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
				} finally {
					merchLock.unlock();
				}
				int count = 0;
				try {
					order.setOrderState(OrderState.succ.id());
					order.setMsg("已结算扣减");
					count = payOrderAcpDao.save(order);
				} catch (Exception e1) {
					logger.error("划拨保存订单记录失败！{}，{}",merchNo,orderNo);
				}
				try {
					if(rdMerchAvailBalList.size() > 0 ) {
						//保存商户可用余额流水
						rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
					}
				} catch (Exception e) {
					logger.error("划拨保存可用余额流水记录失败！{}，{}",merchNo,orderNo);
				}
				if (count == 1) {
					RedisUtil.setOrderAcp(order);
					//如果成功，商户余额减少
					try {
						rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getAmount(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
						
						if(YesNoType.yes.id() ==isplate) {
							// 增加平台资金账户余额以及流水
							RecordFoundAcctDO rdFoundAcct = payHandlerService.balForPlatAdd(order,order.getAmount(), 
									FeeType.platTransfer.id(),OrderType.withdraw.id());
							rdFoundAcct.setCrtDate(crtDate);
							rdFoundAcctDao.save(rdFoundAcct);
	
							// 增加平台资金账户可用余额以及流水
							rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
									order.getAmount(),
									FeeType.platTransfer.id(),OrderType.withdraw.id());
							rdFoundAcct.setCrtDate(crtDate);
							rdFoundAvailAcctDao.save(rdFoundAcct);
						}
					} catch (Exception e) {
						logger.error("划拨保存余额流水记录失败！{}，{}",merchNo,orderNo);
					}
					
					return R.ok("划拨成功");
				} else {// 出错了返还可用余额
					RecordMerchBalDO  rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
					rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
					rdMerchAvailBalDao.save(rdMerchAvailBal);
					
					return R.error("划拨失败！");
				}
			} finally {
				lock.unlock();
			}
		}else {
			return R.error(merchNo + "," + orderNo + "划拨失败！");
		}
	}
	
	@Override
	@Transactional
	public R freeze(String merchNo,BigDecimal money,Integer freeze) {
		
		Merchant merchant = merchantService.get(merchNo);
		if(merchant==null) {
			return R.error(merchNo+" 商户不存在");
		}
		Integer crtDate = DateUtil.getCurrentTimeInt();
		List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
		Order order = new Order();
		order.setOrderNo(ParamUtil.getOrderId());
		order.setMerchNo(merchNo);
		order.setCrtDate(crtDate);
		order.setAmount(money);
		RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
		try {
			merchLock.lock();
			
			PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
			if(freeze == YesNoType.yes.id()) {
				//冻结操作
				if (pab == null || pab.getAvailBal().compareTo(money) < 0) {
					return R.error("商户号 " + merchNo + ",可用余额不足！");
				}
				RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.freeze.id(),
						-1, ProfitLoss.loss.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBal.setTranAmt(order.getAmount());
				
				rdMerchAvailBal.setBeforeAmt(pab.getAvailBal());
				pab.setAvailBal(pab.getAvailBal().subtract(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
				rdMerchAvailBal.setAfterAmt(pab.getAvailBal());
				
				BigDecimal freezeBal = pab.getFreezeBal();
				freezeBal = freezeBal==null?BigDecimal.ZERO:freezeBal;
				pab.setFreezeBal(freezeBal.add(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
				
				RedisUtil.setMerchBal(pab);
				rdMerchAvailBalList.add(rdMerchAvailBal);
			}else {
				//解冻操作
				if (pab == null || pab.getFreezeBal().compareTo(money) < 0) {
					return R.error("商户号 " + merchNo + ",冻结金额不足！");
				}
				RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.unfreeze.id(),
						-1, ProfitLoss.profit.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBal.setTranAmt(order.getAmount());
				
				rdMerchAvailBal.setBeforeAmt(pab.getAvailBal());
				pab.setAvailBal(pab.getAvailBal().add(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
				rdMerchAvailBal.setAfterAmt(pab.getAvailBal());
				
				BigDecimal freezeBal = pab.getFreezeBal();
				freezeBal = freezeBal==null?BigDecimal.ZERO:freezeBal;
				pab.setFreezeBal(freezeBal.subtract(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
				
				RedisUtil.setMerchBal(pab);
				rdMerchAvailBalList.add(rdMerchAvailBal);
			}
		} finally {
			merchLock.unlock();
		}
		int count = 0;
		try {
			if(rdMerchAvailBalList.size() > 0 ) {
				//保存商户可用余额流水
				count = rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
			}
		} catch (Exception e) {
			logger.error("冻结/解冻保存可用余额流水记录失败！{}，{}",merchNo,freeze);
		}
		if (count >= 1) {
			
			return R.ok("操作成功");
		} else {// 出错了返还可用余额
			RecordMerchBalDO  rdMerchAvailBal = null;
			RLock merchLock2 = RedissonLockUtil.getBalMerchLock(merchNo);
			try {
				merchLock2.lock();
				
				PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
				if(freeze == YesNoType.yes.id()) {
					
					rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.unfreeze.id(),
							-1, ProfitLoss.profit.id());
					rdMerchAvailBal.setCrtDate(crtDate);
					rdMerchAvailBal.setTranAmt(order.getAmount());
					
					rdMerchAvailBal.setBeforeAmt(pab.getAvailBal());
					pab.setAvailBal(pab.getAvailBal().add(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
					rdMerchAvailBal.setAfterAmt(pab.getAvailBal());
					
					pab.setFreezeBal(pab.getFreezeBal().subtract(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
					
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
				}else {
					rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.freeze.id(),
							-1, ProfitLoss.loss.id());
					rdMerchAvailBal.setCrtDate(crtDate);
					rdMerchAvailBal.setTranAmt(order.getAmount());
					
					rdMerchAvailBal.setBeforeAmt(pab.getAvailBal());
					pab.setAvailBal(pab.getAvailBal().subtract(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
					rdMerchAvailBal.setAfterAmt(pab.getAvailBal());
					
					pab.setFreezeBal(pab.getFreezeBal().add(rdMerchAvailBal.getTranAmt()).setScale(2, RoundingMode.HALF_UP));
					
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
				}
			} finally {
				merchLock2.unlock();
			}
			rdMerchAvailBalDao.save(rdMerchAvailBal);
			return R.error("操作失败！");
		}
	}

}
