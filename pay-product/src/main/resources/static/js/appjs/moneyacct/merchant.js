var prefix = "/moneyacct/merchant";
$(function() {
	load();
	$("#oneAgent").change(function(){
		var oneAgent = $(this).val();
		findAgentByParent(oneAgent);
		findMerchantByAgent(oneAgent);
	});	
	$("#twoAgent").change(function(){
		var twoAgent = $(this).val();
		findMerchantByAgent(twoAgent);
	});	
});

function findMerchantByAgent(twoAgent){
	if(twoAgent){
		$.ajax({
			cache : true,
			type : "get",
			url : "/moneyacct/findMerchantByAgent",
			data : {parentAgent : twoAgent},
			async : false,
			error : function(request) {
				parent.layer.alert("Connection error");
			},
			success : function(data) {
				if (data.code == 0) {
					if(data.data){
						var datas = data.data;
						var $selMerch = $("#merchNo");
						$selMerch.find("option").remove();
						$selMerch.append("<option value=''>请选择商户</option>");
						for ( var i in datas) {
							$selMerch.append("<option value='"+datas[i].merchNo+"'>"+datas[i].merchantsShortName+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg);
				}
			}
		});
	}else{
		var oneAgent = $("#oneAgent").val();
		if(oneAgent){
			findMerchantByAgent(oneAgent);
		}else{
			var $selMerch = $("#merchNo");
			$selMerch.find("option").remove();
			$selMerch.append("<option value=''>请选择商户</option>");
		}
	}
}

function findAgentByParent(oneAgent){
	if(oneAgent){
		$.ajax({
			cache : true,
			type : "get",
			url : "/moneyacct/findAgentByParent",
			data : {oneAgent : oneAgent},
			async : false,
			error : function(request) {
				parent.layer.alert("Connection error");
			},
			success : function(data) {
				if (data.code == 0) {
					if(data.data){
						var datas = data.data;
						var $selAgent = $("#twoAgent");
						$selAgent.find("option").remove();
						$selAgent.append("<option value=''>请选择代理</option>");
						for ( var i in datas) {
							$selAgent.append("<option value='"+datas[i].agentNumber+"'>"+datas[i].merchantsShortName+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg);
				}
			}
		});
	}else{
		var $selAgent = $("#twoAgent");
		$selAgent.find("option").remove();
		$selAgent.append("<option value=''>请选择代理</option>");
		var $selMerch = $("#merchNo");
		$selMerch.find("option").remove();
		$selMerch.append("<option value=''>请选择商户</option>");
	}
}


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
				oneAgent:$('#oneAgent').val(),
				twoAgent:$('#twoAgent').val(),
				merchNo:$('#merchNo').val()
			};
		},
		columns : [
				{
					field : 'merchNo',
					title : '平台商户号',
					formatter:function(value, row, index) {
                        return row.name+'(' + value+")";
                    }
				},
				{
					field : 'totalEntry',
					title : '账户总入账(元)',
					formatter:function(value, row, index) {
                        return '+ ' + value;
                    }
				},
				{
					field : 'totalOff',
					title : '账户总出账(元)',
					formatter:function(value, row, index) {
                        return '- ' + value;
                    }
				},
				{
					field : 'totalHandFee',
					title : '账户总手续费(元)',
					formatter:function(value, row, index) {
                        return '- ' + value;
                    }
				},
				{
					field : 'balance',
					title : '账户总余额(元)'
				},
				{
					field : 'availBal',
					title : '账户可用余额(元)'
				},
				{
					field : 'forClear',
					title : '账户不可用余额-待结算(元)',
					formatter:function(value, row, index) {
						return parseFloat(row.balance - row.availBal).toFixed(2);
                    }
				},
				{
					field : 'inTrading',
					title : '账户冻结(元)'
				},
				{
					field : 'detail',
					title : '详情',
                    formatter:function(value, row, index) {
						var ret = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="详情" onclick="showDetail(\''
                            + row.merchNo
                            + '\')"><i class="fa fa-bars"></i></a> ';
                        return ret;
                    }
				},{
					field : 'operation',
					title : '操作',
                    formatter:function(value, row, index) {
						var ret = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="划拨到平台账户" onclick="transfer(\''
                            + row.merchNo
                            + '\')">划拨</a> '+
                            '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="冻结/解冻可用余额" onclick="freeze(\''
                            + row.merchNo
                            + '\')">冻结</a> ';
                        return ret;
                    }
				}]
	});
}

