package com.qh.pay.controller;

import com.qh.common.config.CfgKeyConst;
import com.qh.common.utils.*;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.Tesseract4JUtils;
import com.qh.pay.domain.PayQrConfigDO;
import com.qh.pay.service.PayQrConfigService;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 聚富扫码通道配置
 * 
 * @date 2017-12-14 14:37:38
 */
 
@Controller
@RequestMapping("/pay/payQrConfig")
public class PayQrConfigController {
	@Autowired
	private PayQrConfigService payQrConfigService;

	@GetMapping()
	@RequiresPermissions("pay:payQrConfig:payQrConfig")
	String PayQrConfig(Model model){
		model.addAttribute("payCompanys", PayCompany.jfDesc());
		model.addAttribute("outChannels", OutChannel.jfDesc());
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user)){
			model.addAttribute("ifMerch", "1");
			model.addAttribute("merchNo", user.getUsername());
		}
		
	    return "pay/payQrConfig/payQrConfig";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:payQrConfig:payQrConfig")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
        Query query = new Query(params);
        UserDO user = ShiroUtils.getUser();
		List<PayQrConfigDO> payQrConfigList = payQrConfigService.list(query);
		if(ShiroUtils.ifMerch(user)){
        	query.put("merchNo", user.getUsername());
        	for (PayQrConfigDO payQrConfigDO : payQrConfigList) {
				payQrConfigDO.setCostRate(null);
			}
        }
		
		int total = payQrConfigService.count(query);
		PageUtils pageUtils = new PageUtils(payQrConfigList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:payQrConfig:add")
	String add(Model model){
		model.addAttribute("payCompanys", PayCompany.jfDesc());
		model.addAttribute("outChannels", OutChannel.jfDesc());
		UserDO user = ShiroUtils.getUser();
        if(ShiroUtils.ifMerch(user)){
        	model.addAttribute("merchNo", user.getUsername());
        }
	    return "pay/payQrConfig/add";
	}

	@GetMapping("/edit/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:edit")
	String edit(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo, Model model){
		model.addAttribute("payCompanys", PayCompany.jfDesc());
		model.addAttribute("outChannels", OutChannel.jfDesc());
		UserDO user = ShiroUtils.getUser();
        if(!ShiroUtils.ifMerch(user) || merchNo.equals(user.getUsername())){
        	PayQrConfigDO payQrConfig = payQrConfigService.get(outChannel,merchNo);
        	model.addAttribute("payQrConfig", payQrConfig);
        }
	    return "pay/payQrConfig/edit";
	}

	@ResponseBody
	@PostMapping("/getQrs/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:upload")
	R getQrs(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo){
		UserDO user = ShiroUtils.getUser();
		if(!ShiroUtils.ifMerch(user) || merchNo.equals(user.getUsername())){
			PayQrConfigDO payQrConfig = payQrConfigService.get(outChannel,merchNo);
			return R.okData(payQrConfig);
		}
		return R.error("获取图片二维码失败！");
	}



	
	/**
	 * 保存
	 */
	@ResponseBody
	@PostMapping("/save")
	@RequiresPermissions("pay:payQrConfig:add")
	public R save( PayQrConfigDO payQrConfig){
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(payQrConfig.getMerchNo())){
			return R.error("权限错误");
		}
		int count = payQrConfigService.save(payQrConfig);
		if(count == 1){
			return R.ok();
		}else if(count == 2){
			return R.error("该通道已配置");
		}else if(count == 3){
			return R.error("该商户号不存在" + payQrConfig.getMerchNo());
		}
		return R.error();
	}
	/**
	 * 修改
	 */
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:payQrConfig:edit")
	public R update( PayQrConfigDO payQrConfig){
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(payQrConfig.getMerchNo())){
			return R.error("权限错误");
		}
		payQrConfigService.update(payQrConfig);
		return R.ok();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:payQrConfig:remove")
	public R remove(@RequestParam("outChannel") String outChannel,@RequestParam("merchNo") String merchNo){
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(merchNo)){
			return R.error("权限错误");
		}
		if(payQrConfigService.remove(outChannel, merchNo)>0){
			return R.ok();
		}
		return R.error();
	}
	
	@GetMapping("/editQrs/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:upload")
	String editQrs(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo, Model model){
		model.addAttribute("payCompanys", PayCompany.jfDesc());
		model.addAttribute("outChannels", OutChannel.jfDesc());
		UserDO user = ShiroUtils.getUser();
		if(!ShiroUtils.ifMerch(user) || merchNo.equals(user.getUsername())){
        	PayQrConfigDO payQrConfig = payQrConfigService.get(outChannel,merchNo);
        	model.addAttribute("payQrConfig", payQrConfig);
        }
	    return "pay/payQrConfig/editQrs";
	}




	/**
	 *
	 * @Description 批量上传二维码图片
	 * @param outChannel
	 * @param merchNo
	 * @param file
	 * @return
	 */
	@ResponseBody
	@PostMapping("/batchUploadQrs/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:upload")
	R batchUploadQrs(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo,
						   @RequestParam("file") MultipartFile file) throws Exception{
		UserDO user = ShiroUtils.getUser();
		R r = new R();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(merchNo)){
			return R.error("权限错误");
		}


		String moneyAmount =  "";
		if(OutChannel.jfwx.name().equals(outChannel)){
			moneyAmount = Tesseract4JUtils.doWxQrMoneyOcr(ImageIO.read(new ByteArrayInputStream(file.getBytes())));
		}else if(OutChannel.jfali.name().equals(outChannel)){
			moneyAmount = Tesseract4JUtils.doAliQrMoneyOcr(ImageIO.read(new ByteArrayInputStream(file.getBytes())));
		}

		String fileName = file.getOriginalFilename();
		String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
		if(!FileType.ifPicType(fileType) || !fileType.endsWith("jpg")){
			r.put("error","请上传收款二维码图片");
			return r;
		}
		if(file.getSize() > 5*100*1024){
			r.put("error","请上传大小500K以内的图片");
			return r;
		}

		moneyAmount = ParamUtil.subZeroAndDot(moneyAmount);
		if(!moneyAmount.matches("\\d*.?\\d*")){
            r.put("error","请上传正确的二维码图片");
            return r;
        }

		r =  payQrConfigService.updateQrs(outChannel,merchNo,moneyAmount);

		if(R.ifSucc(r)){
			try {
				FileUtil.uploadFile(file.getBytes(), RedisUtil.getSysConfigValue(CfgKeyConst.qr_money_path) + merchNo + File.separator + outChannel + File.separator, moneyAmount.replace(".", "p") + "." + fileType);
			} catch (Exception e) {
				r.put("error","文件上传失败！");
				return r;
			}
		}
		if(R.ifError(r)){
			r.put("error",r.getMsg());
		}

		return r;
	}
	
	/**
	 * 
	 * @Description 上传二维码图片
	 * @param outChannel
	 * @param merchNo
	 * @param moneyAmount
	 * @param file
	 * @return
	 */
	@ResponseBody
	@PostMapping("/uploadQrs/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:upload")
	R uploadQrs(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo,@RequestParam("moneyAmount") String moneyAmount,
			@RequestParam("file") MultipartFile file){
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(merchNo)){
			return R.error("权限错误");
		}
		if(ParamUtil.isEmpty(moneyAmount)){
			return R.error("请输入二维收款码金额");
		}
		String fileName = file.getOriginalFilename();
		String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
		if(!FileType.ifPicType(fileType) || !fileType.endsWith("jpg")){
			return R.error("请上传收款二维码图片");
		}
		if(file.getSize() > 2*100*1024){
			return R.error("请上传大小200K以内的图片");
		}
		
		moneyAmount = ParamUtil.subZeroAndDot(moneyAmount);
		R r =  payQrConfigService.updateQrs(outChannel,merchNo,moneyAmount);
		if(R.ifSucc(r)){
			try {
				FileUtil.uploadFile(file.getBytes(), RedisUtil.getSysConfigValue(CfgKeyConst.qr_money_path) + merchNo + File.separator + outChannel + File.separator, moneyAmount.replace(".", "p") + "." + fileType);
			} catch (Exception e) {
				return R.error("文件上传失败！");
			}
		}
		return r;
	}
	/**
	 * 
	 * @Description 删除二维码收款图片
	 * @param outChannel
	 * @param merchNo
	 * @param moneyAmount
	 * @return
	 */
	@ResponseBody
	@PostMapping("/removeQrs/{outChannel}/{merchNo}")
	@RequiresPermissions("pay:payQrConfig:upload")
	R removeQrs(@PathVariable("outChannel") String outChannel, @PathVariable("merchNo") String merchNo,@RequestParam("moneyAmounts[]") String[] moneyAmounts){
		UserDO user = ShiroUtils.getUser();
		if(ShiroUtils.ifMerch(user) && !user.getUsername().equals(merchNo)){
			return R.error("权限错误");
		}

		R r =  payQrConfigService.removeQrs(outChannel,merchNo, Arrays.asList(moneyAmounts));
		if(R.ifSucc(r)){
			for(String moneyAmount:moneyAmounts){
				moneyAmount = ParamUtil.subZeroAndDot(moneyAmount);
				FileUtil.deleteFile(RedisUtil.getSysConfigValue(CfgKeyConst.qr_money_path) + merchNo + File.separator + outChannel + File.separator + moneyAmount.replace(".", "p") + ".jpg" );
			}
		}
		return r;
	}
}
