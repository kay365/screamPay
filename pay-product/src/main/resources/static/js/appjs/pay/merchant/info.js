function loadFun() {
	
	$(".form_datetime").datetimepicker({
		language : 'zh-CN',
		format: 'yyyy-mm-dd',
		weekStart: 1,
		todayBtn:  1,
		autoclose: 1,
		todayHighlight: 1,
		startView: 2,
		minView: 2,
		forceParse: 0
	});
	
	$("#merchantsIndustryCode").change(function(){
		var industry = $(this).val();
		console.log($(this).find("option:selected").text());
		$("#merchantsIndustry").val($(this).find("option:selected").text());
		getChildByP(industry,"merchantsSubIndustryCode");
	});
	$("#merchantsSubIndustryCode").change(function(){
		$("#merchantsSubIndustry").val($(this).find("option:selected").text());
	});
	//$(".form_datetime").val(formatDate(new Date()));
	$("#payConfigCompany").change(function(){
		var paidChnanel = "{" +
				"payCompany:'" +$(this).find("option:selected").attr("company")+
				"',payMerch:'" +$(this).find("option:selected").val()+
				"'}"
		$("#paidChannelStr").val(paidChnanel);
	});
	
	validateRule();
	
	$("#merchantsIndustryCode").trigger("change");
}


$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/merchant/update",
		data : $('#merchantForm').serialize(),
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

function getChildByP(industryId,idEle){
	if(industryId == ''){
		var $selMerchNo = $("#" + idEle);
		$selMerchNo.find("option").remove();
		$selMerchNo.append("<option value=''>--子级行业--</option>");
		return;
	}
	if(industryId){
		$.ajax({
			cache : true,
			type : "get",
			url : "/pay/agent/getSubs",
			data : {industryId : industryId},
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
						$selMerchNo.append("<option value=''>--子级行业--</option>");
						for ( var i in datas) {
							var selected = "";
							if(datas[i].id == merchantsSubIndustryCode)
								selected = "selected";
							$selMerchNo.append("<option value='"+datas[i].id+"' "+selected+">"+datas[i].name+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg)
				}
			}
		});
	}
}
