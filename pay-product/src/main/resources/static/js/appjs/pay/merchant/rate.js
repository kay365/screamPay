function loadFun() {
	
	validateRule();
	
	$(":checkbox[name=paymentMethod]:checked").each(function(){
		var paymentMethod = $(this).val();
		var rate = rates[paymentMethod];
		$.each(rate,function(key,value){
			var rate = value["rate"];
			var unit = value["unit"];
			$("#paymentMethod_"+key+"_"+paymentMethod).val(rate);
			$("#paymentMethod_"+key+"_ru_"+paymentMethod).val(unit);
		})
	});
	
}


$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	
	if(!getRate()){
		return;
	}
	
	var outChannels = "{";
	$("[name=outChannelOn]:checked").each(function(i){
		//outChannels[$(this).val()] = 1;
		if(i==0){
			outChannels += "'"+$(this).val()+"':1"
		}else{
			outChannels += ",'"+$(this).val()+"':1"
		}
	});
	outChannels += "}";
	
	//费率设置
	$("#tOneStr").val("{}");
//	$("#dZeroStr").val(getRate_d0());
	$("#paidStr").val(getRate_paid());
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/merchant/updateRate",
		data : $('#merchantForm').serialize()+"&outChannelStr="+outChannels,
		async : false,
		error : function(request) {
			parent.layer.alert("连接超时!");
		},
		success : function(data) {
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}
		}
	});

}
function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#merchantForm").validate({
		rules : {
			name : {
				required : true
			},
			merchNo : {
				remote : {
					url : "/pay/merchant/exist", // 后台处理程序
					type : "post", // 数据发送方式
					dataType : "json", // 接受数据格式
					data : { // 要传递的数据
						merchNo : function() {
							return $("#merchNo").val();
						}
					}
				}
			}
		},
		messages : {
			name : {
				required : icon + "请输入商户名称"
			},
			merchNo : {
				remote : icon + "商户号已经存在"
			}
		}
	});
}

function getRate(){
	
	var pmLength = $("[name=paymentMethod]:checked").length;
	if(pmLength <1){
		parent.layer.alert("请选择结算方式!");
		return false;
	}
	if(pmLength >1){
		parent.layer.alert("结算方式只能选一种!");
		return false;
	}
	
	var ocLength = $("[name=outChannelOn]:checked").length;
	if(ocLength <= 0){
		parent.layer.alert("请选择支付方式!");
		return false;
	}
	
	var jsonObj = {};  
	var isSelectPM = false;
	var isRateNull = false
	$(":checkbox[name=paymentMethod]:checked").each(function(){
		var paymentMethod = $(this).val();
		var ocJson = {};
		$("[name=paymentMethod_"+paymentMethod+"]").each(function(){
			if(!$(this).prop("readonly")){
				var outChannel = $(this).attr("ocv");
				var rate = $(this).val();
				var unit = $(this).next().val();
				if(rate==""){
					layer.msg($(this).attr("ock")+" 费率不能为空");
					isRateNull = true;
				}else if(rate <= 0){
					layer.msg($(this).attr("ock")+" 费率不能小于等于0");
					isRateNull = true;
				}else{
					var ruJson = {"rate":rate,"unit":unit};
					ocJson[outChannel] = ruJson;
				}
			}
		});
		if(!isRateNull){
			jsonObj[paymentMethod] = ocJson;
			isSelectPM = true;
		}else{
			jsonObj = {};
			isSelectPM = false;
		}
	});
	console.log(JSON.stringify(jsonObj ));
	$("#dZeroStr").val(JSON.stringify(jsonObj ));
	return isSelectPM;
}

function getRate_paid(){
	var paidd = $("#acp").val();
	var paidd_u = $("#acp_ru").val();
	return '{"rate":'+paidd+',"unit":'+paidd_u+'}';
}
var form;
layui.use(['form'], function () {
	form = layui.form;
    form.render(null, 'paymentMethodDiv');
    form.render(null, 'outChannelOnDiv');
	
	form.on('checkbox(paymentMethod)', function(data){
		pmClick(this);
	});
	form.on('checkbox(outChannelOn)', function(data){
		ocClick(this);
	});
	
	getPAgentRate();
});

function pmClick(obj){
	if($(obj).is(":checked")){
		if($("[name=paymentMethod]:checked").length !=1){
			parent.layer.alert("结算方式只能选一种");
		}
//		$("[name=paymentMethod_"+$(obj).val()+"]").removeAttr("readonly");
		$("[name=outChannelOn]:checked").each(function(){
			$("#paymentMethod_"+$(this).val()+"_"+$(obj).val()).removeAttr("readonly");
		});
		$("#acp").removeAttr("readonly");
	}else{
//		$("[name=paymentMethod_"+$(obj).val()+"]").prop("readonly",true);
		$("[name=outChannelOn]:checked").each(function(){
			$("#paymentMethod_"+$(this).val()+"_"+$(obj).val()).prop("readonly",true);
		});
		if($("[name=paymentMethod]:checked").length <= 0)
			$("#acp").prop("readonly",true);
	}
}
function ocClick(obj){
	if($(obj).is(":checked")){
		$("[name=paymentMethod]:checked").each(function(){
			$("#paymentMethod_"+$(obj).val()+"_"+$(this).val()).removeAttr("readonly");
		});
	}else{
		if($("[name=outChannelOn]:checked").length <= 0){
			parent.layer.alert("必须开启一种支付方式");
			$(obj).prop("checked",true);
			form.render(null, 'outChannelOnDiv');
			return;
		}
		$("[name=paymentMethod]:checked").each(function(){
			$("#paymentMethod_"+$(obj).val()+"_"+$(this).val()).prop("readonly",true);
		});
	}
}

function getPAgentRate(){
		//支付控制
		$(":checkbox[name=paymentMethod]").each(function(){
			var paymentMethod = $(this).val();
			var rate = parentRates[paymentMethod];
			if(rate!=null){
				$(this).removeAttr("disabled");
				form.render(null, 'paymentMethodDiv');
				$.each(rate,function(key,value){
					var rate = value["rate"];
					var unit = value["unit"];
					$("#paymentMethod_"+key+"_"+paymentMethod).prop("min",rate);
					$("#paymentMethod_"+key+"_ru_"+paymentMethod).val(unit);
					$("#paymentMethod_"+key+"_ru_"+paymentMethod).find("option[value!="+unit+"]").remove();
				})
			}else{
				$(this).prop("disabled","disabled");
				form.render(null, 'paymentMethodDiv');
			}
		});
		
		//代付 控制
		var rate = parentPaid.rate;
		var unit = parentPaid.unit;
		$("#acp").prop("min",rate);
		$("#acp_ru").val(unit);
		$("#acp_ru").find("option[value!="+unit+"]").remove();
}
