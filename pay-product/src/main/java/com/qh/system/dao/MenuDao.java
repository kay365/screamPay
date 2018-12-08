package com.qh.system.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.system.domain.MenuDO;

/**
 * 菜单管理
 * @date 2017-10-03 09:45:09
 */
@Mapper
public interface MenuDao {

	MenuDO get(Integer menuId);
	
	List<MenuDO> list(Map<String,Object> map);
	
	int count(Map<String,Object> map);
	
	int save(MenuDO menu);
	
	int update(MenuDO menu);
	
	int remove(Integer menuId);
	
	int batchRemove(Integer[] menuIds);
	
	List<MenuDO> listMenuByUserId(Integer id);
	
	List<String> listUserPerms(Integer id);
}
