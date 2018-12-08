package com.qh.pay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.qh.common.config.Constant;
import com.qh.pay.api.constenum.PayConfigType;
import com.qh.pay.api.utils.AesUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.dao.PayPropertyDao;
import com.qh.pay.domain.PayPropertyDO;
import com.qh.pay.service.PayPropertyService;
import com.qh.redis.service.RedisUtil;



@Service
public class PayPropertyServiceImpl implements PayPropertyService {
	@Autowired
	private PayPropertyDao payPropertyDao;
	
	@Override
	public PayPropertyDO get(Integer id){
		return payPropertyDao.get(id);
	}
	@Override
	public PayPropertyDO get(String configKey){
		return payPropertyDao.getByKey(configKey);
	}
	
	@Override
	public List<PayPropertyDO> list(Map<String, Object> map){
		return payPropertyDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return payPropertyDao.count(map);
	}
	
	@Override
	public int save(PayPropertyDO payProperty){
		String key = payProperty.getConfigKey();
		if(ParamUtil.isNotEmpty(payProperty.getMerchantno())){
			key = payProperty.getMerchantno() + key;
		}
		if(exist(payProperty.getMerchantno(),payProperty.getConfigKey())){
			return Constant.data_exist;
		}
		if(PayConfigType.pass.id() == payProperty.getConfigType()){
            payProperty.setValue(AesUtil.encrypt(payProperty.getValue()));
        }
		RedisUtil.syncPayConfig(payProperty);
		return payPropertyDao.save(payProperty);
	}
	
	@Override
	public int update(PayPropertyDO payProperty){
		if(PayConfigType.pass.id() == payProperty.getConfigType()){
            payProperty.setValue(AesUtil.encrypt(payProperty.getValue()));
        }
		RedisUtil.syncPayConfig(payProperty);
		return payPropertyDao.update(payProperty);
	}
	
	@Override
	public int remove(Integer id){
		RedisUtil.delPayConfig(payPropertyDao.get(id));
		return payPropertyDao.remove(id);
	}
	
	@Override
	public int batchRemove(Integer[] ids){
		for (Integer id : ids) {
			RedisUtil.delPayConfig(payPropertyDao.get(id));
		}
		return payPropertyDao.batchRemove(ids);
	}
	@Override
	public boolean exist(String merchantno, String configKey) {
		return payPropertyDao.exist(merchantno,configKey)> 0;
	}
}
