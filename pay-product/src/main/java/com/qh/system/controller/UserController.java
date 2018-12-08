package com.qh.system.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.common.annotation.Log;
import com.qh.common.controller.BaseController;
import com.qh.common.domain.Tree;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
import com.qh.pay.api.constenum.UserRole;
import com.qh.pay.api.constenum.UserType;
import com.qh.system.domain.DeptDO;
import com.qh.system.domain.RoleDO;
import com.qh.system.domain.UserDO;
import com.qh.system.service.RoleService;
import com.qh.system.service.UserService;

@RequestMapping("/sys/user")
@Controller
public class UserController extends BaseController {
	private String prefix="system/user"  ;
	@Autowired
	UserService userService;
	@Autowired
	RoleService roleService;

	@RequiresPermissions("sys:user:user")
	@GetMapping("")
	String user(Model model) {
		model.addAttribute("userTypes", UserType.desc());
		return prefix + "/user";
	}

	@GetMapping("/list")
	@ResponseBody
	PageUtils list(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		Query query = new Query(params);
		List<UserDO> sysUserList = userService.list(query);
		int total = userService.count(query);
		PageUtils pageUtil = new PageUtils(sysUserList, total);
		return pageUtil;
	}

	@RequiresPermissions("sys:user:add")
	@Log("添加用户")
	@GetMapping("/add")
	String add(Model model) {
		List<RoleDO> roles = roleService.list();
		List<RoleDO> rolesView = new ArrayList<>();
		for (RoleDO roleDO : roles) {
			if(roleDO.getRoleId() >= 5) {
				rolesView.add(roleDO);
			}
		}
		model.addAttribute("roles", rolesView);
		model.addAttribute("userTypes", UserType.addDesc());
		return prefix + "/add";
	}

	@RequiresPermissions("sys:user:edit")
	@Log("编辑用户")
	@GetMapping("/edit/{id}")
	String edit(Model model, @PathVariable("id") Integer id) {
		UserDO userDO = userService.get(id);
		model.addAttribute("user", userDO);
		Integer userType = userDO.getUserType();
		List<RoleDO> roles = roleService.list(id);
		List<RoleDO> rolesView = new ArrayList<>();
		if(UserType.agent.id() == userType || UserType.subAgent.id() == userType || UserType.merch.id() == userType) {
			for (RoleDO roleDO : roles) {
				if(roleDO.getRoleSign() == "true") {
					rolesView.add(roleDO);
				}
			}
		}else if(userDO.getRoleIds().get(0) == UserRole.admin.id()){
			for (RoleDO roleDO : roles) {
				if(roleDO.getRoleId() == 1) {
					rolesView.add(roleDO);
				}
			}
		}else {
			for (RoleDO roleDO : roles) {
				if(roleDO.getRoleId() >= 5) {
					rolesView.add(roleDO);
				}
			}
			model.addAttribute("userTypes", UserType.addDesc());
		}
		model.addAttribute("roles", rolesView);
		return prefix+"/edit";
	}

	@RequiresPermissions("sys:user:add")
	@Log("保存用户")
	@PostMapping("/save")
	@ResponseBody
	R save(UserDO user) {
		if (userService.save(user) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("sys:user:edit")
	@Log("更新用户")
	@PostMapping("/update")
	@ResponseBody
	R update(UserDO user) {
		if (userService.update(user) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("sys:user:remove")
	@Log("删除用户")
	@PostMapping("/remove")
	@ResponseBody
	R remove(Integer id) {
		if (userService.remove(id) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@RequiresPermissions("sys:user:batchRemove")
	@Log("批量删除用户")
	@PostMapping("/batchRemove")
	@ResponseBody
	R batchRemove(@RequestParam("ids[]") Integer[] userIds) {
		int r = userService.batchremove(userIds);
		if (r > 0) {
			return R.ok();
		}
		return R.error();
	}

	@PostMapping("/exit")
	@ResponseBody
	boolean exit(@RequestParam Map<String, Object> params) {
		// 存在，不通过，false
		return !userService.exit(params);
	}

	@RequiresPermissions("sys:user:resetPwd")
	@Log("请求更改用户密码")
	@GetMapping("/resetPwd/{id}")
	String resetPwd(@PathVariable("id") Integer userId, Model model) {

		UserDO userDO = new UserDO();
		userDO.setUserId(userId);
		model.addAttribute("user", userDO);
		return prefix + "/reset_pwd";
	}

	@RequiresPermissions("sys:user:resetFundPwd")
	@Log("请求更改用户密码")
	@GetMapping("/resetFundPwd/{id}")
	String resetFundPwd(@PathVariable("id") Integer userId, Model model) {

		UserDO userDO = new UserDO();
		userDO.setUserId(userId);
		model.addAttribute("user", userDO);
		return prefix + "/reset_fundpwd";
	}

	@Log("提交更改用户密码")
	@PostMapping("/resetPwd")
	@ResponseBody
	R resetPwd(UserDO user) {
		if (userService.resetPwd(user) > 0) {
			return R.ok();
		}
		return R.error();
	}

	@Log("提交更改用户资金密码")
	@PostMapping("/resetFundPwd")
	@ResponseBody
	R resetFundPwd(UserDO user) {
		if (userService.resetFundPwd(user) > 0) {
			return R.ok();
		}
		return R.error();
	}
	
	@GetMapping("/tree")
	@ResponseBody
	public Tree<DeptDO> tree() {
		Tree<DeptDO> tree = new Tree<DeptDO>();
		tree = userService.getTree();
		return tree;
	}

	@GetMapping("/treeView")
	String treeView() {
		return  prefix + "/userTree";
	}

}
