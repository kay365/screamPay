package com.qh.system.service;

import com.qh.system.domain.NoticeDO;

import java.util.List;
import java.util.Map;

/**
 * 公告表
 * 
 * @date 2018-03-08 15:17:29
 */
public interface NoticeService {
	
	NoticeDO get(Integer id);
	
	List<NoticeDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(NoticeDO notice);
	
	int update(NoticeDO notice);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
}
