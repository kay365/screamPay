package com.qh.pay.api.constenum;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName FeeType
 * @Description 费用类型
 * @Date 2017年11月14日 下午3:24:48
 * @version 1.0.0
 */
public enum FeeType {
	/****平台支付分成 手续费用***/
	platIn(0),
	
	/****平台代付分成 手续费用***/
	platAcpIn(1), 
	
	/****商户下单收入 余额增加***/
	merchIn(2), 
	
	/****商户代付支出 余额扣减****/
	merchAcpOut(3),
	
	/****商户代付失败 余额返还**/
	merchAcpFail(4),
	
	/****商户代付未通过 余额返还**/
	merchAcpNopass(5),
	
	/****代理商支付分成 手续费用***/
	agentIn(6), 
	
	/****代理商代付分成  手续费用***/
	agentAcpIn(7),
	
	/****商户支付预付费 手续费用****/
	merchPreHand(8),
	
	/****商户充值 余额增加******/
	merchCharge(9),
	
	/****用户提现 余额减少***/
	withdrawOut(10),
	
	/****用户提现失败 余额返还**/
	withdrawFail(11),
	
	/****用户提现未通过 余额返还**/
	withdrawNopass(12),
	
	/****用户提现分成 手续费用***/
	platWithdrawIn(13),
	
    /***通道支付交易金额*****/
    payMerchTrade(14),
    /***通道支付交易 手续费用*****/
    payMerchTradeHand(15),
    /***通道代付交易金额*****/
    payMerchAcp(16),
    /***通道代付交易手续费用****/
    payMerchAcpHand(17),
    /***商户支付手续费用***/
    merchHandFee(20),
    /***商户代付手续费用***/
    merchAcpHandFee(21),
    /***商户充值手续费用******/
    merchChargeHandFee(22),
    /***商户提现手续费用********/
    merchWithDrawHandFee(23),
    
    /***平台充值分成****/
    platChargeIn(24),
    
    /***平台提现手续费用********/
    platWithDrawHandFee(25),
    
