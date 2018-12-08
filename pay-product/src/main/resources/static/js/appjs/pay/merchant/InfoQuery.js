function loadFun() {
	
	$(".steps").find("li[aria-selected!=true]").attr("class","done");
	
	$("#merchantsIndustryCode").change(function(){
		var industry = $(this).val();
		console.log($(this).find("option:selected").text());
		$("#merchantsIndustry").val($(this).find("option:selected").text());
		getChildByP(industry,"merchantsSubIndustryCode");
	});
	$("#merchantsSubIndustryCode").change(function(){
		$("#merchantsSubIndustry").val($(this).find("option:selected").text());
	});
	
	$("#provinceCode").change(function(){
		var province = $(this).val();
		$("#province").val($(this).find("option:selected").text());
		console.log(province);
		if(province){
			$.ajax({
				cache : true,
				type : "get",
				url : "/common/getCitysByProvince",
				data : {provinceId : province},
				async : false,
				error : function(request) {
					parent.layer.alert("Connection error");
				},
				success : function(data) {
					if (data.code == 0) {
						if(data.data){
							var datas = data.data;
							var $selCity = $("#cityCode");
							$selCity.find("option").remove();
							$selCity.append("<option value=''>请选择</option>");
							for ( var i in datas) {
								var selected = "";
								if(cityCode == datas[i].id)selected = "selected";
								$selCity.append("<option value='"+datas[i].id+"' "+selected+">"+datas[i].name+"</option>");
							}
							
						}
					} else {
						parent.layer.alert(data.msg)
					}
				}
			});
		}
	});
	
	$("#cityCode").change(function(){
		$("#city").val($(this).find("option:selected").text());
	});
	
	$("#accountProvinceCode").change(function(){
		var province = $(this).val();
		$("#accountProvince").val($(this).find("option:selected").text());
		console.log(province)
		if(province){
			$.ajax({
				cache : true,
				type : "get",
				url : "/common/getCitysByProvince",
				data : {provinceId : province},
				async : false,
				error : function(request) {
					parent.layer.alert("Connection error");
				},
				success : function(data) {
					if (data.code == 0) {
						if(data.data){
							var datas = data.data;
							var $selCity = $("#accountCityCode");
							$selCity.find("option").remove();
							$selCity.append("<option value=''>请选择</option>");
							for ( var i in datas) {
								var selected = "";
								if(accountCityCode == datas[i].id)selected = "selected";
								$selCity.append("<option value='"+datas[i].id+"' "+selected+">"+datas[i].name+"</option>");
							}
							$("#accountCityCode,#accountBankCode").trigger("change");
						}
					} else {
						parent.layer.alert(data.msg)
					}
				}
			});
		}
	});
	
	$("#accountCityCode,#accountBankCode").change(function(){
		$("#accountCity").val($("#accountCityCode").find("option:selected").text());
		$("#accountBank").val($("#accountBankCode").find("option:selected").text());
		loadUnionPayData();
	});
	$("#provinceCode").trigger("change");
	$("#accountProvinceCode").trigger("change");
	
	$("#merchantsIndustryCode").trigger("change");
	
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
	
	$("input,select,#merchantBusinessScope").prop("disabled",true);
}

function loadUnionPayData(){
	var cityId = $("#accountCityCode").val();
	var accountBank = $("#accountBankCode").val();
	console.log("accountBank:" + accountBank + ";cityId:" + cityId);
	if(cityId && accountBank){
		$.ajax({
			cache : true,
			type : "get",
			url : "/common/getUnionPay",
			data : {bankCode:accountBank,cityId:cityId},
			async : false,
			error : function(request) {
				parent.layer.alert("Connection error");
			},
			success : function(data) {
				if (data.code == 0) {
					if(data.data){
						var datas = data.data;
						var $selUnionpayNo = $("#accountBankBranchCode");
						$selUnionpayNo.find("option").remove();
						$selUnionpayNo.append("<option value=''>请选择</option>");
						for ( var i in datas) {
							var selected = "";
							if(accountBankBranchCode == datas[i].unionPayNo)selected = "selected";
							$selUnionpayNo.append("<option value='"+datas[i].unionPayNo+"' "+selected+">"+datas[i].bankBranch+"</option>");
						}
						
					}
				} else {
					parent.layer.alert(data.msg)
				}
			}
		});
	}
}

$("#accountBankBranchCode").change(function(){
	$("#accountBankBranch").val($(this).find("option:selected").text());
});

function clearUnionPay(){
	$("#accountBankBranch").val(null);
	$("#bankBranch").val(null);
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


var form;
layui.use(['form'], function () {
	form = layui.form;
    form.render(null, 'paymentMethodDiv');
    form.render(null, 'outChannelOnDiv');
	
	getPAgentRate();
});


function getPAgentRate(){
		//支付控制
		$(":checkbox[name=paymentMethod]").each(function(){
			var paymentMethod = $(this).val();
			var rate = parentRates[paymentMethod];
			if(rate!=null){
				$.each(rate,function(key,value){
					var rate = value["rate"];
					var unit = value["unit"];
					$("#paymentMethod_"+key+"_"+paymentMethod).prop("min",rate);
					$("#paymentMethod_"+key+"_ru_"+paymentMethod).val(unit);
					$("#paymentMethod_"+key+"_ru_"+paymentMethod).find("option[value!="+unit+"]").remove();
				})
			}
			$(this).prop("disabled","disabled");
			form.render(null, 'paymentMethodDiv');
		});
		$(":checkbox[name=outChannelOn]").prop("disabled","disabled");
		form.render(null, 'outChannelOnDiv');
		
		//代付 控制
		var rate = parentPaid.rate;
		var unit = parentPaid.unit;
		$("#acp").prop("min",rate);
		$("#acp_ru").val(unit);
		$("#acp_ru").find("option[value!="+unit+"]").remove();
}

function savePKey(){
	var publicKey = $("#publicKey").val();
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/merchant/updatePKey",
		data : "publicKey="+publicKey,
		async : false,
		error : function(request) {
			layer.alert("连接超时!");
		},
		success : function(data) {
			if (data.code == 0) {
				layer.msg("操作成功");
			} else {
				layer.alert(data.msg)
			}
		}
	});
}
