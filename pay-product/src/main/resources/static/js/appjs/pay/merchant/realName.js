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
	/*//列表选择
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
	})*/
	$("#provinceCode").trigger("change");
	$("#accountProvinceCode").trigger("change");
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


$.validator.setDefaults({
	submitHandler : function() {
		save();
	}
});
function save() {
	
	var beginDateL = $("#beginDateL").val();
	var endDateL = $("#endDateL").val();
	var legalerCardEffectiveTime = beginDateL+"~"+endDateL;
	$("#legalerCardEffectiveTime").val(legalerCardEffectiveTime);
	var beginDateT = $("#beginDateT").val();
	var endDateT = $("#endDateT").val();
	var merchantBusinessTerm = beginDateT+"~"+endDateT;
	$("#merchantBusinessTerm").val(merchantBusinessTerm);
	$.ajax({
		cache : true,
		type : "POST",
		url : "/pay/merchant/updateRealName",
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
			},
			merchantRegisteredNumber: {
                required: true,
                minlength: 2,
                remote: {
                    url: "/pay/merchant/existLicenseNum", // 后台处理程序
                    type: "post", // 数据发送方式
                    dataType: "json", // 接受数据格式
                    data: { // 要传递的数据
                        merchantRegisteredNumber:function () {
                            return  $("#merchantRegisteredNumber").val();
                        },
						merchNo:$("#userId").val()
                    },

                }
            }
		},
		messages : {
			name : {
				required : icon + "请输入商户名称"
			},
			merchNo : {
				remote : icon + "商户号已经存在"
			},
            merchantRegisteredNumber:{
                remote: icon+"营业执照已经存在"
            }
		}
	});
}
layui.use(['upload'], function () {
    var upload = layui.upload;
  //执行实例
    upload.render({
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
	
	upload.render({
		elem: '#img_bis', //绑定元素
		url: '/common/sysFile/upload', //上传接口
		size: 1000,
		accept: 'images',
		exts:'jpg|png|gif|bmp|jpeg',
		done: function (r) {
			//layer.msg(r.msg);
			console.log(r)
			$("#img_bp").attr("src",r.fileName);
			$("#merchantBusinessPhotocopy").val(r.fileName)
		},
		error: function (r) {
			layer.msg(r.msg);
		}
	});
	
});
