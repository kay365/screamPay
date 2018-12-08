var prefix = "/pay/payBank"
$(function() {
	load();
	$("#company").change(function(){
		var company = $(this).val();
		getMechNoByCompany(company,"payMerch");
	});
});

var banks = new Array();

function load() {
	$('#exampleTable').bootstrapTable({
		method : 'get', // 服务器数据的请求方式 get or post
		url : prefix + "/list", // 服务器数据的加载地址
		iconSize : 'outline',
		toolbar : '#exampleToolbar',
		striped : true, // 设置为true会有隔行变色效果
		dataType : "json", // 服务器返回的数据类型
		pagination : true, // 设置为true会在底部显示分页条
		// //设置为limit则会发送符合RESTFull格式的参数
		singleSelect : false, // 设置为true将禁止多选
		// //发送到服务器的数据编码类型
		pageSize : 10, // 如果设置了分页，每页数据条数
		pageNumber : 1, // 如果设置了分布，首页页码
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
				company:$('#company').val(),
				payMerch:$('#payMerch').val(),
				cardType:$("#cardType").val()
			};
		},
		columns : [
				{
					checkbox : true
				},
												{
					field : 'company', 
					title : '支付公司' ,
					formatter:function(value){
						return companys[value];
					}
				},
												{
					field : 'payMerch', 
					title : '支付商户号' 
				},
												{
					field : 'cardType', 
					title : '银行卡类型',
					formatter:function(value){
						return cardTypes[value];
					}
				},
												{
					field : 'banks', 
					title : '银行卡列表' ,
					formatter:function(value,row, index){
						banks[index] = value;
						return '<a class="btn btn-primary btn-sm" href="#" title="查看详情"   mce_href="#" onclick="lookBanks(\''
						+ row.company +'\',\''+ row.payMerch +'\',\''+ row.cardType +'\',\'' + index
						+ '\')"><i class="fa fa-eye"></i></a>';
					}
				},
												{
					title : '操作',
					field : 'id',
					align : 'center',
					formatter : function(value, row, index) {
						var e = '<a class="btn btn-primary btn-sm '+s_edit_h+'" href="#" mce_href="#" title="编辑" onclick="edit(\''
								+ row.company +'\',\''+ row.payMerch +'\',\''+ row.cardType +'\',\'' + index
								+ '\')"><i class="fa fa-edit"></i></a> ';
						var d = '<a class="btn btn-warning btn-sm '+s_remove_h+'" href="#" title="删除"  mce_href="#" onclick="remove(\''
								+ row.company +'\',\''+ row.payMerch +'\',\''+ row.cardType +'\',\'' + index
								+ '\')"><i class="fa fa-remove"></i></a> ';
						return e + d ;
					}
				} ]
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}

function lookBanks(company,payMerch,cardType,index){
	var banksValue = banks[index];
	var result = "未配置支付银行！";
	if(banksValue){
		result = "";
		for(var key in banksValue){
			result += bankCodes[key] + "<br>";
		}
	}
	layer.open({
		title:companys[company] + " " + (payMerch == "0"?"":payMerch + " ") +cardTypes[cardType] + "列表",
		content: result //数组第二项即吸附元素选择器或者DOM
	});
}

function add() {
	layer.open({
		type : 2,
		title : '增加',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '850px', '550px' ],
		content : prefix + '/add' // iframe的url
	});
}
function edit(company,payMerch,cardType) {
	layer.open({
		type : 2,
		title : '编辑',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '850px', '550px' ],
		content : prefix + '/edit/' + company + '/' + payMerch + '/' + cardType // iframe的url
	});
}
function remove(company,payMerch,cardType) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix+"/remove",
			type : "post",
			data : {
				company : company,
				payMerch :payMerch,
				cardType : cardType
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

function getMechNoByCompany(payCompany,idEle){
	if(payCompany){
		$.ajax({
			cache : true,
			type : "get",
			url : "/pay/payProperty/getMechNoByCompany",
			data : {payCompany : payCompany},
			async : false,
			error : function(request) {
				parent.layer.alert("Connection error");
			},
			success : function(data) {
				if (data.code == 0) {
					if(data.data){
						var datas = data.data;
						var $selMerchNo = $("#" + idEle);
						$selMerchNo.find("option").remove();
						$selMerchNo.append("<option value=''>--支付商户号--</option>");
						for ( var i in datas) {
							$selMerchNo.append("<option value='"+datas[i]+"'>"+datas[i]+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg)
				}
			}
		});
	}
}