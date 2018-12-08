$().ready(function() {
	validateRule();
	//时间框
	$(".form_datetime").datetimepicker({
		anguage:  'zh-CN',
		autoclose: 1,
		startView: 1,
		minView: 0,
		maxView: 1,
		forceParse: 0
	});
	//商户号切换
	$("#company").change(function(){
		var payCompany = $(this).val();
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
							$selMerchNo.append("<option value=''>请选择</option>");
							for ( var i in datas) {
								$selMerchNo.append("<option value='"+datas[i]+"'>"+datas[i]+"</option>");
							}
						}
					} else {
						parent.layer.alert(data.msg)
					}
				}
			});
		}
	});
	//日期切换事件
	$(".form_datetime").change(function(){
		var beginTime1 = $("#beginTime1").val();
		var endTime1 = $("#endTime1").val();
		var beginTime2 = $("#beginTime2").val();
		var endTime2 = $("#endTime2").val();
		var payPeriod = "";		
		if(beginTime1 && endTime1){
			payPeriod += beginTime1 + "-" + endTime1;
		}else if(beginTime1){
			payPeriod += beginTime1 + "-" + "24:00";
		}else if(endTime1){
			payPeriod += "00:00" + "-" + endTime1;
		}
		
		if(beginTime2 && endTime2){
			if(payPeriod){
				payPeriod +=",";
			}
			payPeriod += beginTime2 + "-" + endTime2;
		}else if(beginTime2){
			if(payPeriod){
				payPeriod +=",";
			}
			payPeriod += beginTime2 + "-" + "24:00";
		}else if(endTime2){
			if(payPeriod){
				payPeriod +=",";
			}
			payPeriod += "00:00" + "-" + endTime2;
		}
		$("#payPeriod").val(payPeriod);
	});
	
	$("#paymentMethod").trigger("change");
});

$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/payConfigCompany/save",
		data : $('#signupForm').serialize(),// 你的formid
		async : false,
		error : function(request) {
			parent.layer.alert("Connection error");
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
	$("#signupForm").validate({
		rules : {
			company : {
				required : true
			},
			payMerch : {
				required : true
			},
			outChannel : {
				required : true
			}
		},
		messages : {
			company : {
				required : icon + "请选择支付公司"
			},
			payMerch : {
				required : icon + "请选择商户号"
			},
			outChannel : {
				required : icon + "请选择支付方式"
			}
		}
	})
}

function pmChange(obj){
	var pm = $(obj).val();
	if(pm == 1 || pm == 2){
		$("#clearRatio").attr("readonly","readonly").val(1);
	}else{
		$("#clearRatio").removeAttr("readonly");
	}
}