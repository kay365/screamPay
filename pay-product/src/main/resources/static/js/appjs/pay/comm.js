var orderDetails = new Array();
//订单详情
function orderDetail(row,index){
	orderDetails[index] = row;
	return '<a class="btn btn-primary btn-sm" href="#" title="查看详情"   mce_href="#" onclick="lookOrderDetail(\''
	+ index
	+ '\')"><i class="fa fa-eye"></i></a>';
}
//查看订单详情    
function lookOrderDetail(index){
	var order = orderDetails[index];
	var outChannel = order.outChannel;
	var orderType = order.orderType;
	var orderTypeDesc = "支付";
	if(orderType){
		orderTypeDesc = orderType[orderType];
	}
	var result = '<table class="table table-bordered">';
			result += '<tr><td width="118">请求时间：</td><td>' + order.reqTime +  '</td></tr>';
			result += '<tr><td>商户用户标识：</td><td>' + trimNull(order.userId) +  '</td></tr>';
			result += '<tr><td>订单标题：</td><td>' + trimNull(order.title) +  '</td></tr>';
			if(orderType ==0 || orderType ==2){
				result += '<tr><td>产品名称：</td><td>' + trimNull(order.product) +  '</td></tr>';
			}
			result += '<tr><td>银行卡号：</td><td>' + trimNull(order.bankNo) +  '</td></tr>';
			result += '<tr><td>银行卡类型：</td><td>' + trimNull(cardTypes[order.cardType]) +  '</td></tr>';
			result += '<tr><td>账户性质：</td><td>' + trimNull(acctTypes[order.acctType]) +  '</td></tr>';
			if(orderType ==1 || orderType ==3){
				result += '<tr><td>开户手机号：</td><td>' + trimNull(order.mobile) +  '</td></tr>';
				result += '<tr><td>开户行名称：</td><td>' + trimNull(order.bankName) +  '</td></tr>';
				result += '<tr><td>身份证号码：</td><td>' + trimNull(order.certNo) +  '</td></tr>';
			}
			result += '<tr><td>请求ip：</td><td>' + trimNull(order.reqIp) +  '</td></tr>';
			result += '<tr><td style="word-break:break-all;">备注信息：</td><td>' + trimNull(order.memo) +  '</td></tr>';
		result  +='</table>';
	layer.open({
		title:order.merchNo + " " + order.orderNo + " " + orderTypeDesc + "订单详情",
		area : [ '500px', '600px' ],
		content: result
	});
}

function trimNull(value){
	return value?value:"-";
}

//商户号搜索功能
function merchNoSearch(merchNo,merchNos){
	if(!merchNo){
		new SelectBox($('.merchNo'),merchNos,function(data){
			if(data){
				$("#merchNo").val(data.id);
			}else{
				$("#merchNo").val(null);
			}
		},{placeholder: '请选输入商户号',width:'100%',height:"32",textTransform:"uppercase",allowInput: true,textIndent: 12,border:"0"});
		$(".merchNo").css({"border":"none","margin-right":"5px","margin-top":"1px"});
		$(".merchNo input").focus(function(){
			var input = $(this);
			input.parent().css("border","1px solid #1ab394");
		});
		$(".merchNo input").blur(function(){
			$(this).parent().css("border","none");
		});
		$(".merchNo input").keyup(function(){
			$(this).val($(this).val().toUpperCase());
		});
		$(".merchNo i").click(function (){
			$(this).parent().css("border","1px solid #1ab394");
		});
	}
}

//一级代理商搜索功能
function agentSearch(merchNo,merchNos){
	//if(!merchNo){
		new SelectBox($('.agentN'),merchNos,function(data){
			if(data){
				$("#agentN").val(data.id);
			}else{
				$("#agentN").val(null);
			}
		},{placeholder: '请选输入商户号',width:'100%',height:"32",textTransform:"uppercase",allowInput: true,textIndent: 12,border:"1"});
		$(".agentN").css({"border":"1","margin-right":"0px","margin-top":"1px"});
		$(".agentN input").focus(function(){
			var input = $(this);
			input.parent().css("border","1px solid #1ab394");
		});
		$(".agentN input").blur(function(){
			$(this).parent().css("border","1");
		});
		$(".agentN input").keyup(function(){
			$(this).val($(this).val().toUpperCase());
		});
		$(".agentN i").click(function (){
			$(this).parent().css("border","1px solid #1ab394");
		});
	//}
}

function iGetInnerText(testStr) {
    var resultStr = testStr.replace(/\ +/g, ""); //去掉空格
    resultStr = testStr.replace(/[ ]/g, "");    //去掉空格
    resultStr = testStr.replace(/[\r\n]/g, ""); //去掉回车换行
    return resultStr;
}
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
						$selMerch.append("<option value=''>请选商户</option>");
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
			$selMerch.append("<option value=''>请选商户</option>");
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
						$selAgent.append("<option value=''>请选代理</option>");
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
		$selAgent.append("<option value=''>请选代理</option>");
		var $selMerch = $("#merchNo");
		$selMerch.find("option").remove();
		$selMerch.append("<option value=''>请选商户</option>");
	}
}

function loadMechNoByCompany(payCompany){
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
						var $selMerchNo = $("#payMerch");
						$selMerchNo.find("option").remove();
						$selMerchNo.append("<option value=''>请选支付商户号</option>");
						for ( var i in datas) {
							$selMerchNo.append("<option value='"+datas[i]+"'>"+datas[i]+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg);
				}
			}
		});
	}
}
