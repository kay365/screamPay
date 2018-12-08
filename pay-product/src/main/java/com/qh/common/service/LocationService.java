package com.qh.common.service;

import com.qh.common.domain.AreaDO;
import com.qh.common.domain.CityDO;
import com.qh.common.domain.ProvinceDO;

import java.util.List;

public interface LocationService {
    List<ProvinceDO> listProvinces();

    List<CityDO> listCitysByProvinceId(String provinceId);

    List<AreaDO> listAreasByCityId(String cityId);
}
