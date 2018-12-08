package com.qh.query.service.impl;

import java.util.List;
import java.util.Map;

import com.qh.pay.domain.FooterDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.common.utils.Query;
import com.qh.pay.domain.RecordFoundAcctDO;
import com.qh.pay.domain.RecordMerchBalDO;
import com.qh.query.querydao.RecordQueryFoundAcctDao;
import com.qh.query.querydao.RecordQueryFoundAvailAcctDao;
import com.qh.query.querydao.RecordQueryMerchAvailBalDao;
import com.qh.query.querydao.RecordQueryMerchBalDao;
import com.qh.query.service.RecordQueryService;

/**
 * @ClassName RecordQueryServiceImpl
 * @Description 资金流水记录
 * @Date 2017年12月26日 下午2:25:44
 * @version 1.0.0
 */
@Service
public class RecordQueryServiceImpl implements RecordQueryService{
	
	@Autowired
	private RecordQueryMerchBalDao queryMerchBalDao;
	@Autowired
	private RecordQueryMerchAvailBalDao queryMerchAvailBalDao;
	@Autowired
	private RecordQueryFoundAcctDao queryFoundAcctDao;
	@Autowired
	private RecordQueryFoundAvailAcctDao queryFoundAvailAcctDao;
	
	
	
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#merchBalList(com.qh.common.utils.Query)
	 */
	@Override
	public List<RecordMerchBalDO> merchBalList(Query query) {
		return queryMerchBalDao.list(query);
	}


	@Override
	public FooterDO merchBalListFooter(Map<String, Object> params) {
		return queryMerchBalDao.listFooter(params);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#merchBalCount(com.qh.common.utils.Query)
	 */
	@Override
	public int merchBalCount(Query query) {
		return queryMerchBalDao.count(query);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#merchAvailBalList(com.qh.common.utils.Query)
	 */
	@Override
	public List<RecordMerchBalDO> merchAvailBalList(Query query) {
		return queryMerchAvailBalDao.list(query);
	}


	@Override
	public FooterDO merchAvailBalListFooter(Map<String, Object> params) {
		return queryMerchAvailBalDao.listFooter(params);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#merchAvailBalCount(com.qh.common.utils.Query)
	 */
	@Override
	public int merchAvailBalCount(Query query) {
		return queryMerchAvailBalDao.count(query);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#foundAcctList(com.qh.common.utils.Query)
	 */
	@Override
	public List<RecordFoundAcctDO> foundAcctList(Query query) {
		return queryFoundAcctDao.list(query);
	}

	@Override
	public FooterDO foundAcctListFooter(Map<String, Object> params) {
		return queryFoundAcctDao.listFooter(params);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#foundAcctCount(com.qh.common.utils.Query)
	 */
	@Override
	public int foundAcctCount(Query query) {
		return queryFoundAcctDao.count(query);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#foundAvailAcctList(com.qh.common.utils.Query)
	 */
	@Override
	public List<RecordFoundAcctDO> foundAvailAcctList(Query query) {
		return queryFoundAvailAcctDao.list(query);
	}

	@Override
	public FooterDO foundAvailAcctListFooter(Map<String, Object> params) {
		return queryFoundAvailAcctDao.listFooter(params);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.query.service.RecordQueryService#foundAvailAcctCount(com.qh.common.utils.Query)
	 */
	@Override
	public int foundAvailAcctCount(Query query) {
		return queryFoundAvailAcctDao.count(query);
	}

}
