package com.qh.common.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.config.Constant;
import com.qh.common.dao.TaskDao;
import com.qh.common.domain.ScheduleJob;
import com.qh.common.domain.TaskDO;
import com.qh.common.quartz.utils.QuartzManager;
import com.qh.common.service.JobService;
import com.qh.common.utils.R;
import com.qh.common.utils.ScheduleJobUtils;
import com.qh.pay.api.utils.ParamUtil;

@Service
public class JobServiceImpl implements JobService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
	
	@Autowired
	private TaskDao taskScheduleJobMapper;

	@Autowired
	QuartzManager quartzManager;

	@Override
	public TaskDO get(Long id) {
		return taskScheduleJobMapper.get(id);
	}

	@Override
	public List<TaskDO> list(Map<String, Object> map) {
		return taskScheduleJobMapper.list(map);
	}

	@Override
	public int count(Map<String, Object> map) {
		return taskScheduleJobMapper.count(map);
	}

	@Override
	public int save(TaskDO taskScheduleJob) {
		return taskScheduleJobMapper.save(taskScheduleJob);
	}

	@Override
	public int update(TaskDO taskScheduleJob) {
		return taskScheduleJobMapper.update(taskScheduleJob);
	}

	@Override
	public int remove(Long id) {
		try {
			TaskDO scheduleJob = get(id);
			quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			return taskScheduleJobMapper.remove(id);
		} catch (SchedulerException e) {
			e.printStackTrace();
			return 0;
		}

	}

	@Override
	public int batchRemove(Long[] ids) {
		for (Long id : ids) {
			try {
				TaskDO scheduleJob = get(id);
				quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			} catch (SchedulerException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return taskScheduleJobMapper.batchRemove(ids);
	}

	@Override
	public void initSchedule() throws SchedulerException {
		// 这里获取任务信息数据
		List<TaskDO> jobList = taskScheduleJobMapper.list(new HashMap<String, Object>(16));
		for (TaskDO scheduleJob : jobList) {
			if ("1".equals(scheduleJob.getJobStatus())) {
				ScheduleJob job = ScheduleJobUtils.entityToData(scheduleJob);
				quartzManager.addJob(job);
			}

		}
	}

	@Override
	public void changeStatus(Long jobId, String cmd) throws SchedulerException {
		TaskDO scheduleJob = get(jobId);
		if (scheduleJob == null) {
			return;
		}
		if (Constant.STATUS_RUNNING_STOP.equals(cmd)) {
			quartzManager.deleteJob(ScheduleJobUtils.entityToData(scheduleJob));
			scheduleJob.setJobStatus(ScheduleJob.STATUS_NOT_RUNNING);
		} else {
			if (!Constant.STATUS_RUNNING_START.equals(cmd)) {
			} else {
                scheduleJob.setJobStatus(ScheduleJob.STATUS_RUNNING);
                quartzManager.addJob(ScheduleJobUtils.entityToData(scheduleJob));
            }
		}
		update(scheduleJob);
	}

	@Override
	public void updateCron(Long jobId) throws SchedulerException {
		TaskDO scheduleJob = get(jobId);
		if (scheduleJob == null) {
			return;
		}
		if (ScheduleJob.STATUS_RUNNING.equals(scheduleJob.getJobStatus())) {
			quartzManager.updateJobCron(ScheduleJobUtils.entityToData(scheduleJob));
		}
		update(scheduleJob);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.common.service.JobService#runAJobNow(java.lang.Long)
	 */
	@Override
	public R runAJobNow(Long id,Date choiceDate) {
		TaskDO scheduleJob = get(id);
		if(scheduleJob == null){
			return R.error("任务不存在");
		}
		if(ScheduleJob.STATUS_RUNNING.equals(scheduleJob.getJobStatus())){
			try {
				if(ParamUtil.isNotEmpty(choiceDate)){
					quartzManager.runAJobNow(ScheduleJobUtils.entityToData(scheduleJob),choiceDate);
				}else{
					quartzManager.runAJobNow(ScheduleJobUtils.entityToData(scheduleJob));
				}
			} catch (SchedulerException e) {
				logger.error("任务运行失败",e);
				return R.ok("任务运行失败！");
			}
			return R.ok("任务即将运行");
		}
		return R.error("任务未开启！");
	}

}
