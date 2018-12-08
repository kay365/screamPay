$().ready(function() {
	validateRule();
	$("#company").change(function(){
		var company = $(this).val();
		getMechNoByCompany(company,"payMerch");
	});
	$("#bankCheckAll").click(function(){
		$("#divItemAll input:checkbox").prop("checked",$(this).prop("checked"));
	});
	$("#divItemAll input:checkbox").click(function(){
		$("#bankCheckAll").prop("checked",$("#divItemAll input:checkbox:checked").length>0);
	});
});

$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	var bankCodes = new Array();
	$("#divItemAll input:checkbox:checked").each(function(){
		bankCodes[bankCodes.length] = $(this).val();
	});
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/payBank/save",
		data : {
			company : $("#company").val(),
			payMerch : $("#payMerch").val(),
			cardType : $("#cardType").val(),
			'bankCodes' : bankCodes
		},// 你的formid
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
				parent.layer.alert(data.msg);
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
			cardType:{
				required: true
			}
		},
		messages : {
			company : {
				required : icon + "请选择支付公司"
			},
			cardType : {
				required : icon + "请选择银行卡类型"
			}
		}
	})
}
function getMechNoByCompany(payCompany,idEle){
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
						var $selMerchNo = $("#" + idEle);
						$selMerchNo.find("option").remove();
						$selMerchNo.append("<option value=''>--支付商户号--</option>");
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
}