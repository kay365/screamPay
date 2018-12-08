package com.qh.common.service;

import com.qh.common.domain.UnionPayDO;

import java.util.List;

public interface UnionPayService {
    UnionPayDO getById(String id);

    List<UnionPayDO> listByBankAndCity(String bankCode, String cityId);

    List<UnionPayDO> listByCity(String cityId);

    List<UnionPayDO> listByBank(String bankCode);

}
