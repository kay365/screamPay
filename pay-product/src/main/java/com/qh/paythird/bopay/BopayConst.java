package com.qh.paythird.bopay;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName BopayConstants
 * @Description bopay支付常量
 * @Date 2017年11月9日 上午10:58:15
 * @version 1.0.0
 */
public class BopayConst {
	 /***bopay 交易类型 B2B-网银 UNION_B2B***/
    public static final String payType_UNION_B2B = "UNION_B2B";
    /***bopay 交易类型 B2C-储蓄卡 UNION_B2C_SAVINGS***/
    public static final String payType_UNION_B2C_SAVINGS = "UNION_B2C_SAVINGS";
    /***bopay 交易类型 B2C-信用卡 UNION_B2C_CREDIT****/
    public static final String payType_UNION_B2C_CREDIT = "UNION_B2C_CREDIT";
    /***bopay 交易类型 委托代付 ISSU****/
    public static final String payType_ISSU = "ISSU";
    /*** bopay交易类型QQ扫码支付 ***/
    public static final String payType_QQ_SCAN = "QQ_NATIVE";
    
    /***bopay 交易状态 tradeState 待支付-下单成功 WAIT **/
    public static final String tradeState_WAIT = "WAIT";
    /***bopay 交易状态 tradeState 支付成功 SUC  **/
    public static final String tradeState_SUC = "SUC";
    /***bopay 交易状态 tradeState 支付失败 WAIT  */
    public static final String tradeState_FAIL = "FAIL";
    /***bopay 交易状态 tradeState 支付关闭 CLOSE  */
    public static final String tradeState_CLOSE = "CLOSE";
    /***bopay 交易状态 tradeState 处理中 WAIT  */
    public static final String tradeState_HANDLE = "HANDLE";
    
    /***bopay 银行卡类型 cardType WAIT 电子钱包***/
    public static final String cardType_WAIT = "WAIT";
    /***bopay 银行卡类型 cardType SAVINGS 储蓄卡***/
    public static final String cardType_SAVINGS = "SAVINGS";
    /***bopay 银行卡类型 cardType CREDIT 信用卡***/
    public static final String cardType_CREDIT = "CREDIT";
    
    /***账户行政  对私 PRI**/
    public static final Object accType_PRI = "PRI";
    /***账户行政  对公 PUB**/
    public static final Object accType_PUB = "PUB";
    
    /***bopay  bankNumber 银行代码映射**/
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
