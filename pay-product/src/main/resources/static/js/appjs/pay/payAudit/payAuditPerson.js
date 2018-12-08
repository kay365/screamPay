var prefix = "/pay/payAudit"
$(function() {
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
//	$('#auditType').val(0);
	$('#auditResult').val(0);
	load();
});

function formatDate(date){
	return date.getFullYear()+"-"+
	(date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1)+"-"+
	(date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
}

function intTime2Str(time){
	if(time != null){
		var date = new Date(); 
		date.setTime(1000 * time); 
		var year = date.getFullYear();  
		var month = date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;  
		var day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();  
		var hour = date.getHours()< 10 ? "0" + date.getHours() : date.getHours();  
		var minute = date.getMinutes()< 10 ? "0" + date.getMinutes() : date.getMinutes();  
		var second = date.getSeconds()< 10 ? "0" + date.getSeconds() : date.getSeconds();  
		return year + "-" + month + "-" + day+" "+hour+":"+minute+":"+second;  
	}else{
		return "";
	}
}
var companArray = [];
function load() {
	
	$('#exampleTable').bootstrapTable({
		method : 'get', // 服务器数据的请求方式 get or post
		url : prefix + "/listPerson", // 服务器数据的加载地址
		iconSize : 'outline',
		toolbar : '#exampleToolbar',
		striped : true, // 设置为true会有隔行变色效果
		dataType : "json", // 服务器返回的数据类型
		pagination : true, // 设置为true会在底部显示分页条
		singleSelect : false, // 设置为true将禁止多选
		pageSize : 10, // 如果设置了分页，每页数据条数
		pageNumber : 1, // 如果设置了分布，首页页码
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
	            orderNo:$('#orderNo').val(),
	            auditResult:$('#auditResult').val(),
	            auditType:$('#auditType').val(),
	            beginDate:$('#beginDate').val(),
	            endDate:$('#endDate').val()
			};
		},
		columns : [
				{
					checkbox : true
				},
				{
					field : 'merchNo', 
					title : '商户号' ,
					formatter : function(value, row, index) {
						return value;
					}
				},
												{
					field : 'orderNo', 
					title : '订单号' 
				},								{
					field : 'amount', 
					title : '到账金额' 
				},								{
					field : 'poundage', 
					title : '手续费' 
				},
												{
					field : 'auditType', 
					title : '审核类型' ,
					formatter : function(value, row, index) {
						return auditTypes[value];
					}
					
				},
				{
					field : 'auditResult', 
					title : '审核结果', 
					formatter : function(value, row, index) {
						return auditResults[value];
					}
				},
												{
					field : 'auditor', 
					title : '审核人' 
				},
												{
					field : 'auditTime', 
					title : '审核时间' 
				},
												{
					field : 'crtTime', 
					title : '创建时间',
					formatter : function(value, row, index) {
						return intTime2Str(value);
					}
				},
				{
					field : 'memo', 
					title : '备注' 
				},
				{
					field:'acpCompany',
					visible:false,
					title:'dfd'
				},
				 ]
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
