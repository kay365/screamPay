package com.qh.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.common.domain.DictDO;

/**
 * 字典表
 * 
 * @date 2017-10-03 15:45:42
 */
@Mapper
public interface DictDao {

	DictDO get(Long id);

	List<DictDO> list(Map<String, Object> map);

	int count(Map<String, Object> map);

	int save(DictDO dict);

	int update(DictDO dict);

	int remove(Long id);

	int batchRemove(Long[] ids);

	List<DictDO> listType();
}
