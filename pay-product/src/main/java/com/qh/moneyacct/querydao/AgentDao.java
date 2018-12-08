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
public interface AgentDao {
    
    
    @Select("select agent_number,merchants_short_name,IFNULL(contacts,'') contacts from agent where level = 1")
    @Results({
        @Result(column = "agent_number", jdbcType = JdbcType.VARCHAR, property = "agentNumber", typeHandler = StringTypeHandler.class),
        @Result(column = "merchants_short_name", jdbcType = JdbcType.VARCHAR, property = "merchantsShortName", typeHandler = StringTypeHandler.class),
        @Result(column = "contacts", jdbcType = JdbcType.VARCHAR, property = "display", typeHandler = StringTypeHandler.class), 
    })
    List<Map<String,Object>> findOneLevelAgent();
    
    @Select("select agent_number,merchants_short_name,IFNULL(contacts,'') contacts from agent where parent_agent = #{parentAgent}")
    @Results({
        @Result(column = "agent_number", jdbcType = JdbcType.VARCHAR, property = "agentNumber", typeHandler = StringTypeHandler.class),
        @Result(column = "merchants_short_name", jdbcType = JdbcType.VARCHAR, property = "merchantsShortName", typeHandler = StringTypeHandler.class),
        @Result(column = "contacts", jdbcType = JdbcType.VARCHAR, property = "display", typeHandler = StringTypeHandler.class), 
    })
    List<Map<String,Object>> findAgentByParent(@Param("parentAgent") String parentAgent);
    
    @Select("select level from agent where agent_number = #{agentNumber}")
    Integer findLevel(@Param("agentNumber") String agentNumber);
    
}
