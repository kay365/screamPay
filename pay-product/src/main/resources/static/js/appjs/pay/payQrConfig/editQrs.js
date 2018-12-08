var batchUploader;
var uploader;

$().ready(function() {


    load();
	// $("#divQrs a").click(function(){
	// 	var $a = $(this);
	// 	showPic($a);
	// });


    $('#remove').click(function(){
        var selected= $('#exampleTable').bootstrapTable('getSelections');
        if(selected.length==0){
            layer.msg("请选中一行!");
            return;
        }
        var amounts = [];
        for(var i=0;i<selected.length;i++){
            amounts.push(selected[i].amount);
        }
        removeBatch(amounts);
    })

	$('#signupForm').submit(function(){
		return false;
	});

    $('#moneyAmountFile').click(function(event) {
        event.preventDefault();
		event.stopPropagation();
    })


	$('#moneyAmountFileBatch').click(function(){
	    var html = '<label class="col-sm-4 control-label">资金密码:</label>' +
            '<div class="col-sm-8">' +
            '<input type="password" id="fundpasswordInput"  class="form-control">' +
            '</div>';
        layer.confirm(html, {title:'验证资金密码'}, function(index){
            //do something
            var fundPassword = $('#fundpasswordInput').val();
            $.ajax({
                type : "POST",
                url : "/salt",
                data:{username:username},
                success : function(r) {
                    if (r.code == 0) {
                         checkFundPassword(md5(username+md5(fundPassword)+r.data),showBatchUploadQr);
                        layer.close(index);
                    } else {
                        layer.msg(r.msg);
                    }
                }
            })
        });


	});
});


function load(){
    $('#exampleTable').bootstrapTable({
        iconSize : 'outline',
        striped : true, // 设置为true会有隔行变色效果
        dataType : "json", // 服务器返回的数据类型
        pagination : true, // 设置为true会在底部显示分页条
        // queryParamsType : "limit",
        // //设置为limit则会发送符合RESTFull格式的参数
        singleSelect : false, // 设置为true将禁止多选
        // contentType : "application/x-www-form-urlencoded",
        // //发送到服务器的数据编码类型
        //search : true, // 是否显示搜索框
        showColumns : false, // 是否显示内容下拉框（选择显示的列）
        sidePagination : "client", // 设置在哪里进行分页，可选值为"client" 或者 "server"
        columns : [
            {
                checkbox : true
            },
            {
                field : 'amount',
                title : '金额'
            },
            {
                title : '详情',
                field : 'amount',
                align : 'center',
                formatter : function(value, row, index) {
                    var d = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="详情" onclick="showAmountPic(\''
                        + row['amount']
                        + '\')"><i class="fa fa-eye"></i></a> ';
                    var r = '<a class="btn btn-warning btn-sm " href="#" title="删除"  mce_href="#" onclick="remove(\''
                        + row['amount']
                        + '\')"><i class="fa fa-remove"></i></a> ';

                    return d + r;
                }
            } ]
    });
    reload();

}


function removeBatch(amounts){
    $.ajax({
        url : "/pay/payQrConfig/removeQrs/" + outChannel + "/" + merchNo,
        type : "post",
        data : {
            'moneyAmounts': amounts,
        },
        success : function(r) {
            if (r.code==0) {
                layer.msg(r.msg);
                reload();
            }else{
                layer.msg(r.msg);
            }
        }
    });

}

function reload() {
    $.ajax({
        url : "/pay/payQrConfig/getQrs/" + outChannel + "/" + merchNo,
        type : "post",
        data : {
        },
        success : function(r) {
            if (r.code==0) {
                var qrs = r.data.qrs;
                var amountImages = [];
                for(var key in qrs){
                    var obj = {};
                    obj.amount = key;
                    amountImages.push(obj);
                }
                $('#exampleTable').bootstrapTable('load',amountImages);
            }else{
                layer.msg(r.msg);
            }
        }
    });

    $('#exampleTable').bootstrapTable('refresh');
}

function remove(amount){
    var amounts = [];
    amounts.push(amount);
    removeBatch(amounts);
}

