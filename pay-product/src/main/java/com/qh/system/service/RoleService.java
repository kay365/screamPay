package com.qh.system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qh.system.domain.RoleDO;

@Service
public interface RoleService {

	RoleDO get(Integer id);

	List<RoleDO> list();

	int save(RoleDO role);

	int update(RoleDO role);

	int remove(Integer id);

	List<RoleDO> list(Integer userId);

	int batchremove(Integer[] ids);
}
