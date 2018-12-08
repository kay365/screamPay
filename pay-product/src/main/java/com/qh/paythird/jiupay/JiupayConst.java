package com.qh.paythird.jiupay;

/**
 * @ClassName JiupayConst
 * @Description 九派常量
 * @Date 2017年11月22日 下午6:31:08
 * @version 1.0.0
 */
public class JiupayConst {
	/***sdk.signType签名密码 rsa****/
	public static final String sdk_signType_rsa = "RSA256";
	/***sdk.version**/
	public static final String sdk_version = "1.0";
	/***sdk.version**/
	public static final String sdk_version1 = "1.1";
	/***sdk.charset***/
	public static final String sdk_charset = "02";
	/***支付请求类型 rpmBankPayment 网银支付*/
	public static final String service_rpmBankPayment = "rpmBankPayment";
	/***支付请求类型 rpmQuickPayInit 快捷支付预下单***/
	public static final String service_rpmQuickPayInit = "rpmQuickPayInit";
	/***支付请求类型 rpmQuickPayCommit 快捷支付 支付确认***/
	public static final String service_rpmQuickPayCommit = "rpmQuickPayCommit";
	/***证件类型 身份证**/
	public static final String idType_identity = "00";
	/***支付请求类型 rpmBankList 获取银行卡列表***/
	public static final String service_rpmBankList = "rpmBankList";
	/***支付请求类型 rpmBindCard 绑卡请求**/
	public static final String service_rpmBindCard = "rpmBindCard";
	/***支付请求类型 rpmBindCardInit 绑卡请求发送短信**/
	public static final String service_rpmBindCardInit= "rpmBindCardInit";
	/***支付请求类型 rpmBindCardCommit 绑卡短信验证确认******/
	public static final String service_rpmBindCardCommit = "rpmBindCardCommit";
	/***支付请求类型 rpmQuickPaySms 短信重发*****/
	public static final String service_rpmQuickPaySms = "rpmQuickPaySms";
	/***支付请求类型 支付查询****/
	public static final String service_rpmPayQuery = "rpmPayQuery";
	/***代付请求类型 九派单笔代付****/
	public static final String service_capSingleTransfer = "capSingleTransfer";
	/***代付请求类型 九派单笔代付查询******/
	public static final String service_capOrderQuery = "capOrderQuery";
	
	/***银行卡类型 借记卡 0**/
	public static final String cardType_savings = "00";
	/***银行卡类型 信用卡 1**/
	public static final String cardType_credit = "01";
	/***快捷支付银行卡类型 借记卡快捷**/
	public static final String cardType_DQP = "DQP";
	/***快捷支付银行卡类型 信用卡快捷**/
	public static final String cardType_CQP = "CQP";
	/***支付类型   B2B***/
	public static final String payType_B2B = "B2B";
	/***支付类型   B2C***/
	public static final String payType_B2C = "B2C";
	/***支付订单 有效期**/
	public static final int validNum = 30;
	/***支付订单 有效期单位 分钟***/
	public static final String validUnit_min = "00";
	/***支付订单 有效期单位 小时***/
	public static final String validUnit_h = "01";
	/***支付订单 有效期单位 天***/
	public static final String validUnit_d = "02";
	/***支付订单 有效期单位 月***/
	public static final String validUnit_m = "03";
	/***卡状态 生效 0 **/
	public static final String cardSts_effect = "0";
	/***卡状态 无效 0 **/
	public static final String cardSts_disabled = "1";
	/***卡状态 删除 2 **/
	public static final String cardSts_del = "2";
	/***卡状态 短信待验证 3 **/
	public static final String cardSts_msg_valid = "3";
	
	/***商户签名**/
	public static final String param_merchantSign = "merchantSign";
	/***商户证书**/
	public static final String param_merchantCert = "merchantCert";
	
	/***九派签名**/
	public static final String param_serverSign = "serverSign";
	/***九派证书**/
	public static final String param_serverCert = "serverCert";
	/***九派返回结果**/
	public static final String rspCode_five_zero = "00000";
	
	
	/***订单状态 等待付款 WP******/
	public static final String orderSts_WP = "WP";
	/***订单状态 支付中 PP******/
	public static final String orderSts_PP = "PP";
	/***订单状态 支付完成 PD******/
	public static final String orderSts_PD = "PD";
	/***订单状态 订单关闭 CZ******/
	public static final String orderSts_CZ = "CZ";
	/***订单状态 订单过期 EX******/
	public static final String orderSts_EX = "EX";
	/***订单状态 交易取消 CA******/
	public static final String orderSts_CA = "CA";
	/***订单状态 订单退款 RF******/
	public static final String orderSts_RE = "RE";
	/***订单状态 全额退款成功 RF******/
	public static final String orderSts_RF = "RF";
	/***订单状态 部分退款成功 RP******/
	public static final String orderSts_RP = "RP";
	/***订单状态 退款已受理 RQ******/
	public static final String orderSts_RQ = "RQ";
	/***订单状态  订单不存在 NE******/
	public static final String orderSts_NE = "NE";
	/***订单状态 风控拒绝 RK******/
	public static final String orderSts_RK = "RK";
	/***订单状态 支付处理中 B2******/
	public static final String orderSts_B2 = "B2";
	/***订单状态 退款处理中 R1******/
	public static final String orderSts_R1 = "R1";
	/***返回成功状态 code IPS00000****/
	public static final String rspCode_succ = "IPS00000";
	
	/***代付账户类型 卡****/
	public static final String accType_card = "0";
	/***代付账户类型 对公账号****/
	public static final String accType_pub = "2";
	
	/***代付订单状态 订单初始化 U*****/
	public static final String orderSts_acp_U = "U";
	/***代付订单状态 处理中 P*****/
	public static final String orderSts_acp_P = "P";
	/***代付订单状态 处理成功 S*****/
	public static final String orderSts_acp_S = "S";
	/***代付订单状态 处理失败 F*****/
	public static final String orderSts_acp_F = "F";
	/***代付订单状态 退汇 R*****/
	public static final String orderSts_acp_R = "R";
	/***代付订单状态 待人工处理 N*****/
	public static final String orderSts_acp_N = "N";
	/***代付默认资金用途**/
	public static final Object capUser_default = "00";
	
}
