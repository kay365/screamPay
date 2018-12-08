function loadFun() {
	//$("#agentForm").find("input[type='text']").val(77);
	/*$("#merchantsIndustryCode").change(function(){
		var industry = $(this).val();
		console.log($(this).find("option:selected").text());
		$("#merchantsIndustry").val($(this).find("option:selected").text());
		getChildByP(industry,"merchantsSubIndustryCode");
	});
	$("#merchantsSubIndustryCode").change(function(){
		$("#merchantsSubIndustry").val($(this).find("option:selected").text());
	});*/
	//$(".form_datetime").val(formatDate(new Date()));
	
	$("#merchantsIndustryCode").change(function(){
		var industry = $(this).val();
		console.log($(this).find("option:selected").text());
		$("#merchantsIndustry").val($(this).find("option:selected").text());
		getChildByP(industry,"merchantsSubIndustryCode");
	});
	$("#merchantsSubIndustryCode").change(function(){
		$("#merchantsSubIndustry").val($(this).find("option:selected").text());
	});
	
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
	
//	agentSearch(agentN,merchNos);
	
	validateRule();
	
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
								$selCity.append("<option value='"+datas[i].id+"'>"+datas[i].name+"</option>");
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
								$selCity.append("<option value='"+datas[i].id+"'>"+datas[i].name+"</option>");
							}
							
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
	//列表选择
	$(".userBankInfo").click(function(){
		var index = $(this).find("td:first").text();
		var userBank = userBanks[index];
		$("#bankNo").val(userBank.bankNo);
		$("#acctName").val(userBank.acctName);
		$("#certNo").val(userBank.certNo);
		$("#mobile").val(userBank.phone);
		$("#accountBank").val(userBank.bankCode);
		var $selUnionpayNo = $("#accountBankBranch");
		$selUnionpayNo.append("<option value='"+userBank.unionpayNo+"'>"+userBank.bankBranch+"</option>");
		$("#accountBankBranch").val(userBank.unionpayNo);
		$("#bankBranch").val(userBank.bankBranch);
	})
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
							$selUnionpayNo.append("<option value='"+datas[i].unionPayNo+"'>"+datas[i].bankBranch+"</option>");
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
/*layui.use(['element','form'], function(){
	  var $ = layui.jquery
	  ,element = layui.element; //Tab的切换功能，切换事件监听等，需要依赖element模块
});*/


$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	/*var outChannels = new Object();
	$("input[type=checkbox][name=outChannel]:checked").each(function(){
		outChannels[$(this).val()] = 1;
	});
	
	var handRates = new Object();
	$("input[name=handRate]").each(function(){
		var handRate = $(this).val();
		if(handRate){
			handRates[$(this).attr("id").substring(8)] = handRate;
		}
	});*/
	
	/*var feeRates = new Object();
	$("input[name=feeRate]").each(function(){
		var feeRate = $(this).val();
		if(feeRate){
			feeRates[$(this).attr("id").substring(7)] = feeRate;
		}
	});*/
	
	if(!getRate()){
		return;
	}
	
	var beginDateL = $("#beginDateL").val();
	var endDateL = $("#endDateL").val();
	var legalerCardEffectiveTime = beginDateL+"~"+endDateL;
	$("#legalerCardEffectiveTime").val(legalerCardEffectiveTime);
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	var contractEffectiveTime = beginDateL+"~"+endDateL;
	$("#contractEffectiveTime").val(contractEffectiveTime);
	
	//费率设置
	$("#tOneStr").val("{}");
//	$("#dZeroStr").val(getRate_d0());
	$("#paidStr").val(getRate_paid());
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/agent/save",
		data : $('.wizard-big').serialize(),
		async : false,
		error : function(request) {
			$("#commitComplete").hide();
			$("#commitValid").show();
			isTwoCommit = false;
			parent.layer.alert("连接超时!或管理员电话重复!");
		},
		success : function(data) {
			if (data.code == 0) {
				$("#commitComplete").show();
				$("#commitWait").hide();
				/*parent.layer.msg("操作成功");*/
				parent.reLoad();
				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
				parent.layer.close(index);

			} else {
				$("#commitComplete").hide();
				$("#commitValid").show();
				isTwoCommit = false;
				parent.layer.alert(data.msg)
			}
		}
	});

}
function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#agentForm").validate({
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
							$selMerchNo.append("<option value='"+datas[i].id+"'>"+datas[i].name+"</option>");
						}
					}
				} else {
					parent.layer.alert(data.msg)
				}
			}
		});
	}
}

