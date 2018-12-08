$().ready(function() {
	validateRule();
	initConfigType($("input[name='configType']:checked").val());
	$("input[name='configType']").click(function(){
		initConfigType($(this).val());
	});
	$("#payCompany").change(function(){
		$(":radio[name='configType'][value=5]").removeAttr("disabled");
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
							var $selMerchNo = $("#merchantno");
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
	
	$("#merchantno").change(function(){
		var merchNo = $(this).val();
		if(merchNo){
			$(":radio[name='configType'][value=5]").attr("disabled","disabled").removeAttr("checked");
		}else{
			$(":radio[name='configType'][value=5]").removeAttr("disabled");
		}
	});
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
		save();
	}
});
function save() {
	var configType = $("input[name='configType']:checked").val();
	if(!configType){
		layer.msg("请选择配置类型！");
		return;
	}
	if(ifFileType(configType)){
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
		url : "/pay/payProperty/save",
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
			payCompany : {
				required : true
			},
			configType : {
				required : true
			},
			configKey:{
				required : true,
				remote : {
					url : "/pay/payProperty/exist", // 后台处理程序
					type : "post", // 数据发送方式
					dataType : "json", // 接受数据格式
					data : { // 要传递的数据
						configKey : function() {
							return  $("#configKey").val();
						},
						merchantno : function(){
							return $("#merchantno").val();
						}
					}
				}
			},
			value : {
				required : true
			}
		},
		messages : {
			payCompany : {
				required : icon + "请选择支付公司"
			},
			configType : {
				required : icon + "请选择配置类型"
			},
			configKey:{
				required : icon + "请输入配置标识",
				remote:icon + "配置标识已经存在"
			},
			value : {
				required : icon + "请输入值"
			}
		}
	})
}