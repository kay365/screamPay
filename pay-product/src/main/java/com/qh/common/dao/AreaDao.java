package com.qh.common.dao;

import com.qh.common.domain.AreaDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AreaDao {
    List<AreaDO> list();

    List<AreaDO> listByCityId(@Param("cityId") String cityId);

    AreaDO get(@Param("areaId") String areaId);
}
