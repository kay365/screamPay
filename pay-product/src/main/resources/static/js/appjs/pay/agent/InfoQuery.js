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
	
	$("#merchantsIndustryCode").trigger("change");
	
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
		$("#accountCity").val($("#accountCity").find("option:selected").text());
		$("#accountBank").val($("#accountBankCode").find("option:selected").text());
		loadUnionPayData();
	});
	$("#provinceCode").trigger("change");
	$("#accountProvinceCode").trigger("change");
	
	
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
	
	$("input,select,textarea").prop("disabled",true);
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
							if(merchantsSubIndustryCode == datas[i].id)
								selected = "selected";
								
							$selMerchNo.append("<option value='"+datas[i].id+"' "+selected+" >"+datas[i].name+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg)
				}
			}
		});
	}
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

layui.use(['form'], function () {
	var form = layui.form;
    var upload = layui.upload;
    form.render(null, 'paymentMethodDiv');
});