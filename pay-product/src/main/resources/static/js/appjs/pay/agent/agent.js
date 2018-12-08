
var prefix = "/pay/agent"
$(function() {
	
	//$(".form_datetime").val(formatDate(new Date()));
	load();
});
var firstSwitch = true;
var channelSwitchs = new Object();
//var handRates = new Object();
var feeRates = new Object();
function load() {
	
	var columns = [{
						field : 'agentNumber', 
						align : 'center',
						title : '代理商户号 ' 
					},
					{
						field : 'merchantsName', 
						align : 'center',
						title : '代理商名称' 
					},
					/*{
						field : 'contacts', 
						align : 'center',
						title : '联系人' 
					},
					{
						field : 'contactsPhone', 
						align : 'center',
						title : '联系人电话' 
					},*/
					{
						field : 'agentType', 
						align : 'center',
						title : '代理商类型',
						formatter : function(value, row, index){
							
							return  merTypes[value]; 
						}
					},
													{
						field : 'parentAgent', 
						align : 'center',
						title : '上级代理名称 ' 
					},
													{
						field : 'createTime', 
						align : 'center',
						title : '创建时间' 
					},
													{
						field : 'auditStatus', 
						title : '审核状态 ',
						align : 'center',
						formatter : function(value, row, index){
							if(value == 0 && s_batch_audit == '')
								return '<input name="switch" type="checkbox" '+s_batch_audit+' data-agent-id="'+row.agentId+'" data-size="small" data-on-text="正在审核" data-label-text="审核中" data-off-text="点击审核" data-on-color="info" data-off-color="info">';
							else
								return  "<span class='label label-"+auditStatusColor[value]+"'>"+auditStatus[value]+"</span>";
						}
					},
													{
						field : 'status', 
						title : '状态 ',
						align : 'center',
						formatter : function(value, row, index){
							if(s_batch_oerate == ''){
								var checked = value==1?"checked":"";
								return '<div class="switch">'+
					                        '<div class="onoffswitch">'+
					                            '<input type="checkbox" '+checked+' '+s_batch_oerate+' class="onoffswitch-checkbox" id="example'+index+'" onclick="batchOperate('+row.agentId+',this)">'+
					                            '<label class="onoffswitch-label" for="example'+index+'">'+
					                                '<span class="onoffswitch-inner"></span><span class="onoffswitch-switch"></span>'+
					                           ' </label>'+
					                       ' </div>'+
					                    '</div>';
							}else{
								var color = value==1?"primary":"danger";
								return "<span class='label label-"+color+"'>"+statuss[value]+"</span>";
							}
				//			return  "<input name='switch' type='checkbox' "+checked+" "+s_batch_oerate+" data-agent-id='"+row.agentId+"' data-size='mini' data-on-text='开启' data-off-text='禁用' data-on-color='success' data-off-color='danger'>";
						}
					}];
	
	if(s_edit_h == ''){
		
		columns.push({
						field : 'feeRate', 
						title : '服务费率' ,
						align : 'center',
						formatter : function(value, row, index){
							feeRates[index] = value;
							return '<a class="btn btn-primary btn-sm" href="#" mce_href="#" onclick="lookFeeRate(\''
							+ row.agentId+'\')"><i class="fa fa-edit"></i> 查看/修改</a>';
						}
					});
		columns.push({
					field : 'agentInfo', 
					title : '实名信息' ,
					align : 'center',
					formatter : function(value, row, index){
						return '<a class="btn btn-primary btn-sm" href="#" mce_href="#" onclick="agentInfo(\''
						+ row.agentId+'\')"><i class="fa fa-edit"></i> 查看/修改</a>';
					}
		});
		columns.push({
					title : '基本信息',
					field : 'id',
					align : 'center',
					formatter : function(value, row, index) {
						var e = '<a class="btn btn-primary" href="#" title="" onclick="edit(\''
								+ row.agentId
								+ '\')"><i class="fa fa-edit"></i> 查看/修改</a> ';
						return e;
					}
		});
	}else{
		columns.push({
			title : '查看',
			field : 'id',
			align : 'center',
			formatter : function(value, row, index) {
				var e = '<a class="btn btn-primary" href="#" title="" onclick="infoQuery(\''
						+ row.agentNumber
						+ '\')"><i class="fa fa-eye"></i> 详情</a> ';
				return e;
			}
		});
	}
	
	if(s_query_money == ""){
		columns.push({
			title : '查看钱包',
			field : 'id',
			align : 'center',
			formatter : function(value, row, index) {
				var e = '<a class="btn btn-primary" href="#" title="" onclick="showMoneyDetail(\''
						+ row.agentNumber
						+ '\')"><i class="fa fa-eye"></i> 钱包</a> ';
				return e;
			}
		});
	}
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
		//search : true, // 是否显示搜索框
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
				agentNum:$('#agentNum').val(),
				merchantsName:$('#merchantsName').val(),
				status:$('#status').val(),
				auditStatus:$('#auditStatus').val(),
			};
		},
		onLoadSuccess:function(){
			createSwitch();
			firstSwitch = false;
		},
		onToggle:function(){
			if(!firstSwitch)
				createSwitch();
		},
		columns : columns
	});
}
function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}

