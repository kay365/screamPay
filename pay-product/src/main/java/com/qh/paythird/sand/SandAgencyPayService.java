package com.qh.paythird.sand;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.paythird.sand.utils.SandAgencyPayConst;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * 衫德代付Service
 */
public class SandAgencyPayService {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 发起代付
     *
     * @return
     */
    public R order(Order order) {

        logger.info("衫德代付 代付：{");

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("orderCode", order.getOrderNo());
        jsonObject.put("version", SandAgencyPayConst.getVersion());
        jsonObject.put("productId", SandAgencyPayConst.getProductId());
        jsonObject.put("tranTime", SandAgencyPayConst.getCurrentTime());
        jsonObject.put("tranAmt", order.getAmount());
        jsonObject.put("currencyCode", SandAgencyPayConst.getCurrencyCode());
        jsonObject.put("accAttr", SandAgencyPayConst.getAccAttr());
        jsonObject.put("accNo", order.getBankNo());
        jsonObject.put("accType", order.getAcctType());
        jsonObject.put("accName", order.getAcctName());

//		jsonObject.put("provNo", "sh");// 收款人开户省份编码
//		jsonObject.put("cityNo", "sh");// 收款人开户城市编码
        if ("1".equals(SandAgencyPayConst.getAccAttr())) { // 对公时必须传入一下参数>>bankName>>bankType
            if (StringUtils.isEmpty(order.getBankName()) || StringUtils.isEmpty(order.getBankCode())) {
                logger.info("衫德代付  代付: 收款账户开户行名称或收款人账户联行号为空");
                return R.error("收款账户开户行名称或收款人账户联行号为空");
            }
        }
        jsonObject.put("bankName", order.getBankName());// 收款账户开户行名称
        jsonObject.put("bankType", order.getBankCode());// 收款人账户联行号
        jsonObject.put("remark", order.getMemo());
        jsonObject.put("reqReserved", "请求方保留测试");

//        HttpUtil httpUtil = new HttpUtil();
//        String responseData = null;
//        try {
//            responseData = httpUtil.post(SandAgencyPayConst.getPayUrl(), SandAgencyPayConst.getShopId(), SandAgencyPayConst.getTransCode(), jsonObject.toJSONString());
//            JSONObject data = JSON.parseObject(responseData);
//            String respDesc = data.getString("respDesc");// 代付返回信息
//            String resultFlag = data.getString("resultFlag");// 代付返回状态码
//            if (SandAgencyPayConst.ERROR_CODE.equals(resultFlag)) {// 代付失败
//                return R.error(respDesc);
//            } else {
//                logger.info("衫德代付 代付： 订单状态 >>：" + respDesc + ">>>>>>>" + data.toJSONString());
//                return R.ok(respDesc);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.info("衫德代付 : 代付异常" + e.getMessage());
//            return R.error("代付异常");
//        } finally {
//            logger.info("衫德代付 代付：}");
//        }
        return null;
    }

    /**
     * 查询代付订单
     *
     * @param order 订单
     * @return
     */
    public R query(Order order, HttpServletRequest request) {

        logger.info("衫德代付 订单查询：{");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderCode", order.getOrderNo());
        jsonObject.put("version", SandAgencyPayConst.getVersion());
        jsonObject.put("productId", SandAgencyPayConst.getProductId());
        jsonObject.put("tranTime", SandAgencyPayConst.getCurrentTime());
//        HttpUtil httpUtil = new HttpUtil();
//        try {
//            String responseData = httpUtil.post(SandAgencyPayConst.getQueryOrderUrl(), SandAgencyPayConst.getShopId(), SandAgencyPayConst.getTransCode(), jsonObject.toJSONString());
//            JSONObject data = JSON.parseObject(responseData);
//            String respDesc = data.getString("respDesc");
//            if (SandAgencyPayConst.ERROR_CODE.equals(data.getString("resultFlag"))) {
//                logger.info("衫德代付 订单查询：{}", respDesc);
//                return R.error(respDesc);
//            }else {
//                logger.info("衫德代付 订单查询：{}", respDesc);
//                return R.ok(respDesc);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.info("衫德代付 订单查询：异常>>" + e.getMessage());
//            return R.error("查询异常");
//        } finally {
//            logger.info("衫德代付 订单查询：}");
//        }
           return null;
    }

}
