package com.qh.sms.service;

import com.qh.common.config.CfgKeyConst;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.service.RedisUtil;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SMSUtils {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SMSUtils.class);

    private static boolean sendTypeNotEmpty(String sendType) {
        if (!ParamUtil.isNotEmpty(sendType)) {
            logger.error("请在系统参数中配置sms_send_type");
            return false;
        }
        return true;
    }

    public static boolean sendMessage(String phone, String context) {
        String sendType = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_send_type);
        if (sendTypeNotEmpty(sendType)) {
            return SMSServiceFactory.getSMSService(sendType).sendMessage(phone, context);
        }
        return false;
    }

    public static boolean sendMessageCustomByTmplCode(String phone, String templateCode, ArrayList<String> params) {
        String sendType = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_send_type);
        if (sendTypeNotEmpty(sendType)) {
            return SMSServiceFactory.getSMSService(sendType).sendMessageByTemplate(phone, templateCode, params);
        }
        return false;
    }


    public static boolean sendMessageReg(String phone, String validcode) {
        String sendType = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_send_type);
        if (sendTypeNotEmpty(sendType)) {
            return SMSServiceFactory.getSMSService(sendType).sendMessageReg(phone, validcode);
        }
        return false;
    }

    public static boolean sendMessageConfirm(String phone, String opt, String validcode) {
        String sendType = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_send_type);
        if (sendTypeNotEmpty(sendType)) {
            return SMSServiceFactory.getSMSService(sendType).sendMessageConfirm(phone, opt, validcode);
        }
        return false;
    }

    public static boolean sendMessageNotify(String phone, String context) {
        String sendType = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_send_type);
        if (sendTypeNotEmpty(sendType)) {
            return SMSServiceFactory.getSMSService(sendType).sendMessageNotify(phone, context);
        }
        return false;
    }
}
