$().ready(function() {
	validateRule();
	initConfigType($("input[name='configType']:checked").val());
});
function initConfigType(configType){
	//文件类型
	if(ifFileType(configType)){
		$("#divFileUpload").show();
		$("#divConfigValue").hide();
	}else{
		$("#divFileUpload").hide();
		$("#divConfigValue").show();
	}
	//商户号类型 不能修改值
	if(configType == 5){
		$('#value').attr("readonly","readonly");
	}
}
//判断文件类型
function ifFileType(configType){
	return '3' == configType || '4' == configType;
}
layui.use('upload', function () {
    var upload = layui.upload;
    //执行实例
    var uploadInst = upload.render({
        elem: '#keyFileUpload', //绑定元素
        url: '/common/sysFile/uploadKeyFile', //上传接口
        size: 1000,
        accept: 'file',
        done: function (r) {
            layer.msg(r.msg);
            if(r.code == 0){
            	$("#fileUploadValue").val(r.fileName);
            	$("#fileName").attr("href","/files/" + r.fileName).text(r.fileName);
            }
        },
        error: function (r) {
            layer.msg(r.msg);
        }
    });
});
$.validator.setDefaults({
	submitHandler : function() {
		update();
	}
});
function update() {
	if(ifFileType($("input[name='configType']:checked").val())){
		var fileUploadValue = $("#fileUploadValue").val();
		if(!fileUploadValue){
			layer.msg("请上传相应的文件！");
			return;
		}else{
			$("#value").val(fileUploadValue);
		}
	}
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/payProperty/update",
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
			payConfigType : {
				required : true
			},
			value : {
				required : true
			}
		},
		messages : {
			payConfigType : {
				required : icon + "请选择配置类型"
			},
			value : {
				required : icon + "请输入值"
			}
		}
	})
}