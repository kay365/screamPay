package com.qh.system.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.system.domain.ConfigDO;

/**
 * 系统配置
 * @date 2017-10-26 17:12:22
 */
@Mapper
public interface ConfigDao {

	ConfigDO get(Integer id);
	
	List<ConfigDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(ConfigDO config);
	
	int update(ConfigDO config);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);

	/**
	 * @Description 通过配置项查询
	 * @param configItem
	 * @return
	 */
	ConfigDO getByItem(String configItem);
}
