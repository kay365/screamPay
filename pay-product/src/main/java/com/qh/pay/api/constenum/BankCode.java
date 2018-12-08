package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName BankCode
 * @Description 银行代码
 * @Date 2017年10月31日 上午11:19:32
 * @version 1.0.0
 */
public enum BankCode {
	/**工商银行***/
	ICBC("1001"),
	/**农业银行****/
	ABC("1002"),
	/**中国银行****/
	BOC("1003"),
	/**建设银行****/
	CCB("1004"),
	/**交通银行****/
	BCOM("1005"),
	/**招商银行****/
	CMB("1012"),
	/**广东发展银行****/
	GDB("1017"),
	/**中信银行****/
	CITIC("1007"),
	/**民生银行****/
	CMBC("1010"),
	/**光大银行****/
	CEB("1008"),
	/**平安银行****/
	PABC("1011"),
	/**上海浦东发展银行****/
	SPDB("1014"),
	/**中国邮政储蓄银行****/
	PSBC("1006"),
	/**华夏银行****/
	HXB("1009"),
	/**兴业银行****/
	CIB("1013"),
	/**北京银行***/
	BOB("1016"),
	/**上海银行****/
	BOS("1025"),
	/**北京农村商业银行***/
	BRCB("1103"),
	;
	private String code;

	private BankCode(String code) {
		this.code = code;
	}

	public String code() {
		return this.code;
	}
	private static final Map<String, String> descMap = new HashMap<String, String>(32);
	static {
		descMap.put(ICBC.name(), "工商银行");
		descMap.put(ABC.name(), "农业银行");
		descMap.put(BOC.name(), "中国银行");
		descMap.put(CCB.name(), "建设银行");
		descMap.put(BCOM.name(), "交通银行");
		descMap.put(CMB.name(), "招商银行");
		descMap.put(GDB.name(), "广东发展银行");
		descMap.put(CITIC.name(), "中信银行");
		descMap.put(CMBC.name(), "民生银行");
		descMap.put(CEB.name(), "光大银行");
		descMap.put(PABC.name(), "平安银行");
		descMap.put(SPDB.name(), "上海浦东发展银行");
		descMap.put(PSBC.name(), "中国邮政储蓄银行");
		descMap.put(HXB.name(), "华夏银行");
		descMap.put(CIB.name(), "兴业银行");
		descMap.put(BOB.name(), "北京银行");
		descMap.put(BOS.name(), "上海银行");
		descMap.put(BRCB.name(), "北京农村商业银行");
	}
	public static Map<String, String> desc() {
		return descMap;
	}
	private static final Map<String,String> codeMap = new HashMap<String,String>(32);
	static{
		codeMap.put("建设银行", "1004");
		codeMap.put("农业银行", "1002");
		codeMap.put("工商银行", "1001");
		codeMap.put("中国银行", "1003");
		codeMap.put("浦发银行", "1014");
		codeMap.put("光大银行", "1008");
		codeMap.put("平安银行", "1011");
		codeMap.put("兴业银行", "1013");
		codeMap.put("邮政储蓄银行", "1006");
		codeMap.put("中信银行", "1007");
		codeMap.put("华夏银行", "1009");
		codeMap.put("招商银行", "1012");
		codeMap.put("广发银行", "1017");
		codeMap.put("北京银行", "1016");
		codeMap.put("上海银行", "1025");
		codeMap.put("民生银行", "1010");
		codeMap.put("交通银行", "1005");
		codeMap.put("北京农村商业银行", "1103");
	}
	public static Map<String, String> codeDescMap() {
		return codeMap;
	}
	
}
