package com.qh.paythird.huiFuBao.utils;



import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName 
 * @Description 百信达支付常量
 * @Date 2017年11月9日 上午10:58:15
 * @version 1.0.0
 */
public class HuifubaoConst {
	 /***Huifubao 交易类型 B2B-网银 UNION_B2B***/
    public static final String payType_UNION_B2B = "UNION_B2B";
    /***Huifubao 交易类型 B2C-储蓄卡 UNION_B2C_SAVINGS***/
    public static final String payType_UNION_B2C_SAVINGS = "UNION_B2C_SAVINGS";
    /***Huifubao 交易类型 B2C-信用卡 UNION_B2C_CREDIT****/
    public static final String payType_UNION_B2C_CREDIT = "UNION_B2C_CREDIT";
    /***Huifubao 交易类型 委托代付 ISSU****/
    public static final String payType_ISSU = "ISSU";
    /*** bopay交易类型QQ扫码支付 ***/
    public static final String payType_QQ_SCAN = "QQ_NATIVE";
    
    /***Huifubao 交易状态 tradeState 待支付-下单成功 WAIT **/
    public static final String tradeState_WAIT = "WAIT";
    /***Huifubao 交易状态 tradeState 支付成功 SUC  **/
    public static final String tradeState_SUC = "result=1";
    /***Huifubao 交易状态 tradeState 支付失败 WAIT  */
    public static final String tradeState_FAIL = "result=-1";
    /***Huifubao 交易状态 tradeState 支付关闭 CLOSE  */
    public static final String tradeState_CLOSE = "CLOSE";
    /***Huifubao 交易状态 tradeState 处理中 WAIT  */
    public static final String tradeState_HANDLE = "result=0";
    
    /***Huifubao 银行卡类型 cardType WAIT 电子钱包***/
    public static final String cardType_WAIT = "WAIT";
    /***Huifubao 银行卡类型 cardType SAVINGS 储蓄卡***/
    public static final String cardType_SAVINGS = "SAVINGS";
    /***Huifubao 银行卡类型 cardType CREDIT 信用卡***/
    public static final String cardType_CREDIT = "CREDIT";
    
    /***Huifubao 银行卡类型 cardType CREDIT 信用卡***/
    public static final String hfb_childNo = "hfb_childNo";
    /***Huifubao 网关key***/
    public static final String hfb_key = "hfb_key";
    /***Huifubao 代付key***/
    public static final String hfb_df_key = "hfb_df_key";
    
    public static final String hfb_df_3deskey = "hfb_df_3deskey";
    /***Huifubao 银行卡类型 cardType CREDIT 信用卡***/
    public static final String hfb_privateKey = "hfb_privateKey";
    /***Huifubao 银行卡类型 cardType CREDIT 信用卡***/
    public static final String hfb_req_DfURL = "hfb_req_DfURL";
    /***Huifubao 代付查询地址***/
    public static final String hfb_query_DfURL = "hfb_query_DfURL";
    /***Huifubao 银行卡类型 cardType CREDIT 信用卡***/
    public static final String hfb_resURL = "hfb_resURL";
    /***Huifubao  网银请求地址***/
    public static final String hfb_res_wyURL = "hfb_res_wyURL";
    public static final String hfb_query_wyURL = "hfb_query_wyURL";
    /***当前接口版本号***/
    public static final String hfb_version = "hfb_version";
    public static final String hfb_pay_type = "hfb_pay_type";
    //付款理由
    public static final String hfb_df_reason = "hfb_df_reason";


    /**
     * 微信wap商户的key
     */
    public static final String PAYMERCHAT_KEY = "hfb_public_key";

    /**
     * 微信wap支付接口版本
     */
    public static final String VERSION_WAP = "hfb_wap_version";

    /**
     * 微信wap支付接口请求地址
     */
    public static final String REQ_WAP_URL = "hfb_wap_url";

    /**
     * 商户访问网站地址
     */
    public static final String Merchant_REQ_WAP_URL = "hfb_merchant_req_ip";

    /**
     * 商户访问网站地址
     */
    public static final String Merchant_REQ_WAP_NAME = "hfb_merchant_req_name";

    /**查询地址
     *
     */
    public static final String REQ_QUERY_WAP_URL = "hfb_query_url";




    /***对公标示 **/
    public static final String accType_yhk = "1";
    /***对私标示**/
    public static final String accType_PUB = "0";
    
    public static final String unionpayNo = "301551000405";
    
    /***Huifubao  bankNumber 银行代码映射**/
    public static final  Map<String,String> bankNumberMap = new HashMap<>();
    static{
    	bankNumberMap.put("ICBC","1");
    	bankNumberMap.put("ABC","3");
    	bankNumberMap.put("BOC","5");
    	bankNumberMap.put("CCB","2");
    	bankNumberMap.put("BCOM","6");
    	bankNumberMap.put("CMB","7");
    	bankNumberMap.put("GDB","11");
    	bankNumberMap.put("CITIC","12");
    	bankNumberMap.put("CMBC","14");
    	bankNumberMap.put("CEB","8");
    	bankNumberMap.put("PABC","18");
    	bankNumberMap.put("PSBC","4");
    	bankNumberMap.put("HXB","10");
    	bankNumberMap.put("CIB","13");
    	bankNumberMap.put("BOB","33");
    	bankNumberMap.put("BOS","16");
    	bankNumberMap.put("BRCB","49");
    
    }
}
