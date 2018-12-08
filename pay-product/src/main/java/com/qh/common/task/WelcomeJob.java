package com.qh.common.task;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.qh.oa.domain.Response;

@Component
public class WelcomeJob implements Job{
	@Autowired
	SimpMessagingTemplate template;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
    	template.convertAndSend("/topic/getResponse", new Response("欢迎体验聚富云支付系统！" ));
    }

}