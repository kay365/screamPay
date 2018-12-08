package com.qh.pay.dao;

import com.qh.pay.domain.IndustryDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * @date 2018-02-26 10:41:24
 */
@Mapper
public interface IndustryDao {

	IndustryDO get(Long id);
	
	List<IndustryDO> list(Map<String,Object> map);
	List<IndustryDO> listParent(Map<String,Object> map);
	List<IndustryDO> listSub(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(IndustryDO industry);
	
	int update(IndustryDO industry);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);
}
