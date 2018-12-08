package com.qh.common.controller;

import java.util.Date;
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

import com.qh.common.domain.TaskDO;
import com.qh.common.service.JobService;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;

/**
 * 
 * @date 2017-09-26 20:53:48
 */
@Controller
@RequestMapping("/common/job")
public class JobController extends BaseController{
	@Autowired
	private JobService taskScheduleJobService;

	@GetMapping()
	@RequiresPermissions("common:job:job")
	String taskScheduleJob() {
		return "common/job/job";
	}

	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("common:job:job")
	public PageUtils list(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		Query query = new Query(params);
		List<TaskDO> taskScheduleJobList = taskScheduleJobService.list(query);
		int total = taskScheduleJobService.count(query);
		PageUtils pageUtils = new PageUtils(taskScheduleJobList, total);
		return pageUtils;
	}

	@GetMapping("/add")
	@RequiresPermissions("common:job:add")
	String add() {
		return "common/job/add";
	}

	@RequiresPermissions("common:job:edit")
	@GetMapping("/edit/{id}")
	String edit(@PathVariable("id") Long id, Model model) {
		TaskDO job = taskScheduleJobService.get(id);
		model.addAttribute("job", job);
		return "common/job/edit";
	}

	/**
	 * 信息
	 */
	@RequestMapping("/info/{id}")
	@RequiresPermissions("common:job:job")
	public R info(@PathVariable("id") Long id) {
		TaskDO taskScheduleJob = taskScheduleJobService.get(id);
		return R.ok().put("taskScheduleJob", taskScheduleJob);
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("common:job:add")
	public R save(TaskDO taskScheduleJob) {
		if (taskScheduleJobService.save(taskScheduleJob) > 0) {
			return R.ok();
		}
		return R.error();
	}

	/**
	 * 修改
	 */
	@ResponseBody
	@PostMapping("/update")
	@RequiresPermissions("common:job:edit")
	public R update(TaskDO taskScheduleJob) {
		taskScheduleJobService.update(taskScheduleJob);
		return R.ok();
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ResponseBody
	@RequiresPermissions("common:job:remove")
	public R remove(Long id) {
		if (taskScheduleJobService.remove(id) > 0) {
			return R.ok();
		}
		return R.error();
	}

	/**
	 * 删除
	 */
	@PostMapping("/batchRemove")
	@ResponseBody
	@RequiresPermissions("common:job:batchRemove")
	public R remove(@RequestParam("ids[]") Long[] ids) {
		taskScheduleJobService.batchRemove(ids);
		return R.ok();
	}

	/***
	 * 
	 * @Description 操作任务
	 * @param id
	 * @param cmd
	 * @return
	 */
	@PostMapping(value = "/changeJobStatus")
	@ResponseBody
	@RequiresPermissions("common:job:cmd")
	public R changeJobStatus(Long id,String cmd ) {
		String label = "停止";
		if ("start".equals(cmd)) {
			label = "启动";
		} else {
			label = "停止";
		}
		try {
			taskScheduleJobService.changeStatus(id, cmd);
			return R.ok("任务" + label + "成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.ok("任务" + label + "失败");
	}

	/***
	 * 
	 * @Description 立即执行一个任务
	 * @param id
	 * @param cmd
	 * @return
	 */
	@PostMapping(value = "/runAJobNow")
	@ResponseBody
	@RequiresPermissions("common:job:runAJobNow")
	public R runAJobNow(Long id,Date choiceDate) {
		try {
			return taskScheduleJobService.runAJobNow(id,choiceDate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return R.ok("任务立即执行失败");
	}
}
