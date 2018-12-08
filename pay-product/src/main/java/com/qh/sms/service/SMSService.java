package com.qh.sms.service;

import java.util.ArrayList;

/**
 * 短信服务接口
 *
 * @Author chensi
 * @Time 2017/12/19 15:07
 */
public interface SMSService {

    /**
     * 发送短信通用接口
     *
     * @param phone   手机号
     * @param context 信息内容
     * @return 短信是否成功标识
     */
    boolean sendMessage(String phone, String context);

    boolean sendMessageByTemplate(String phone, String templateCode, ArrayList<String> params);

    boolean sendMessageReg(String phone, String validCode);

    boolean sendMessageConfirm(String phone, String opt, String validCode);

    boolean sendMessageNotify(String phone, String context);

    String sendType();
}
