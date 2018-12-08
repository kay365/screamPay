/**
 * Copyright : http://www.sandpay.com.cn , 2011-2014
 * Project : sandpay-cashier-sdk
 * $Id$
 * $Revision$
 * Last Changed by pxl at 2016-12-27 下午5:11:16
 * $URL$
 * 
 * Change Log
 * Author      Change Date    Comments
 *-------------------------------------------------------------
 * pxl         2016-12-27        Initailized
 */
package com.qh.paythird.sand.utils.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author pan.xl
 *
 */
public class DateUtil {

    private static final String DATE_FORMAT_14 = "yyyyMMddHHmmss";
	private static final String DATE_FORMAT_08 = "yyyyMMdd";
	
	public static String getCurrentDate14() {
		String date = new SimpleDateFormat(DATE_FORMAT_14).format(new Date());
		return date;
	}
	
	public static String getCurrentDate08() {
		String date = new SimpleDateFormat(DATE_FORMAT_08).format(new Date());
		return date;
	}
	
	 /**
     * 得到字符串形式昨天时间,日期格式采用默认的格式.
     * @return String
     */
    public static String getYesterday(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String yesterday = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
		return yesterday;
	}
}
