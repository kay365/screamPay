package com.qh.pay.service;

import com.qh.pay.domain.IndustryDO;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @date 2018-02-26 10:41:24
 */
public interface IndustryService {
	
	IndustryDO get(Long id);
	
	List<IndustryDO> list(Map<String, Object> map);
	
	List<IndustryDO> listParent(Map<String, Object> map);
	List<IndustryDO> listSub(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(IndustryDO industry);
	
	int update(IndustryDO industry);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);
}
