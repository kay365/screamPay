package com.qh.common.service.impl;

import com.qh.common.dao.ProvinceDao;
import com.qh.common.dao.UnionPayDao;
import com.qh.common.domain.UnionPayDO;
import com.qh.common.service.UnionPayService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnionPayServiceImpl implements UnionPayService {
    @Autowired
    UnionPayDao unionPayDao;

    @Autowired
    ProvinceDao provinceDao;

    @Override
    public UnionPayDO getById(String id) {
        return unionPayDao.get(id);
    }

    @Override
    public List<UnionPayDO> listByBankAndCity(String bankCode, String cityId) {
        return unionPayDao.listByBankAndCity(bankCode,cityId);
    }

    @Override
    public List<UnionPayDO> listByCity(String cityId) {
        return unionPayDao.listByCity(cityId);
    }

    @Override
    public List<UnionPayDO> listByBank(String bankCode) {
        return unionPayDao.listByBank(bankCode);
    }

}
