package com.qh.pay.api.constenum;

/**
 * @ClassName PayParamKey
 * @Description 支付参数
 * @Date 2017年11月1日 下午8:06:37
 * @version 1.0.0
 */
public enum OrderParamKey {
	merchNo,//商户号
	orderNo,//订单号 由发起支付方提供 在该商户号下保证唯一 20 位以内(字母数字组成)
	outChannel,//渠道编码 微信WAP支付：wap 微信公众号支付：gzh 微信扫码：wx QQ钱包扫码：qq
			//支付宝扫码：ali 快捷支付：q 网银支付：wy 代付：acp
	title,//标题标题20个字符以内
	product,//产品描述 20个字符以内
	amount,//金额(单位元) 币种 人民币 两位小数
	currency,//币种 CNY 3个字符
	orderState,//订单状态  0:成功 1:失败 2:处理中 3:关闭 
	returnUrl,//前端返回地址 100字符以内
	notifyUrl,//后台通知地址 100字符以内
	reqTime,//请求时间 格式 yyyyMMddHHmmss 15个字符
	userId,//商户号下的用户唯一标志 在该商户号下保证唯一 20 位以内(字母数字组成)
	memo,//订单备注留言信息 50个字符
	
	acctName,//代付 银行卡持有人姓名 20字符
	certType,//证件类型 1身份证
	certNo,//代付身份证号码 20 字符
	bankNo,//代付 银行卡号 25个字符
	mobile,// 代付 开户卡手机号 15 个字符
	bankCode,// 代付开户行号 20 字符
	bankName,// 代付开户行名称 20 个字符
	cardType,//银行卡类型 默认储蓄卡  储蓄卡 0,信用卡 1;
	acctType,//账户性质 默认对私  对私 0, 对公1;
	bankBranch,
	bankCity,
	bankProvince,
	
	reqIp,//支付ip 由支付发起方设置
	sign,//签约标志
	payCompany,//支付公司
	payMerch,//支付商户号
	businessNo,//支付订单号(第三方)
}
