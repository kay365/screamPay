package com.qh.pay.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.qh.pay.api.constenum.UserType;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;

/**
 * 聚富代理
 * @author Administrator
 *
 */
public interface AgentService {

	Agent get(String merchNo);
	
	Agent get(Integer agentId);
	//全表数据
	Agent getById(Integer agentId);

	Agent getWithBalance(String merchNo);
	
	List<Agent> list(Map<String, Object> map);
	List<Agent> listAgent(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(Agent agent);
	
	int update(Agent agent);
	
	int remove(String merchNos);
	
	int remove(Integer agentId);
	
	int batchRemove(String[] merchNos);
	
	int batchRemove(Integer[] agentIds);
	int batchOperate(String flag,Integer[] agentIds);
	
	int batchAudit(Map<String, Object> map);

	/**
	 * @Description 默认商户号
	 * @return
	 */
	String defaultMerchantNo();

	/**
	 * @Description 是否存在
	 * @param merchNo
	 * @return
	 */
	boolean exist(String merchNo);


	/**
	 * 
	 * @Description 创建支付账户余额
	 * @param merchant
	 * @return
	 */
	public static PayAcctBal createPayAcctBal(Agent agent){
		PayAcctBal payAcctBal = new PayAcctBal();
		payAcctBal.setUserId(agent.getAgentId());
		payAcctBal.setUsername(agent.getAgentNumber());
		Integer userType = agent.getLevel()==1?UserType.agent.id():UserType.subAgent.id();
		payAcctBal.setUserType(userType);
		payAcctBal.setBalance(BigDecimal.ZERO);
		payAcctBal.setAvailBal(BigDecimal.ZERO);
		return payAcctBal;
	}

	/**
	 * @Description 获取所有的商户号
	 * @return
	 */
	Set<Object> getAllMerchNos();
	List<String> getAgents(Map<String, String> angets);
}
