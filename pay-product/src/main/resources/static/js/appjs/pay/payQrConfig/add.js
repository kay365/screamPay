$().ready(function() {
	validateRule();
    $('#apiKey').val(md5(Math.random().toString(36)+Date.now()));
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
		url : "/pay/payQrConfig/save",
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
			merchNo : {
				required : true
			},
			outChannel : {
				required : true
			},
			accountNo : {
				required : true
			},
			accountName : {
				required : true
			}
		},
		messages : {
			merchNo : {
				required : icon + "请输入商户号"
			},
			outChannel : {
				required : icon + "请选择支付渠道"
			},
			accountNo : {
				required : icon + "请输入收款账号"
			},
			accountName : {
				required : icon + "请输入收款名称"
			}
		}
	})
}