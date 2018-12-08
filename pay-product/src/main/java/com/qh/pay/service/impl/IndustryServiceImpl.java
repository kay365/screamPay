package com.qh.pay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.qh.pay.dao.IndustryDao;
import com.qh.pay.domain.IndustryDO;
import com.qh.pay.service.IndustryService;



@Service
public class IndustryServiceImpl implements IndustryService {
	@Autowired
	private IndustryDao industryDao;
	
	@Override
	public IndustryDO get(Long id){
		return industryDao.get(id);
	}
	
	@Override
	public List<IndustryDO> list(Map<String, Object> map){
		return industryDao.list(map);
	}
	@Override
	public List<IndustryDO> listParent(Map<String, Object> map){
		return industryDao.listParent(map);
	}
	@Override
	public List<IndustryDO> listSub(Map<String, Object> map){
		return industryDao.listSub(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return industryDao.count(map);
	}
	
	@Override
	public int save(IndustryDO industry){
		return industryDao.save(industry);
	}
	
	@Override
	public int update(IndustryDO industry){
		return industryDao.update(industry);
	}
	
	@Override
	public int remove(Long id){
		return industryDao.remove(id);
	}
	
	@Override
	public int batchRemove(Long[] ids){
		return industryDao.batchRemove(ids);
	}
	
}
