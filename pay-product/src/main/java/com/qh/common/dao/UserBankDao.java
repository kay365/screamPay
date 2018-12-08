package com.qh.common.dao;

import com.qh.common.domain.UserBankDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 用户银行卡
 * @date 2018-01-10 14:39:21
 */
@Mapper
public interface UserBankDao {

	UserBankDO get(@Param("username") String username, @Param("bankNo") String bankNo);
	
	List<UserBankDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(UserBankDO userBank);
	
	int update(UserBankDO userBank);
	
}
