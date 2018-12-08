package com.qh.sms.service;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SMSServiceFactory implements ApplicationContextAware {

    private static Map<String, SMSService> smsServiceBeanMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, SMSService> map = applicationContext.getBeansOfType(SMSService.class);
        smsServiceBeanMap = new HashMap<>();
        map.forEach((key, value) -> smsServiceBeanMap.put(value.sendType(), value));
    }

    public static <T extends SMSService> T getSMSService(String sendType) {
        return (T) smsServiceBeanMap.get(sendType);
    }
}
