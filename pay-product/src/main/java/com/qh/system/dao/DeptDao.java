package com.qh.system.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.system.domain.DeptDO;

/**
 * 部门管理
 * @date 2017-10-03 15:35:39
 */
@Mapper
public interface DeptDao {

	DeptDO get(Integer deptId);
	
	List<DeptDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(DeptDO dept);
	
	int update(DeptDO dept);
	
	int remove(Integer deptId);
	
	int batchRemove(Integer[] deptIds);
	
	Integer[] listParentDept();
}