function agentInfo(agentNum){
	layer.open({
		type : 2,
		title : '代理实名信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/agentInfo/' + agentNum // iframe的url
	});
}

function lookFeeRate(merchNo){
	layer.open({
		type : 2,
		title : '代理费率信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/rateInfo/' + merchNo // iframe的url
	});
}

function infoQuery(merchNo){
	layer.open({
		type : 2,
		title : '代理信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/infoQuery?agentNo=' + merchNo // iframe的url
	});
}

function add() {
	var layero = layer.open({
		type : 2,
		title : '增加',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '1050px', '650px' ],
		content : prefix + '/add' // iframe的url
	});
	layer.full(layero);
}
function edit(agentNum) {
	layer.open({
		type : 2,
		title : '代理基本信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/edit/' + agentNum // iframe的url
	});
}
function remove(merchNo) {
	layer.confirm('确定要删除选中的记录？', {
		btn : [ '确定', '取消' ]
	}, function() {
		$.ajax({
			url : prefix+"/remove",
			type : "post",
			data : {
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

//flag 1:启用 0:禁用
function batchOperate(agentId,obj) {
	var state = $(obj).is(":checked");
	/*var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要操作的数据");
		return;
	}*/
	var mess = state?'启用':'禁用';
	var flag = state?1:0;
	layer.confirm("确认要 " + mess + " 吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var agentIds = new Array();
		agentIds[0] = ""+agentId;
		/*// 遍历所有选择的行数据，取每条数据对应的ID
		$.each(rows, function(i, row) {
			agentIds[i] = row['agentId'];
		});*/
		$.ajax({
			type : 'POST',
			data : {
				"agentIds" : agentIds,
				"flag":flag
			},
			url : prefix + '/batchOperate',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();
				} else {
					layer.msg(r.msg);
					$(obj).prop("checked",!state);
				}
			}
		});
	}, function() {
		$(obj).prop("checked",!state);
//		$(obj).bootstrapSwitch("state",!state,true);
	});
}

function batchAudit(agentId,obj) {
	
	var agentIds = new Array();
	agentIds[0] = ""+agentId;
	layer.confirm("请选择审核状态!", {
		title:'资料审核',
		closeBtn:0,
		btn : [ '通过', '不通过','关闭']
	// 按钮
	, btn1:function() {
		$.ajax({
			type : 'POST',
			data : {
				"agentIds" : agentIds,
				"flag":true
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
	}, btn2:function() {
		$.ajax({
			type : 'POST',
			data : {
				"agentIds" : agentIds,
				"flag":false
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
	},btn3:function(){$(obj).bootstrapSwitch("state",false,true);}});
}

function createSwitch(){
	$(':checkbox[name=switch]').not('[data-switch-no-init]').bootstrapSwitch({
		onSwitchChange: function (event, state) {
			var agentId = $(this).data('agent-id');
			console.log(state,agentId);
			return batchAudit(agentId,this);
//			event.preventDefault();
//			return console.log(state, event.isDefaultPrevented(),agentId);
	    }
	});
}

function showMoneyDetail(agentNo) {
	var index = layer.open({
		type : 2,
		title : agentNo + ' 代理商钱包详情',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '800px', '550px' ],
		maxmin: true,
		content : '/moneyacct/agent/detail/' + agentNo // iframe的url
	});
	layer.full(index);
}
