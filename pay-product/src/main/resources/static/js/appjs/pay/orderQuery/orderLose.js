var prefix = "/orderQuery/orderLose"
$(function() {
	merchNoSearch(merchNo,merchNos);
	//时间框
	$(".form_datetime").datetimepicker({
		  language : 'zh-CN',
		  format: 'yyyy-mm-dd',
	      weekStart: 1,
	      todayBtn:  1,
	      autoclose: 1,
		  todayHighlight: 1,
		  startView: 2,
		  minView: 2,
		  forceParse: 0
	});
	$(".form_datetime").val(formatDate(new Date()));
	load();
});



function load() {
	$('#exampleTable').bootstrapTable({
		method : 'get', // 服务器数据的请求方式 get or post
		url : prefix + "/list", // 服务器数据的加载地址
		iconSize : 'outline',
		toolbar : '#exampleToolbar',
		striped : true, // 设置为true会有隔行变色效果
		dataType : "json", // 服务器返回的数据类型
		pagination : true, // 设置为true会在底部显示分页条
		singleSelect : false, // 设置为true将禁止多选
		pageSize : 10, // 如果设置了分页，每页数据条数
		pageNumber : 1, // 如果设置了分布，首页页码
		//search : true, // 是否显示搜索框
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
				businessNo:$('#businessNo').val(),
	            merchNo:$('#merchNo').val(),
	            payCompany:$('#payCompany').val(),
	            orderState:$('#orderState').val(),
	            outChannel:$('#outChannel').val(),
	            beginDate:$('#beginDate').val(),
	            endDate:$('#endDate').val()
			};
		},
		columns : [
			{
				field : 'merchNo', 
				title : '平台商户号' 
			},
			{
				field : 'businessNo',
				title : '业务单号' 
			},
			{
				field : 'orderNo', 
				title : '订单号' 
			},
											{
				field : 'payCompany',
				visible: merchNo == null,
				title : '支付公司',
				formatter:function(value){
					return payCompanys[value];
				}
			},
											{
				field : 'outChannel', 
				title : '渠道',
				formatter:function(value){
					return outChannels[value];
				}
			},
											{
				field : 'amount', 
				title : '订单金额(元)' 
			},
											{
				field : 'qhAmount', 
				title : '平台手续费' 
			},
											{
				field : 'orderState', 
				title : '订单状态',
				formatter:function(value){
					return orderStates[value];
				}
			},
											{
				field : 'msg', 
				title : '消息提示' 
			},
											{
				field : 'crtDate', 
				title : '创建时间',
				formatter:function(value, row, index){
					return intTime2Str(value); 
				}
			}]
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
