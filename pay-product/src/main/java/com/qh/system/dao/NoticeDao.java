package com.qh.system.dao;

import com.qh.system.domain.NoticeDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * 公告表
 * @date 2018-03-08 15:17:29
 */
@Mapper
public interface NoticeDao {

	NoticeDO get(Integer id);
	
	List<NoticeDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(NoticeDO notice);
	
	int update(NoticeDO notice);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
}
