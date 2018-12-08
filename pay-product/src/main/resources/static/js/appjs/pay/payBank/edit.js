$().ready(function() {
	validateRule();
	$("#company").change(function(){
		var company = $(this).val();
		getMechNoByCompany(company,"payMerch");
	});
	$("#bankCheckAll").click(function(){
		$("#divItemAll input:checkbox").prop("checked",$(this).prop("checked"));
	});
	$("#bankCheckAll").prop("checked",$("#divItemAll input:checkbox:checked").length>0);
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
		url : "/pay/payBank/update",
		data : {
			company :company,
			payMerch : payMerch,
			cardType : cardType,
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
	$("#signupForm").validate();
}