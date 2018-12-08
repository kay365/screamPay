package com.qh.moneyacct.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.qh.common.utils.Query;
import com.qh.moneyacct.domain.MoneyacctDO;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.RecordFoundAcctDO;

@Mapper
public interface RcFoundBalDao {

    List<MoneyacctDO> findAgentList(Query query);
    
    int findAgentListCount(Query query);
    
    MoneyacctDO findOneAgent(Query query);

    List<RecordFoundAcctDO> findAgentDetailList(Query query);

    int findAgentDetailListCount(Query query);

    FooterDO findAgentDetailListFooter(Map<String, Object> params);

}
