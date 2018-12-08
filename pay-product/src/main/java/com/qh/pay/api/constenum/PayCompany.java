package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

import com.qh.common.config.Constant;

/**
 * @ClassName PayCompany
 * @Description 支付公司
 * @Date 2017年11月9日 上午9:44:10
 * @version 1.0.0
 */
public enum PayCompany {
	/**银生宝   快捷 网关  代付**/
	ysb,
	/**聚富****/
	jf,
	/**天付宝**/
	tfb,
	/**环迅**/
	hx,
	/**bopay**/
	bopay,
	/**九派***/
	jiupay,
	/**芯付**/
	xinfu,
	/**点芯**/
	dianxin,
	/**芯钱包**/
	xinqianbao,
	/**比可支付**/
	beecloud,
	/**汇收银**/
	hsy,
	/**小天支付(代付)**/
	xiaotian,
	/**摩宝**/
	mobao,
	/**百信达**/
	bxd,
	/**杉德**/
	sd,
	/**汇付宝**/
	hfb,
	/**吖吖谷**/
	yyg,
	/**威富通**/
	wft
	;
	private static final Map<String,String> descMap = new HashMap<>(10);
	
	private static final Map<String,PayCompany> enumMap = new HashMap<>(10);
	
	static{
		descMap.put(ysb.name(), "银生宝");
		enumMap.put(ysb.name(), ysb);
		
		descMap.put(tfb.name(), "天付宝");
		enumMap.put(tfb.name(), tfb);
		
		descMap.put(hx.name(), "环迅");
		enumMap.put(hx.name(), hx);
		
		descMap.put(bopay.name(), "bopay");
		enumMap.put(bopay.name(), bopay);
		
		descMap.put(jiupay.name(), "九派");
		enumMap.put(jiupay.name(), jiupay);
		
		descMap.put(xinfu.name(), "芯付");
		enumMap.put(xinfu.name(), xinfu);
		
		descMap.put(dianxin.name(), "点芯");
		enumMap.put(dianxin.name(), dianxin);
		
		descMap.put(xinqianbao.name(), "芯钱包");
		enumMap.put(xinqianbao.name(), xinqianbao);
		
		descMap.put(beecloud.name(), "比可");
		enumMap.put(beecloud.name(), beecloud);

		descMap.put(hsy.name(), "汇收银");
		enumMap.put(hsy.name(), hsy);
		
		descMap.put(xiaotian.name(), "小天");
		enumMap.put(xiaotian.name(), xiaotian);
		
		descMap.put(mobao.name(), "摩宝");
		enumMap.put(mobao.name(), mobao);
		
		descMap.put(bxd.name(), "百信达");
		enumMap.put(bxd.name(), bxd);
		
		descMap.put(sd.name(), "杉德");
		enumMap.put(sd.name(), sd);
		
		descMap.put(hfb.name(), "汇付宝");
		enumMap.put(hfb.name(), hfb);
		
		descMap.put(yyg.name(), "吖吖谷");
		enumMap.put(yyg.name(), yyg);
		
		descMap.put(wft.name(), "威富通");
		enumMap.put(wft.name(), wft);
	}
	
	/***当前支付公司*******/
	private static final Map<String,String> jfDescMap = new HashMap<>(4);
	static{
		jfDescMap.put(jf.name(), Constant.pay_name);
		enumMap.put(jf.name(), jf);
	}
	
	public static Map<String, String> jfDesc() {
		return jfDescMap;
	}
	
	/***配置支付公司支持的卡类型****/
	private static final Map<String,Integer> companyCardTypeMap = new HashMap<>();
	static{
		companyCardTypeMap.put(jiupay.name(), CardType.savings.id());
		companyCardTypeMap.put(beecloud.name(), CardType.savings.id());
	}
	
	public static Integer companyCardType(String name){
		return companyCardTypeMap.get(name);
	}
	
	/***配置支付公司是否需要绑卡短信****/
	private static final Map<String,Integer> companyBindCardSMSMap = new HashMap<>();
	static{
		companyBindCardSMSMap.put(jiupay.name(), YesNoType.yes.id());
		companyBindCardSMSMap.put(beecloud.name(), YesNoType.not.id());
	}
	
	public static Integer companyBindCardSMS(String name){
		return companyBindCardSMSMap.get(name);
	}
	
	/***配置支付公司是否有重发短信验证码****/
	private static final Map<String,Integer> companyResendSMSMap = new HashMap<>();
	static{
		companyResendSMSMap.put(jiupay.name(), YesNoType.yes.id());
		companyResendSMSMap.put(beecloud.name(), YesNoType.not.id());
	}
	
	public static Integer companyResendSMS(String name){
		return companyResendSMSMap.get(name);
	}
	
	/***配置支付公司代付是否需要银联行号****/
	private static final Map<String,Integer> companyUnionPayNeedMap = new HashMap<>();
	static{
		companyUnionPayNeedMap.put(jiupay.name(), YesNoType.not.id());
		companyUnionPayNeedMap.put(beecloud.name(), YesNoType.yes.id());
	}
	
	public static Integer companyUnionPay(String name){
		return companyUnionPayNeedMap.get(name);
	}
	
	public static PayCompany payCompany(String name){
		return enumMap.get(name);
		
	}
	
	/****所有通道*****************/;
    private static final Map<String,String> allDescMap = new HashMap<>(16);
    static{
        allDescMap.putAll(descMap);
        allDescMap.putAll(jfDescMap);
    }
	
	/**
     * @Description 返回所有的通道
     * @return
     */
    public static final Map<String,String> all() {
        return allDescMap;
    }
	
	public static Map<String, String> desc() {
		return descMap;
	}
	
}