function getRate(){
	
	var jsonObj = {};  
	var isSelectPM = false;
	var isRateNull = false
	$(":checkbox[name=paymentMethod]:checked").each(function(){
		var paymentMethod = $(this).val();
		var ocJson = {};
		$("[name=paymentMethod_"+paymentMethod+"]").each(function(){
			var outChannel = $(this).attr("ocv");
			var rate = $(this).val();
			var unit = $(this).next().val();
			if(rate==""){
				layer.msg($(this).attr("ock")+" 费率不能为空");
				isRateNull = true;
			}else if(rate <= 0){
				layer.msg($(this).attr("ock")+" 费率不能小于等于0");
				isRateNull = true;
			}else{
				var ruJson = {"rate":rate,"unit":unit};
				ocJson[outChannel] = ruJson;
			}
		});
		if(!isRateNull){
			jsonObj[paymentMethod] = ocJson;
			isSelectPM = true;
		}else{
			jsonObj = {};
			isSelectPM = false;
		}
	});
	console.log(JSON.stringify(jsonObj ));
	$("#dZeroStr").val(JSON.stringify(jsonObj ));
	return isSelectPM;
}

/*function getRate_t1(){
	
	var wap_t1 = $("#wap_t1").val();
	var wap_t1_u = $("#wap_t1_u").val();
	var gzh_t1 = $("#gzh_t1").val();
	var gzh_t1_u = $("#gzh_t1_u").val();
	var wx_t1 = $("#wx_t1").val();
	var wx_t1_u = $("#wx_t1_u").val();
	var qq_t1 = $("#qq_t1").val();
	var qq_t1_u = $("#qq_t1_u").val();
	var ali_t1 = $("#ali_t1").val();
	var ali_t1_u = $("#ali_t1_u").val();
	var q_t1 = $("#q_t1").val();
	var q_t1_u = $("#q_t1_u").val();
	var wy_t1 = $("#wy_t1").val();
	var wy_t1_u = $("#wy_t1_u").val();
	var acp = $("#acp").val();
	var acp_u = $("#acp_u").val();
	
	var rate_t1 = '{'+
	'"wap": {"rate": '+wap_t1+', "unit": '+wap_t1_u+'},'+
	'"gzh": {"rate": '+gzh_t1+', "unit": '+gzh_t1_u+'},'+
	'"wx": {"rate": '+wx_t1+', "unit": '+wx_t1_u+'},'+
	'"qq": {"rate": '+qq_t1+', "unit": '+qq_t1_u+'},'+
	'"ali": {"rate": '+ali_t1+', "unit": '+ali_t1_u+'},'+
	'"q": {"rate": '+q_t1+', "unit": '+q_t1_u+'},'+
	'"wy": {"rate": '+wy_t1+', "unit": '+wy_t1_u+'},'+
	'"acp": {"rate": '+acp+', "unit": '+acp_u+'},'+
	'}'
	return rate_t1;
}
function getRate_d0(){
	
	if(1==1)return "";
	var wap_d0 = $("#wap_d0").val();
	var wap_d0_u = $("#wap_d0_u").val();
	var gzh_d0 = $("#gzh_d0").val();
	var gzh_d0_u = $("#gzh_d0_u").val();
	var wx_d0 = $("#wx_d0").val();
	var wx_d0_u = $("#wx_d0_u").val();
	var qq_d0 = $("#qq_d0").val();
	var qq_d0_u = $("#qq_d0_u").val();
	var ali_d0 = $("#ali_d0").val();
	var ali_d0_u = $("#ali_d0_u").val();
	var q_d0 = $("#q_d0").val();
	var q_d0_u = $("#q_d0_u").val();
	var wy_d0 = $("#wy_d0").val();
	var wy_d0_u = $("#wy_d0_u").val();
	var acp = $("#acp").val();
	var acp_u = $("#acp_u").val();
	
	var rate_d0 = '{'+
	'"wap": {"rate": '+wap_d0+', "unit": '+wap_d0_u+'},'+
	'"gzh": {"rate": '+gzh_d0+', "unit": '+gzh_d0_u+'},'+
	'"wx": {"rate": '+wx_d0+', "unit": '+wx_d0_u+'},'+
	'"qq": {"rate": '+qq_d0+', "unit": '+qq_d0_u+'},'+
	'"ali": {"rate": '+ali_d0+', "unit": '+ali_d0_u+'},'+
	'"q": {"rate": '+q_d0+', "unit": '+q_d0_u+'},'+
	'"wy": {"rate": '+wy_d0+', "unit": '+wy_d0_u+'},'+
	'"acp": {"rate": '+acp+', "unit": '+acp_u+'},'+
	'}'
	return rate_d0;
}*/

