var prefix = "/pay/payAcctBal";
var curSelectVal = '';
var curTableDetailData = [];
$(function() {
    loadSelect();
	load();
});

function load() {
	$('#exampleTable')
			.bootstrapTable(
					{
						method : 'get', // 服务器数据的请求方式 get or post
						url : prefix + "/list", // 服务器数据的加载地址
					//	showRefresh : true,
					//	showToggle : true,
					//	showColumns : true,
						iconSize : 'outline',
						toolbar : '#exampleToolbar',
						striped : true, // 设置为true会有隔行变色效果
						dataType : "json", // 服务器返回的数据类型
						pagination : true, // 设置为true会在底部显示分页条
						// queryParamsType : "limit",
						// //设置为limit则会发送符合RESTFull格式的参数
						singleSelect : false, // 设置为true将禁止多选
						// contentType : "application/x-www-form-urlencoded",
						// //发送到服务器的数据编码类型
						pageSize : 10, // 如果设置了分页，每页数据条数
						pageNumber : 1, // 如果设置了分布，首页页码
						//search : true, // 是否显示搜索框
						showColumns : false, // 是否显示内容下拉框（选择显示的列）
						sidePagination : "client", // 设置在哪里进行分页，可选值为"client" 或者 "server"
						queryParams : function(params) {
							return {
								//说明：传入后台的参数包括offset开始索引，limit步长，sort排序列，order：desc或者,以及所有列的键值对
								limit: params.limit,
								offset:params.offset,
					           // name:$('#searchName').val(),
								userType:curSelectVal,
					            username:$('#username').val()
							};
						},
						// //请求服务器数据时，你可以通过重写参数的方式添加一些额外的参数，例如 toolbar 中的参数 如果
						// queryParamsType = 'limit' ,返回参数必须包含
						// limit, offset, search, sort, order 否则, 需要包含:
						// pageSize, pageNumber, searchText, sortName,
						// sortOrder.
						// 返回false将会终止请求
						columns : [
								{
									field : 'username',
									title : '用户名'
								},
																{
									field : 'userType',
									title : '用户类型' ,
									formatter:function(value, row, index) {
										if(value != null){
											return  userTypes[value];
										}
									}
								},
								{
									field : 'balance',
									title : '余额(元)'
								},
								{
									field : 'availBal',
									title : '可用余额(元)'
								},
								{
									field : 'companyPayAvailBal',
									title : '详情',
                                    formatter:function(value, row, index) {
                                        curTableDetailData[index] = value;
										var ret = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="详情" onclick="showDetail('
                                            + index
                                            + ')"><i class="fa fa-bars"></i></a> ';
                                        return ret;
                                    }
								}]
					});
}


function loadSelect(){
	var html = '';
	if(userTypes){
		for(var key in userTypes){
            html += '<option value="' + key + '">' + userTypes[key] + '</option>';
		}
	}
    $(".chosen-select").append(html);
    $(".chosen-select").chosen({
        maxHeight : 200
    });
    //点击事件
    $('.chosen-select').on('change', function(e, params) {
        console.log(params.selected);
        curSelectVal = params.selected;
        var opt = {
            query : {
            	userType:params.selected,
                username:$('#username').val()
            }
        }
        $('#exampleTable').bootstrapTable('refresh', opt);
    });
}

function reLoad() {
	$('#exampleTable').bootstrapTable('refresh');
}

function showDetail(index){
	$('#detail').removeClass('hidden');
	debugger;

	var obj = curTableDetailData[index];

    var html = '';
	for(var key in obj){
        html += genFormGroupHtml(key,obj[key]);
	}
    $('#detailForm').html(html);


    layer.open({
        type : 1,
        title : '详情',
        maxmin : true,
        shadeClose : false, // 点击遮罩关闭层
        area : [ '700px', '500px' ],
        cancel:function(index){
        	$('#detail').addClass('hidden');
		},
        content : $('#detail')
    });
}

function genFormGroupHtml(key,value){
	var formGroup =   '<div class="form-group">							'
					+ '	<label class="col-sm-2">支付渠道：</label>	'
        			+ '	<label class="col-sm-4">'+key+'</label>	'
					+ '	<label class="col-sm-2">剩余金额：</label>		'
        			+ '	<label class="col-sm-2">'+value+'</label>	'
					+ '</div>';
	return formGroup;
}