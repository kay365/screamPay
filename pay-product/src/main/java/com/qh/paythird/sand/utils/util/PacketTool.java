/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午4:54:04
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.utils.util;

import com.qh.paythird.sand.utils.SandPayRequestHead;
import org.apache.commons.lang.StringUtils;

/**
 * @author pan.xl
 *
 */
public class PacketTool {

	
	public static void setDefaultRequestHead(SandPayRequestHead head, String method, String productId, String mid) {
		setDefaultRequestHead(head, method, productId, mid, "");
	}

    public static void setDefaultRequestHead(SandPayRequestHead head, String method, String productId, String mid, String plMid, String accessChannelNo) {
        setDefaultRequestHead(head, method, productId, mid, plMid);
        head.setAccessChannelNo(accessChannelNo);
    }

	public static void setDefaultRequestHead(SandPayRequestHead head, String method, String productId, String mid, String plMid) {
		
		head.setVersion(SandpayConstants.DEFAULT_VERSION);
		head.setMethod(method);
		head.setProductId(productId);
		
		head.setMid(mid);
		
		if(StringUtils.isBlank(plMid)) {
			head.setAccessType(SandpayConstants.AccessType.merchant.code);
		} else {
			head.setAccessType(SandpayConstants.AccessType.platform.code);
			head.setPlMid(plMid);
		}
		
		head.setChannelType(SandpayConstants.ChannelType.INTERNET.getCode());
		head.setReqTime(DateUtil.getCurrentDate14());

	}
}
