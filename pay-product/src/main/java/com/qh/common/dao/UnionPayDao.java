package com.qh.common.dao;

import com.qh.common.domain.UnionPayDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UnionPayDao {
    UnionPayDO get(@Param("unionPayNo")  String unionPayNo);

    List<UnionPayDO> listByBankAndCity(@Param("bankCode") String bankCode, @Param("cityId") String cityId);

    List<UnionPayDO> listByCity(@Param("cityId") String cityId);

    List<UnionPayDO> listByBank(@Param("bankCode") String bankCode);
}
