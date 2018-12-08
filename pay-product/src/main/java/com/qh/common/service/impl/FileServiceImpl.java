package com.qh.common.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.dao.FileDao;
import com.qh.common.domain.FileDO;
import com.qh.common.service.FileService;



@Service
public class FileServiceImpl implements FileService {
	@Autowired
	private FileDao sysFileMapper;
	
	@Override
	public FileDO get(Long id){
		return sysFileMapper.get(id);
	}
	
	@Override
	public List<FileDO> list(Map<String, Object> map){
		return sysFileMapper.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return sysFileMapper.count(map);
	}
	
	@Override
	public int save(FileDO sysFile){
		return sysFileMapper.save(sysFile);
	}
	
	@Override
	public int update(FileDO sysFile){
		return sysFileMapper.update(sysFile);
	}
	
	@Override
	public int remove(Long id){
		return sysFileMapper.remove(id);
	}
	
	@Override
	public int batchRemove(Long[] ids){
		return sysFileMapper.batchRemove(ids);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.common.service.FileService#saveByUrl(com.qh.common.domain.FileDO)
	 */
	@Override
	public int saveByUrl(FileDO sysFile) {
		FileDO dataFile = sysFileMapper.getByUrl(sysFile.getUrl());
		if(dataFile != null){
			sysFile.setId(dataFile.getId());
			return sysFileMapper.update(sysFile);
		}else{
			return sysFileMapper.save(sysFile);
		}
	}
	
}
