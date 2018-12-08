package com.qh.pay.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mysql.fabric.xmlrpc.base.Array;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.dao.AgentMapper;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.AgentService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.dao.UserDao;
import com.qh.system.dao.UserRoleDao;

@Service
public class AgentServiceImpl implements AgentService {

	@Autowired
	private AgentMapper agentDao;
	
	@Override
	public Agent get(String merchNo) {
		Agent agent = (Agent) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_agent, merchNo);
		if(agent == null){
			agent = agentDao.getByMerchNo(merchNo);
			if(agent != null){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_agent, merchNo, agent);
			}
		}
		return agent;
	}
	
	@Override
	public Agent get(Integer agentId){
		return agentDao.get(agentId);
	}
	//全表数据
	@Override
	public Agent getById(Integer agentId){
		return agentDao.getById(agentId);
	}

	@Override
	public Agent getWithBalance(String merchNo) {

		Agent agent = get(merchNo);
		syncBalanceFromCache(agent);
		return agent;
	}

	@Override
	public List<Agent> list(Map<String, Object> map) {
		List<Agent> agents =  agentDao.list(map);
		//同步缓存中的余额
		for (Agent agent : agents) {
			syncBalanceFromCache(agent);
		}
		return agents;
	}
	@Override
	public List<Agent> listAgent(Map<String, Object> map) {
		List<Agent> agents =  agentDao.listAgent(map);
		//同步缓存中的余额
		for (Agent agent : agents) {
			syncBalanceFromCache(agent);
		}
		return agents;
	}

	public void syncBalanceFromCache(Agent agent){
		PayAcctBal acctBal =  (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_merch, agent.getAgentNumber());
		if(acctBal != null){
			agent.setBalance(acctBal.getBalance());
		}
	}
	
	@Override
	public int count(Map<String, Object> map) {
		return agentDao.count(map);
	}

	@Override
	public int save(Agent agent) {
	
		int count = agentDao.save(agent);
		if(count>0) {
			updateRedis(agent.getAgentNumber());
		}
		return count;
	}

	@Override
	public int update(Agent agent) {
		// TODO Auto-generated method stub
		
		int count = agentDao.update(agent);
		if(count>0) {
			updateRedis(get(agent.getAgentId()).getAgentNumber());
		}
		return count;
	}

	@Override
	public int remove(String merchNos) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int remove(Integer agentId){
		return agentDao.remove(agentId);
	}

	@Override
	public int batchRemove(String[] merchNos) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int batchRemove(Integer[] agentIds){
		return agentDao.batchRemove(agentIds);
	}
	
	@Override
	public int batchOperate(String flag,Integer[] agentIds){
		int count = 0;
		if("1".equals(flag)){
			count = agentDao.batchqiyong(agentIds);
		}else{
			count = agentDao.batchjinyong(agentIds);
		}
		if(count >0 ) {
			updateRedis(get(agentIds[0]).getAgentNumber());
		}
		return count;
	}

	@Override
	public String defaultMerchantNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exist(String agentN) {
		List<Object> list = RedisUtil.getRedisTemplate().opsForHash().values(RedisConstants.cache_agent);
		for(int i=0;i<list.size();i++){
			Agent agent = (Agent) list.get(i);
			if(agentN.equals(agent.getAgentNumber())){
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Object> getAllMerchNos() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public List<String> getAgents(Map<String, String> angets) {
		//Set<Object> agents = RedisUtil.getRedisTemplate().opsForHash().keys(RedisConstants.cache_agent);
		//Map<Object,Object> map = RedisUtil.getRedisTemplate().opsForHash().entries(RedisConstants.cache_agent);
		List<Object> list = RedisUtil.getRedisTemplate().opsForHash().values(RedisConstants.cache_agent);
		List<String> one_level = new ArrayList<String>();
		for(int i=0;i<list.size();i++){
			Agent agent = (Agent) list.get(i);
			
			if(angets.containsKey("isall") && agent.getStatus() == YesNoType.yes.id()) {
				if(agent.getLevel() == AgentLevel.two.id()) {
					Agent parentAgent = this.get(agent.getAgentNumber());
					if(parentAgent.getStatus() == YesNoType.not.id()) {
						continue;
					}
				}
				one_level.add(agent.getAgentNumber());
				if(angets!=null)
					angets.put(agent.getAgentNumber(), agent.getMerchantsName());
			}else if(agent.getLevel()!=null&&AgentLevel.one.id() == agent.getLevel()&&agent.getStatus() == YesNoType.yes.id()){
				one_level.add(agent.getAgentNumber());
				if(angets!=null)
					angets.put(agent.getAgentNumber(), agent.getMerchantsName());
			}
		}
		if(angets.containsKey("isall")) 
			angets.remove("isall");
		return one_level;
	}

	@Override
	public int batchAudit(Map<String, Object> map) {
		
		int count = agentDao.batchAudit(map);
		if(count > 0) {
			Integer[] agentIds = (Integer[])map.get("array");
			updateRedis(get(agentIds[0]).getAgentNumber());
		}
		return count;
	}
	
	
	private void updateRedis(String merchNo) {
		
		Agent agent = agentDao.getByMerchNo(merchNo);
		if(agent != null){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_agent, merchNo, agent);
		}
	}

}
