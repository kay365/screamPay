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
import com.qh.pay.api.constenum.PaymentMethod;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.service.PayHandlerService;
import com.qh.redis.service.RedissonLockUtil;

/**
 * @ClassName TOneOrderClearTask
 * @Description T1订单清算任务
 */
@Component
public class TOneOrderClearTask implements Job{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TOneOrderClearTask.class);
	
	
	@Autowired
	private PayHandlerService payHandlerService;
	
	/* (非 Javadoc)
	 * Description:
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		boolean ifWd = DateUtil.ifWorkingDays();
		JobDetail jobDetail = context.getJobDetail();
		String jobClass = jobDetail.getJobClass().getSimpleName();
		logger.info("jobClass:{}", jobClass);
		String params = jobDetail.getJobDataMap().getString(Constant.task_params);
		
		if(PaymentMethod.T1.name().equals(params) || PaymentMethod.T0.name().equals(params)) {
			//工作日计算方式判断
			if(!ifWd) {
				logger.info("非工作日不做清算{}", DateUtil.getCurrentDateStr());
				return;
			}
		}
		
		Date date = (Date) context.getMergedJobDataMap().get(Constant.task_choiceDate);
		logger.info("choiceDate:{}",date);
		if(ParamUtil.isNotEmpty(params)){
			RLock rLock =  RedissonLockUtil.getLock(params);
			if(rLock.tryLock()){
				try {
					logger.info("{}订单清算任务", params);
					payHandlerService.orderClear(params,date);
				} finally {
					rLock.unlock();
				}
			}
		}
	}

}
