package com.qh.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class HsyPayServiceTest extends PayBaseServiceTest{

    /**
     *
     * 扫码支付测试
     * @throws Exception
     */
    @Test
    public void order_test() throws Exception{
        JSONObject jsObj = new JSONObject();
        String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //商户号
        jsObj.put("merchNo", merchNo);
        //订单号
        jsObj.put("orderNo", reqTime + new Random().nextInt(10000));
        //支付渠道
        jsObj.put("outChannel", OutChannel.qq.name());
        if(OutChannel.wy.name().equals(jsObj.get("outChannel"))){
            jsObj.put("bankCode", "CMB");
            jsObj.put("bankName", "招商银行");
        }
        //订单标题
        jsObj.put("title", "商城网银下单");
        //产品名称
        jsObj.put("product", "产品名称");
        //支付金额 单位 元
        jsObj.put("amount", String.valueOf(new Random().nextInt(100)));
        //币种
        jsObj.put("currency", "CNY");
        //前端返回地址
        jsObj.put("returnUrl", "http://www.baidu.com");
        //后台通知地址
        jsObj.put("notifyUrl", "http://www.baidu.com");
        //请求时间
        jsObj.put("reqTime", reqTime);
        //userId
        jsObj.put("userId", "123456789");
        //对公
        jsObj.put("acctType", 1);
        logger.info("请求source:" + jsObj.toString());
        byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
        String sign = RSAUtil.sign(context, mcPrivateKey);
        logger.info("签名结果：{}" ,sign);
        JSONObject jo = new JSONObject();
        jo.put("sign", sign);
        jo.put("context", context);
        logger.info("请求参数：{}", jo.toJSONString());
        String result = RequestUtils.doPostJson(url, jo.toJSONString());
        logger.info("请求结果！{}",result);
        jo = JSONObject.parseObject(result);
        if("0".equals(jo.getString("code"))){
            sign = jo.getString("sign");
            context = jo.getBytes("context");
            if(RSAUtil.verify(context, publicKey, sign)){
                String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
                logger.info("解密结果：" + source);
                jo = JSONObject.parseObject(source);
                logger.info("网银支付链接地址：{}", jo.getString("code_url"));
                logger.info("扫码支付链接地址：{}", jo.getString("hy_pay_url"));
                logger.info("扫码支付链接地址：{}", jo.getString("hy_js_auth_pay_url"));
            }else{
                logger.info("验签失败！{}");
            }
        }
    }


    /**
     *
     * @Description 支付订单查询
     * @throws Exception
     */
    @Test
    public void order_query() throws Exception{
        String orderNo = "201801051554377323";
        JSONObject jsObj = new JSONObject();
        //商户号
        jsObj.put("merchNo", merchNo);
        jsObj.put("orderNo", orderNo);

        //setOrderIng(orderNo);

        byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
        String sign = RSAUtil.sign(context, mcPrivateKey);
        logger.info("签名结果：{}" ,sign);
        JSONObject jo = new JSONObject();
        jo.put("sign", sign);
        jo.put("context", context);
        logger.info("请求参数：{}", jo.toJSONString());
        String result = RequestUtils.doPostJson(url + "/query", jo.toJSONString());
        logger.info("请求结果！{}",result);
        jo = JSONObject.parseObject(result);
        if("0".equals(jo.getString("code"))){
            sign = jo.getString("sign");
            context = jo.getBytes("context");
            if(RSAUtil.verify(context, publicKey, sign)){
                String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
                logger.info("解密结果：" + source);
                jo = JSONObject.parseObject(source);
                logger.info("订单支付状态:{}", jo.getString("orderState"));
                if("1".equals(jo.getString("orderState"))){
                    //回调成功通知订单保存
                    payService.orderDataMsg(merchNo, orderNo);
                }
            }else{
                logger.info("验签失败！{}");
            }

        }

    }



}
