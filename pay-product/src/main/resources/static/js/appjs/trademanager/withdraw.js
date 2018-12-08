var prefix = "/trademanager/withdraw"
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
//    reLoad(load);
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
	$("#payCompany").change(function(){
		var payCompany = $(this).val();
		loadMechNoByCompany(payCompany);
	});
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
//		showFooter:true,
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
				orderNo:$('#orderNo').val(),
	            businessNo:$('#businessNo').val(),
	            agent:$('#oneAgent').val(),
				subAgent:$('#twoAgent').val(),
	            merchNo:$('#merchNo').val(),
	            payCompany:$('#payCompany').val(),
	            orderState:$('#orderState').val(),
	            clearState:$('#clearState').val(),
	            outChannel:$('#outChannel').val(),
	            payMerch:$('#payMerch').val(),
	            beginDate:$('#beginDate').val(),
	            endDate:$('#endDate').val()
			};
		},
		onLoadSuccess:function(){
			stati();
		},
		columns : [
			{
				field : 'merchNo', 
				title : '平台用户',
				formatter:function(value,row,index){
					return row.merchName+"("+value+")";
				},
				footerFormatter:function(){
					return "总计";
				}
			},
			{
				field : 'orderNo', 
				title : '订单号' 
			},{
				field : 'userType', 
				visible: user != null,
				title : '用户类型',
				formatter:function(value){
					return userTypes[value];
				}
			},
			{
				field : 'payCompany',
				visible: user != null,
				title : '支付公司',
				formatter:function(value){
					return payCompanys[value];
				}
			},
			{
				field : 'payMerch', 
				visible: user != null,
				title : '支付商户号',
			},
											{
				field : 'outChannel', 
				title : '渠道',
				formatter:function(value){
					return outChannels[value];
				}
			},
			{
				field : 'businessNo', 
				visible: user != null,
				title : '第三方支付订单号' 
			},
			{
				field : 'amount', 
				title : '订单金额(元)',
                footerFormatter:sumFooterFormatter
			},
            {
                field : 'realAmount',
                title : '实际支付金额',
                footerFormatter:sumFooterFormatter
            },
            {
                field : 'costAmount',
                visible: userType == 0,
                title : '成本金额' ,
                footerFormatter:sumFooterFormatter
            },
            {
                field : 'qhAmount',
                title : '手续费',
                footerFormatter:sumFooterFormatter
            },
            /*{
                field : 'agentAmount',
                visible: userType == 0 || userType == 3,
                title : '代理分润(一级)' ,
                footerFormatter:sumFooterFormatter
            },
            {
                field : 'subAgentAmount',
                visible: userType == 0 || userType == 3 || userType == 6,
                title : '代理分润(二级)' ,
                footerFormatter:sumFooterFormatter
            },*/
			{
				field : 'orderState', 
				title : '订单状态',
				formatter:function(value){
					return orderStates[value];
				}
			},
            /*{
                field : 'clearState',
                title : '清算状态',
                formatter:function(value){
                    return clearStates[value];
                }
            },
            {
                field : 'noticeState',
                title : '通知状态',
                formatter:function(value){
                    return noticeStates[value];
                }
            },*/
            {
				field : 'detail',
				title : '订单详情',
				formatter:function(value,row,index){
					return orderDetail(row,index);
				}
			},{
				field : 'operate',
				title : '操作',
				visible:(syncOrderFlag || noticeOrderFlag),
				formatter:function(value,row,index){
					var html = "";
					if((row.orderState == 0 || row.orderState == 3) && syncOrderFlag){
						html += ' <a class="btn btn-primary " href="#" mce_href="#" title="手动同步支付订单" onclick="syncOrderAcp(\''
							+ row.outChannel + '\',\'' + row.merchNo + '\',\'' + row.orderNo
							+ '\')">同步</a> ';
					}else if(row.noticeState == 0 && noticeOrderFlag){
						/*html += ' <a class="btn btn-success " href="#" mce_href="#" title="通知" onclick="noticeOrderAcp(\''
							+ row.outChannel + '\',\'' + row.merchNo + '\',\'' + row.orderNo
							+ '\')">通知</a> ';*/
					}
					return html;
				}
			},{
				field : 'crtDate', 
				title : '创建时间',
				formatter:function(value, row, index){
					return intTime2Str(value); 
				}
			},
			{
				field : 'msg', 
				title : '消息提示' 
			}]
	});
}
var footerData;

function reLoad() {
    /*$.ajax({
        type : "POST",
        url : prefix + "/list/footer",
        data:{
        	orderNo:$('#orderNo').val(),
            businessNo:$('#businessNo').val(),
            agent:$('#oneAgent').val(),
			subAgent:$('#twoAgent').val(),
            merchNo:$('#merchNo').val(),
            payCompany:$('#payCompany').val(),
            orderState:$('#orderState').val(),
            clearState:$('#clearState').val(),
            outChannel:$('#outChannel').val(),
            payMerch:$('#payMerch').val(),
            beginDate:$('#beginDate').val(),
            endDate:$('#endDate').val()
		},
        success : function(r) {
            if (r.code == 0) {
                footerData = r.data;
                if(cb){
                    cb();
                }else{*/
                    $('#exampleTable').bootstrapTable('refresh');
            /*    }
            } else {
                layer.msg(r.msg);
            }
        }
    })*/

}

