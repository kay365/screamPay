package com.qh.paythird.tx;

import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.paythird.tx.utils.TXAgencyPayConst;
import com.qh.paythird.tx.utils.TXAgencyRequestUtil;
import com.qh.paythird.tx.utils.MapUtils;
import com.qh.redis.service.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @// TODO: 2018/4/10 天下支付Service(代)
 */
public class TXAgencyPayService {

    private static final Logger logger = LoggerFactory.getLogger(TXAgencyPayService.class);

    /**
     * @param order
     * @return
     * @// TODO: 2018/4/10   发起支付>>代付
     */
    public R order(Order order) {
        logger.info("天下支付  代付：{");
        try {
            String shopId = RedisUtil.getPayCommonValue(TXAgencyPayConst.SHOP_ID);
            String payUrl = RedisUtil.getPayCommonValue(TXAgencyPayConst.PAY_URL);
            TreeMap<String, String> paramsMap = new TreeMap<String, String>();
            paramsMap.put("version", "1.0");
            paramsMap.put("spid", shopId);
            String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            paramsMap.put("sp_serialno", order.getOrderNo());
            paramsMap.put("sp_reqtime", time);
            paramsMap.put("tran_amt", "" + order.getAmount().intValue() * 100);
            paramsMap.put("cur_type", order.getCurrency());
            paramsMap.put("pay_type", "1");   //普通余额支付填 1；垫资代付填3
            paramsMap.put("acct_name", new String(order.getAcctName().getBytes(), "UTF-8"));  //收款人姓名
            paramsMap.put("acct_id", order.getBankNo());   //收款人账号
            paramsMap.put("acct_type", String.valueOf(order.getAcctType()));   //0 借记卡， 1 贷记卡， 2 对公账户
            paramsMap.put("mobile", order.getMobile());
            paramsMap.put("bank_name", order.getBankName());
            paramsMap.put("bank_settle_no", order.getBankCode());  //对私可不值，对公必传
            paramsMap.put("bank_branch_name", order.getBankBranch());
            paramsMap.put("business_type", "20101"); // 20: 101 业务往来款 >> 102 员工工资 >> 103 报销 >> 104 合作款项 >> 105 赔付保金 >> 999 其他
            paramsMap.put("memo", "4"); // 1:转账/2:工资代发/3:退款/4:支付/5:费用报销/6:还款
            Map<String, String> result = TXAgencyRequestUtil.sendRequest(payUrl, paramsMap);
            if (String.valueOf(Constant.result_code_succ).equals(result.get(Constant.result_code))) {// 成功
                result.put(Constant.result_code, String.valueOf(Constant.result_code_succ));
                result.put(Constant.result_msg, String.valueOf("支付成功"));
                return R.ok("代付成功");
            } else {
                return R.error("代付失败，" + result.get("retmsg"));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("天下支付代付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("代付异常");
        } finally {
            logger.info("天下支付 代付：}");
        }
    }


    /**
     * @param shopSerialNo 商户代付单号
     * @return
     * @// TODO: 2018/4/10 查询订单
     */
    public R query(String shopSerialNo) {
        // 封装参数
        TreeMap<String, String> urlParams = new TreeMap<>();
        String version = RedisUtil.getPayCommonValue(TXAgencyPayConst.VERSION);
        String queryUrl = RedisUtil.getPayCommonValue(TXAgencyPayConst.QUERY_ONE_URL);
        String shopId = RedisUtil.getPayCommonValue(TXAgencyPayConst.SHOP_ID);
        urlParams.put("version", version);
        urlParams.put("spid", shopId); // 商户号
        urlParams.put("sp_serialno", shopSerialNo); // 商户代付单号
        urlParams.put("sp_reqtime", TXAgencyRequestUtil.getNumberDate());// 请求时间
        Map<String, String> result = TXAgencyRequestUtil.sendRequest(queryUrl, urlParams);
        return MapUtils.stringCastToR(result);
    }


}
