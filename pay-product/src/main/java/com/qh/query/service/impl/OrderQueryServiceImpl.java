package com.qh.query.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qh.pay.domain.FooterDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.utils.Query;
import com.qh.pay.api.Order;
import com.qh.query.querydao.OrderAcpQueryDao;
import com.qh.query.querydao.OrderLoseQueryDao;
import com.qh.query.querydao.OrderQueryDao;
import com.qh.query.service.OrderQueryService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {
	@Autowired
	private OrderQueryDao orderQueryDao;
	@Autowired
	private OrderAcpQueryDao orderAcpQueryDao;
	@Autowired
	private OrderLoseQueryDao orderLoseQueryDao;

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#get(java.lang.String, java.lang.String)
	 */
	@Override
	public Order get(String orderNo, String merchNo) {
		return orderQueryDao.get(orderNo, merchNo);
	}
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#getOrders(java.lang.String, int, int, java.util.Map)
	 */
	@Override
	public List<Object> getOrders(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map) {
		Set<Object> orderNoSet = RedisUtil.getRedisTemplate().opsForZSet().rangeByScore(RedisConstants.cache_sort_order + merchNo,
				beginDateInt, endDateInt, (Integer)map.get("offset"), (Integer)map.get("limit"));
		return RedisUtil.getRedisTemplate().opsForHash().multiGet(RedisConstants.cache_order + merchNo, orderNoSet);
	}

	@Override
	public List<Object> getOrdersFooter(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map) {
		Set<Object> orderNoSet = RedisUtil.getRedisTemplate().opsForZSet().rangeByScore(RedisConstants.cache_sort_order + merchNo,
				beginDateInt, endDateInt);
		return RedisUtil.getRedisTemplate().opsForHash().multiGet(RedisConstants.cache_order + merchNo, orderNoSet);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#getOrdersCount(java.lang.String, int, int, java.util.Map)
	 */
	@Override
	public int getOrdersCount(String merchNo, int beginDateInt, int endDateInt) {
		Long count = RedisUtil.getRedisTemplate().opsForZSet().count(RedisConstants.cache_sort_order + merchNo,	beginDateInt, endDateInt);
		if(count ==null){
			return 0;
		}else{
			return count.intValue();
		}
	}
	
	
	@Override
	public List<Order> list(Map<String, Object> map) {
		return orderQueryDao.list(map);
	}

	@Override
	public FooterDO listFooter(Map<String, Object> map) {
		return orderQueryDao.listFooter(map);
	}

	@Override
	public int count(Map<String, Object> map) {
		return orderQueryDao.count(map);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#getAcp(java.lang.String, java.lang.String)
	 */
	@Override
	public Order getAcp(String orderNo, String merchNo) {
		return orderAcpQueryDao.get(orderNo, merchNo);
	}
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#getAcpOrders(java.lang.String, int, int, com.qh.common.utils.Query)
	 */
	@Override
	public List<Object> getAcpOrders(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map) {
		Set<Object> orderNoSet = RedisUtil.getRedisTemplate().opsForZSet().rangeByScore(RedisConstants.cache_sort_acp_order + merchNo,
				beginDateInt, endDateInt, (Integer)map.get("offset"), (Integer)map.get("limit"));
		return RedisUtil.getRedisTemplate().opsForHash().multiGet(RedisConstants.cache_order_acp + merchNo, orderNoSet);
	}

	@Override
	public List<Object> getAcpOrdersFooter(String merchNo, int beginDateInt, int endDateInt, Map<String, Object> map) {
		Set<Object> orderNoSet = RedisUtil.getRedisTemplate().opsForZSet().rangeByScore(RedisConstants.cache_sort_acp_order + merchNo,
				beginDateInt, endDateInt);
		return RedisUtil.getRedisTemplate().opsForHash().multiGet(RedisConstants.cache_order_acp + merchNo, orderNoSet);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#getAcpOrdersCount(java.lang.String, int, int)
	 */
	@Override
	public int getAcpOrdersCount(String merchNo, int beginDateInt, int endDateInt) {
		Long count = RedisUtil.getRedisTemplate().opsForZSet().count(RedisConstants.cache_sort_acp_order + merchNo,	beginDateInt, endDateInt);
		if(count ==null){
			return 0;
		}else{
			return count.intValue();
		}
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#listAcp(java.util.Map)
	 */
	@Override
	public List<Order> listAcp(Map<String, Object> map) {
		return orderAcpQueryDao.list(map);
	}

	@Override
	public FooterDO listAcpFooter(Map<String, Object> map) {
		return orderAcpQueryDao.listFooter(map);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#countAcp(java.util.Map)
	 */
	@Override
	public int countAcp(Map<String, Object> map) {
		return orderAcpQueryDao.count(map);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#listLose(com.qh.common.utils.Query)
	 */
	@Override
	public List<Order> listLose(Query query) {
		return orderLoseQueryDao.list(query);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.OrderQueryService#countLose(com.qh.common.utils.Query)
	 */
	@Override
	public int countLose(Query query) {
		return orderLoseQueryDao.count(query);
	}

}
