package com.qh.pay.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.constenum.UserRole;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.pay.dao.MerchantMapper;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.MerchantService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.dao.UserDao;
import com.qh.system.dao.UserRoleDao;
import com.qh.system.domain.UserDO;
import com.qh.system.domain.UserRoleDO;



@Service
public class MerchantServiceImpl implements MerchantService {
	@Autowired
	private MerchantMapper merchantDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserRoleDao userRoleDao;

	
	/**
	 * 先从缓存中获取，缓存中没有再从数据库中同步
	 */
	@Override
	public Merchant get(String merchNo) {
		Merchant merchant = (Merchant) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_merchant, merchNo);
		if(merchant == null){
			merchant = merchantDao.getByMerchNo(merchNo);
			if(merchant != null){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchNo, merchant);
			}
		}
		return merchant;
	}
	@Override
	public Merchant get(Integer userId) {
		Merchant merchant = merchantDao.get(userId);
		return merchant;
	}
	@Override
	public Merchant getById(String merchNo) {
		Merchant merchant = merchantDao.getById(merchNo);
		return merchant;
	}

	@Override
	public Merchant getWithBalance(String merchNo) {
		Merchant merchant = get(merchNo);
		syncBalanceFromCache(merchant);
		return merchant;
	}

	@Override
	public List<Merchant> list(Map<String, Object> map){
		List<Merchant> merchants =  merchantDao.list(map);
		//同步缓存中的余额
		for (Merchant merchant : merchants) {
			syncBalanceFromCache(merchant);
		}
		return merchants;
	}
	
	public void syncBalanceFromCache(Merchant merchant){
		PayAcctBal acctBal =  (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_merch, merchant.getMerchNo());
		if(acctBal != null){
			merchant.setBalance(acctBal.getBalance());
		}
	}
	
	@Override
	public int count(Map<String, Object> map){
		return merchantDao.count(map);
	}
	
	@Override
	@Transactional
	public int save(Merchant merchant){
		
		if(merchantDao.exist(merchant.getMerchNo()) == 1){
			return Constant.data_exist;
		}
		UserDO user = userDao.getByUserName(merchant.getMerchNo());
		if(user != null){
			return Constant.data_exist;
		}
		
		if((user = createUserForMerchant(merchant))  != null){
			merchant.setUserId(user.getUserId());
			int count = merchantDao.save(merchant);
			if(count == 1){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchant.getMerchNo(), merchant);	
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_bal_merch, merchant.getMerchNo(), MerchantService.createPayAcctBal(merchant));
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_license, merchant.getMerchNo(),merchant.getMerchantRegisteredNumber());
				}
			return count;
		}else{
			return 0;
		}
	}
	
	private UserDO createUserForMerchant(Merchant merchant){
		UserDO user = new UserDO();
		user.setUserIdCreate(ShiroUtils.getUserId());
		user.setUsername(merchant.getMerchNo());
		user.setPassword(Md5Util.MD5(RedisUtil.getSysConfigValue(CfgKeyConst.pass_default_merch)));
		user.setName(merchant.getMerchantsName());
		user.setMobile(merchant.getManagerPhone());
//		String state  = RedisUtil.getSysConfigValue(CfgKeyConst.state_default_merch);
//		if(ParamUtil.isNotEmpty(state)){
			user.setStatus(YesNoType.yes.id());
//		}
		user.setUserType(UserType.merch.id());
		
		if(userDao.save(user) > 0){
			Integer userId = user.getUserId();
			UserRoleDO ur = new UserRoleDO();
			ur.setUserId(userId);
			ur.setRoleId(UserRole.merch.id());
			userRoleDao.save(ur);
			return user;
		}else{
			return null;
		}
	}
	
	
	@Override
	public int update(Merchant merchant){
		int count =  merchantDao.update(merchant);
		if(count>0) {
			updateRedis(merchant.getMerchNo());
		}
		/*Merchant redisMerchant = (Merchant) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_merchant, merchant.getMerchNo());
		if(redisMerchant != null){
			if(ParamUtil.isNotEmpty(merchant.getMerchantsName())){
				redisMerchant.setMerchantsName(merchant.getMerchantsName());
			}
			if(ParamUtil.isNotEmpty(merchant.getParentAgent())){
				redisMerchant.setParentAgent(merchant.getParentAgent());
			}
			if(ParamUtil.isNotEmpty(merchant.getPublicKey())){
				redisMerchant.setPublicKey(merchant.getPublicKey());
			}
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchant.getMerchNo(), merchant);		
		}*/
		return count;
	}
	
	@Override
	public int remove(String merchNo){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_merchant, merchNo);
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_bal_merch, merchNo);
		userRoleDao.removeByUsername(merchNo);
		userDao.removeByUsername(merchNo);
		return merchantDao.removeByMerchNo(merchNo);
	}
	
	@Override
	public int batchRemove(String[] merchNos){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_merchant, (Object[])merchNos);
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_bal_merch, (Object[])merchNos);
		userRoleDao.batchRemoveByUsername(merchNos);
		userDao.batchRemoveByUsername(merchNos);
		return merchantDao.batchRemoveByMerchNo(merchNos);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.MerchantService#defaultMerchantNo()
	 */
	@Override
	public String defaultMerchantNo() {
		String merchNo = QhPayUtil.getMerchNoPrefix() + ParamUtil.generateCode6();
		while (exist(merchNo)) {
			merchNo = QhPayUtil.getMerchNoPrefix() + ParamUtil.generateCode6();
		}
		return merchNo;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.MerchantService#exist(java.lang.String)
	 */
	@Override
	public boolean exist(String merchNo) {
		return RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_merchant, merchNo) != null;
	}

	@Override
	public Boolean existLicenseNum(String number) {
		List<Object> list = RedisUtil.getRedisTemplate().opsForHash().values(RedisConstants.cache_license);
		for (Object o : list) {
			if (number.equals(o.toString())) {
				return true;
			}
		}
		return false;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.MerchantService#getAllMerchNos()
	 */
	@Override
	public Set<Object> getAllMerchNos() {
		return RedisUtil.getRedisTemplate().opsForHash().keys(RedisConstants.cache_merchant);
	}
	@Override
	public int batchOperate(String flag,Integer[] merchantId){
		int count = 0;
		if("1".equals(flag)){
			count = merchantDao.batchqiyong(merchantId);
		}else{
			count = merchantDao.batchjinyong(merchantId);
		}
		if(count >0 ) {
			updateRedis(merchantDao.get(merchantId[0]).getMerchNo());
		}
		return count;
	}
	@Override
	public int batchAudit(Map<String, Object> map){
		int count =  merchantDao.batchAudit(map);
		if(count >0 ) {
			Integer[] merchantId = (Integer[])map.get("array");
			updateRedis(merchantDao.get(merchantId[0]).getMerchNo());
		}
		return count;
	}

	@Override
	public int batchWithdrawal(Map<String, Object> map) {
		int count =   merchantDao.batchWithdrawal(map);
		if(count >0 ) {
			Integer[] merchantId = (Integer[])map.get("array");
			updateRedis(merchantDao.get(merchantId[0]).getMerchNo());
		}
		return count;
	}

	@Override
	public int batchPaid(Map<String, Object> map) {
		int count =   merchantDao.batchPaid(map);
		if(count >0 ) {
			Integer[] merchantId = (Integer[])map.get("array");
			updateRedis(merchantDao.get(merchantId[0]).getMerchNo());
		}
		return count;
	}
	
	private void updateRedis(String merchNo) {
		
		Merchant merchant = merchantDao.getByMerchNo(merchNo);
		if(merchant != null){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchNo, merchant);
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_license, merchNo, merchant.getMerchantRegisteredNumber());
		}
	}
}
