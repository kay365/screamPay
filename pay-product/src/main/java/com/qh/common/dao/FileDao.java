package com.qh.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.common.domain.FileDO;

/**
 * 文件上传
 * @date 2017-10-03 15:45:42
 */
@Mapper
public interface FileDao {

	FileDO get(Long id);
	
	FileDO getByUrl(String url);
	
	List<FileDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(FileDO file);
	
	int update(FileDO file);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);
}
