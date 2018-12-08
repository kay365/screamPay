package com.qh.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qh.system.dao.RoleDao;
import com.qh.system.dao.RoleMenuDao;
import com.qh.system.dao.UserDao;
import com.qh.system.dao.UserRoleDao;
import com.qh.system.domain.RoleDO;
import com.qh.system.domain.RoleMenuDO;
import com.qh.system.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

	public static final String ROLE_ALL_KEY = "\"role_all\"";

	public static final String DEMO_CACHE_NAME = "role";

	@Autowired
	RoleDao roleMapper;
	@Autowired
	RoleMenuDao roleMenuMapper;
	@Autowired
	UserDao userMapper;
	@Autowired
	UserRoleDao userRoleMapper;

	@Override
	public List<RoleDO> list() {
		List<RoleDO> roles = roleMapper.list(new HashMap<>(16));
		return roles;
	}

	@Override
	public List<RoleDO> list(Integer userId) {
		List<Integer> rolesIds = userRoleMapper.listRoleId(userId);
		List<RoleDO> roles = roleMapper.list(new HashMap<>(16));
		for (RoleDO roleDO : roles) {
			roleDO.setRoleSign("false");
			for (Integer roleId : rolesIds) {
				if (Objects.equals(roleDO.getRoleId(), roleId)) {
					roleDO.setRoleSign("true");
					break;
				}
			}
		}
		return roles;
	}

	@Transactional
	@Override
	public int save(RoleDO role) {
		int count = roleMapper.save(role);
		List<Integer> menuIds = role.getMenuIds();
		Integer roleId = role.getRoleId();
		List<RoleMenuDO> rms = new ArrayList<>();
		for (Integer menuId : menuIds) {
			RoleMenuDO rmDo = new RoleMenuDO();
			rmDo.setRoleId(roleId);
			rmDo.setMenuId(menuId);
			rms.add(rmDo);
		}
		roleMenuMapper.removeByRoleId(roleId);
		if (rms.size() > 0) {
			roleMenuMapper.batchSave(rms);
		}
		return count;
	}

	@CacheEvict(value = DEMO_CACHE_NAME)
	@Transactional
	@Override
	public int remove(Integer id) {
		int count = roleMapper.remove(id);
		roleMenuMapper.removeByRoleId(id);
		return count;
	}

	@Override
	public RoleDO get(Integer id) {
		RoleDO roleDO = roleMapper.get(id);
		return roleDO;
	}

	@CacheEvict(value = DEMO_CACHE_NAME)
	@Override
	public int update(RoleDO role) {
		int r = roleMapper.update(role);
		List<Integer> menuIds = role.getMenuIds();
		Integer roleId = role.getRoleId();
		roleMenuMapper.removeByRoleId(roleId);
		List<RoleMenuDO> rms = new ArrayList<>();
		for (Integer menuId : menuIds) {
			RoleMenuDO rmDo = new RoleMenuDO();
			rmDo.setRoleId(roleId);
			rmDo.setMenuId(menuId);
			rms.add(rmDo);
		}
		// roleMenuMapper.removeByRoleId(roleId);
		if (rms.size() > 0) {
			roleMenuMapper.batchSave(rms);
		}
		return r;
	}

	@Override
	public int batchremove(Integer[] ids) {
		int r = roleMapper.batchRemove(ids);
		return r;
	}

}
