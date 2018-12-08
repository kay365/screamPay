package com.qh.common.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.SchedulerException;

import com.qh.common.domain.TaskDO;
import com.qh.common.utils.R;

/**
 * 
 * 
 * @date 2017-09-26 20:53:48
 */
public interface JobService {
	
	TaskDO get(Long id);
	
	List<TaskDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(TaskDO taskScheduleJob);
	
	int update(TaskDO taskScheduleJob);
	
	int remove(Long id);
	
	int batchRemove(Long[] ids);

	void initSchedule() throws SchedulerException;

	void changeStatus(Long jobId, String cmd) throws SchedulerException;

	void updateCron(Long jobId) throws SchedulerException;

	/**
	 * @Description 任务立即执行
	 * @param id
	 * @param choiceDate 
	 */
	R runAJobNow(Long id, Date choiceDate);
}