function checkFundPassword(fundPassword,cb){
    $.ajax({
        type : "POST",
        url : "/pay/qr/checkFundPassword",
        data:{fundPassword:fundPassword},
        success : function(r) {
            if (r.code == 0) {
                cb();
            } else {
                layer.msg(r.msg);
            }
        }
    })
}

function showBatchUploadQr(){
    var html = '<div id="batchUpload" class="file-loading">' +
        '<input id="input-zh" name="file" type="file" multiple>' +
        '</div>';
    layer.open({
        type : 1,
        title : '批量新增收款二维码',
        shade:false,
        skin:'layui-layer-rim',
        area : ['900px','400px'],
        resize:false,
        content : html
    });

    $("#input-zh").fileinput({
        theme: 'fa',
        language: 'zh',
        maxFileCount:20,
        uploadUrl: '/pay/payQrConfig/batchUploadQrs/' + outChannel + '/' + merchNo,
        hideThumbnailContent: true // hide image, pdf, text or other content in the thumbnail preview
    });
}


function showAmountPic(amount){
    if(picIndex){
        layer.close(picIndex);
    }
	if(amount){
	    console.log(amount);
		var amountPath = amount.replace('￥','').replace('.','p');
		var content = "<img width='250' src='/files/"+merchNo + "/"+ outChannel + "/" + amountPath + ".jpg?r="+Math.random()+"'>";
		picIndex = layer.open({
		  type: 1,
		  shade: false,
		  skin: 'layui-layer-rim', //加上边框
		  area: ['450px', '400px'], //宽高
		  title: "收款码展示", //不显示标题
		  content: content, //捕获的元素，注意：最好该指定的元素要存放在body最外层，否则可能被其它的相对元素所影响
		  btn: ['关闭'],
            btn1: function(index, layero){
		      if(picIndex){
            	layer.close(picIndex);
              }
            }
		});
	}
}

//只能输入两位小数
function noNumbers(e) {
	var keynum
	var keychar
	var numcheck
	if (window.event) { // IE
		keynum = e.keyCode
	} else if (e.which) {// Netscape/Firefox/Opera
		keynum = e.which
	}
	keychar = String.fromCharCode(keynum);
	// 判断是数字,且小数点后面只保留两位小数
	if (!isNaN(keychar)) {
		var index = e.currentTarget.value.indexOf(".");
		if (index >= 0 && e.currentTarget.value.length - index > 2) {
			return false;
		}
		return true;
	}
	// 如果是小数点 但不能出现多个 且第一位不能是小数点
	if ("." == keychar) {
		if (e.currentTarget.value == "") {
			return false;
		}
		if (e.currentTarget.value.indexOf(".") >= 0) {
			return false;
		}
		return true;
	}
	return false;
}

var outChannel = $("#outChannel").val();
var merchNo = $("#merchNo").val();
var index = null;
var picIndex = null;




// 文件上传
layui.use('upload', function() {
    uploader = layui.upload.render({
        elem : '#moneyAmountFile', // 绑定元素
        url : '/pay/payQrConfig/uploadQrs/' + outChannel + '/' + merchNo,// 上传接口
        size : 1000,
        accept : 'file',
        before : function(obj) {
            index = layer.load(); // 上传loading
            this.data = {
                'moneyAmount' : $("#moneyAmount").val()
            };
        },
        done : function(r) {
            layer.close(index);
            layer.msg(r.msg);
            if (r.code == 0 && r.fileName) {
                reload();
                // $("#moneyAmountFileName").attr("href", "/files/" +merchNo + "/"+ outChannel + "/" + r.fileName.replace('.','p')+".jpg").
                // attr("download",r.fileName.replace('.','p')).text(r.fileName + ".jpg");
                // $("#divQrs").prepend('<a class="col-sm-1 layui-btn layui-btn-primary" style="margin:0;padding:0; font-weight: bolder;color: green;overflow: hidden;border-radius:19px;">'+ r.fileName +'</a>');
                // $("#divQrs a:first").bind("click",function(){
                //     showAmountPic($(this),r.fileName);
                // });
            }
        },
        error : function(r) {
            layer.msg(r.msg);
        }
    });
});
