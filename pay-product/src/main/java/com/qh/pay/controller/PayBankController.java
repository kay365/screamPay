package com.qh.pay.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qh.pay.api.constenum.BankCode;
import com.qh.pay.api.constenum.CardType;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.domain.PayBankDO;
import com.qh.pay.service.PayBankService;
import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;

/**
 * 支付银行
 * 
 * @date 2017-12-27 11:45:47
 */

@Controller
@RequestMapping("/pay/payBank")
public class PayBankController {
	@Autowired
	private PayBankService payBankService;

	@GetMapping()
	@RequiresPermissions("pay:payBank:payBank")
	String PayBank(Model model) {
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("companys", PayCompany.desc());
		model.addAttribute("bankCodes", BankCode.desc());
		return "pay/payBank/payBank";
	}

	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payBank:payBank")
	public PageUtils list(@RequestParam Map<String, Object> params) {
		// 查询列表数据
		Query query = new Query(params);
		List<PayBankDO> payBankList = payBankService.list(query);
		int total = payBankService.count(query);
		PageUtils pageUtils = new PageUtils(payBankList, total);
		return pageUtils;
	}

	@GetMapping("/add")
	@RequiresPermissions("pay:payBank:add")
	String add(Model model) {
		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("companys", PayCompany.desc());
		model.addAttribute("bankCodes", BankCode.desc());
		return "pay/payBank/add";
	}

	@GetMapping("/edit/{company}/{payMerch}/{cardType}")
	@RequiresPermissions("pay:payBank:edit")
	String edit(@PathVariable("company") String company, @PathVariable("payMerch") String payMerch,
			@PathVariable("cardType") Integer cardType, Model model) {
		List<String> banks = payBankService.getBanks(company, payMerch, cardType);
		model.addAttribute("banks", banks);
		model.addAttribute("cardType", cardType);
		model.addAttribute("company", company);
		model.addAttribute("payMerch", payMerch);

		model.addAttribute("cardTypes", CardType.desc());
		model.addAttribute("companys", PayCompany.desc());
		model.addAttribute("bankCodes", BankCode.desc());
		return "pay/payBank/edit";
	}

	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:payBank:add")
	public R save(@RequestParam(value="bankCodes[]",required=false) String[] bankCodes,PayBankDO payBank) {
		if (ParamUtil.isEmpty(payBank.getCardType()) || ParamUtil.isEmpty(payBank.getCardType())) {
			return R.error("参数错误");
		}
		if (payBankService.get(payBank.getCompany(), payBank.getPayMerch(), payBank.getCardType()) != null) {
			return R.error("配置已经存在");
		}
		if(bankCodes==null||bankCodes.length == 0){
			payBank.setBanks(null);
		}else{
			Map<String,String> banks = new HashMap<>();
			for (String bankCode:bankCodes) {
				banks.put(bankCode, "1");
			}
			payBank.setBanks(banks);
		}
		if (payBankService.save(payBank) > 0) {
			return R.ok();
		}
		return R.error();
	}

	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:payBank:edit")
	public R update(@RequestParam(value="bankCodes[]",required=false) String[] bankCodes,PayBankDO payBank) {
		if (ParamUtil.isEmpty(payBank.getCardType()) || ParamUtil.isEmpty(payBank.getCardType())) {
			return R.error("参数错误");
		}
		if (payBankService.get(payBank.getCompany(), payBank.getPayMerch(), payBank.getCardType()) == null) {
			return R.error("配置不存在");
		}
		if(bankCodes==null||bankCodes.length == 0){
			payBank.setBanks(null);
		}else{
			Map<String,String> banks = new HashMap<>();
			for (String bankCode:bankCodes) {
				banks.put(bankCode, "1");
			}
			payBank.setBanks(banks);
		}
		payBankService.update(payBank);
		return R.ok();
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ResponseBody
	@RequiresPermissions("pay:payBank:remove")
	public R remove(@RequestParam("company") String company, @RequestParam("payMerch") String payMerch,
			@RequestParam("cardType") Integer cardType) {
		if (payBankService.remove(company, payMerch, cardType) > 0) {
			return R.ok();
		}
		return R.error();
	}

}
