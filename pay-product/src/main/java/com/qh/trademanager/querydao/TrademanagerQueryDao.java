package com.qh.trademanager.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.qh.common.utils.Query;
import com.qh.pay.api.Order;
import com.qh.pay.domain.FooterDO;

@Mapper
public interface TrademanagerQueryDao {

    Order get(@Param("merchNo") String merchNo,@Param("orderNo") String orderNo);
    
    List<Order> payList(Query query);

    int payCount(Query query);

    FooterDO payListFooter(Map<String, Object> params);
    
    Order getAcp(@Param("merchNo") String merchNo,@Param("orderNo") String orderNo);

    List<Order> acpList(Query query);

    int acpCount(Query query);

    FooterDO acpListFooter(Map<String, Object> params);
    
    FooterDO payListStati(Map<String, Object> params);
    
    FooterDO acpListStati(Map<String, Object> params);

}
