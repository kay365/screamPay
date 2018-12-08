package com.qh.common.service.impl;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.dao.GeneratorMapper;
import com.qh.common.domain.ColumnDO;
import com.qh.common.domain.TableDO;
import com.qh.common.service.GeneratorService;
import com.qh.common.utils.GenUtils;


@Service
public class GeneratorServiceImpl implements GeneratorService {
	@Autowired
	GeneratorMapper generatorMapper;

	@Override
	public List<TableDO> list() {
		List<TableDO> list = generatorMapper.list();
		return list;
	}

	@SuppressWarnings("deprecation")
	@Override
	public byte[] generatorCode(String[] tableNames) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);
		for(String tableName : tableNames){
			//查询表信息
			TableDO table = generatorMapper.get(tableName);
			//查询列信息
			List<ColumnDO> columns = generatorMapper.listColumns(tableName);
			//生成代码
			GenUtils.generatorCode(table, columns, zip);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}

}
