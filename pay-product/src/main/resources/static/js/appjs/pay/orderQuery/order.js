var prefix = "/orderQuery/order"
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
    reLoad(load);
    
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
		// //设置为limit则会发送符合RESTFull格式的参数
		singleSelect : false, // 设置为true将禁止多选
		// //发送到服务器的数据编码类型
		pageSize : 10, // 如果设置了分页，每页数据条数
		pageNumber : 1, // 如果设置了分布，首页页码
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		showFooter:true,
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
	            merchNo:$('#merchNo').val(),
	            orderNo:$('#orderNo').val(),
	            beginDate:$("#beginDate").val(),
	            endDate:$("#endDate").val()
			};
		},
		columns : [{
			field : 'merchNo', 
			title : '平台商户',
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
		},
										{
			field : 'payCompany', 
			title : '支付公司',
			visible: merchNo == null,
			formatter:function(value){
				return payCompanys[value];
			}  
		},
										{
			field : 'payMerch', 
			title : '支付商户号',
			visible: merchNo == null,
		},
										{
			field : 'outChannel', 
			title : '渠道',
			formatter:function(value){
				return outChannels[value];
			} 
		},
		/*								{
			field : 'title', 
			title : '标题' 
		},
										{
			field : 'product', 
			title : '产品名称' 
		},*/
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
			visible: merchNo == null,
			title : '成本金额',
            footerFormatter:sumFooterFormatter
		},
		{
			field : 'qhAmount',
			title : '平台手续费',
            footerFormatter:sumFooterFormatter
		},
		{
			field : 'agentAmount',
			visible: merchNo == null,
			title : '商户代理金额',
            footerFormatter:sumFooterFormatter
		},
		{
			field : 'orderState', 
			title : '订单状态',
			formatter: function(value){
				return orderStates[value];
			}
		},{
			field : 'detail',
			title : '订单详情',
			formatter:function(value,row,index){
				return orderDetail(row,index);
			}
		},{
			title : '操作',
			field : 'id',
			visible:syncOrderFlag,
			align : 'center',
			formatter : function(value, row, index) {
				return  '<a class="btn btn-primary " href="#" mce_href="#" title="手动同步支付订单" onclick="syncOrder(\''
						+ row.outChannel + '\',\'' + row.merchNo + '\',\'' + row.orderNo
						+ '\')"><i class="fa fa-refresh"></i></a> ';
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

function syncOrder(outChannel,merchNo,orderNo){
	console.log(jfOutChannels[outChannel]);
	if(jfOutChannels[outChannel]){
		var content = '<form class="form-horizontal m-t">' +
						  '<label class="col-sm-4 control-label text-left">请输入业务单号：</label>' + 
						  '<div class="col-sm-8">' +
						  		'<input id="syncBusinessNo" name="syncBusinessNo" class="form-control" type="text" maxlength="32" placeholder="请输入业务单号">' +
						  '</div>' +
						  '<br><div class="text-center text-danger">注* 该订单将直接同步成功！</div>';
					  '</form>';
		var index = layer.open({
			  type: 1,
			  shade: false,
			  skin: 'layui-layer-rim', //加上边框
			  area: ['500px', '250px'], //宽高
			  title: merchNo + " " + orderNo + " " + jfOutChannels[outChannel] + " 订单手动同步", //不显示标题
			  content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
			  btn: ['确定', '关闭'],
	            btn1: function(index, layero){
	            	var syncBusinessNo = $("#syncBusinessNo").val();
	            	ajaxSyncOrder(outChannel,merchNo,orderNo,syncBusinessNo,index);
	            },
	            btn2: function(index, layero){
	            	layer.close(index);
	            }
			});
	}else{
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
	            	ajaxSyncOrder(outChannel,merchNo,orderNo,notify_url,index);
	            },
	            btn2: function(index, layero){
	            	layer.close(index);
	            }
			});
		
	}
}

function ajaxSyncOrder(outChannel,merchNo,orderNo,businessNo,layerIndex){
	$.ajax({
		type : "get",
		url : "/pay/syncOrder/" + outChannel + "/" + merchNo + "/" + orderNo,
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

var footerData;

function reLoad(cb) {
    $.ajax({
        type : "POST",
        url : prefix + "/list/footer",
        data:{
        	merchNo:$('#merchNo').val(),
            orderNo:$('#orderNo').val(),
            beginDate:$("#beginDate").val(),
            endDate:$("#endDate").val()
        },
        success : function(r) {
            if (r.code == 0) {
                footerData = r.data;
                if(cb){
                	cb();
				}else{
                    $('#exampleTable').bootstrapTable('refresh');
				}
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
