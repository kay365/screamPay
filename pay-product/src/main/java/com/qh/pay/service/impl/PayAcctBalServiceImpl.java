package com.qh.pay.service.impl;

import java.util.*;

import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import org.springframework.stereotype.Service;

import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.PayAcctBalService;



@Service
public class PayAcctBalServiceImpl implements PayAcctBalService {

	@Override
	public List<PayAcctBal> list(){
		List totalList = new ArrayList<>();
		totalList.add(RedisUtil.getPayFoundBal());
		totalList.addAll(RedisUtil.getHashValueList(RedisConstants.cache_bal_merch));
		totalList.addAll(RedisUtil.getHashValueList(RedisConstants.cache_bal_agent));

		return totalList;
	}

	@Override
	public List<PayAcctBal> list(int userType) {
		List totalList = new ArrayList<>();
		if(userType == UserType.foundAcct.id()){
			totalList.add(RedisUtil.getPayFoundBal());
		}else if(userType == UserType.merch.id()){
			totalList.addAll(RedisUtil.getHashValueList(RedisConstants.cache_bal_merch));
		}else if(userType == UserType.agent.id()){
			totalList.addAll(RedisUtil.getHashValueList(RedisConstants.cache_bal_agent));
		}
		return totalList;
	}

	@Override
	public List<PayAcctBal> listBlur(String username) {
		List totalList = new ArrayList<>();
		PayAcctBal pab = RedisUtil.getPayFoundBal();
		if(ParamUtil.isNotEmpty(pab)){
			if(pab.getUsername().matches(".*"+username+".*")){
				totalList.add(pab);
			}
		}
		totalList.addAll(RedisUtil.getHashValueListForStringObjBlur(RedisConstants.cache_bal_merch,username));
		totalList.addAll(RedisUtil.getHashValueListForStringObjBlur(RedisConstants.cache_bal_agent,username));
		return totalList;
	}

	@Override
	public List<PayAcctBal> listBlur(int userType, String username) {
		List totalList = new ArrayList<>();
		if(userType == UserType.foundAcct.id()){
			PayAcctBal pab = RedisUtil.getPayFoundBal();
			if(ParamUtil.isNotEmpty(pab)){
				if(pab.getUsername().matches(".*"+username+".*")){
					totalList.add(pab);
				}
			}
		}else if(userType == UserType.merch.id()){
			totalList.addAll(RedisUtil.getHashValueListForStringObjBlur(RedisConstants.cache_bal_merch,username));
		}else if(userType == UserType.agent.id()){
			totalList.addAll(RedisUtil.getHashValueListForStringObjBlur(RedisConstants.cache_bal_agent,username));
		}
		return totalList;
	}

	@Override
	public int count(String key) {
		return RedisUtil.getHashValueCount(key);
	}
}
