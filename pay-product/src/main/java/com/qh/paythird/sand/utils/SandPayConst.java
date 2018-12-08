package com.qh.paythird.sand.utils;

import com.qh.redis.service.RedisUtil;

/**
 * 衫德支付
 */
public class SandPayConst {

    // 支付地址
    public static final String PAY_URL = "sd_pay_url";
    // 代付地址
    public static final String ACP_URL = "sd_acp_url";
    // 代付地址
    public static final String sd_quickPay_url = "sd_quickPay_url";
    // 商户id
    public static final String SHOP_ID = "sd_shop_id";

    // 产品id
    public static final String PRODUCT_ID = "sd_product_id";

    // 证书
    public static final String CERT = "sd_cert";

    // 证书2
    public static final String PFX = "sd_pfx";

    // 密码
    public static final String PASSWORD = "sd_password";

    // 私钥
    public static final String PUBLIC_KEY = "sd_public_key";

    // 版本
    public static final String VERSION = "sd_version";

    // 商户门店编号
    public static final String STORE_ID = "sd_store_id";

    // 商户终端编号
    public static final String TERMINAL_ID = "sd_terminal_id";

    // 清算模式
    public static final String CLEAR_CYCLE = "sd_clear_cycle";

    // 订单查询地址
    public static final String ORDER_QUERY_URL = "sd_order_query_url";

    // 微信subAppid
    public static final String WX_SUB_APP_ID = "sd_wx_sub_app_id";

    // 微信用户Id
    public static final String WX_USER_ID = "sd_wx_user_id";

    // 获取密码
    public static final String getPassword(String key){
        return getPayKey(key);
    }

    /*// 获取证书2
    public static final String getPfx(){
        return getPayFilePathKey(PFX);
    }*/

    // 获取证书 lujing
    public static final String getCert(String name){
        return getPayFilePathKey(name);
    }

    // 获取微信ID
    public static final String getWxSubAppId(){
        return getPayKey(WX_SUB_APP_ID);
    }

    //获取微信用户id
    public static final String getWxUserId(){
        return getPayKey(WX_USER_ID);
    }

    // 获取公钥
    public static final String getPublicKey() {
        return getPayKey(PUBLIC_KEY);
    }

    // 获取订单查询地址
    public static final String getOrderQueryUrl() {
        return getPayKey(ORDER_QUERY_URL);
    }

    // 获取订单支付地址
    public static final String getPayUrl() {
        return getPayKey(PAY_URL);
    }

    // 获取商户终端编号
    public static final String getTerminalId() {
        return getPayKey(TERMINAL_ID);
    }

    // 获取商户门店编号
    public static final String getStoreId() {
        return getPayKey(STORE_ID);
    }

    // 获取清算模式
    public static final String getClearCycle() {
        return getPayKey(CLEAR_CYCLE);
    }

    // 获取支付参数
    private static final String getPayKey(String key) {
        return RedisUtil.getPayCommonValue(key);
    }
    // 获取支付参数
    private static final String getPayFilePathKey(String key) {
    	return RedisUtil.getPayFilePathValue(key);
    }

    // 获取版本号
    public static final String getVersion() {
        return getPayKey(VERSION);
    }

    // 获取产品id
    public static final String getProductId() {
        return getPayKey(PRODUCT_ID);
    }

    // 获取商户id
    public static final String getShopId() {
        return getPayKey(SHOP_ID);
    }


}
