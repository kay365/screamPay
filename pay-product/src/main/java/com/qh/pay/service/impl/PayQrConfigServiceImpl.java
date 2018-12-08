package com.qh.pay.service.impl;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.dao.PayQrConfigDao;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayQrConfigDO;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayQrConfigService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;



@Service
public class PayQrConfigServiceImpl implements PayQrConfigService {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayQrConfigServiceImpl.class);
	
	@Autowired
	private PayQrConfigDao payQrConfigDao;
	
	@Autowired
	private MerchantService merchantService;
	
	@Override
	public PayQrConfigDO get(Integer id){
		return payQrConfigDao.get(id);
	}
	
	@Override
	public List<PayQrConfigDO> list(Map<String, Object> map){
		return payQrConfigDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return payQrConfigDao.count(map);
	}
	
	@Override
	public int save(PayQrConfigDO payQrConfig){
		//检查商户号
		if(!merchantService.exist(payQrConfig.getMerchNo())){
			//是不是资金账号
			PayAcctBal pab = RedisUtil.getPayFoundBal();
			if(pab==null || !payQrConfig.getMerchNo().equals(pab.getUsername())){
				return Constant.data_noexist;
			}
		}
		PayQrConfigDO payQrCfg = (PayQrConfigDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_qrs + payQrConfig.getOutChannel(),
				 payQrConfig.getMerchNo());
		if(payQrCfg != null){
			logger.warn("改通道配置已经存在{}，{}，{}", payQrConfig.getMerchNo(), payQrConfig.getOutChannel());
			return Constant.data_exist;
		}
		int count = payQrConfigDao.save(payQrConfig);
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + payQrConfig.getOutChannel(),
					payQrConfig.getMerchNo(), payQrCfg);
		}
		return count;
	}
	
	@Override
	public int update(PayQrConfigDO payQrConfig){
		int count = payQrConfigDao.updateByCode(payQrConfig);
		if(count > 0){
			payQrConfig.setQrs(get(payQrConfig.getOutChannel(), payQrConfig.getMerchNo()).getQrs());
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + payQrConfig.getOutChannel(),
					payQrConfig.getMerchNo(), payQrConfig);
		}
		return count;
	}
	
	@Override
	public int remove(String outChannel,String merchNo){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_qrs + outChannel, 
				merchNo);
		return payQrConfigDao.remove(outChannel,merchNo);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrConfigService#get(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public PayQrConfigDO get(String outChannel, String merchNo) {
		PayQrConfigDO payQrCfg = (PayQrConfigDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_qrs + outChannel,merchNo);
		if(payQrCfg == null){
			payQrCfg =  payQrConfigDao.getByCode(outChannel, merchNo);
			if(payQrCfg != null){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + outChannel,
						merchNo, payQrCfg);
			}
		}
		return payQrCfg;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrConfigService#updateQrs(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R updateQrs(String outChannel, String merchNo, String moneyAmount) {
		//支付二维码扫码图片
		PayQrConfigDO payQrCfg = get(outChannel, merchNo);
		if(payQrCfg == null){
			return R.error("该商户通道不存在" + merchNo);
		}
		if(payQrCfg.getQrs() == null){
			payQrCfg.setQrs(new HashMap<>());
		}
		if(payQrCfg.getQrs().get(moneyAmount) != null){
			return R.error("该付款码金额已经存在");
		}
		payQrCfg.getQrs().put(moneyAmount, 1);
		int count = payQrConfigDao.updateQrs(outChannel,merchNo,payQrCfg.getQrs());
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + outChannel,
					merchNo, payQrCfg);
		}
		return R.ok("处理成功").put("fileName", moneyAmount);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayQrConfigService#removeQrs(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R removeQrs(String outChannel, String merchNo, String moneyAmount) {
		//支付二维码扫码图片
		PayQrConfigDO payQrCfg = get(outChannel, merchNo);
		if(payQrCfg.getQrs()!=null){
			payQrCfg.getQrs().remove(moneyAmount);
		}else{
			return R.error("二维码收款图片不存在");
		}
		int count = payQrConfigDao.updateQrs(outChannel,merchNo,payQrCfg.getQrs());
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + outChannel,
					merchNo, payQrCfg);
		}else{
			return R.error("删除失败");
		}
		return R.ok("处理成功");
	}

	@Override
	public R removeQrs(String outChannel, String merchNo, List<String> moneyAmounts) {
		//支付二维码扫码图片
		PayQrConfigDO payQrCfg = get(outChannel, merchNo);
		if(payQrCfg.getQrs()!=null){
			for(String moneyAmount:moneyAmounts){
				payQrCfg.getQrs().remove(moneyAmount);
			}
		}else{
			return R.error("二维码收款图片不存在");
		}
		int count = payQrConfigDao.updateQrs(outChannel,merchNo,payQrCfg.getQrs());
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_qrs + outChannel,
					merchNo, payQrCfg);
		}else{
			return R.error("删除失败");
		}
		return R.ok("处理成功");
	}
}


