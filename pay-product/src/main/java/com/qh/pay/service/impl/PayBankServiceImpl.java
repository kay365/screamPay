package com.qh.pay.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qh.pay.api.PayConstants;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.dao.PayBankDao;
import com.qh.pay.domain.PayBankDO;
import com.qh.pay.service.PayBankService;
import com.qh.redis.service.RedisUtil;



@Service
public class PayBankServiceImpl implements PayBankService {
	@Autowired
	private PayBankDao payBankDao;
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayBankService#getBanks(java.lang.String, java.lang.Integer)
	 */
	@Override
	public List<String> getBanks(String company, Integer cardType) {
		List<String> banks = RedisUtil.getBanks(cardType, company);
		if(CollectionUtils.isEmpty(banks)){
			PayBankDO payBank = get(company, PayConstants.pay_merchNo_default, cardType);
			if(payBank != null && payBank.getBanks()!=null){
				return new ArrayList<>(payBank.getBanks().keySet());
			}
		}
		return banks;
	}
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayBankService#getBanks(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public List<String> getBanks(String company, String payMerch, Integer cardType) {
		
		if(PayConstants.pay_merchNo_default.equals(payMerch)){
			return getBanks(company, cardType);
		}
		List<String> banks = RedisUtil.getBanks(cardType, company, payMerch);
		if(CollectionUtils.isEmpty(banks)){
			PayBankDO payBank = get(company, payMerch, cardType);
			if(payBank != null && payBank.getBanks()!=null){
				return new ArrayList<>(payBank.getBanks().keySet());
			}
		}
		return banks;
	}

	
	@Override
	public PayBankDO get(String company,String payMerch,Integer cardType){
		if(ParamUtil.isEmpty(payMerch)){
			payMerch = PayConstants.pay_merchNo_default;
		}
		return payBankDao.get(company,payMerch,cardType);
	}
	
	@Override
	public List<PayBankDO> list(Map<String, Object> map){
		return payBankDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return payBankDao.count(map);
	}
	
	@Override
	public int save(PayBankDO payBank){
		if(ParamUtil.isEmpty(payBank.getPayMerch())){
			payBank.setPayMerch(PayConstants.pay_merchNo_default);
		}
		int count = payBankDao.save(payBank);
		if(count > 0 && payBank != null && payBank.getBanks()!=null){
			if(PayConstants.pay_merchNo_default.equals(payBank.getPayMerch())){
				RedisUtil.setBanks(payBank.getCardType(), payBank.getCompany(), new ArrayList<>(payBank.getBanks().keySet()));
			}else{
				RedisUtil.setBanks(payBank.getCardType(), payBank.getCompany(), payBank.getPayMerch(),new ArrayList<>(payBank.getBanks().keySet()));
			}
		}
		return count;
	}
	
	@Override
	public int update(PayBankDO payBank){
		if(ParamUtil.isEmpty(payBank.getPayMerch())){
			payBank.setPayMerch(PayConstants.pay_merchNo_default);
		}
		int count = payBankDao.update(payBank);
		if(count > 0){
			if(payBank.getBanks() == null){
				payBank.setBanks(new HashMap<>());
			}
			if(PayConstants.pay_merchNo_default.equals(payBank.getPayMerch())){
				RedisUtil.setBanks(payBank.getCardType(), payBank.getCompany(), new ArrayList<>(payBank.getBanks().keySet()));
			}else{
				RedisUtil.setBanks(payBank.getCardType(), payBank.getCompany(), payBank.getPayMerch(),new ArrayList<>(payBank.getBanks().keySet()));
			}
		}
		return count;
	}
	
	@Override
	public int remove(String company,String payMerch,Integer cardType){
		int count = payBankDao.remove(company,payMerch,cardType);
		if(count > 0){
			if(PayConstants.pay_merchNo_default.equals(payMerch)){
				RedisUtil.setBanks(cardType, company, null);
			}else{
				RedisUtil.setBanks(cardType, company, payMerch,null);
			}
		}
		return count;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayBankService#setBanks(int, java.lang.String, java.util.List)
	 */
	@Override
	public void setBanks(int cardType, String payCompany, List<String> banks) {
		setBanks(cardType, payCompany, PayConstants.pay_merchNo_default, banks);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayBankService#setBanks(int, java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void setBanks(int cardType, String payCompany, String payMerch, List<String> banks) {
		PayBankDO payBank = this.get(payCompany, payMerch, cardType);
		if(payBank != null){
			this.setBanksFromList(banks, payBank);
			this.update(payBank);
		}else{
			payBank = new PayBankDO();
			payBank.setCompany(payCompany);
			payBank.setCardType(cardType);
			payBank.setPayMerch(payMerch);
			this.setBanksFromList(banks, payBank);
			this.save(payBank);
		}
	}

	/**
	 * @Description 设置银行卡保存数据
	 * @param banks
	 * @param payBank
	 */
	private void setBanksFromList(List<String> banks, PayBankDO payBank) {
		Map<String,String> bankMap = new HashMap<>();
		for (String bankCode:banks) {
			bankMap.put(bankCode, "1");
		}
		payBank.setBanks(bankMap);
	}

}
