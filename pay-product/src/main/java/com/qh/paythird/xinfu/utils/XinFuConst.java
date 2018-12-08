package com.qh.paythird.xinfu.utils;

/**
 * 芯付常量
 * @author Swell
 *
 */
public class XinFuConst {

	public static final String ORDER_STATUS_SUCC = "1";
	public static final String ORDER_STATUS_ERROR = "2";
	public static final String ORDER_STATUS_WAIT = "0";
	
	/**
	 * 支付方式——微信支付
	 */
	public static final String WAY_WX = "wei_qr";
	/**
	 * 支付方式——QQ支付
	 */
	public static final String WAY_QQ = "qq_qr";
	/**
	 * 支付方式——京东快捷支付
	 */
	public static final String WAY_JD = "jd_qr";
	/**
	 * 支付方式——支付宝支付
	 * 暂时不支持
	 */
	public static final String WAY_ALI = "ali_qr";
	
	/**
	 * APP ID
	 */
	public static final String APPID = "xinfu_appid";
	
	/**
	 * 支付密钥
	 */
	public static final String KEY = "xinfu_key";
	
	/**
	 * 支付接口版本
	 */
	public static final String VERSION = "xinfu_version";
	
	/**
	 * 预下单地址
	 */
	public static final String DOWN_ORDER = "xinfu_down_order";
	
	/**
	 * 获取支付二维码地址
	 */
	public static final String GET_CODE = "xinfu_get_code";
	
	/**
	 * 查询订单地址
	 */
	public static final String QUERY_STATUS = "xinfu_query_status";
}
