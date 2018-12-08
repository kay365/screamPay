package com.qh.pay.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.exception.BDException;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.AuditResult;
import com.qh.pay.api.constenum.AuditType;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.dao.PayAuditDao;
import com.qh.pay.domain.PayAuditDO;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.PayAuditService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.pay.service.PayService;
import com.qh.redis.service.RedisUtil;



@Service
public class PayAuditServiceImpl implements PayAuditService {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayAuditServiceImpl.class);
	@Autowired
	private PayAuditDao payAuditDao;
	@Autowired
	private PayService payService;
	@Autowired
	private PayConfigCompanyService payCfgCompService;
	
	
	@Override
	public PayAuditDO get(String orderNo, String merchNo, Integer auditType){
		return payAuditDao.get(orderNo, merchNo, auditType);
	}
	
	@Override
	public List<PayAuditDO> list(Map<String, Object> map){
		if(map.get("beginDate") != null)
			map.put("beginDate", DateUtil.getBeginTimeIntZero((Date) map.get("beginDate")));
		if(map.get("endDate") != null)
			map.put("endDate", DateUtil.getEndTimeIntLast((Date) map.get("endDate")));
		return payAuditDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return payAuditDao.count(map);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayAuditService#audit(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	@Transactional
	public int audit(String orderNo, String merchNo, Integer auditType, Integer auditResult,String company) {
		String username = ShiroUtils.getUsername();
		return singleAudit(orderNo, merchNo, auditType, auditResult,username,company);
	}

	
	private int singleAudit(String orderNo, String merchNo, Integer auditType, Integer auditResult,String username,String company){
		if(ParamUtil.isNotEmpty(orderNo) && ParamUtil.isNotEmpty(merchNo) && auditType != null && auditResult != null){
			PayAuditDO payAudit = payAuditDao.get(orderNo, merchNo, auditType);
			if(payAudit == null || payAudit.getAuditResult()!= AuditResult.init.id()){
				return 0;
			}
			payAudit.setAuditResult(auditResult);
			payAudit.setAuditTime(new Date());
			payAudit.setAuditor(username);
			payAudit.setMemo(username + (auditResult == 1?"审核通过":"审核不通过"));
			if(company!=null && !"".equals(company.trim())) {
				Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
				if(order == null) {
					return 0;
				}
				String[] companys = company.split(",");
				String payCompany = companys[1];
				String payMerch = companys[0];
				PayConfigCompanyDO payCfgComp = payCfgCompService.get(payCompany, payMerch, order.getOutChannel());
				String callbackDomain = "";
				if(payCfgComp!=null) {
					callbackDomain = payCfgComp.getCallbackDomain();
				}else
					return 0;
				order.setPayCompany(payCompany);
				order.setPayMerch(payMerch);
				order.setCallbackDomain(callbackDomain);
				RedisUtil.setOrderAcp(order);
			}
			return payAuditDao.update(payAudit);
		}
		return 0;
	}
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayAuditService#batchAudit(java.lang.String[], java.lang.String[], java.lang.Integer[], java.lang.Integer)
	 */
	@Override
	@Transactional
	public int batchAudit(String[] orderNos, String[] merchNos, Integer[] auditTypes, Integer auditResult,String[] companys) {
		String username = ShiroUtils.getUsername();
		int count = 0;
		int len = orderNos.length;
		for (int i = 0; i < len; i++) {
			count += singleAudit(orderNos[i], merchNos[i], auditTypes[i], auditResult, username,companys[i]);
		}
		if(count < len){
			String msg = "本次批量审核失败,审核数量：" + len + ",实际审核数量：" + count;
			logger.info(msg);
			throw new BDException(msg);
		}
		return count;
	}

	/* (非 Javadoc)
	 * Description:线下转账
	 * @see com.qh.pay.service.PayAuditService#offlineTransfer(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public R offlineTransfer(String orderNo, String merchNo, Integer auditType) {
		if(ParamUtil.isNotEmpty(orderNo) && ParamUtil.isNotEmpty(merchNo) && auditType != null){
			if(auditType != AuditType.order_withdraw.id()){
				return R.error("线下转账只支持提现审核类型");
			}
			PayAuditDO payAudit = payAuditDao.get(orderNo, merchNo, auditType);
			if(payAudit == null || payAudit.getAuditResult()== AuditResult.noPass.id()){
				return R.error(AuditType.desc().get(auditType) + "未通过！");
			}
			R r = payService.offlineTransfer(orderNo,merchNo,auditType);
			if(R.ifSucc(r) && Integer.valueOf(AuditResult.init.id()).equals(payAudit.getAuditResult())){
				payAudit.setAuditResult(AuditResult.pass.id());
				payAudit.setAuditTime(new Date());
				payAudit.setAuditor(ShiroUtils.getUsername());
				payAudit.setMemo(ShiroUtils.getUsername()+"线下转账成功，直接审核通过");
				payAuditDao.update(payAudit);
			}
			return r;
		}
		return R.error("参数错误");
	}
	

	
}
