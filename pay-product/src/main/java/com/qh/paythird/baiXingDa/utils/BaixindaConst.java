package com.qh.paythird.baiXingDa.utils;


import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName 
 * @Description 百信达支付常量
 * @Date 2017年11月9日 上午10:58:15
 * @version 1.0.0
 */
public class BaixindaConst {
	 /***baixinda 交易类型 B2B-网银 UNION_B2B***/
    public static final String payType_UNION_B2B = "UNION_B2B";
    /***baixinda 交易类型 B2C-储蓄卡 UNION_B2C_SAVINGS***/
    public static final String payType_UNION_B2C_SAVINGS = "UNION_B2C_SAVINGS";
    /***baixinda 交易类型 B2C-信用卡 UNION_B2C_CREDIT****/
    public static final String payType_UNION_B2C_CREDIT = "UNION_B2C_CREDIT";
    /***baixinda 交易类型 委托代付 ISSU****/
    public static final String payType_ISSU = "ISSU";
    /*** bopay交易类型QQ扫码支付 ***/
    public static final String payType_QQ_SCAN = "QQ_NATIVE";
    
    /***baixinda 交易状态 tradeState 待支付-下单成功 WAIT **/
    public static final String tradeState_WAIT = "WAIT";
    /***baixinda 交易状态 tradeState 支付成功 SUC  **/
    public static final String tradeState_SUC = "000000";
    /***baixinda 交易状态 tradeState 支付失败 WAIT  */
    public static final String tradeState_FAIL = "FAIL";
    /***baixinda 交易状态 tradeState 支付关闭 CLOSE  */
    public static final String tradeState_CLOSE = "CLOSE";
    /***baixinda 交易状态 tradeState 处理中 WAIT  */
    public static final String tradeState_HANDLE = "HANDLE";
    
    /***baixinda 银行卡类型 cardType WAIT 电子钱包***/
    public static final String cardType_WAIT = "WAIT";
    /***baixinda 银行卡类型 cardType SAVINGS 储蓄卡***/
    public static final String cardType_SAVINGS = "SAVINGS";
    /***baixinda 银行卡类型 cardType CREDIT 信用卡***/
    public static final String cardType_CREDIT = "CREDIT";
    
    /***baixinda 银行卡类型 cardType CREDIT 信用卡***/
    public static final String bxd_childNo = "bxd_childNo";
    /***baixinda 银行卡类型 cardType CREDIT 信用卡***/
    public static final String bxd_key = "bxd_key";
    /***baixinda 银行卡类型 cardType CREDIT 信用卡***/
    public static final String bxd_privateKey = "bxd_privateKey";
    /***baixinda ***/
    public static final String bxd_resURL = "bxd_resURL";
    /***baixinda wap支付请求地址***/
    public static final String bxd_resH5_url = "bxd_resH5_url";
    /***baixinda 银联二维码支付请求地址***/
    public static final String bxd_reqYl_url = "bxd_reqYl_url";
    /***baixinda 银联二维码支付请求地址***/
    public static final String bxd_acp_url = "bxd_acp_url";
    /***baixinda 渠道编码***/
    public static final String bxd_channl_no = "bxd_channl_no";
    
    
    
    /***对公标示  银行卡**/
    public static final String accType_yhk = "00";
    /***对公标示  对公 PUB**/
    public static final String accType_PUB = "02";
    
    public static final String unionpayNo = "301551000405";
    /***交易码**/
    public static final String tradeCode_acp = "DF0003";
    public static final String tradeCode_acp_query = "DF0004";
    public static final String tradeCode_pay = "PP1017";
    public static final String tradeCode_pay_query = "PP1024";
    
    /***baixinda  bankNumber 银行代码映射**/
    public static final  Map<String,String> bankNumberMap = new HashMap<>();
    static{
    	bankNumberMap.put("建设银行", "1004");
    	bankNumberMap.put("农业银行", "1002");
    	bankNumberMap.put("工商银行", "1001");
    	bankNumberMap.put("中国银行", "1003");
    	bankNumberMap.put("浦发银行", "1014");
    	bankNumberMap.put("光大银行", "1008");
    	bankNumberMap.put("平安银行", "1011");
    	bankNumberMap.put("兴业银行", "1013");
    	bankNumberMap.put("邮政储蓄银行", "1006");
    	bankNumberMap.put("中信银行", "1007");
    	bankNumberMap.put("华夏银行", "1009");
    	bankNumberMap.put("招商银行", "1012");
    	bankNumberMap.put("广发银行", "1017");
    	bankNumberMap.put("北京银行", "1016");
    	bankNumberMap.put("上海银行", "1025");
    	bankNumberMap.put("民生银行", "1010");
    	bankNumberMap.put("交通银行", "1005");
    	bankNumberMap.put("北京农村商业银行", "1103");
    }
}
