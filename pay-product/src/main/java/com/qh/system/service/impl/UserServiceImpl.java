package com.qh.system.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qh.common.domain.Tree;
import com.qh.common.utils.BuildTree;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.service.RedisUtil;
import com.qh.system.dao.DeptDao;
import com.qh.system.dao.UserDao;
import com.qh.system.dao.UserRoleDao;
import com.qh.system.domain.DeptDO;
import com.qh.system.domain.UserDO;
import com.qh.system.domain.UserRoleDO;
import com.qh.system.service.UserService;

@Transactional
@Service
public class UserServiceImpl implements UserService {
	@Autowired
	UserDao userMapper;
	@Autowired
	UserRoleDao userRoleMapper;
	@Autowired
	DeptDao deptMapper;

	@Override
	public UserDO get(Integer id) {
		List<Integer> roleIds = userRoleMapper.listRoleId(id);
		UserDO user = userMapper.get(id);
		DeptDO dept = null;
		if(user.getDeptId() != null &&(dept = deptMapper.get(user.getDeptId())) != null){
			user.setDeptName(dept.getName());
		}
		user.setRoleIds(roleIds);
		return user;
	}

	@Override
	public List<UserDO> list(Map<String, Object> map) {
		return userMapper.list(map);
	}

	@Override
	public int count(Map<String, Object> map) {
		return userMapper.count(map);
	}

	@Transactional
	@Override
	public int save(UserDO user) {
		if(ParamUtil.isEmpty(user.getUserType())){
			user.setUserType(UserType.user.id());
		}
		user.setGmtCreate(new Date());
		int count = userMapper.save(user);
		if(count > 0 && (UserType.agent.id() == user.getUserType() || UserType.subAgent.id() == user.getUserType())){
			RedisUtil.setAgentBal(UserService.createAgentBalFromUser(user));
		}
		
		Integer userId = user.getUserId();
		List<Integer> roles = user.getRoleIds();
		userRoleMapper.removeByUserId(userId);
		List<UserRoleDO> list = new ArrayList<>();
		for (Integer roleId : roles) {
			UserRoleDO ur = new UserRoleDO();
			ur.setUserId(userId);
			ur.setRoleId(roleId);
			list.add(ur);
		}
		if (list.size() > 0) {
			userRoleMapper.batchSave(list);
		}
		return count;
	}


	@Override
	public int update(UserDO user) {
		user.setGmtModified(new Date());
		int r = userMapper.update(user);
		Integer userId = user.getUserId();
		List<Integer> roles = user.getRoleIds();
		userRoleMapper.removeByUserId(userId);
		List<UserRoleDO> list = new ArrayList<>();
		for (Integer roleId : roles) {
			UserRoleDO ur = new UserRoleDO();
			ur.setUserId(userId);
			ur.setRoleId(roleId);
			list.add(ur);
		}
		if (list.size() > 0) {
			userRoleMapper.batchSave(list);
		}
		return r;
	}

	@Override
	public int remove(Integer userId) {
		userRoleMapper.removeByUserId(userId);
		return userMapper.remove(userId);
	}

	
	
	@Override
	public boolean exit(Map<String, Object> params) {
		boolean exit;
		exit = userMapper.list(params).size() > 0;
		return exit;
	}

	@Override
	public Set<String> listRoles(Integer userId) {
		return null;
	}

	@Override
	public int resetPwd(UserDO user) {
		int r = userMapper.updatePassword(user);
		return r;
	}

	@Override
	public int resetFundPwd(UserDO user) {
		int r = userMapper.updateFundPassword(user);
		return r;
	}

	@Transactional
	@Override
	public int batchremove(Integer[] userIds) {
		int count = userMapper.batchRemove(userIds);
		userRoleMapper.batchRemoveByUserId(userIds);
		return count;
	}

	@Override
	public Tree<DeptDO> getTree() {
		List<Tree<DeptDO>> trees = new ArrayList<Tree<DeptDO>>();
		List<DeptDO> depts = deptMapper.list(new HashMap<String, Object>(16));
		Integer[] pDepts = deptMapper.listParentDept();
		Integer[] uDepts = userMapper.listAllDept();
		Integer[] allDepts = (Integer[]) ArrayUtils.addAll(pDepts, uDepts);
		for (DeptDO dept : depts) {
			if (!ArrayUtils.contains(allDepts, dept.getDeptId())) {
				continue;
			}
			Tree<DeptDO> tree = new Tree<DeptDO>();
			tree.setId(dept.getDeptId().toString());
			tree.setParentId(dept.getParentId().toString());
			tree.setText(dept.getName());
			Map<String, Object> state = new HashMap<>(16);
			state.put("opened", true);
			state.put("mType", "dept");
			tree.setState(state);
			trees.add(tree);
		}
		List<UserDO> users = userMapper.list(new HashMap<String, Object>(16));
		for (UserDO user : users) {
			Tree<DeptDO> tree = new Tree<DeptDO>();
			tree.setId(user.getUserId().toString());
			tree.setParentId(user.getDeptId().toString());
			tree.setText(user.getName());
			Map<String, Object> state = new HashMap<>(16);
			state.put("opened", true);
			state.put("mType", "user");
			tree.setState(state);
			trees.add(tree);
		}
		// 默认顶级菜单为０，根据数据库实际情况调整
		Tree<DeptDO> t = BuildTree.build(trees);
		return t;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.system.service.UserService#updatePassword(com.qh.system.domain.UserDO)
	 */
	@Override
	public void updatePassword(UserDO dataUserDo) {
		userMapper.updatePassword(dataUserDo);
		
	}

	@Override
	public void updateFundPassword(UserDO dataUserDo) {
		userMapper.updateFundPassword(dataUserDo);
	}

}
