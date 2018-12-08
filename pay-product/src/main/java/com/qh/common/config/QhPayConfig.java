package com.qh.common.config;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.QhPayUtil;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.ConfigDO;

@Component
@ConfigurationProperties(prefix="qhpay")
public class QhPayConfig {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(QhPayConfig.class);
	
	//上传路径
	private String uploadPath;
	
	private String publicKey;
	
	private String privateKeyPath;
	
	//二维码扫码图片
	private String qrMoneyPath;
	
	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
//		QhPayUtil.setQhPublicKey(publicKey);
	}

	public String getPrivateKeyPath() {
		return privateKeyPath;
	}
	
	public String getQrMoneyPath() {
		return qrMoneyPath;
	}

	public void setQrMoneyPath(String qrMoneyPath) {
		this.qrMoneyPath = qrMoneyPath;
	}

	public void setPrivateKeyPath(String privateKeyPath) {
		this.privateKeyPath = privateKeyPath;
		/*try {
			QhPayUtil.setQhPrivateKey(ParamUtil.readTxtFileFilter(privateKeyPath));
		} catch (Exception e) {
			logger.error("支付私钥加载失败！" + e.getMessage());
		}*/
	}
	
	@Bean
    public Converter<String, Date> addNewConvert() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
            	return DateUtil.parseDateSource(source);
            }
        };
    }
	
}
