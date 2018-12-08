package com.qh.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.common.service.LocationService;
import com.qh.common.service.UnionPayService;
import com.qh.common.utils.R;

/**
 * @ClassName CommonController
 * @Description 通用数据获取
 * @Date 2018年1月11日 上午10:32:31
 * @version 1.0.0
 */
@RequestMapping("/common")
@Controller
public class CommonController {
	@Autowired
	private LocationService locationService;
	@Autowired
	private UnionPayService unionPayService;

	/**
	 * 
	 * @Description 通过省获取城市列表
	 * @param provinceId
	 * @return
	 */
	@GetMapping("/getCitysByProvince")
	@ResponseBody
	public R getCitysByProvince(String provinceId){
		return R.okData(locationService.listCitysByProvinceId(provinceId));
	}
	
	/**
	 * 
	 * @Description 通过城市和银行获取支行列表
	 * @param provinceId
	 * @return
	 */
	@GetMapping("/getUnionPay")
	@ResponseBody
	public R getUnionPay(@RequestParam("bankCode")String bankCode, @RequestParam("cityId") String cityId){
		return R.okData(unionPayService.listByBankAndCity(bankCode, cityId));
	}
}
