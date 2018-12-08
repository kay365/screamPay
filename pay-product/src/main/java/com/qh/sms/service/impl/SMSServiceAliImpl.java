package com.qh.sms.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.qh.common.config.CfgKeyConst;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.service.RedisUtil;
import com.qh.sms.SMSConstants;
import com.qh.sms.service.SMSService;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * 短信服务接口阿里云平台实现
 *
 * @Author chensi
 * @Time 2017/12/19 15:14
 */
@Service
public class SMSServiceAliImpl implements SMSService {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SMSServiceAliImpl.class);

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";

    @Override
    public boolean sendMessage(String phone, String context) {
        return false;
    }

    @Override
    public boolean sendMessageByTemplate(String phone, String templateCode, ArrayList<String> params) {
        String accessKeyId = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_accesskey_id);
        if (ParamUtil.isEmpty(accessKeyId)) {
            logger.error("请在系统参数中配置sms_aliy_accesskey_id");
            return false;
        }

        String accessKeySecret = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_accesskey_secret);
        if (ParamUtil.isEmpty(accessKeySecret)) {
            logger.error("请在系统参数中配置sms_aliy_accesskey_secret");
            return false;
        }

        String signName = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_sign_name);
        if (ParamUtil.isEmpty(signName)) {
            signName = "";
        }

        //可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        try {
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        } catch (ClientException e) {
            e.printStackTrace();
        }
        IAcsClient acsClient = new DefaultAcsClient(profile);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phone);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName(signName);
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode(templateCode);
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        
        JSONObject tpJson = new JSONObject();
        tpJson.put("code", params.get(0));
        request.setTemplateParam(tpJson.toJSONString());

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        String smsId = ""+ParamUtil.generateCode8();
        request.setOutId(smsId);

        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
            logger.info("成功发送短信："+smsId);
        } catch (ClientException e) {
            logger.error("短信发送失败，phone:" + phone + ",result:" + sendSmsResponse.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean sendMessageReg(String phone, String validCode) {
        String regCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_tmpl_code_reg);
        if (ParamUtil.isEmpty(regCode)) {
            logger.error("请在系统参数中配置sms_aliy_tmpl_code_reg");
            return false;
        }
        ArrayList<String> params = new ArrayList<>();
        params.add(validCode);
        return sendMessageByTemplate(phone,regCode,params);
    }

    @Override
    public boolean sendMessageConfirm(String phone, String opt, String validCode) {
        String confirmCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_tmpl_code_confirm);
        if (ParamUtil.isEmpty(confirmCode)) {
            logger.error("请在系统参数中配置sms_aliy_tmpl_code_confirm");
            return false;
        }
        ArrayList<String> params = new ArrayList<>();
        params.add(opt);
        params.add(validCode);
        return sendMessageByTemplate(phone,confirmCode,params);
    }

    @Override
    public boolean sendMessageNotify(String phone, String context) {
        String notifyCode = RedisUtil.getSMSConfigValue(CfgKeyConst.sms_aliy_tmpl_code_notify);
        if (ParamUtil.isEmpty(notifyCode)) {
            logger.error("请在系统参数中配置sms_aliy_tmpl_code_notify");
            return false;
        }
        ArrayList<String> params = new ArrayList<>();
        params.add(context);
        return sendMessageByTemplate(phone,notifyCode,params);
    }

    @Override
    public String sendType() {
        return SMSConstants.sms_send_type_aliy;
    }
}
