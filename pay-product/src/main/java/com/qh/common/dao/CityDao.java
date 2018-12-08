package com.qh.common.dao;

import com.qh.common.domain.CityDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CityDao {
    List<CityDO> list();

    List<CityDO> listByProvinceId(@Param("provinceId") String provinceId);

    CityDO get(@Param("cityId") String cityId);
}
