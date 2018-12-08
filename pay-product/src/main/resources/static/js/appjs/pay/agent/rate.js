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
	
	//费率设置
	$("#tOneStr").val("{}");
	$("#paidStr").val(getRate_paid());
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/agent/updateRate",
		data : $('.wizard-big').serialize(),
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
	$("#agentForm").validate({
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
	
	var jsonObj = {};  
	var isSelectPM = false;
	var isRateNull = false
	$(":checkbox[name=paymentMethod]:checked").each(function(){
		var paymentMethod = $(this).val();
		var ocJson = {};
		$("[name=paymentMethod_"+paymentMethod+"]").each(function(){
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
	return '{"rate":"'+paidd+'","unit":"'+paidd_u+'"}';
}
var form;
layui.use(['form'], function () {
	form = layui.form;
    var upload = layui.upload;
    form.render(null, 'paymentMethodDiv');
	
	form.on('checkbox', function(data){
		pmClick(this);
	});
	
	getPAgentRate();
});
function pmClick(obj){
	if($(obj).is(":checked")){
		$("[name=paymentMethod_"+$(obj).val()+"]").removeAttr("readonly");
		$("#acp").removeAttr("readonly");
	}else{
		$("[name=paymentMethod_"+$(obj).val()+"]").prop("readonly",true);
		if($("[name=paymentMethod]:checked").length <= 0)
			$("#acp").prop("readonly",true);
	}
}


function getPAgentRate(){
	if(parentRates){
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
				$(this).prop("disabled","disabled").prop("checked",false);
				form.render(null, 'paymentMethodDiv');
			}
			pmClick(this);
		});
		
		//代付 控制
		var rate = parentPaid.rate;
		var unit = parentPaid.unit;
		$("#acp").prop("min",rate);
		$("#acp_ru").val(unit);
		$("#acp_ru").find("option[value!="+unit+"]").remove();
	}
}