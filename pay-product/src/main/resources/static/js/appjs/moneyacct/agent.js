var prefix = "/moneyacct/agent";
$(function() {
	load();
	$("#oneAgent").change(function(){
		var oneAgent = $(this).val();
		findAgentByParent(oneAgent);
	});	
	$("#twoAgent").change(function(){
		var twoAgent = $(this).val();
	});	
});

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
				agent:$('#oneAgent').val(),
				subAgent:$('#twoAgent').val()
			};
		},
		columns : [
				{
					field : 'agentNo',
					title : '平台代理商',
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
					title : '账户冻结-交易中(元)',
					formatter:function(value, row, index) {
                        return 0;
                    }
				},
				{
					field : 'detail',
					title : '详情',
                    formatter:function(value, row, index) {
						var ret = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="详情" onclick="showDetail(\''
                            + row.agentNo
                            + '\')"><i class="fa fa-bars"></i></a> ';
                        return ret;
                    }
				}]
	});
}

function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}
function showDetail(agentNo) {
	var index = layer.open({
		type : 2,
		title : agentNo + ' 代理商钱包详情',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '550px' ],
		maxmin: true,
		content : prefix + '/detail/' + agentNo // iframe的url
	});
	layer.full(index);
}
