package com.qh.common.dao;

import com.qh.common.domain.ProvinceDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProvinceDao {
    List<ProvinceDO> list();

    ProvinceDO get(@Param("provinceId") String provinceId);
}
