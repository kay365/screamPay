package com.qh.moneyacct.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.common.utils.Query;
import com.qh.moneyacct.domain.MoneyacctDO;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.RecordPayMerchBalDO;

@Mapper
public interface RcPayMerchBalDao {

	List<MoneyacctDO> findPayMerchForOutChannelList(Query query);
	
    List<MoneyacctDO> findPayMerchList(Query query);

    int findPayMerchListCount(Query query);

    MoneyacctDO findOnePayMerch(Query query);
    
    List<RecordPayMerchBalDO> findPayMerchDetailList(Query query);

    int findPayMerchDetailListCount(Query query);

    FooterDO findPayMerchDetailListFooter(Map<String,Object> map);

}
