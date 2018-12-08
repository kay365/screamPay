/**
 * 
 */
package com.qh.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qh.common.domain.TableDO;

/**
 * @author 1992lcg@163.com
 * @Time 2017年9月6日
 * @description
 * 
 */
@Service
public interface GeneratorService {
	List<TableDO> list();

	byte[] generatorCode(String[] tableNames);
}