    /***代理商提现手续费用********/
    agentWithDrawHandFee(26),
	/**平台划拨金额**/
	platTransfer(27),
	/**冻结**/
	freeze(28),
	/**解冻**/
    unfreeze(29);
	/**** 费用类型描述 ****/
	private static final Map<Integer, String> descMap = new HashMap<>(8);
	static {
		descMap.put(platIn.id, "平台支付分成");
		descMap.put(platAcpIn.id, "平台代付分成");
		descMap.put(merchIn.id, "商户下单收入");
		descMap.put(merchAcpOut.id, "商户代付支出");
		descMap.put(merchAcpFail.id, "商户代付失败返还");
		descMap.put(merchAcpNopass.id, "商户代付审核返还");
		descMap.put(agentIn.id, "代理商支付分成");
		descMap.put(agentAcpIn.id, "代理商代付分成");
		descMap.put(merchPreHand.id, "商户预付手续费");
		descMap.put(merchCharge.id, "商户充值收入");
		descMap.put(withdrawOut.id, "提现支出");
		descMap.put(withdrawFail.id, "提现失败返还");
		descMap.put(withdrawNopass.id, "提现未通过返还");
		descMap.put(platWithdrawIn.id, "平台提现收入");
		
		descMap.put(payMerchTrade.id, "通道交易金额");
        descMap.put(payMerchTradeHand.id, "通道交易手续费");
        descMap.put(payMerchAcp.id, "通道代付金额");
        descMap.put(payMerchAcpHand.id, "通道代付手续费");
        
        descMap.put(merchHandFee.id, "商户支付手续费");
        descMap.put(merchAcpHandFee.id, "商户代付手续费");
        descMap.put(merchChargeHandFee.id, "商户充值手续费");
        descMap.put(merchWithDrawHandFee.id, "商户提现手续费");
        
        descMap.put(platChargeIn.id, "平台充值分成");
        
        descMap.put(platWithDrawHandFee.id, "平台提现手续费");
        descMap.put(agentWithDrawHandFee.id, "代理商提现手续费");
        
	}

	
	/**** 商户费用类型描述 ****/
	private static final Map<Integer, String> merchDescMap = new HashMap<>(8);
	static {
		merchDescMap.put(merchIn.id, "支付");//descMap.get(merchIn.id));
		merchDescMap.put(merchHandFee.id, "支付手续费");//descMap.get(merchHandFee.id));
		merchDescMap.put(merchAcpOut.id, "下发");//descMap.get(merchAcpOut.id));
		merchDescMap.put(merchAcpFail.id, "下发失败");//descMap.get(merchAcpFail.id));
		 merchDescMap.put(merchAcpHandFee.id, "下发手续费");//descMap.get(merchAcpHandFee.id));
//		merchDescMap.put(merchAcpNopass.id, "");//descMap.get(merchAcpNopass.id));
//		merchDescMap.put(merchPreHand.id, "");//descMap.get(merchPreHand.id));
//		merchDescMap.put(merchCharge.id, "");//descMap.get(merchCharge.id));
		merchDescMap.put(withdrawOut.id, "提现");//descMap.get(withdrawOut.id));
		merchDescMap.put(withdrawFail.id, "提现失败");//descMap.get(withdrawFail.id));
//		merchDescMap.put(withdrawNopass.id, "");//descMap.get(withdrawNopass.id));
//        merchDescMap.put(merchChargeHandFee.id, "");//descMap.get(merchChargeHandFee.id));
        merchDescMap.put(merchWithDrawHandFee.id, "提现手续费");//descMap.get(merchWithDrawHandFee.id));
		
		
	}
	
	
	/**** 平台费用类型描述 ****/
    private static final Map<Integer, String> platDescMap = new HashMap<>(8);
    static {
        platDescMap.put(platIn.id, "支付手续费分润");//descMap.get(platIn.id));
        platDescMap.put(platAcpIn.id, "下发手续费分润");//descMap.get(platAcpIn.id));
        platDescMap.put(platWithdrawIn.id, "提现手续费分润");//descMap.get(platWithdrawIn.id));
//        platDescMap.put(platChargeIn.id, "");//descMap.get(platChargeIn.id));
        platDescMap.put(withdrawOut.id, "提现");//descMap.get(withdrawOut.id));
        platDescMap.put(withdrawFail.id, "提现失败");//descMap.get(withdrawFail.id));
//        platDescMap.put(withdrawNopass.id, "");//descMap.get(withdrawNopass.id));
        platDescMap.put(platWithDrawHandFee.id, "提现手续费");//descMap.get(platWithDrawHandFee.id));
        
        platDescMap.put(platTransfer.id, "划拨到账");
       
        
    }
	/**** 代理费用类型描述 ****/
	private static final Map<Integer, String> agentDescMap = new HashMap<>(8);
	static {
		agentDescMap.put(agentIn.id, "支付手续费分润");//descMap.get(agentIn.id));
		agentDescMap.put(agentAcpIn.id, "下发手续费分润");//descMap.get(agentAcpIn.id));
		agentDescMap.put(withdrawOut.id, "提现");//descMap.get(withdrawOut.id));
		agentDescMap.put(withdrawFail.id, "提现失败");//descMap.get(withdrawFail.id));
//		agentDescMap.put(withdrawNopass.id, "");//descMap.get(withdrawNopass.id));
		agentDescMap.put(agentWithDrawHandFee.id, "提现手续费");//descMap.get(agentWithDrawHandFee.id));
	}
	
    /**** 支付通道费用类型描述 ****/
    private static final Map<Integer, String> payMerchDescMap = new HashMap<>(8);
    static {
        payMerchDescMap.put(payMerchTrade.id, "支付");//descMap.get(payMerchTrade.id));
        payMerchDescMap.put(payMerchTradeHand.id, "支付手续费");//descMap.get(payMerchTradeHand.id));
        payMerchDescMap.put(payMerchAcp.id, "下发/提现");//descMap.get(payMerchAcp.id));
        payMerchDescMap.put(payMerchAcpHand.id, "下发/提现手续费");//descMap.get(payMerchAcpHand.id));
        
    }
	
	private int id;

	private FeeType(int id) {
		this.id = id;
	}

	public int id() {
		return id;
	}

	public static Map<Integer, String> desc() {
		return descMap;
	}
	public static Map<Integer, String> merchDesc() {
		return merchDescMap;
	}
	public static Map<Integer, String> agentDesc() {
		return agentDescMap;
	}
	public static Map<Integer, String> platDesc() {
        return platDescMap;
    }
    public static Map<Integer, String> payMerchDesc() {
        return payMerchDescMap;
    }
}
