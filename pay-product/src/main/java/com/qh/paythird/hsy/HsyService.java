package com.qh.paythird.hsy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.service.PayService;
import com.qh.paythird.dianxin.utils.MD5Utils;
import com.qh.paythird.hsy.utils.HsyConst;
import com.qh.redis.service.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class HsyService {

    private static final Logger logger = LoggerFactory.getLogger(HsyService.class);

    private static final HashMap<String, Pair> channelMap = new HashMap<>();

    static {
        channelMap.put(OutChannel.wx.name(), Pair.of(PayConstants.web_qrcode_url, HsyConst.hsy_code_url));
        channelMap.put(OutChannel.gzh.name(), Pair.of(PayConstants.web_gzh_qrcode_url, HsyConst.hsy_gzh_url));
        channelMap.put(OutChannel.ali.name(), Pair.of(PayConstants.web_qrcode_url, HsyConst.hsy_code_url));
        channelMap.put(OutChannel.qq.name(), Pair.of(PayConstants.web_qrcode_url, HsyConst.hsy_code_url));
        channelMap.put(OutChannel.q.name(), Pair.of(PayConstants.web_eval_url, HsyConst.hsy_code_url));
        channelMap.put(OutChannel.wy.name(), Pair.of(PayConstants.web_eval_url, HsyConst.hsy_code_url));
    }

    private static final HashMap<String, String> orcMap = new HashMap<>();

    static {
        orcMap.put(OutChannel.wx.name(), HsyConst.hsy_WX_NATIVE);
        orcMap.put(OutChannel.gzh.name(), HsyConst.hsy_WX_JSAPI);
        orcMap.put(OutChannel.ali.name(), HsyConst.hsy_ALI_QRCODE);
        orcMap.put(OutChannel.qq.name(), HsyConst.hsy_QQ_QRCODE);
        orcMap.put(OutChannel.q.name(), HsyConst.hsy_BANK_QUICK_PAY_FD);
        orcMap.put(OutChannel.wy.name(), HsyConst.hsy_BANK_QUICK_PAY_FD);
    }

    /**
     * @param order
     * @return
     * @Description 支付发起
     */
    public R order(Order order) {
        String outChannel = order.getOutChannel();
        logger.info("汇收银支付 " + OutChannel.getDesc(outChannel) + " 渠道开始------------------------------------------------------");
        try {
            String hsyOutChannel = orcMap.get(outChannel);
            if (ParamUtil.isEmpty(hsyOutChannel)) {
                logger.error("汇收银支付 不支持的支付渠道：{}", order.getOutChannel());
                return R.error("不支持的支付渠道");
            }

            R r = pay(order, hsyOutChannel);
            return buildPayUrl(r, outChannel);
        } finally {
            logger.info("汇收银支付 " + OutChannel.getDesc(outChannel) + " 渠道结束------------------------------------------------------");
        }
    }


    /**
     * 处理返回参数，将正确的值返回到正确的key
     *
     * @param r
     * @param channel
     * @return
     */
    private R buildPayUrl(R r, String channel) {
        Map<String, String> resultMap = (Map<String, String>) r.getData();
        if(ParamUtil.isNotEmpty(resultMap)){
            Pair pair = channelMap.get(channel);
            resultMap.put(pair.getFirst().toString(), resultMap.get(pair.getSecond().toString()));
        }
        if(resultMap!=null && resultMap.size()>0){
            resultMap.remove(HsyConst.hsy_code_url);
            resultMap.remove(HsyConst.hsy_pay_url);
            resultMap.remove(HsyConst.hsy_gzh_url);
        }
        return r;
    }

    /**
     * 支付
     *
     * @param order
     * @return
     */
    private R pay(Order order, String way) {
        logger.info("汇收银支付 开始-------------------------------------------------");
        logger.info("MerchNo:{},OrderNo:{}", order.getMerchNo(), order.getOrderNo());
        try {
            TreeMap<String, String> requestParams = new TreeMap<>();
            String reqTime = DateUtil.getCurrentNumStr();
            String url = RedisUtil.getPayCommonValue(HsyConst.hsy_requrl);
            if (ParamUtil.isEmpty(url)) {
                logger.error("hsy_requrl");
                return R.error("hsy_requrl!");
            }

            //组装公共参数
            requestParams.put("method", HsyConst.hsy_method_applypay);
            requestParams.put("timestamp", reqTime);
            buildPublicArgs(requestParams, order);

            //组装业务参数
            JSONObject biz_content = new JSONObject();
            biz_content.put("out_trade_no", order.getOrderNo());
            biz_content.put("subject", order.getTitle());
            biz_content.put("total_fee", ParamUtil.yuanToFen(order.getAmount()));
            biz_content.put("client_ip", order.getReqIp());
            biz_content.put("channel_Type", way);
            biz_content.put("notify_url", PayService.commonNotifyUrl(order));
            biz_content.put("return_url", PayService.commonReturnUrl(order));
            requestParams.put("biz_content", biz_content.toString());

            //组装签名
            requestParams.put("sign", MD5Utils.commonSign(requestParams, RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_key)).toUpperCase());

            //向汇收银发起支付请求
            JSONObject param = JSONObject.parseObject(JSON.toJSONString(requestParams));
            logger.info("汇收银支付请求地址：" + url);
            logger.info("汇收银支付请求参数：" + param.toString());
            String responseResult = RequestUtils.doPostJson(url, param.toString(), HsyConst.hsy_charset_default);
            logger.info("汇收银返回结果：" + responseResult);

            //检测返回结果是否为空
            if (ParamUtil.isEmpty(responseResult)) {
                logger.error("汇收银请求结果异常！");
                return R.error("请求结果异常！");
            }

            //解析返回结果
            TreeMap<String, String> treeMap = JSON.parseObject(responseResult, TreeMap.class);

            String key = RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_key);

//            //验签
//            if (!MD5Utils.checkParam(treeMap,key , HsyConst.hsy_charset_default)) {
//                logger.error("汇收银返回验签失败！");
//                return R.error("返回验签失败！");
//            }

            //验证返回结果是否成功
            if (!HsyConst.hsy_ret_code_succ.equalsIgnoreCase(treeMap.get("return_code")) || !HsyConst.hsy_ret_code_succ.equalsIgnoreCase(treeMap.get("result_code"))) {
                String errMsg = treeMap.get("return_msg");
                errMsg = ParamUtil.isEmpty(treeMap.get("error_msg")) ? treeMap.get("error_msg") : errMsg;
                logger.error("汇收银返回支付失败！" + errMsg);
                return R.error("支付失败：" + errMsg);
            }

            //反填Order属性
            order.setBusinessNo(treeMap.get("hy_bill_no"));
            //设置总金额
            order.setAmount(ParamUtil.fenToYuan(treeMap.get("total_fee")));

            //返回支付url
            Map<String, String> resultMap = PayService.initRspData(order);
            resultMap.put(HsyConst.hsy_code_url, treeMap.get("code_url"));
            resultMap.put(HsyConst.hsy_pay_url, treeMap.get("hy_pay_url"));
            resultMap.put(HsyConst.hsy_gzh_url, treeMap.get("hy_js_auth_pay_url"));
            return R.okData(resultMap);
        } catch (Exception e) {
            logger.info("汇收银支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        } finally {
            logger.info("汇收银支付 结束-------------------------------------------------");
        }
    }

    /**
     * @param order
     * @return
     * @Description 支付查询
     */
    public R query(Order order) {
        logger.info("汇收银支付查询 开始-------------------------------------------------");
        logger.info("MerchNo:{},OrderNo:{}", order.getMerchNo(), order.getOrderNo());
        try {
            TreeMap<String, String> requestParams = new TreeMap<>();
            String reqTime = DateUtil.getCurrentNumStr();
            String url = RedisUtil.getPayCommonValue(HsyConst.hsy_queryurl);
            if (ParamUtil.isEmpty(url)) {
                logger.error("未配置hsy_queryurl");
                return R.error("未配置hsy_queryurl!");
            }

            //组装公共参数
            requestParams.put("method", HsyConst.hsy_method_query);
            requestParams.put("timestamp", reqTime);
            buildPublicArgs(requestParams, order);

            //组装业务参数
            JSONObject biz_content = new JSONObject();
            biz_content.put("out_trade_no", order.getOrderNo());
            requestParams.put("biz_content", biz_content.toString());

            //组装签名
            requestParams.put("sign", MD5Utils.commonSign(requestParams, RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_key)).toUpperCase());

            //向汇收银发起支付请求
            JSONObject param = JSONObject.parseObject(JSON.toJSONString(requestParams));
            logger.info("汇收银支付请求地址：" + url);
            logger.info("汇收银支付请求参数：" + param.toString());
            String responseResult = RequestUtils.doPostJson(url, param.toString(), HsyConst.hsy_charset_default);
            logger.info("汇收银返回结果：" + responseResult);

            //检测返回结果是否为空
            if (ParamUtil.isEmpty(responseResult)) {
                logger.error("汇收银请求结果异常！");
                return R.error("请求结果异常！");
            }

            //解析返回结果
            Map<String, String> treeMap = JSON.parseObject(responseResult, TreeMap.class);
            logger.info("汇收银后台参数："+treeMap);

//            //验签
//            if (!MD5Utils.checkParam(treeMap, RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_key), HsyConst.hsy_charset_default)) {
//                logger.error("汇收银返回验签失败！");
//                return R.error("验签失败！");
//            }

            //验证返回结果是否成功
            boolean paySuccess = HsyConst.hsy_ret_code_succ.equalsIgnoreCase(treeMap.get("return_code")) &&
                    HsyConst.hsy_ret_code_succ.equalsIgnoreCase(treeMap.get("result_code")) &&
                    HsyConst.hsy_ret_code_succ.equalsIgnoreCase(treeMap.get("trade_status"));
            if (!paySuccess) {
                if (HsyConst.hsy_ret_code_undeal.equals(treeMap.get("trade_status"))) {
                    order.setOrderState(OrderState.ing.id());
                    logger.info("返回支付状态：未支付");
                    return R.error("返回支付状态：未支付");
                } else {
                    order.setOrderState(OrderState.fail.id());
                    logger.info("返回支付状态：支付失败");
                    return R.error("返回支付状态：支付失败");
                }
            }

            //反填Order属性
            order.setOrderState(OrderState.succ.id());
            //设置实际支付金额
            BigDecimal realAmount = ParamUtil.fenToYuan(treeMap.get("real_fee"));
            if (ParamUtil.isNotEmpty(realAmount)) {
                order.setRealAmount(realAmount);
            } else {
                if (ParamUtil.isEmpty(order.getRealAmount())) {
                    order.setRealAmount(BigDecimal.ZERO);
                }
            }
            //设置总金额
            BigDecimal totalAmount = ParamUtil.fenToYuan(treeMap.get("total_fee"));
            if (ParamUtil.isNotEmpty(totalAmount)) {
                order.setAmount(totalAmount);
            } else {
                if (ParamUtil.isEmpty(order.getAmount())) {
                    order.setAmount(BigDecimal.ZERO);
                }
            }
            logger.info("返回支付状态：支付成功");
            return R.ok("返回支付状态：支付成功");
        } catch (Exception e) {
            logger.info("汇收银支付查询 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付查询异常");
        } finally {
            logger.info("汇收银支付查询 结束-------------------------------------------------");
        }
    }

    /**
     * 组装公共参数
     *
     * @param requestParams
     * @param order
     */
    private void buildPublicArgs(TreeMap<String, String> requestParams, Order order) {

        requestParams.put("version", HsyConst.hsy_version_default);
        requestParams.put("app_id", RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_app_id));
        requestParams.put("mch_uid", RedisUtil.getPayCommonValue(HsyConst.hsy_mch_uid));
        requestParams.put("charset", HsyConst.hsy_charset_default);
        requestParams.put("sign_type", HsyConst.hsy_sign_type);
    }

    /**
     * @param order
     * @param request
     * @return
     * @Description 支付回调
     */
    public R notify(Order order, String requestBody) {
        logger.info("汇收银支付回调 开始-------------------------------------------------");
        logger.info("MerchNo:{},OrderNo:{}", order.getMerchNo(), order.getOrderNo());
        try {
            if(ParamUtil.isEmpty(requestBody)){
                logger.error("汇收银后台参数为空:"+requestBody);
                return R.error();
            }

            //解析回调参数
            TreeMap<String, String> result =  JSON.parseObject(requestBody,TreeMap.class);
            logger.info("汇收银后台参数："+result);

            //验签
            if (!MD5Utils.checkParam(result, RedisUtil.getPayCommonValue(order.getPayMerch() + HsyConst.hsy_key), HsyConst.hsy_charset_default)) {
                logger.error("汇收银后台通知验签失败！");
                return R.error();
            }

            //反填订单状态
            boolean paySuccess = HsyConst.hsy_ret_code_succ.equalsIgnoreCase(result.get("trade_status"));
            if (!paySuccess) {
                if (HsyConst.hsy_ret_code_undeal.equals(result.get("trade_status"))) {
                    order.setOrderState(OrderState.ing.id());
                    logger.info("返回支付状态：未支付");
                    return R.error();
                } else {
                    order.setOrderState(OrderState.fail.id());
                    logger.info("返回支付状态：支付失败");
                    return R.error();
                }
            }
            order.setOrderState(OrderState.succ.id());
            //设置实际支付金额
            BigDecimal realAmount = ParamUtil.fenToYuan(result.get("real_fee"));
            if (ParamUtil.isNotEmpty(realAmount)) {
                order.setRealAmount(realAmount);
            } else {
                if (ParamUtil.isEmpty(order.getRealAmount())) {
                    order.setRealAmount(BigDecimal.ZERO);
                }
            }
            //设置总金额
            BigDecimal totalAmount = ParamUtil.fenToYuan(result.get("total_fee"));
            if (ParamUtil.isNotEmpty(totalAmount)) {
                order.setAmount(totalAmount);
            } else {
                if (ParamUtil.isEmpty(order.getAmount())) {
                    order.setAmount(BigDecimal.ZERO);
                }
            }
            logger.info("返回支付状态：支付成功");
            return R.ok();
        } catch (Exception e) {
            logger.info("汇收银支付回调 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        } finally {
            logger.info("汇收银支付回调 结束-------------------------------------------------");
        }
    }
}
