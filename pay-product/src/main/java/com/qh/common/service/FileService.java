package com.qh.common.service;

import java.util.List;
import java.util.Map;

import com.qh.common.domain.FileDO;

/**
 * 文件上传
 * 
 * @date 2017-09-19 16:02:20
 */
public interface FileService {
	
	FileDO get(Long id);
	
	List<FileDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(FileDO sysFile);
	
	int update(FileDO sysFile);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);

	/**
	 * @Description 根据url保存
	 * @param sysFile
	 * @return
	 */
	int saveByUrl(FileDO sysFile);
}
