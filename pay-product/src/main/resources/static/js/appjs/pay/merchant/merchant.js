
var prefix = "/pay/merchant"
$(function() {
	load();
});
var firstSwitch = true;
var channelSwitchs = new Object();
var handRates = new Object();
var feeRates = new Object();

function load() {
	
	var columns = [{
					field : 'merchNo', 
					align : 'center',
					title : '商户号' 
				},
												{
					field : 'merchantsName', 
					align : 'center',
					title : '商户名称' 
				},
												{
					field : 'merchantsShortName', 
					align : 'center',
					title : '商户简称' 
				},								{
					field : 'parentAgent', 
					align : 'center',
					title : '上级代理名称' 
				},								{
					field : 'payChannelType', 
					align : 'center',
					title : '通道分类' ,
					formatter:function(value, row, index){
						return payChannelType[value];
					}
				},
												{
					field : 'auditStatus', 
					title : '审核状态',
					align : 'center',
					formatter : function(value, row, index){
						
						if(value == 0 && s_batch_audit == '')
							return '<input name="switch" type="checkbox" '+s_batch_audit+' data-user-id="'+row.userId+'" data-size="small" data-on-text="正在审核" data-label-text="审核中" data-off-text="点击审核" data-on-color="info" data-off-color="info">';
						else
							return  "<span class='label label-"+auditStatusColor[value]+"'>"+auditStatus[value]+"</span>";
					} 
				},
				{
					field : 'supportPaid', 
					title : '代付状态' ,
					align : 'center',
					formatter : function(value, row, index){
						if(s_batch_paid == ''){
							var checked = value==1?"checked":"";
							return '<div class="switch">'+
				                        '<div class="onoffswitch">'+
				                            '<input type="checkbox" '+checked+' '+s_batch_paid+' class="onoffswitch-checkbox" id="paid'+index+'" onclick="batchPaid('+row.userId+',this)">'+
				                            '<label class="onoffswitch-label" for="paid'+index+'">'+
				                                '<span class="onoffswitch-inner"></span><span class="onoffswitch-switch"></span>'+
				                           ' </label>'+
				                       ' </div>'+
				                    '</div>';
						}else{
							var color = value==1?"primary":"danger";
							return "<span class='label label-"+color+"'>"+statuss[value]+"</span>";
						}
					} 
				},
												{
					field : 'supportWithdrawal', 
					title : '提现状态',
					align : 'center',
					formatter : function(value, row, index){
						if(s_batch_withdrawal == ''){
							var checked = value==1?"checked":"";
							return '<div class="switch">'+
				                        '<div class="onoffswitch">'+
				                            '<input type="checkbox" '+checked+' '+s_batch_withdrawal+' class="onoffswitch-checkbox" id="withdrawal'+index+'" onclick="batchWithdrawal('+row.userId+',this)">'+
				                            '<label class="onoffswitch-label" for="withdrawal'+index+'">'+
				                                '<span class="onoffswitch-inner"></span><span class="onoffswitch-switch"></span>'+
				                           ' </label>'+
				                       ' </div>'+
				                    '</div>';
						}else{
							var color = value==1?"primary":"danger";
							return "<span class='label label-"+color+"'>"+statuss[value]+"</span>";
						}
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
				                            '<input type="checkbox" '+checked+' '+s_batch_oerate+' class="onoffswitch-checkbox" id="example'+index+'" onclick="batchOperate('+row.userId+',this)">'+
				                            '<label class="onoffswitch-label" for="example'+index+'">'+
				                                '<span class="onoffswitch-inner"></span><span class="onoffswitch-switch"></span>'+
				                           ' </label>'+
				                       ' </div>'+
				                    '</div>';
						}else{
							var color = value==1?"primary":"danger";
							return "<span class='label label-"+color+"'>"+statuss[value]+"</span>";
						}
					}
				},
												{
					field : 'crtTime', 
					align : 'center',
					title : '创建时间' 
				}];
	if(s_send_email == ''){
		columns.push({
            field : 'sendEmail',
            title : '发送邮箱' ,
            align : 'center',
            formatter : function(value, row, index){
                var text= "发送";
                return '<a class="btn btn-success btn-sm" href="#" id="pk' + row.merchNo  + '" mce_href="#" onclick="sendEmail(\''
                    + row.merchNo+'\',\''+0
                    + '\')"><i class="fa fa-key"></i> '+text+'</a>';
            }
        });
	}
	if(s_edit_h == ''){
        
		columns.push({
				field : 'publicKey', 
				title : '密钥配置' ,
				align : 'center',
				formatter : function(value, row, index){
					var text= "配置";
					return '<a class="btn btn-success btn-sm" href="#" id="pk' + row.merchNo  + '" mce_href="#" onclick="lookPublicKey(\''
					+ row.merchNo+'\',\'' + row.publicKey
					+ '\')"><i class="fa fa-key"></i> '+text+'</a>';
				}
				});
		columns.push({
					field : 'feeRate', 
					title : '费率信息' ,
					align : 'center',
					formatter : function(value, row, index){
						feeRates[index] = value;
						return '<a class="btn btn-primary btn-sm" href="#" mce_href="#" onclick="lookFeeRate(\''
						+ row.merchNo+'\')"><i class="fa fa-eye"></i> 查看/修改</a>';
					}
					});
		columns.push({
					field : 'agentInfo', 
					title : '实名信息' ,
					align : 'center',
					formatter : function(value, row, index){
						return '<a class="btn btn-primary btn-sm" href="#"  mce_href="#" onclick="merchantInfo(\''
						+ row.merchNo+'\')"><i class="fa fa-eye"></i> 查看/修改</a>';
					}
		});
		columns.push({
					title : '基本信息',
					field : 'id',
					align : 'center',
					formatter : function(value, row, index) {
						var e = '<a class="btn btn-primary btn-sm '+s_edit_h+'" href="#" mce_href="#" onclick="edit(\''
								+ row.merchNo
								+ '\')"><i class="fa fa-edit"></i> 查看/修改</a> ';
						return e;
					}
		});
	}else{
		columns.push({
			title : '查看详情',
			field : 'id',
			align : 'center',
			formatter : function(value, row, index) {
				var e = '<a class="btn btn-primary" href="#" title="" onclick="infoQuery(\''
						+ row.merchNo
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
						+ row.merchNo
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
				merchNo:$('#merchNo').val(),
				auditStatus:$("#auditStatus").val(),
				status:$('#status').val(),
				merchantsName:$("#merchantsName").val(),
				payChannelType:$("#payChannelType").val()
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

function lookChannelSwitch(merchNo, index){
	var result = "未开启任何渠道";
	var channelSwitch = channelSwitchs[index];
	if(channelSwitch){
		result = "";
		for(var key in channelSwitch){
			result += outChannels[key] + " 开启<br>";
		}
	}
	layer.open({
		title:merchNo + "渠道开启详情",
		content: result //数组第二项即吸附元素选择器或者DOM
	});
}

function lookPublicKey(merchNo,publicKey){
    layer.open({
        type : 2,
        title : 'RSA秘钥配置',
        maxmin : true,
        shadeClose : false, // 点击遮罩关闭层
        area : [ '550px', '300px' ],
        content : prefix + '/RSAConfig/'+merchNo // iframe的url
    });

    // layer.open({
	 //  title:merchNo + " RSA公钥 设置",
    //   content: "<div class=''><input type='text' class='layui-layer-input' value='RSA公钥' placeholder='RSA私钥'><textarea class='layui-layer-input' style='width:230px;margin-top:5px;' placeholder='内容'></textarea></div>"
    //     ,yes: function(index, layero){
    //         //按钮【按钮一】的回调
    //         console.log($(layero).find("input[type='text']").val());
    //         console.log($(layero).find("textarea").val());
    //     }
    // });
    // layer.prompt({title: merchNo + " RSA公钥 设置", formType: 2,value:publicKey}, function(text, index){
		// $.ajax({
		// 	cache : true,
		// 	type : "POST",
		// 	url : "/pay/merchant/updatePKey",
		// 	data : "merchNo="+merchNo+"&publicKey="+text,
		// 	async : false,
		// 	error : function(request) {
		// 		parent.layer.alert("连接超时!");
		// 	},
		// 	success : function(data) {
		// 		if (data.code == 0) {
		// 			layer.msg("操作成功");
		// 			layer.close(index);
		// 			reLoad();
		// 		} else {
		// 			parent.layer.alert(data.msg)
		// 		}
		// 	}
		// });
    //
    // });
}
function add() {
	layer.open({
		type : 2,
		title : '增加',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/add' // iframe的url
	});
}

function merchantInfo(agentNum){
	layer.open({
		type : 2,
		title : '商户实名信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/merchantInfo/' + agentNum // iframe的url
	});
}

function lookFeeRate(merchNo){
	layer.open({
		type : 2,
		title : '商户费率信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '1050px', '650px' ],
		content : prefix + '/rateInfo/' + merchNo // iframe的url
	});
}
function edit(merchNo) {
	layer.open({
		type : 2,
		title : '商户基本信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/edit/' + merchNo // iframe的url
	});
}

function infoQuery(merchNo){
	layer.open({
		type : 2,
		title : '商户信息',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		content : prefix + '/infoQuery?merNo=' + merchNo // iframe的url
	});
}

function showMoneyDetail(merchNo) {
	var index = layer.open({
		type : 2,
		title : merchNo + ' 商户钱包详情',
		maxmin : true,
		shadeClose : false, // 点击遮罩关闭层
		area : [ '950px', '650px' ],
		maxmin: true,
		content :  '/moneyacct/merchant/detail/' + merchNo // iframe的url
	});
	layer.full(index);
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
		var merchNos = new Array();
		// 遍历所有选择的行数据，取每条数据对应的ID
		$.each(rows, function(i, row) {
			merchNos[i] = row['merchNo'];
		});
		$.ajax({
			type : 'POST',
			data : {
				"merchNos" : merchNos
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

//flag 1:启用 2:禁用
function batchOperate(userId,obj) {
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
		var merchantIds = new Array();
		merchantIds[0] = ""+userId;
//		// 遍历所有选择的行数据，取每条数据对应的ID
//		$.each(rows, function(i, row) {
//			merchantIds[i] = row['userId'];
//		});

		$.ajax({
			type : 'POST',
			data : {
				"merchantIds" : merchantIds,
				"flag":flag
			},
			url : prefix + '/batchOperate',
			success : function(r) {
				if (r.code == 0) {
					layer.msg(r.msg);
					reLoad();

				} else {
					$(obj).prop("checked",!state);
					layer.msg(r.msg);
				}
			}
		});


	}, function() {
		$(obj).prop("checked",!state);
	},function() {

    });
}

//flag 1:启用 2:禁用
function batchPaid(userId,obj) {
	var state = $(obj).is(":checked");
	/*var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要操作的数据");
		return;
	}*/
	var mess = state?'启用':'禁用';
	var flag = state?1:0;
	layer.confirm("确认要 " + mess + " 代付状态吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var merchantIds = new Array();
		merchantIds[0] = ""+userId;
//		// 遍历所有选择的行数据，取每条数据对应的ID
//		$.each(rows, function(i, row) {
//			merchantIds[i] = row['userId'];
//		});
		$.ajax({
			type : 'POST',
			data : {
				"merchantIds" : merchantIds,
				"flag":flag
			},
			url : prefix + '/batchPaid',
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
		$(obj).prop("checked",!state);
	});
}

//flag 1:启用 2:禁用
function batchWithdrawal(userId,obj) {
	var state = $(obj).is(":checked");
	/*var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
	if (rows.length == 0) {
		layer.msg("请选择要操作的数据");
		return;
	}*/
	var mess = state?'启用':'禁用';
	var flag = state?1:0;
	layer.confirm("确认要 " + mess + " 提现状态吗?", {
		btn : [ '确定', '取消' ]
	// 按钮
	}, function() {
		var merchantIds = new Array();
		merchantIds[0] = ""+userId;
//		// 遍历所有选择的行数据，取每条数据对应的ID
//		$.each(rows, function(i, row) {
//			merchantIds[i] = row['userId'];
//		});
		$.ajax({
			type : 'POST',
			data : {
				"merchantIds" : merchantIds,
				"flag":flag
			},
			url : prefix + '/batchWithdrawal',
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
		$(obj).prop("checked",!state);
	});
}

function batchAudit(userId,obj) {
	var merchantIds = new Array();
	merchantIds[0] = ""+userId;
	layer.confirm("请选择审核状态!", {
		title:'审核',
		closeBtn:0,
		btn : [ '通过', '不通过','关闭']
	, btn1:function() {
		$.ajax({
			type : 'POST',
			data : {
				"merchantIds" : merchantIds,
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
				"merchantIds" : merchantIds,
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
			var userId = $(this).data('user-id');
			console.log(state,userId);
			return batchAudit(userId,this);
//			event.preventDefault();
//			return console.log(state, event.isDefaultPrevented(),agentId);
	    }
	});
}

function sendEmail(merchNo,state) {
    $.ajax({
        type : 'POST',
        data : {
            "merchNo" : merchNo,
			"state":state
        },
        url : prefix + '/sendEmail',
        success : function(r) {
            if (r.code == 0) {
                layer.msg("发送成功");
                reLoad();
            } else {
                if("已发送邮箱"== r.msg){
                    layer.confirm("已经发送过该邮箱，请问是否继续发送？", {
                        title: '继续发送',
                        closeBtn: 0,
                        btn: ['是', '否']
                        , btn1: function () {
                            sendEmail(merchNo, 1);
                        }, btn2: function () {

                        }
                    });
				}else {
                    layer.msg(r.msg);
                }

            }
        }
    });
}