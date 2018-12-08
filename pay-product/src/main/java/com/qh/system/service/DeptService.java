package com.qh.system.service;

import java.util.List;
import java.util.Map;

import com.qh.common.domain.Tree;
import com.qh.system.domain.DeptDO;

/**
 * 部门管理
 * 
 * @date 2017-09-27 14:28:36
 */
public interface DeptService {
	
	DeptDO get(Integer deptId);
	
	List<DeptDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(DeptDO sysDept);
	
	int update(DeptDO sysDept);
	
	int remove(Integer deptId);
	
	int batchRemove(Integer[] deptIds);

	Tree<DeptDO> getTree();
}
