package com.qh.sms.service.impl;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.qh.common.config.CfgKeyConst;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.service.RedisUtil;
import com.qh.sms.SMSConstants;
import com.qh.sms.service.SMSService;
import com.qh.sms.template.SMSTmplConstants;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SMSServiceTXImpl implements SMSService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SMSServiceTXImpl.class);

    @Override
    public boolean sendMessage(String phone, String msg) {
        String appId = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_appid);
        if (ParamUtil.isEmpty(appId)) {
            logger.error("请在系统参数中配置sms_tx_appid");
            return false;
        }
        String appKey = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_appkey);
        if (ParamUtil.isEmpty(appKey)) {
            logger.error("请在系统参数中配置sms_tx_appkey");
            return false;
        }

        String signName = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_sign_name);
        if (ParamUtil.isEmpty(signName)) {
            signName = "";
        }

        SmsSingleSenderResult singleSenderResult = null;

        try {
            //初始化单发
            SmsSingleSender singleSender = new SmsSingleSender(Integer.parseInt(appId), appKey);

            System.out.println("----------------------------------------------------------------------------------");
            System.out.println(signName + " " + msg);
            System.out.println("----------------------------------------------------------------------------------");

            //普通单发
            singleSenderResult = singleSender.send(0, "86", phone, signName + " " + msg, "", "");
        } catch (Exception e) {
            logger.error("短信发送失败，phone:" + phone + ",result:" + singleSenderResult.toString(), e);
        }

        if (singleSenderResult.result == 0) {
            return true;
        } else {
            logger.error("短信发送失败，phone:" + phone + ",result:" + singleSenderResult.toString());
            return false;
        }
    }


    @Override
    public boolean sendMessageByTemplate(String phone, String templateCode, ArrayList<String> params) {
        String appId = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_appid);
        if (ParamUtil.isEmpty(appId)) {
            logger.error("请在系统参数中配置sms_tx_appid");
            return false;
        }
        String appKey = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_appkey);
        if (ParamUtil.isEmpty(appKey)) {
            logger.error("请在系统参数中配置sms_tx_appkey");
            return false;
        }

        String signName = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_sign_name);
        if (ParamUtil.isEmpty(signName)) {
            signName = "";
        }

        SmsSingleSenderResult singleSenderResult = null;

        try {
            SmsSingleSender singleSender = new SmsSingleSender(Integer.parseInt(appId), appKey);
            singleSenderResult = singleSender.sendWithParam("86", phone, Integer.parseInt(templateCode), params, signName, "", "");
        } catch (Exception e) {
            logger.error("短信发送失败，phone:" + phone + ",result:" + singleSenderResult.toString(), e);
        }

        if (singleSenderResult.result == 0) {
            logger.info("短信发送成功，phone:" + phone + ",result:" + singleSenderResult.toString());
            return true;
        } else {
            logger.error("短信发送失败，phone:" + phone + ",result:" + singleSenderResult.toString());
            return false;
        }
    }

    @Override
    public boolean sendMessageReg(String phone, String validcode) {
        String tmplCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_code_reg);
        String ctx = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_ctx_default_reg);
        if (ParamUtil.isEmpty(tmplCode) && ParamUtil.isEmpty(ctx)) {
            ctx = SMSTmplConstants.getTmplStr(SMSConstants.sms_templete_code_default_reg);
        }
        return sendMessage(phone, String.format(ctx, validcode));
    }

    @Override
    public boolean sendMessageConfirm(String phone, String opt, String validcode) {
        String tmplCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_code_confirm);
        String ctx = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_ctx_default_confirm);
        if (ParamUtil.isEmpty(tmplCode) && ParamUtil.isEmpty(ctx)) {
            ctx = SMSTmplConstants.getTmplStr(SMSConstants.sms_templete_code_default_confirm);
        }
        return sendMessage(phone, String.format(ctx, opt, validcode));
    }

    @Override
    public boolean sendMessageNotify(String phone, String context) {
        String tmplCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_code_notify);
        String ctx = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_tx_tmpl_ctx_default_notify);
        if (ParamUtil.isEmpty(tmplCode) && ParamUtil.isEmpty(ctx)) {
            ctx = SMSTmplConstants.getTmplStr(SMSConstants.sms_templete_code_default_notify);
        }
        return sendMessage(phone, String.format(ctx, context));
    }

    @Override
    public String sendType() {
        return SMSConstants.sms_send_type_tx;
    }
}
