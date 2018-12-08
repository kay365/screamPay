package com.qh.moneyacct.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.qh.common.utils.Query;
import com.qh.moneyacct.domain.MoneyacctDO;
import com.qh.pay.domain.FooterDO;
import com.qh.pay.domain.RecordMerchBalDO;

@Mapper
public interface RcMerchBalDao {

    @Select("SELECT SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from record_merch_bal where merch_no = #{merchNo}")
    MoneyacctDO statMerchByNo(@Param("merchNo")String merchNo);

    @Select("SELECT SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from record_merch_bal a where a.merch_no = (select merch_no from merchant b where b.merch_no =#{merchNo} and b.parent_agent = #{agentNo}")
    MoneyacctDO statMerchByAgentAndMerchNo(@Param("agentNo") String agentNo, @Param("merchNo")String merchNo);

    @Select("SELECT c.merch_no,SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from (select a.merch_no from merchant a where a.parent_agent = #{subAgent} limit #{offset}, #{limit}) c LEFT JOIN record_merch_bal b on c.merch_no = b.merch_no GROUP BY c.merch_no")
    List<MoneyacctDO> statMerchByAgent(Query query);

    @Select("select count(1) from merchant a where a.parent_agent = #{subAgent}")
    int statMerchByAgentCount(Query query);

    @Select("SELECT SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from record_merch_bal a where a.merch_no = (select b.merch_no from merchant b where b.merch_no = #{merchNo}  and  b.parent_agent = " + 
            "(select agent_number from agent c where c.agent_number = #{subAgent} and c.parent_agent=#{agentNo}))")
    MoneyacctDO statMerchByAgentAndMerchNoLimit(@Param("agentNo")String agentNo, @Param("subAgent")String subAgent, @Param("merchNo")String merchNo);

    
    @Select("SELECT c.merch_no, SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from (select a.merch_no from merchant a where a.parent_agent = "
            + " (select agent_number from agent c where c.agent_number = #{subAgent} and c.parent_agent=#{agent}}) LIMIT #{offset}, #{limit}) c "
            + " LEFT JOIN record_merch_bal b on c.merch_no = b.merch_no GROUP BY c.merch_no")
    List<MoneyacctDO> statMerchByAgentLimit(Query query);

    @Select("select count(1) from agent c where c.agent_number = #{subAgent} and c.parent_agent=#{agent}}")
    int statMerchByAgentCountLimit(Query query);

    @Select("SELECT c.merch_no, SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from (select a.merch_no from merchant a LIMIT #{offset}, #{limit}) c LEFT JOIN record_merch_bal b on c.merch_no = b.merch_no GROUP BY c.merch_no")
    List<MoneyacctDO> statMerchAll(Query query);

    @Select("select count(1) from merchant")
    int statMerchAllCount(Query query);

    @Select("SELECT SUM(CASE WHEN fee_type in(2,9) THEN tran_amt ELSE 0 END) AS totalEntry," + 
            " SUM(CASE WHEN fee_type in(3,10) THEN tran_amt when fee_type in(4,5,11,12) THEN -tran_amt ELSE 0 END)AS totalOff," + 
            " SUM(CASE WHEN fee_type in(8,20,21,22,23) and profit_loss = 0 THEN tran_amt WHEN fee_type in(8,20,21,22,23) and profit_loss = 1 THEN -tran_amt ELSE 0 END) AS totalHandFee " +
            " from record_merch_bal a where a.merch_no = (select merch_no from merchant b where b.merch_no =#{merchNo} and b.parent_agent in "
            + "(select agent_number from agent c where c.parent_agent=#{agentNo})")
    MoneyacctDO statMerchByAgentLimitAndMerchNo(@Param("agentNo")String userName, @Param("merchNo")String merchNo);

    List<RecordMerchBalDO> findMerchantDetailList(Query query);

    int findMerchantDetailListCount(Query query);
    
    FooterDO findMerchantDetailListFooter(Map<String,Object> map);

}
