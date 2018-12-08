package com.qh.common.task;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qh.common.config.Constant;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.PayHandlerService;
import com.qh.redis.service.RedissonLockUtil;

/**
 * @ClassName JiupayOrderClearTask
 * @Description 九派订单清算任务
 * @Date 2017年12月7日 下午3:19:18
 * @version 1.0.0
 */
@Component
public class JiupayOrderClearTask implements Job{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JiupayOrderClearTask.class);
	
	
	@Autowired
	private PayHandlerService payHandlerService;
	
	/* (非 Javadoc)
	 * Description:
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDetail jobDetail = context.getJobDetail();
		String jobClass = jobDetail.getJobClass().getSimpleName();
		logger.info("jobClass:{}", jobClass);
		String params = jobDetail.getJobDataMap().getString(Constant.task_params);
		Date date = (Date) context.getMergedJobDataMap().get(Constant.task_choiceDate);
		logger.info("choiceDate:{}",date);
		if(ParamUtil.isNotEmpty(params)){
			String[] datas = params.split(",");
			for (String company : datas) {
				if(ParamUtil.isNotEmpty(company)){
					RLock rLock =  RedissonLockUtil.getLock(company);
					if(rLock.tryLock()){
						try {
							logger.info("九派订单清算任务:{}", company);
							payHandlerService.orderClear(company,date);
						} finally {
							rLock.unlock();
						}
					}
				}
			}
		}
	}

}
