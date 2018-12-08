var prefix = "/orderQuery/orderHis"
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
		singleSelect : false, // 设置为true将禁止多选
		pageSize : 10, // 如果设置了分页，每页数据条数
		pageNumber : 1, // 如果设置了分布，首页页码
		//search : true, // 是否显示搜索框
		showColumns : false, // 是否显示内容下拉框（选择显示的列）
		sidePagination : "server", // 设置在哪里进行分页，可选值为"client" 或者 "server"
		showFooter:true,
		queryParams : function(params) {
			return {
				//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
				limit: params.limit,
				offset:params.offset,
	            orderNo:$('#orderNo').val(),
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
		columns : [
			{
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
				visible: merchNo == null,
				title : '支付公司',
				formatter:function(value){
					return payCompanys[value];
				}
			},
											{
				field : 'payMerch', 
				visible: merchNo == null,
				title : '支付商户号' 
			},
											{
				field : 'outChannel', 
				title : '渠道',
				formatter:function(value){
					return outChannels[value];
				}
			},
			/*{
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
                title : '成本金额' ,
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
                title : '商户代理金额' ,
                footerFormatter:sumFooterFormatter
            },
			{
				field : 'orderState', 
				title : '订单状态',
				formatter:function(value){
					return orderStates[value];
				}
			},
            {
                field : 'clearState',
                title : '清算状态',
                formatter:function(value){
                    return clearStates[value];
                }
            },{
				field : 'detail',
				title : '订单详情',
				formatter:function(value,row,index){
					return orderDetail(row,index);
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

function reLoad(cb) {
    $.ajax({
        type : "POST",
        url : prefix + "/list/footer",
        data:{
        	orderNo:$('#orderNo').val(),
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

