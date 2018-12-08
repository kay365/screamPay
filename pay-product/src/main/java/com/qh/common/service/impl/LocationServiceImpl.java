package com.qh.common.service.impl;

import com.qh.common.dao.AreaDao;
import com.qh.common.dao.CityDao;
import com.qh.common.dao.ProvinceDao;
import com.qh.common.domain.AreaDO;
import com.qh.common.domain.CityDO;
import com.qh.common.domain.ProvinceDO;
import com.qh.common.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    ProvinceDao provinceDao;

    @Autowired
    CityDao cityDao;

    @Autowired
    AreaDao areaDao;

    @Override
    public List<ProvinceDO> listProvinces() {
        return provinceDao.list();
    }

    @Override
    public List<CityDO> listCitysByProvinceId(String provinceId) {
        return cityDao.listByProvinceId(provinceId);
    }

    @Override
    public List<AreaDO> listAreasByCityId(String cityId) {
        return areaDao.listByCityId(cityId);
    }
}
