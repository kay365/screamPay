package com.qh.common.config;
/**
 * 
  * @ClassName: Constant
  * @Description: 公用常量
  * @date 2017年10月27日 上午9:51:19
  *
 */
public class Constant {
	/***支付公司名称*****/
	public static final String pay_name = "聚富";
	/*** 返回码定义 *****/
	public static final String result_code = "code";
	/*** 返回消息 **/
	public static final String result_msg = "msg";
	/*** 返回成功值：0 ******/
	public static final int result_code_succ = 0;
	/*** 返回失败值：1 *****/
	public static final int result_code_error = 1;
	/*** 返回成功信息 success ***/
	public static final String result_msg_succ = "success";
	
	public static final String result_msg_succ_ = "result=success";
	/*** 返回成功信息 ok***/
	public static final String result_msg_ok = "ok";
	public static final String result_msg_ok_ = "result=ok";
	/*** 返回成功信息 error ***/
	public static final String result_msg_error = "error";
	/*** 返回数据 data ****/
	public static final String result_data = "data";

	
	/***系统编码 UTF-8****/
	public static final String ec_utf_8 = "UTF-8";
	/***系统编码 GBK****/
	public static final String ec_gbk = "GBK";
	
	
    //自动去除表前缀
    public static String AUTO_REOMVE_PRE = "true";
    //停止计划任务
    public static String STATUS_RUNNING_STOP = "stop";
    //开启计划任务
    public static String STATUS_RUNNING_START = "start";
    
    public static final int data_exist = 2;
    
    public static final int data_noexist = 3;
    /***商户信息参数 merch*****/
    public static final String param_merch = "merch";
    /***json数据参数 jsonData*****/
    public static final String param_jsonData = "jsonData";
    
    
    /***任务--参数***/
    public static final String task_params = "params";
    /***任务--动态选择时间**/
    public static final String task_choiceDate = "choiceDate";
}
