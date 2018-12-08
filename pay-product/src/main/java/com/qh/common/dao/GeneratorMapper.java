package com.qh.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.qh.common.domain.ColumnDO;
import com.qh.common.domain.TableDO;

@Mapper
public interface GeneratorMapper {
	List<TableDO> list();

	int count(Map<String, Object> map);

	@Select("select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables \r\n"
			+ "	where table_schema = (select database()) and table_name = #{tableName}")
	TableDO get(String tableName);

	@Select("select column_name columnName, data_type dataType, column_comment comments, column_key columnKey, extra from information_schema.columns\r\n"
			+ " where table_name = #{tableName} and table_schema = (select database()) order by ordinal_position")
	List<ColumnDO> listColumns(String tableName);
}
