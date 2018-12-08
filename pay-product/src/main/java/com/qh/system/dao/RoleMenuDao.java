package com.qh.system.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.system.domain.RoleMenuDO;

/**
 * 角色与菜单对应关系
 * @date 2017-10-03 11:08:59
 */
@Mapper
public interface RoleMenuDao {

	RoleMenuDO get(Integer id);
	
	List<RoleMenuDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(RoleMenuDO roleMenu);
	
	int update(RoleMenuDO roleMenu);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	
	List<Integer> listMenuIdByRoleId(Integer roleId);
	
	int removeByRoleId(Integer roleId);
	
	int batchSave(List<RoleMenuDO> list);
}
