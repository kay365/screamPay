package com.qh.paythird.dianxin.utils.b;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qh.paythird.dianxin.utils.DianXinConst;
import com.qh.redis.service.RedisUtil;


public class RequestUtil {

	private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);
	
	public static JSONObject request(String api, JSONObject json,String merchantCode) throws Exception{
        HttpHelper http = new HttpHelper();
        //业务参数加密
        Map<String, Object> map = new HashMap<String, Object>();
        String input = json.toString();
        logger.info("点芯扫码预下单请求明文参数：" + input);
        String encoded = Base64.encodeBase64String(input.getBytes());

        //验签加密
        String newstr = input+RedisUtil.getPayCommonValue(merchantCode+DianXinConst.B_KEY);
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();
        //appid
        map.put("appid", RedisUtil.getPayCommonValue(merchantCode+DianXinConst.B_APPID));
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        logger.info("点芯扫码预下单请求加密参数：" + new JSONObject(map).toString());
        return http.getJSONFromHttp(api, map, HttpMethodType.POST);
    }
	
	public static JSONObject getCode(String tranId,String merchantCode,String way)  throws Exception{
        JSONObject json = new JSONObject();
        json.put("version",RedisUtil.getPayCommonValue(DianXinConst.B_VERSION));
        json.put("way",way);

        json.put("tranId", tranId);

        String input = json.toString();
        logger.info("点芯扫码 获取二维码请求明文参数：" + input);
        String encoded = Base64.encodeBase64String(input.getBytes());

        //签名参数
        String newstr = input + RedisUtil.getPayCommonValue(merchantCode+DianXinConst.B_KEY);
        String return_newstr = Md5.getMd5ofStr(newstr);
        String return_bigstr = return_newstr.toUpperCase();

        //appid参数
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("appid", RedisUtil.getPayCommonValue(merchantCode+DianXinConst.B_APPID));
        map.put("params", encoded);
        map.put("signs", return_bigstr);
        logger.info("点芯扫码 获取二维码请求加密参数：" + new JSONObject(map).toString());
        String res = new HttpHelper().sendHttp(RedisUtil.getPayCommonValue(DianXinConst.B_GET_CODE),map, HttpMethodType.GET);
        return new JSONObject(res);
    }
}
