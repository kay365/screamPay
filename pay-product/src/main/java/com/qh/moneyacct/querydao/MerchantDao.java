package com.qh.moneyacct.querydao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.StringTypeHandler;

@Mapper
public interface MerchantDao {
    @Select("select merch_no,merchants_short_name,IFNULL(contacts,'') contacts from merchant where parent_agent = #{parentAgent}")
    @Results({
        @Result(column = "merch_no", jdbcType = JdbcType.VARCHAR, property = "merchNo", typeHandler = StringTypeHandler.class),
        @Result(column = "merchants_short_name", jdbcType = JdbcType.VARCHAR, property = "merchantsShortName", typeHandler = StringTypeHandler.class),
        @Result(column = "contacts", jdbcType = JdbcType.VARCHAR, property = "display", typeHandler = StringTypeHandler.class), 
    })
    List<Map<String,Object>> findMerchantByAgent(@Param("parentAgent") String parentAgent);
    
    
}
