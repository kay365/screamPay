
var prefix = "/pay/payQrConfig"
$(function() {
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
		// queryParamsType : "limit",
		// //设置为limit则会发送符合RESTFull格式的参数
		singleSelect : true, // 设置为true将禁止多选
		// contentType : "application/x-www-form-urlencoded",
		// //发送到服务器的数据编码类型
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
	            merchNo:$('#merchNo').val()
			};
		},
		columns : [
				{
					checkbox : true
				},
				{
					field : 'merchNo', 
					title : '商户号' 
				},
												{
					field : 'outChannel', 
					title : '支付渠道' ,
					formatter:function(value, row, index) {
						if(value != null){
							return  outChannels[value];
						}
					}
				},
												{
					field : 'accountNo', 
					title : '收款账号' 
				},
												{
					field : 'accountName', 
					title : '收款名称' 
				},
												{
					field : 'accountPhone',
					title : '收款电话'
				},
				{
					field : 'costRate', 
					title : '成本费率' ,
					visible: merchNo == null
				},
												{
					field : 'jfRate', 
					title : '手续费率' 
				},
												{
					field : 'serviceTel', 
					title : '客户电话' 
				},
												{
					field : 'apiKey',
					title : 'apiKey信息'
            	},
												{
					field : 'memo', 
					title : '备注信息' 
				},
												{
					field : 'qrs', 
					title : '收款二维码图片' ,
					formatter : function(value, row, index) {
						var e = '<a class="btn btn-primary btn-sm '+s_upload_h+'" href="#" mce_href="#" title="上传收款码" onclick="editQrs(\''
								 	+ row.outChannel + '\',\'' + row.merchNo
								+ '\')"><i class="fa fa-folder-open"></i></a> ';
						return e
					}
				},
				{
					title : '操作',
					field : 'id',
					align : 'center',
					formatter : function(value, row, index) {
						var e = '<a class="btn btn-primary btn-sm '+s_edit_h+'" href="#" mce_href="#" title="编辑" onclick="edit(\''
								 	+ row.outChannel + '\',\'' + row.merchNo
								+ '\')"><i class="fa fa-edit"></i></a> ';
						var d = '<a class="btn btn-warning btn-sm '+s_remove_h+'" href="#" title="删除"  mce_href="#" onclick="remove(\''
									+ row.outChannel + '\',\'' + row.merchNo
								+ '\')"><i class="fa fa-remove"></i></a> ';
						return e + d ;
					}
				} ]
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function add() {
	layer.open({
		type : 2,
		title : '增加',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '520px' ],
		content : prefix + '/add' // iframe的url
	});
}

function editQrs(outChannel,merchNo) {
	var index = layer.open({
		type : 2,
		title : '编辑扫码图片',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '550px' ],
		maxmin: true,
		content : prefix + '/editQrs/' + outChannel + "/" + merchNo // iframe的url
	});
	layer.full(index);
}


function edit(outChannel,merchNo) {
	layer.open({
		type : 2,
		title : '编辑',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '550px' ],
		content : prefix + '/edit/' + outChannel + "/" + merchNo // iframe的url
	});
}
function remove(outChannel,merchNo) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix+"/remove",
			type : "post",
			data : {
				'outChannel' : outChannel,
				'merchNo' : merchNo
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

function batchRemove() {
	var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要删除的数据");
		return;
	}
	layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var ids = new Array();
		// 遍历所有选择的行数据，取每条数据对应的ID
		$.each(rows, function(i, row) {
			ids[i] = row['id'];
		});
		$.ajax({
			type : 'POST',
			data : {
				"ids" : ids
			},
			url : prefix + '/batchRemove',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
				}
			}
		});
	}, function() {

	});
}