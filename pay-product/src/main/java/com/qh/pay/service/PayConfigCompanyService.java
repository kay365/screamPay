package com.qh.pay.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.redis.RedisConstants;

/**
 * 支付公司配置
 * 
 * @date 2017-11-06 16:00:33
 */
public interface PayConfigCompanyService {
    
    PayConfigCompanyDO get(String company,String payMerch,String outChannel);
    
    List<PayConfigCompanyDO> list(Map<String, Object> map);
    
    int count(Map<String, Object> map);
    
    PayConfigCompanyDO save(PayConfigCompanyDO payConfigCompany);
    
    PayConfigCompanyDO update(PayConfigCompanyDO payConfigCompany);
    
    int remove(String company,String payMerch,String outChannel);

    /**
     * @Description 批量删除
     * @param companys
     * @param payMerchs
     * @param outChannels
     * @return
     */
    int batchRemove(String[] companys, String[] payMerchs, String[] outChannels);
    
    
    List<Object> getPayCfgCompByOutChannel(String outChannel);

    static PayAcctBal createPayAcctBal(PayConfigCompanyDO payCfgCmp) {
        PayAcctBal payAcctBal = new PayAcctBal();
        payAcctBal.setUserId(PayCompany.payCompany(payCfgCmp.getCompany()).ordinal() * 100 + OutChannel.outChannel(payCfgCmp.getOutChannel()).ordinal());
        payAcctBal.setUsername(payCfgCmp.getCompany() + RedisConstants.link_symbol + 
                payCfgCmp.getPayMerch() + RedisConstants.link_symbol + payCfgCmp.getOutChannel());
        payAcctBal.setUserType(UserType.payMerch.id());
        payAcctBal.setBalance(BigDecimal.ZERO);
        payAcctBal.setAvailBal(BigDecimal.ZERO);
        return payAcctBal;
    }
    
}
