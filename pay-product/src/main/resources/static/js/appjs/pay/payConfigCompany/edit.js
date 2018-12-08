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
		update();
	}
});
function update() {
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/payConfigCompany/update",
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
		},
		messages : {
		}
	})
}

function pmChange(obj){
	var pm = $(obj).val();
	if(pm == 1 || pm == 2){
		$("#clearRatio").attr("readonly","readonly");
	}else{
		$("#clearRatio").removeAttr("readonly");
	}
}