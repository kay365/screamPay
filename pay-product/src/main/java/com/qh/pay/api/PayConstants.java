package com.qh.pay.api;

import java.math.BigDecimal;


/**
 * 
 * @ClassName Constants
 * @Description 用到的常量类
 * @Date 2017年5月30日 下午3:13:53
 * @version 1.0.0
 */
public class PayConstants {
	/***跳转脚本*****/
    public final static String web_eval_url = "eval_url";
    /***跳转 code_url**/
    public final static String web_code_url = "code_url";
    /***微信公众号 js api 参数**/
    public final static String web_payinfo = "payinfo";
    /***跳转 code_img_url**/
    public final static String web_code_img_url = "code_img_url";
    /***跳转 qrcode_url**/
    public final static String web_qrcode_url = "qrcode_url";
    /***跳转 跨公众号二维码支付**/
    public final static String web_gzh_qrcode_url = "gzh_qrcode_url";
    /***跳转 form_url**/
    public final static String web_form_url = "form_url";
    /***跳转 local_url**/
    public final static String web_local_url = "local_url";
    /***跳转 wap_url***/
    public final static String web_wap_url = "wap_url";
    /***确认 window******/
    public final static String web_local_confirm = "local_confirm";
    /***确认 window******/
    public final static String web_local_confirm_url = "local_confirm_url";
    
    /***提交跳转数据参数****/
    public final static String web_jumpData = "jumpData";
    /***提交密文参数***/
    public final static String web_context = "context";
    /***form提交表单***/
    public final static String web_action = "action";
    /***form提交参数***/
    public final static String web_params = "params";
    /***支付错误链接地址**/
    public final static String url_pay_error = "pay/error";
    /***支付跳转中间地址***/
    public final static String url_pay_jump = "pay/jump";
    /***支付跳转中间绑卡***/
    public final static String url_pay_card = "pay/card";
    /***支付跳转扫码通道界面****/
    public final static String url_pay_qr = "pay/qr";
    /***支付跳转充值（扫码通道）****/
    public final static String url_pay_charge = "pay/charge";
    /***支付跳转充值（扫码通道） 管理员人工充值入口****/
    public final static String url_pay_superCharge = "pay/superCharge";
    /***支付错误链接地址**/
    public final static String url_pay_error_frame = "pay/errorFrame";
    /***支付跳转提现*******/
    public final static String url_pay_withdraw = "pay/withdraw";
    /***任意金额******/
    public final static String qr_any_money_flag = "qrAnyMoneyFlag";
    /**
     * 是否为实时代付
     */
    public final static String acp_real_time = "acpRealTime";
    
    /***银行卡类型***/
	public final static String card_type = "cardType";
    /***银行卡列表  储蓄**/
	public final static String bank_savings = "bank_savings";
	/***银行卡列表  信用***/
	public final static String bank_credits = "bank_credits";
	/***签约详情**/
	public final static String userSign = "userSign";
	/***签约列表**/
	public final static String userSigns = "userSigns";
	/***银行代码描述**/
	public final static String bankCodeDesc = "bankCodeDesc";
	/***默认商户号****/
	public final static String pay_merchNo_default = "0";
	
	/**支付费率 json key**/
	public final static String PAYMENT_RATE = "rate";
	public final static String PAYMENT_UNIT = "unit";
	/**最低手续费率**/
	public final static BigDecimal MIN_FEE = new BigDecimal(0.2);
}