function stati(){
	$.ajax({
        type : "POST",
        url : prefix + "/list/stati",
        data:{
           /* agent:$('#oneAgent').val(),
			subAgent:$('#twoAgent').val(),*/
            merchNo:$('#merchNo').val(),
            payCompany:$('#payCompany').val(),
            payMerch:$('#payMerch').val(),
//            clearState:$('#clearState').val(),
//            outChannel:$('#outChannel').val(),
            beginDate:$('#beginDate').val(),
            endDate:$('#endDate').val()
		},
        success : function(r) {
            if (r.code == 0) {
                var data = r.data;
                $("#orderSum").text(" "+data.orderSum+" ");
                $("#succSum").text(" "+(data.succSum==null?"0":data.succSum)+" ");
                $("#errSum").text(" "+(data.errSum==null?"0":data.errSum)+" ");
                $("#ingSum").text(" "+(data.ingSum==null?"0":data.ingSum)+" ");
                $("#noticeErrSum").text(" "+(data.noticeErrSum==null?"0":data.noticeErrSum)+" ");
                $("#amount").text(" "+(data.amount==null?"0":data.amount)+" ");
                $("#qhAmount").text(" "+(data.qhAmount==null?"0":data.qhAmount)+" ");
                $("#amountIng").text(" "+(data.amountIng==null?"0":data.amountIng)+" ");
                $("#qhAmountIng").text(" "+(data.qhAmountIng==null?"0":data.qhAmountIng)+" ");
                $("#realAmount").text(" "+parseFloat(data.amount+data.qhAmount+data.amountIng+data.qhAmountIng).toFixed(2)+" ");
                var where = "";
                var merchNo = $('#merchNo').val();
                if(merchNo != "")where += "商户号：" + merchNo + "；";
                var payCompany = $('#payCompany').val();
                var payMerch = $('#payMerch').val();
                if(payCompany && payCompany != ""){
                	payCompany = $('#payCompany').find("option:selected").text();
                	where += "支付公司：" + payCompany + "["+payMerch+"]；"
                };
                /*var outChannel = $('#outChannel').val();
                if(outChannel != ""){
                	outChannel = $('#outChannel').find("option:selected").text();
                	where += "支付渠道：" + outChannel + "；"
                };*/
                var beginDate = $('#beginDate').val();
                var endDate = $('#endDate').val();
                where += "时间：" + beginDate + " ~ "+endDate+"";
                $("#statiWhere").text(where);
            } else {
                layer.msg(r.msg);
            }
        }
    })
}

function sumFooterFormatter(data){
	var field = this.field;
	if(footerData && footerData[field]){
		return ''+footerData[field];
	}
	return '0';
}

function syncOrderAcp(outChannel,merchNo,orderNo){
	var content = '<form class="form-horizontal m-t">' +
	  '<div class="col-sm-12">' +
	  		'<input id="notify_url" name="notify_url" class="form-control" type="text" maxlength="1000" placeholder="请输入回调地址">' +
	  '</div>' +
	  '<br><div class="text-center text-danger">注* 请确认回调地址是否正确！为空会选择商户请求的地址！</div>';
	'</form>';
	var index = layer.open({
		  type: 1,
		  shade: false,
		  skin: 'layui-layer-rim', //加上边框
		  area: ['500px', '250px'], //宽高
		  title: merchNo + " " + orderNo + " "  + " 订单手动同步", //不显示标题
		  content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
		  btn: ['确定','取消'],
          btn1: function(index, layero){
          	var notify_url = $("#notify_url").val();
          	ajaxSyncOrderAcp(outChannel,merchNo,orderNo,notify_url,index);
          },
          btn2: function(index, layero){
          	layer.close(index);
          }
		});
	
}

function ajaxSyncOrderAcp(outChannel,merchNo,orderNo,businessNo,layerIndex){
	$.ajax({
		type : "get",
		url : "/pay/syncOrderAcp/" + outChannel + "/" + merchNo + "/" + orderNo,
		async : true,
		data : {businessNo : businessNo},
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			layer.alert(data.msg);
			if (data.code == 0) {
				layer.close(layerIndex);
				 reLoad();
			}
		}
	});
}


function noticeOrderAcp(outChannel,merchNo,orderNo){
	var content = '<form class="form-horizontal m-t">' +
	  '<div class="col-sm-12">' +
	  		'<input id="notify_url" name="notify_url" class="form-control" type="text" maxlength="1000" placeholder="请输入回调地址">' +
	  '</div>' +
	  '<br><div class="text-center text-danger">注* 请确认回调地址是否正确！为空会选择商户请求的地址！</div>';
	'</form>';
	var index = layer.open({
		  type: 1,
		  shade: false,
		  skin: 'layui-layer-rim', //加上边框
		  area: ['500px', '250px'], //宽高
		  title: merchNo + " " + orderNo + " "  + " 通知", //不显示标题
		  content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
		  btn: ['确定','取消'],
            btn1: function(index, layero){
            	var notify_url = $("#notify_url").val();
            	ajaxNoiceOrder(outChannel,merchNo,orderNo,notify_url,index);
            },
            btn2: function(index, layero){
            	layer.close(index);
            }
		});
	
}

function ajaxNoiceOrder(outChannel,merchNo,orderNo,businessNo,layerIndex){
	$.ajax({
		type : "get",
		url : "/pay/noticeOrderAcp/" + outChannel + "/" + merchNo + "/" + orderNo,
		data : {businessNo : businessNo},
		async : true,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			layer.alert(data.msg);
			if (data.code == 0) {
				 layer.close(layerIndex);
				 reLoad();
			}
		}
	});
}

