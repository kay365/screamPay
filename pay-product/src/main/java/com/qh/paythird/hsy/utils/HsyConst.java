package com.qh.paythird.hsy.utils;

public class HsyConst {
    /***汇收银 method 统一下单heemoney.pay.applypay ***/
    public static final String hsy_method_applypay = "heemoney.pay.applypay";
    /***汇收银 method 统一下单heemoney.pay.query ***/
    public static final String hsy_method_query = "heemoney.pay.query";
    /***汇收银 版本号****/
    public static final String hsy_version_default = "1.0";
    /***汇收银 版本号****/
    public static final String hsy_charset_default = "utf-8";
    /***汇收银签名类型****/
    public static final String hsy_sign_type = "md5";
    /***汇收银通道 微信扫码****/
    public static final String hsy_WX_NATIVE = "WX_NATIVE";
    /***汇收银通道 微信公众号****/
    public static final String hsy_WX_JSAPI = "WX_JSAPI";
    /***汇收银通道 微信h5****/
    public static final String hsy_WX_H5 = "WX_H5";
    /***汇收银通道 微信刷卡****/
    public static final String hsy_WX_MICROPAY = "WX_MICROPAY";
    /***汇收银通道 支付宝扫码****/
    public static final String hsy_ALI_QRCODE = "ALI_QRCODE";
    /***汇收银通道 支付宝WAP****/
    public static final String hsy_ALI_WAP = "ALI_WAP";
    /***汇收银通道 支付宝刷卡****/
    public static final String hsy_ALI_SWIPEE = "ALI_SWIPE";
    /***汇收银通道 QQ扫码****/
    public static final String hsy_QQ_QRCODE = "QQ_QRCODE";
    /***汇收银通道 快捷支付封顶****/
    public static final String hsy_BANK_QUICK_PAY_FD = "BANK_QUICK_PAY_FD";
    /***汇收银通道 快捷支付不封顶****/
    public static final String hsy_BANK_QUICK_PAY_BFD= "BANK_QUICK_PAY_BFD";
    /***汇收银 返回 成功****/
    public static final String hsy_ret_code_succ = "SUCCESS";
    /***汇收银 返回 未支付****/
    public static final String hsy_ret_code_undeal = "Undeal";
    /***汇收银 返回 失败****/
    public static final String hsy_ret_code_faild = "Failure";
    /***汇收银 二维码链接地址****/
    public static final String hsy_code_url = "code_url";
    /***汇收银 支付链接地址****/
    public static final String hsy_pay_url = "hy_pay_url";
    /***汇收银 公众号支付链接地址****/
    public static final String hsy_gzh_url = "hy_js_auth_pay_url";

    /***汇收银 支付统一下单请求地址*****/
    public static final String hsy_requrl = "hsy_requrl";
    /***汇收银 支付订单查询请求地址*****/
    public static final String hsy_queryurl = "hsy_queryurl";
    /***汇收银 应用ID，商户的应用id*****/
    public static final String hsy_app_id = "hsy_app_id";
    /***汇收银 key***/
    public static final String hsy_key = "hsy_key";
    /***汇收银 应用ID，商户统一编号*****/
    public static final String hsy_mch_uid = "hsy_mch_uid";
    /***汇收银 后台通知地址*****/
    public static final String hsy_notify_url = "hsy_notify_url";
}
