package com.qh.common.config;

/**
 * @ClassName ConfigKeyConstant
 * @Description 系统级别的配置常量key
 * @Date 2017年10月30日 下午4:33:16
 * @version 1.0.0
 */
public class CfgKeyConst {
	/***系统配置参数 ip域名******/
    public static final String ip = "ip";
    /***系统配置文件路径***/
    public static final String payFilePath = "payFilePath";
    /***通道分类**/
    public static final String pay_channel_type = "pay_channel_type";
    /***聚富公钥***/
    public static final String qhPublicKey = "qhPublicKey";
    /***聚富私钥***/
    public static final String qhPrivateKey = "qhPrivateKey";
    /***发送邮箱信息***/
    public static final String email_message = "email_message";

    /***聚富公钥路径****/
    public static final String  publicKeyPath = "publicKeyPath";
    /***聚富私钥路径****/
    public static final String privateKeyPath = "privateKeyPath";
    /***聚富商户默认密码***/
    public static final String pass_default_merch = "pass_default_merch";
    /***聚富商户默认状态***/
    public static final String state_default_merch = "state_default_merch";
    /***聚富支付域名****/
    public static final String pay_domain = "pay_domain";
    /***聚富前台回调设置***/
    public static final String pay_return_url = "pay_return_url";
    /***聚富后台通知设置***/
    public static final String pay_notify_url = "pay_notify_url";
    /***聚富支付跳转中间页面***/
    public static final String pay_jump_url = "pay_jump_url";
    /***聚富支付跳转绑卡页面***/
    public static final String pay_card_url = "pay_card_url";
    /***聚富扫码通道跳转扫码页面**********/
    public static final String pay_qr_url = "pay_qr_url";
    /***聚富代付前台回调设置***/
    public static final String pay_acp_return_url = "pay_acp_return_url";
    /***聚富代付后台通知设置***/
    public static final String pay_acp_notify_url = "pay_acp_notify_url";
    /***聚富扫码通道二维码路径***/
    public static final String qr_money_path = "qr_money_path";

    /***使用哪个短信发送平台***/
    public static final String sms_send_type = "sms_send_type";

    /****************************************************短信配置********************************************/

    /***阿里云注册模板编号***/
    public static final String sms_aliy_tmpl_code_reg = "sms_aliy_tmpl_code_reg";
    /***阿里云注册模板内容  未配置sms_aliy_tmpl_code_reg时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_reg = "sms_aliy_tmpl_ctx_default_reg";
    /***阿里云确认模板编号***/
    public static final String sms_aliy_tmpl_code_confirm = "sms_aliy_tmpl_code_confirm";
    /***阿里云确认模板默认内容  未配置sms_aliy_tmpl_code_confirm时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_confirm = "sms_aliy_tmpl_ctx_default_confirm";
    /***阿里云通知模板编号***/
    public static final String sms_aliy_tmpl_code_notify = "sms_aliy_tmpl_code_notify";
    /***阿里云确认模板默认内容  未配置sms_aliy_tmpl_code_notify时生效 ***/
    public static final String sms_aliy_tmpl_ctx_default_notify = "sms_aliy_tmpl_ctx_default_notify";

    /***阿里云短信账号***/
    public static final String sms_aliy_accesskey_id = "sms_aliy_accesskey_id";
    /***阿里云短信密码***/
    public static final String sms_aliy_accesskey_secret = "sms_aliy_accesskey_secret";
    /***阿里云短信签名***/
    public static final String sms_aliy_sign_name = "sms_aliy_sign_name";

    /***发件人邮箱（用来发送邮件的邮箱地址）***/
    public static final String email_account = "email_account";
    /***发件人邮箱smtp授权码***/
    public static final String email_password = "email_password";
    /***发件人邮箱的 SMTP 服务器地址***/
    public static final String email_smtp_host = "email_smtp_host";


    /***腾讯云注册模板编号***/
    public static final String sms_tx_tmpl_code_reg = "sms_tx_template_code_reg";
    /***腾讯云注册模板默认内容  未配置sms_tx_tmpl_code_reg时生效 ***/
    public static final String sms_tx_tmpl_ctx_default_reg = "sms_tx_tmpl_ctx_default_reg";
    /***腾讯云确认模板编号***/
    public static final String sms_tx_tmpl_code_confirm = "sms_tx_tmpl_code_confirm";
    /***腾讯云确认模板默认内容  未配置sms_tx_tmpl_code_confirm时生效 ***/
    public static final String sms_tx_tmpl_ctx_default_confirm = "sms_tx_tmpl_ctx_default_confirm";
    /***腾讯云通知模板编号***/
    public static final String sms_tx_tmpl_code_notify = "sms_tx_tmpl_code_notify";
    /***腾讯云通知模板默认内容  未配置sms_tx_tmpl_code_notify时生效***/
    public static final String sms_tx_tmpl_ctx_default_notify = "sms_tx_tmpl_ctx_default_notify";

    /***腾讯云短信appid***/
    public static final String sms_tx_appid = "sms_tx_appid";
    /***腾讯云短信appkey***/
    public static final String sms_tx_appkey = "sms_tx_appkey";
    /***腾讯云短信签名***/
    public static final String sms_tx_sign_name = "sms_tx_sign_name";
    
    public static final String sms_send_type_tx = "tx";
    public static final String sms_send_type_aliy = "aliy";
    
    /**修改密码验证码key**/
    public static final String sms_code_phone_update_pass = "sms_code_phone_update_pass_";

    /******************************************************************************************************/
    
    /**支付通道单次轮洵比例**/
    public static final String COMPANY_SIGLE_POLL_MONEY = "company_single_poll_money";
    
    /**支付通道 上一次使用索引**/
    public static final String COMPANY_USE_INDEX = "company_use_index_";
    
    /**下发自动审核配置**/
    public static final String PAY_AUDIT_AUTO_ACP = "pay_audit_auto_acp";
    
    /**商户单日 和单月限额key**/
    public static final String MERCHANT_DAY_LIMIT = "merchant_day_limit_";
    public static final String MERCHANT_MONTH_LIMIT = "merchant_month_limit_";
    
    /**是否关闭支付**/
    public static final String COMPANY_CLOSE_PAY = "company_close_pay";
    /**是否关闭下发**/
    public static final String COMPANY_CLOSE_ACP = "company_close_acp";
    /**是否关闭提现**/
    public static final String COMPANY_CLOSE_WITHDRAW = "company_close_withdraw";
    
    /**订单金额 3秒之内不能再次请求 **/
    public static final String ORDER_MONEY_THREE_SECONDS = "order_money_three_seconds_";

}