function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function showDetail(merchNo) {
	var index = layer.open({
		type : 2,
		title : merchNo + ' 商户钱包详情',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '550px' ],
		maxmin: true,
		content : prefix + '/detail/' + merchNo // iframe的url
	});
	layer.full(index);
}

function transfer(merchNo){
	var content = '<form class="form-horizontal m-t">' +
			  '<div ><label class="col-sm-4 control-label text-left">请输入划拨金额：</label>' + 
			  '<div class="col-sm-8">' +
			  		'<input id="money" name="money" class="form-control" type="number" maxlength="32" >' +
			  '</div></div>' +
			  '<br/><br/><div ><label class="col-sm-4 control-label text-left">是否划拨给平台：</label>' + 
			  '<div class="col-sm-8">' +
			  		'<select class="form-control" id="isplate"> <option value="1">是</option><option value="0">否</option></select>' +
			  '</div></div>' +
			  '<div><div class="text-center text-danger">注:选择[是[,钱将直接到平台账上;选择[否],直接扣除</div><div>';
		'</form>';
		var index = layer.open({
			type: 1,
			shade: false,
			skin: 'layui-layer-rim', //加上边框
			area: ['500px', '280px'], //宽高
			closeBtn:0,
			title: merchNo + " " + " 已结算扣减", //不显示标题
			content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
			btn: ['确定', '关闭'],
			btn1: function(index, layero){
				
				var money = $("#money").val();
				if(money!="" || parseFloat(money)>0){
					var isplate = $("#isplate").val();
					var msg = isplate == 1?"划拨到平台账户吗":"扣减该商户的余额"
					if(confirm("确定"+msg+"？")){
						ajaxTransfer(merchNo,money,index,isplate);
					}
				}else{
					layer.alert("金额不能为空");
				}
				
			},
			btn2: function(index, layero){
				layer.close(index);
			}
		});
}
function ajaxTransfer(merchNo,money,index,isplate){
	$.ajax({
		type : "get",
		url : "/pay/transfer/" + merchNo,
		data : {money : money,isplate:isplate},
		async : true,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			layer.alert(data.msg);
			if (data.code == 0) {
				 layer.close(index);
				 reLoad();
			}
		}
	});
}


function freeze(merchNo){
	var content = '<form class="form-horizontal m-t">' +
			  '<div ><label class="col-sm-4 control-label text-left">请输入金额：</label>' + 
			  '<div class="col-sm-8">' +
			  		'<input id="money" name="money" class="form-control" type="number" maxlength="32" >' +
			  '</div></div>' +
			  '<br/><br/><div ><label class="col-sm-4 control-label text-left"></label>' + 
			  '<div class="col-sm-8">' +
			  		'<select class="form-control" id="freeze"> <option value="1">冻结</option><option value="0">解冻</option></select>' +
			  '</div></div>' +
			  '<div><div class="text-center text-danger">';
		'</form>';
		var index = layer.open({
			type: 1,
			shade: false,
			skin: 'layui-layer-rim', //加上边框
			area: ['500px', '280px'], //宽高
			closeBtn:0,
			title: merchNo + " " + " 冻结/解冻可用余额", //不显示标题
			content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
			btn: ['确定', '关闭'],
			btn1: function(index, layero){
				
				var money = $("#money").val();
				if(money!="" || parseFloat(money)>0){
					var freeze = $("#freeze").val();
					var msg = freeze == 1?"冻结":"解冻"
					if(confirm("确定要执行["+msg+"]操作吗？")){
						ajaxFreeze(merchNo,money,index,freeze);
					}
				}else{
					layer.alert("金额不能为空");
				}
				
			},
			btn2: function(index, layero){
				layer.close(index);
			}
		});
}
function ajaxFreeze(merchNo,money,index,freeze){
	$.ajax({
		type : "get",
		url : "/pay/freeze/" + merchNo,
		data : {money : money,freeze:freeze},
		async : true,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			layer.alert(data.msg);
			if (data.code == 0) {
				 layer.close(index);
				 reLoad();
			}
		}
	});
}

