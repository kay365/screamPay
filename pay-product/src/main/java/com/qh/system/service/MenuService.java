package com.qh.system.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.qh.common.domain.Tree;
import com.qh.system.domain.MenuDO;

@Service
public interface MenuService {
	Tree<MenuDO> getSysMenuTree(Integer id);

	List<Tree<MenuDO>> listMenuTree(Integer id);

	Tree<MenuDO> getTree();

	Tree<MenuDO> getTree(Integer id);

	List<MenuDO> list();

	int remove(Integer id);

	int save(MenuDO menu);

	int update(MenuDO menu);

	MenuDO get(Integer id);

	Set<String> listPerms(Integer userId);
}
