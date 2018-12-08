package com.qh.paythird.tx;

import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.tx.utils.TXPayConst;
import com.qh.paythird.tx.utils.TXPayMD5Utils;
import com.qh.paythird.tx.utils.TXPayRSAUtils;
import com.qh.paythird.tx.utils.TXPayRequestUtils;
import com.qh.redis.service.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @// TODO: 2018/4/10 天下支付Service
 */
@Service
public class TXPayService {

    private static final Logger logger = LoggerFactory.getLogger(TXPayService.class);


    /**
     * @param order
     * @return
     * @// TODO: 2018/4/10 发起订单
     */
    public R order(Order order) {
        logger.info("天付宝  支付：{");
        try {

            // 支付通道检查
            if (!OutChannel.wy.name().equals(order.getOutChannel())) {
                logger.error("天付宝 不支持的支付通道：{}", order.getOutChannel());
                return R.error("不支持的支付通道");
            }

            TreeMap<String, String> paramsMap = new TreeMap<>();

            // 获取系统参数
            String shopId = RedisUtil.getPayCommonValue(TXPayConst.SHOP_ID);
            String shopUserId = RedisUtil.getPayCommonValue(TXPayConst.SHOP_USER_ID);
            String curType = RedisUtil.getPayCommonValue(TXPayConst.CUR_TYPE);
            String errorPageUrl = RedisUtil.getPayCommonValue(TXPayConst.ERROR_PAGE_URL);
            String expireTime = RedisUtil.getPayCommonValue(TXPayConst.EXPIRE_TIME);
            String channel = RedisUtil.getPayCommonValue(TXPayConst.CHANNEL);
            String encodeType = RedisUtil.getPayCommonValue(TXPayConst.ENCODE_TYPE);
            String cardPayUrl = RedisUtil.getPayCommonValue(TXPayConst.CARD_PAY_API);
            String serverEncode = RedisUtil.getPayCommonValue(TXPayConst.SERVICE_ENCODE);

            // 封装参数
            paramsMap.put("spid", shopId);
            paramsMap.put("sp_userid", shopUserId);
            paramsMap.put("spbillno", order.getOrderNo());//>>------------------需自填参数<<
            paramsMap.put("money", String.valueOf(order.getAmount()));//>>------------------需自填参数<<
            paramsMap.put("cur_type", curType);
            paramsMap.put("notify_url", PayService.commonNotifyUrl(order));
            paramsMap.put("return_url", PayService.commonReturnUrl(order));
            paramsMap.put("errpage_url", errorPageUrl);
            paramsMap.put("memo", order.getMemo());//>>------------------需自填参数<<
            paramsMap.put("expire_time", expireTime);// 订单有效时间
            paramsMap.put("attach", ""); // 商户自带数据
            paramsMap.put("card_type", String.valueOf(order.getCardType()));//>>------------------需自填参数<<
            paramsMap.put("bank_segment", order.getBankCode());//>>------------------需自填参数<<
            paramsMap.put("user_type", String.valueOf(order.getUserType()));//>>------------------需自填参数<<
            paramsMap.put("channel", channel);
            paramsMap.put("encode_type", encodeType);
            paramsMap.put("risk_ctrl", "");// 用于风险控制管理的一组数据,使用json格式

            //拼接签名原串
            String paramSrc = RequestUtils.getParamSrc(paramsMap);
            logger.info("天付宝 签名原串>>: " + paramSrc);
            //生成签名
            String sign = TXPayMD5Utils.sign(paramSrc);
            logger.info("天付宝 签名>>: " + sign);
            //rsa加密原串
            String encryptSrc = paramSrc + "&sign=" + sign;//加密原串
            logger.info("天付宝 rsa加密原串>>: " + encryptSrc);
            //rsa密串
            String cipherData = TXPayRSAUtils.encrypt(encryptSrc);
            logger.info("天付宝 rsa密串>>: " + cipherData);
            String result = RequestUtils.doPost(cardPayUrl, "cipher_data=" + URLEncoder.encode(cipherData, serverEncode));
            logger.info("天付宝 支付返回信息>>: " + result);
            logger.info("天付宝 解密结果>>: " + result);
        } catch (Exception e) {
            logger.error("天付宝 支付异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        }
        logger.info("天付宝  支付：}");
        return R.ok("支付成功");
    }


    /**
     * @param request
     * @return
     * @// TODO: 2018/4/10 支付返回结果回调
     */
    public R payReturn(HttpServletRequest request) {
        logger.info("天付宝 支付结果回调 ：{");

        //获取天付宝GET过来反馈信息
        String cipherData = request.getParameter("cipher_data");

        //数据解密
        String responseData = TXPayRSAUtils.decrypt(cipherData);

        //封装数据
        HashMap<String, String> map = TXPayRequestUtils.parseString(responseData);

        //rsa验签
        try {
            if (TXPayRSAUtils.verify(map.get("source"), map.get("sign"))) {// 通过
                String result = map.get("result");
                String resultMsg = map.get("retmsg");
                String resultCode = map.get("retcode");
                if ("1".equals(result)) {
                    return R.ok("验证签名成功");
                } else {
                    logger.error("天付宝 支付异常：[{}:{}]", resultMsg, resultCode);
                    return R.ok().put(Constant.result_code, Constant.result_code_error).put(Constant.result_msg, resultMsg + "：" + resultCode);
                }
            } else {// 失败
                logger.error("天付宝 验证签名：失败");
                return R.error("验证签名失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // rsa验证签名出错
            logger.error("天付宝 验证签名：异常 >> " + e.getMessage());
            return R.error("系统异常");
        } finally {
            logger.info("天付宝 支付结果回调：}");
        }
    }


    /**
     * @param order
     * @param request
     * @return
     * @// TODO: 2018/4/10 支付回调
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("天付宝  支付回调 ：{");

        // 获取系统参数
        String serverEncode = RedisUtil.getPayCommonValue(TXPayConst.SERVICE_ENCODE);

        //封装参数
        Map<String, String> requestParams = RequestUtils.getAllRequestParamStream(request, serverEncode);

        // 获取回调的参数
        String shopId = requestParams.get("spid");
        String shopBillNo = requestParams.get("spbillno"); // 订单号
        String listId = requestParams.get("listid"); // 商户号
        String money = requestParams.get("money"); // 订单支付的金额
        String curType = requestParams.get("cur_type"); // 金额类型
        String result = requestParams.get("result");// 支付结果 1：成功 2：失败
        String payType = requestParams.get("pay_type"); // 交易类型
        String userType = requestParams.get("user_type"); // 用户类型
        String sign = requestParams.get("sign");
        String encodeType = requestParams.get("encode_type"); // 服务器编码

        try {
            // 支付成功
            if ("1".equals(result) || "1".equals(String.valueOf(request))) {
                order.setRealAmount(BigDecimal.valueOf(Long.valueOf(money)));
                order.setSign(sign);
                order.setOrderNo(shopBillNo);
                order.setCurrency(curType);
                order.setUserType(Integer.valueOf(userType));
                order.setMerchNo(listId);

                logger.info("天付宝支付回调>> : 交易成功");

                return R.ok("交易成功");

            } else {
                logger.info("天付宝支付回调>> : 异常");
                return R.ok().put(Constant.result_code, Constant.result_code_error).put(Constant.result_msg, "支付异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("天付宝支付回调>> : 异常 >>" + e.getMessage());
            return R.ok().put(Constant.result_code, Constant.result_code_error).put(Constant.result_msg, "支付异常");
        } finally {
            logger.info("天付宝  支付回调 ：}");
        }
    }

    /**
     * @return
     * @// TODO: 2018/4/10 查询订单
     */
    public R query(Order order) {

        logger.info("天付宝  订单查询:{");

        String shopId = RedisUtil.getPayCommonValue(TXPayConst.SHOP_ID);
        String shopUserId = RedisUtil.getPayCommonValue(TXPayConst.SHOP_USER_ID);
        String channel = RedisUtil.getPayCommonValue(TXPayConst.CHANNEL);
        String encodeType = RedisUtil.getPayCommonValue(TXPayConst.ENCODE_TYPE);
        String serverEncode = RedisUtil.getPayCommonValue(TXPayConst.SERVICE_ENCODE);
        String orderQueryAPI = RedisUtil.getPayCommonValue(TXPayConst.ORDER_QUERY_API);
        TreeMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("spid", shopId);
        paramsMap.put("sp_userid", shopUserId);
        paramsMap.put("spbillno", order.getOrderNo());

        paramsMap.put("channel", channel);
        paramsMap.put("encode_type", encodeType);
        paramsMap.put("risk_ctrl", "");

        //拼接签名原串
        String paramSrc = RequestUtils.getParamSrc(paramsMap);

        //生成签名
        String sign = TXPayMD5Utils.sign(paramSrc);

        //rsa加密原串
        String encryptSrc = paramSrc + "&sign=" + sign;//加密原串

        //rsa密串
        String cipherData = TXPayRSAUtils.encrypt(encryptSrc);

        try {
            logger.info("天付宝  订单查询：发送请求");
            //发起请求
            String responseData = RequestUtils.doPost(orderQueryAPI, "cipher_data=" + URLEncoder.encode(cipherData, serverEncode));

            //如果返回码不为00，返回结果不会加密，因此不往下处理，
            if (!RequestUtils.getXmlElement(responseData, "retcode").equals("00")) {

            }
            //获得服务器返回的加密数据
            String cipherResponseData = RequestUtils.getXmlElement(responseData, "cipher_data");

            //对服务器返回的加密数据进行rsa解密
            responseData = TXPayRSAUtils.decrypt(cipherResponseData);

            //分解解密后的字符串
            HashMap<String, String> map = TXPayRequestUtils.parseString(responseData);

            //rsa验签
            if (TXPayRSAUtils.verify(map.get("source"), map.get("sign"))) {
                logger.info("天付宝  订单查询：成功");
                return R.ok("支付成功");
            } else {
                logger.info("天付宝  订单查询：失败");
                return R.error("支付失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("天付宝  订单查询：异常 >> " + e.getMessage());
            return R.error("订单查询异常");
        } finally {
            logger.info("天付宝 订单查询:}");
        }
    }


}
