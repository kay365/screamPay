package com.qh.paythird.sand.utils;

import com.qh.redis.service.RedisUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 衫德支付配置类
 */
public class SandAgencyPayConst {

    // 版本号
    public static final String VERSION = "sd_a_version";

    // 产品id
    public static final String PRODUCT_ID = "sd_a_product_id";

    // 币种
    public static final String CURRENCY_CODE = "sd_a_currency_code";

    // 账户属性 0 对私（默认） 1 对公
    public static final String ACC_ATTR = "sd_a_acc_attr";

    // 代付地址
    public static final String PAY_URL = "sd_a_pay_url";

    // 商户id
    public static final String SHOP_ID = "sd_a_shop_id";

    // 实时代付
    public static final String TRANS_CODE = "sd_a_trans_code";

    // 代付成功
    public static final String SUCCESS_CODE = "0";

    // 代付失败
    public static final String ERROR_CODE = "1";

    // 处理中
    public static final String ING_CODE = "2";


    public static final String QUERY_ORDER_URL = "sd_a_query_order_url";

    /**
     * 获取订单查询地址
     *
     * @return
     */
    public static String getQueryOrderUrl() {
        return getPayKey(QUERY_ORDER_URL);
    }


    /**
     * 实时代付
     *
     * @return
     */
    public static String getTransCode() {
        return getPayKey(SHOP_ID);
    }


    /**
     * 获取商户id
     *
     * @return
     */
    public static String getShopId() {
        return getPayKey(SHOP_ID);
    }

    /**
     * 获取代付地址
     *
     * @return
     */
    public static String getPayUrl() {
        return getPayKey(PAY_URL);
    }


    /**
     * 获取账户属性
     *
     * @return
     */
    public static String getAccAttr() {
        return getPayKey(ACC_ATTR);
    }



    /**
     * 获取版本号
     *
     * @return
     */
    public static String getVersion() {
        return getPayKey(VERSION);
    }

    /**
     * 获取币种
     *
     * @return
     */
    public static String getCurrencyCode() {
        return getPayKey(PRODUCT_ID);
    }

    /**
     * 获取产品ID
     *
     * @return
     */
    public static String getProductId() {
        return getPayKey(CURRENCY_CODE);
    }

    /**
     * 获取当前时间的格式
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }


    private static String getPayKey(String key) {
        return RedisUtil.getPayCommonValue(key);
    }

}
