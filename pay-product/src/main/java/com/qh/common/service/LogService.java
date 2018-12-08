package com.qh.common.service;

import org.springframework.stereotype.Service;

import com.qh.common.domain.LogDO;
import com.qh.common.domain.PageDO;
import com.qh.common.utils.Query;
@Service
public interface LogService {
	PageDO<LogDO> queryList(Query query);
	int remove(Long id);
	int batchRemove(Long[] ids);
}
