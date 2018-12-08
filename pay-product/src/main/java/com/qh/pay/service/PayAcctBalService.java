package com.qh.pay.service;

import com.qh.pay.domain.PayAcctBal;

import java.util.List;

/**
 * 账号余额表
 * 
 * @date 2017-11-06 11:41:35
 */
public interface PayAcctBalService {

	List<PayAcctBal> list();

	List<PayAcctBal> list(int userType);

	List<PayAcctBal> listBlur(String username);

	List<PayAcctBal> listBlur(int userType,String username);

	int count(String key);
}
