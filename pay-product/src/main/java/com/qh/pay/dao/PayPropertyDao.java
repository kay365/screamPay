package com.qh.pay.dao;

import com.qh.pay.domain.PayPropertyDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付参数配置
 * @date 2017-10-27 17:52:44
 */
@Mapper
public interface PayPropertyDao {

	PayPropertyDO get(Integer id);
	
	PayPropertyDO getByKey(String key);
	
	List<PayPropertyDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(PayPropertyDO payProperty);
	
	int update(PayPropertyDO payProperty);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	
	int removeByKey(@Param("merchantno")String merchantno, @Param("configKey")String configKey);
	
	int batchRemoveByKey(String[] configKeys);

	/**
	 * @Description 是否存在
	 * @param configKey
	 * @return
	 */
	int exist(@Param("merchantno") String merchantno, @Param("configKey")String configKey);
}
