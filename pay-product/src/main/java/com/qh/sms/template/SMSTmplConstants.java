package com.qh.sms.template;

import com.qh.sms.SMSConstants;

import java.util.HashMap;

public class SMSTmplConstants {
    public static HashMap<String, String> map;

    private static void init() {
        if (map == null) {
            map = new HashMap<>();
            map.put(SMSConstants.sms_templete_code_default_reg, "您的验证码为%s，如非本人操作，请忽略。");
            map.put(SMSConstants.sms_templete_code_default_confirm, "您正在进行%s操作，您的验证码为%s。");
            map.put(SMSConstants.sms_templete_code_default_notify, "您的%s网关已掉线，请重新登录网关。");
        }
    }

    public static String getTmplStr(String tmplCode) {
        init();
        return map.get(tmplCode);
    }
}
