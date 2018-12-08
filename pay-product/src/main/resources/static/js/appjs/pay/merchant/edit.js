$(function() {
    validateRule();

	//$("#merchantForm").find("input[type='text']").val(7);
	$("#merchantsIndustryCode").change(function(){
		var industry = $(this).val();
		console.log($(this).find("option:selected").text());
		$("#merchantsIndustry").val($(this).find("option:selected").text());
		getChildByP(industry,"merchantsSubIndustryCode");
	});
	$("#merchantsSubIndustryCode").change(function(){
		$("#merchantsSubIndustry").val($(this).find("option:selected").text());
	});
	
	$("#payConfigCompany").change(function(){
		var paidChnanel = "{" +
				"payCompany:'" +$(this).find("option:selected").text()+
				"',payMerch:'" +$(this).find("option:selected").val()+
				"'}"
		$("#paidChannelStr").val(paidChnanel);
	});
	
	/*$("input[type=checkbox][name=outChannel]").each(function(){
		$(this)
	})*/
	//$(".form_datetime").val(formatDate(new Date()));
	
	//agentSearch(agentN,merchNos);

	
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
		$("#accountCity").val($(this).find("option:selected").text());
		$("#accountBank").val($(this).find("option:selected").text());
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
});

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
layui.use(['element','form'], function(){
	  var $ = layui.jquery
	  ,element = layui.element; //Tab的切换功能，切换事件监听等，需要依赖element模块
});


$.validator.setDefaults({
	submitHandler : function() {
		update();
	}
});
function update() {
	var outChannels = "{";
	$("input[type=checkbox][name=outChannel]:checked").each(function(i){
		//outChannels[$(this).val()] = 1;
		if(i==0){
			outChannels += "'"+$(this).val()+"':1"
		}else{
			outChannels += ",'"+$(this).val()+"':1"
		}
	});
	outChannels += "}";
	
	var beginDateL = $("#beginDateL").val();
	var endDateL = $("#endDateL").val();
	var legalerCardEffectiveTime = beginDateL+"~"+endDateL;
	$("#legalerCardEffectiveTime").val(legalerCardEffectiveTime);
	var beginDate = $("#beginDate").val();
	var endDate = $("#endDate").val();
	var contractEffectiveTime = beginDateL+"~"+endDateL;
	$("#contractEffectiveTime").val(contractEffectiveTime);
	var beginDateT = $("#beginDateT").val();
	var endDateT = $("#endDateT").val();
	var merchantBusinessTerm = beginDateT+"~"+endDateT;
	$("#merchantBusinessTerm").val(merchantBusinessTerm);
	//费率设置
	$("#tOneStr").val(getRate_t1());
	$("#dZeroStr").val(getRate_d0());
	$("#paidStr").val(getRate_paid());
	
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/merchant/update",
		data : $('#merchantForm').serialize()+"&outChannelStr="+outChannels,// 你的formid
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
	$("#merchantForm").validate({
		rules : {
			/*name : {
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
			}*/
		},
		messages : {
			/*name : {
				required : icon + "请输入商户名称"
			}/*,
			merchNo : {
				remote : icon + "商户号已经存在"
			}*/
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

function getRate_t1(){
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
}

function getRate_paid(){
	var paidd = $("#acp").val();
	var paidd_u = $("#acp_u").val();
	return '{"rate":'+paidd+',"unit":'+paidd_u+'}';
}

layui.use('upload', function () {
    var upload = layui.upload;
    //执行实例
    var uploadInst = upload.render({
        elem: '#img_front', //绑定元素
        url: '/common/sysFile/upload', //上传接口
        size: 1000,
        accept: 'file',
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
});
layui.use('upload', function () {
    var upload = layui.upload;
    //执行实例
    var uploadInst = upload.render({
        elem: '#img_back', //绑定元素
        url: '/common/sysFile/upload', //上传接口
        size: 1000,
        accept: 'file',
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
});
layui.use('upload', function () {
	var upload = layui.upload;
	//执行实例
	var uploadInst = upload.render({
		elem: '#img_pic', //绑定元素
		url: '/common/sysFile/upload', //上传接口
		size: 1000,
		accept: 'file',
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
});

layui.use('upload', function () {
	var upload = layui.upload;
	//执行实例
	var uploadInst = upload.render({
		elem: '#img_bis', //绑定元素
		url: '/common/sysFile/upload', //上传接口
		size: 1000,
		accept: 'file',
		done: function (r) {
			//layer.msg(r.msg);
			console.log(r)
			$("#img_bus").attr("src",r.fileName);
			$("#merchantBusinessPhotocopy").val(r.fileName)
		},
		error: function (r) {
			layer.msg(r.msg);
		}
	});
});
