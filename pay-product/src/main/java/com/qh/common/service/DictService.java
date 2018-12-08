package com.qh.common.service;

import java.util.List;
import java.util.Map;

import com.qh.common.domain.DictDO;

/**
 * 字典表
 * 
 * @date 2017-09-29 18:28:07
 */
public interface DictService {
	
	DictDO get(Long id);
	
	List<DictDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(DictDO sysDict);
	
	int update(DictDO sysDict);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);

	List<DictDO> listType();
	
	String getName(String type,String value);
}
