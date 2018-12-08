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
	$("#endDate").val(formatDate(new Date()));
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
		url : prefix + "/list", // 服务器数据的加载地址
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
	            merchNo:$('#merchNo').val(),
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
						return row.merchName+"("+value+")";
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
				{
					field : 'offlineTransfer', 
					title : '选择代付方式',
					visible : offlineTransfer,
					formatter : function(value, row, index) {
						if(row.auditType != 2){
							return "不支持";
						}else if(row.auditResult == 2){
							return "审核未通过";
						}else if(row.auditResult == 1){
							return "审核通过";
						}else{
							var hidden = offlineTransfer?"":"hidden";
							/**
							return '<a class="btn btn-primary btn-sm ' + hidden + '" title="线下转账直接扣减余额" onclick="offlineTransfer(\''
							+ row.orderNo + '\',\'' + row.merchNo + '\',' + row.auditType
							+ ')"><i class="fa fa-credit-card-alt"></i>线下转账</a> ';**/
							var selectHtml = "<option value='-1'>线下转账</option>";
							$.each(payConfigCompany,function(index,obj){
								selectHtml += "<option value='"+obj.payMerch+"' company='"+obj.company+"'>"+obj.callbackDomain+"("+obj.payMerch+")"+"</option>";
							})
							selectHtml = "<select class='form-control' onChange='acpSelect(this,"+index+")'>" + selectHtml + "</select>";
							companArray[index] = '-1';
							return selectHtml;
						}
					}
				},
												{
					title : '操作',
					field : 'auditResult',
					align : 'center',
					formatter : function(value, row, index) {
						if(value == 0){
							return '<a class="btn btn-primary btn-sm ' + s_audit_h + '" href="#" title="通过"  mce_href="#" onclick="audit(\''
							+ row.orderNo + '\',\'' + row.merchNo + '\',\'' + row.auditType + '\',' + 1
							+ ','+index+')"><i class="fa fa-calendar-check-o"></i>通过</a> ' +
							'<a class="btn btn-danger btn-sm ' + s_audit_h + '" href="#" title="不通过"  mce_href="#" onclick="audit(\''
							+ row.orderNo + '\',\'' + row.merchNo + '\',\'' + row.auditType + '\',' + 2
							+ ','+index+')"><i class="fa fa-calendar-check-o"></i>不通过</a> ';
						}else{
							return "已审核";
						}
					}
				} ]
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}

function acpSelect(obj,index){
	var valueSelected = $(obj).children('option:selected').val();
	var companySelected = $(obj).children('option:selected').attr("company");
	companArray[index] = valueSelected+","+companySelected;
	console.log(companArray);
}

function offlineTransfer(orderNo,merchNo,auditType){
	layer.confirm('确定要执行线下转账操作吗？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix+"/offlineTransfer",
			type : "post",
			data : {
				orderNo : orderNo,
				merchNo : merchNo,
				auditType : auditType,
			},
			success : function(r) {
				if (r.code==0) {
					layer.msg(r.msg);
					reLoad();
				}else{
					layer.msg(r.msg);
				}
			}
		});
	})
}

function audit(orderNo,merchNo,auditType,auditResult,index) {
	if(companArray[index] == '-1' && auditResult == 1){
		return offlineTransfer(orderNo,merchNo,auditType);
	}
	if(companArray[index] == '-1' && auditResult != 1){
		companArray[index] = "";
	}
	
	var msg = (auditResult == 1?'通过':'不通过');
	layer.confirm('确定要审核选中的记录'+msg+'？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix+"/audit",
			type : "post",
			data : {
				orderNo : orderNo,
				merchNo : merchNo,
				auditType : auditType,
				auditResult : auditResult,
				company : companArray[index]
			},
			success : function(r) {
				if (r.code==0) {
					layer.msg(r.msg);
					reLoad();
				}else{
					layer.msg(r.msg);
				}
			}
		});
	})
}

function batchAudit(auditResult) {
	var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要审核的数据");
		return;
	}
	var msg = (auditResult == 1?'通过':'不通过');
	layer.confirm("确认要批量审核" + msg + "选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var orderNos = new Array();
		var merchNos = new Array();
		var auditTypes = new Array();
		var companys = new Array();
		// 遍历所有选择的行数据，取每条数据对应的ID
		var auditFlag = false;
		$.each(rows, function(i, row) {
			if(row['auditResult'] != 0 || companArray[i] == '-1'){
				auditFlag = true;
			}
			
			orderNos[i] = row['orderNo'];
			merchNos[i] = row['merchNo'];
			auditTypes[i] = row['auditType'];
			companys[i] = companArray[i];
		});
		if(auditFlag){
			layer.msg("包含已审核过的数据或代付方式为线下打款，请重新选择！");
			return;
		}
		$.ajax({
			type : 'POST',
			data : {
				orderNos : orderNos,
				merchNos : merchNos,
				auditTypes : auditTypes,
				auditResult : auditResult,
				companys : companys
			},
			url : prefix + '/batchAudit',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	}, function(result) {
		layer.msg("批量审核" + msg + "已取消！");
	});
}

function setting() {
	layer.open({
		type : 2,
		title : '设置',
		maxmin : false,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '400px', '260px' ],
		content : prefix + '/settingPage' // iframe的url
	});
}