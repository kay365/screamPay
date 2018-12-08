package com.qh.paythird.tx.utils;

public class TXPayConst {

    // 接口域名，测试访问域名（正式环境改为：api.tfb8.com）
    public static final String HOST_NAME = "tx_host_name";

    // MD5密钥，国采分配给商户的用于签名和验签的key
    public static final String KEY = "tx_key";

    // 商户/平台在国采注册的账号。国采维度唯一，固定长度10位,1800212300
    public static final String SHOP_ID = "tx_shop_id";

    // 用户号，持卡人在商户/平台注册的账号。商户/平台维度唯一，必须为纯数字
    public static final String SHOP_USER_ID = "tx_shop_user_id";

    // 服务器编码类型
    public static final String SERVICE_ENCODE = "tx_service_encode";

    // 订单有效时长，以国采服务器时间为准的订单有效时间长度。单位:秒，如果不填则采用默认值
    public static final String EXPIRE_TIME  = "tx_expire_time";

    // 订单金额的类型。1 – 人民币(单位: 分)
    public static final String CUR_TYPE = "tx_cur_type";

    // 商户的用户使用的终端类型。1 – PC端，2 – 手机端
    public static final String CHANNEL = "tx_channel";

    // 签名的方法。目前支持: MD5，RSA
    public static final String ENCODE_TYPE = "tx_encode_type";

    // 当申请过程中出现异常时展示错误信息的页面地址，如果为空将展示国采自己的错误提示页面。(不能含有’&=等字符)
    public static final String ERROR_PAGE_URL = "tx_error_page_url";

    // 商户的私钥,需要PKCS8格式
    public static final String PRIMARY_KEY = "tx_primary_key";

    // 天付宝公钥
    public static final String PUBLIC_KEY = "tx_public_key";

    // 银行卡快捷签约支付申请调用的接口名
    public static final String CARD_PAY_API = "tx_card_pay_api";

    // 支付结果单笔查询
    public static final String ORDER_QUERY_API = "tx_order_query_api";

}