function getRate_paid(){
	var paidd = $("#acp").val();
	var paidd_u = $("#acp_ru").val();
	return '{"rate":"'+paidd+'","unit":"'+paidd_u+'"}';
}
var form;
layui.use(['upload','form'], function () {
	form = layui.form;
    var upload = layui.upload;
    form.render(null, 'paymentMethodDiv');
  //执行实例
    var uploadInst = upload.render({
        elem: '#img_front', //绑定元素
        url: '/common/sysFile/upload', //上传接口
        size: 1000,
        accept: 'images',
		exts:'jpg|png|gif|bmp|jpeg',
        done: function (r) {
            //layer.msg(r.msg);
        	console.log(r)
            $("#img_f").attr("src",r.fileName);
        	$("#legalerCardPicFront").val(r.fileName)
        },
        error: function (r) {
            layer.msg(r.msg);
        }
    });
    
    //执行实例
   upload.render({
        elem: '#img_back', //绑定元素
        url: '/common/sysFile/upload', //上传接口
        size: 1000,
        accept: 'images',
		exts:'jpg|png|gif|bmp|jpeg',
        done: function (r) {
            //layer.msg(r.msg);
        	console.log(r)
            $("#img_b").attr("src",r.fileName);
        	$("#legalerCardPicBack").val(r.fileName)
        },
        error: function (r) {
            layer.msg(r.msg);
        }
    });
    
  //执行实例
	upload.render({
		elem: '#img_pic', //绑定元素
		url: '/common/sysFile/upload', //上传接口
		size: 1000,
		accept: 'images',
		exts:'jpg|png|gif|bmp|jpeg',
		done: function (r) {
			//layer.msg(r.msg);
			console.log(r)
			$("#img_p").attr("src",r.fileName);
			$("#accountPic").val(r.fileName)
		},
		error: function (r) {
			layer.msg(r.msg);
		}
	});
	
	form.on('checkbox', function(data){
		pmClick(this);
	});
});
function pmClick(obj){
	if($(obj).is(":checked")){
		$("[name=paymentMethod_"+$(obj).val()+"]").removeAttr("readonly");
		$("#acp").removeAttr("readonly");
	}else{
		$("[name=paymentMethod_"+$(obj).val()+"]").prop("readonly",true);
		if($("[name=paymentMethod]:checked").length <= 0)
			$("#acp").prop("readonly",true);
	}
}

function getPAgentRate(){
	var pAgent = $("#parentAgent").val();
	if(pAgent==""){
		$(":checkbox[name=paymentMethod]").each(function(){
			var paymentMethod = $(this).val();
			$(this).removeAttr("disabled").prop("checked",false);
			form.render(null, 'paymentMethodDiv');
			$.each(outChannels,function(key,value){
				$("#paymentMethod_"+key+"_"+paymentMethod).removeAttr("min");
				$("#paymentMethod_"+key+"_ru_"+paymentMethod).empty();
				$("#acp_ru").empty();
				$.each(rateUnits,function(key1,value){
					$("#paymentMethod_"+key+"_ru_"+paymentMethod).append('<option value="'+key1+'" >'+value+'</option>');
					$("#acp_ru").append('<option value="'+key1+'" >'+value+'</option>');
				});
			});
			pmClick(this);
		});
		return;
	}
	$.ajax({
		cache : true,
		type : "get",
		url : "/pay/agent/getRate/"+pAgent,
		async : false,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			if (data.code == 0) {
				if(data.data){
					var datas = data.data;
					//支付控制
					var rates = datas.rate;
					$(":checkbox[name=paymentMethod]").each(function(){
						var paymentMethod = $(this).val();
						var rate = rates[paymentMethod];
						if(rate!=null){
							$(this).removeAttr("disabled");
							form.render(null, 'paymentMethodDiv');
							$.each(rate,function(key,value){
								var rate = value["rate"];
								var unit = value["unit"];
								$("#paymentMethod_"+key+"_"+paymentMethod).prop("min",rate);
								$("#paymentMethod_"+key+"_ru_"+paymentMethod).val(unit);
								$("#paymentMethod_"+key+"_ru_"+paymentMethod).find("option[value!="+unit+"]").remove();
							})
						}else{
							$(this).prop("disabled","disabled").prop("checked",false);
							form.render(null, 'paymentMethodDiv');
						}
						pmClick(this);
					});
					
					//代付 控制
					var paid = datas.paid;
					var rate = paid.rate;
					var unit = paid.unit;
					$("#acp").prop("min",(parseFloat(rate)+0.01).toFixed(2));
					$("#acp_ru").val(unit);
					$("#acp_ru").find("option[value!="+unit+"]").remove();
				}
			} else {
				parent.layer.alert(data.msg)
			}
		}
	});
}