package com.qh.pay.dao;

import com.qh.common.dao.AreaDao;
import com.qh.common.dao.CityDao;
import com.qh.common.dao.ProvinceDao;
import com.qh.common.dao.UnionPayDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class UnionPayDaoTest {
    @Autowired
    UnionPayDao unionPayDao;

    @Autowired
    ProvinceDao provinceDao;

    @Autowired
    CityDao cityDao;

    @Autowired
    AreaDao areaDao;


    @Test
    @Rollback
    public  void testInsert(){
        System.out.println(unionPayDao.get("102100000128"));
        System.out.println(unionPayDao.listByBank("ICBC"));
        System.out.println(unionPayDao.listByCity("110100"));
        System.out.println(unionPayDao.listByBankAndCity("ICBC","110100"));

        System.out.println("------------------------------------------------------------");

        System.out.println(provinceDao.list());
        System.out.println(provinceDao.get("110000"));

        System.out.println("------------------------------------------------------------");

        System.out.println(cityDao.list());
        System.out.println(cityDao.listByProvinceId("120000"));
        System.out.println(cityDao.get("120100"));

        System.out.println("------------------------------------------------------------");

        System.out.println(areaDao.list());
        System.out.println(areaDao.listByCityId("110100"));
        System.out.println(areaDao.get("110101"));
    }
}
