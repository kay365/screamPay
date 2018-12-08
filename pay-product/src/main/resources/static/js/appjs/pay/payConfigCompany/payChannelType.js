var prefix = "/pay/payConfigCompany/payChannelType"
$(function() {
    load();
});

function load() {
    $('#exampleTable').bootstrapTable({
        method : 'post', // 服务器数据的请求方式 get or post
        url : prefix + "/list", // 服务器数据的加载地址
        iconSize : 'outline',
        toolbar : '#exampleToolbar',
        striped : true, // 设置为true会有隔行变色效果
        dataType : "json", // 服务器返回的数据类型
        pagination : true, // 设置为true会在底部显示分页条
        singleSelect : false, // 设置为true将禁止多选
        // contentType : "application/x-www-form-urlencoded",
        pageSize : 10, // 如果设置了分页，每页数据条数
        pageNumber : 1, // 如果设置了分布，首页页码
        search : true, // 是否显示搜索框
        showColumns : false, // 是否显示内容下拉框（选择显示的列）
        queryParams : function(params) {
            return {
                channelName:$("#channelName").val()
            };
        },
        // data: $("#channelName").val(),
        columns : [
            {
                field : 'typeId',
                title : '通道分类Id',
            },
            {
                field : 'typeName',
                title : '通道分类名称'
            },
            {
                title : '操作',
                field : 'id',
                align : 'center',
                formatter : function(value, row, index) {
                    var e = '<a class="btn btn-primary btn-sm" href="#" mce_href="#" title="编辑" onclick="edit(\''
                        + row.typeId+ '\',\'' +row.typeName
                        + '\')"><i class="fa fa-edit"></i></a> ';
                    return e
                }
            } ]
    });
}
function reLoad() {
    $('#exampleTable').bootstrapTable('refresh');
}
function add() {
    layer.open({
        id:1,
        type: 1,
        title:'新增通道分类',
        skin:'layui-layer-rim',
        area:['450px', 'auto'],

        content: '<div class="row" style="width: 420px;  margin-left:7px; margin-top:10px;">'
        +'<div class="col-sm-12">'
        +'<div class="input-group">'
        +'<span class="input-group-addon"> 通  道  id :</span>'
        +'<input id="typeId" type="text" class="form-control " readonly="readonly"  placeholder="请输入通道id">'
        +'</div>'
        +'<div class="input-group">'
        +'<span class="input-group-addon"> 通道名称:</span>'
        +'<input id="typeName" type="text" class="form-control"   placeholder="请输入通道名称">'
        +'</div>'
        +'</div>'
        +'</div>'
        +'<script type="text/javascript">'
        +'$().ready(function(){$("#typeId").val(getTypeId());})</script>'
        ,
        btn:['保存','取消'],
        btn1: function (index,layero) {
        $.ajax({
            url:prefix+"/update",
            type:"post",
            data:{
                typeId :$("#typeId").val(),
                typeName:$("#typeName").val()
            },
            success : function(r) {
                if (r.code==0) {
                    layer.msg(r.msg);
                    parent.reLoad();
                    var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
                    parent.layer.close(index);
                }else{
                    layer.msg(r.msg);
                }
            }
        });
    },
    btn2:function (index,layero) {
        layer.close(index);
    },


});
}
function edit(id,name) {
    // layer.open({
    //     type : 2,
    //     title : '编辑',
    //     maxmin : true,
    //     shadeClose : false, // 点击遮罩关闭层
    //     area : [ '800px', '560px' ],
    //     content : prefix + '/edit/' +id // iframe的url
    // });
    layer.open({
        id:1,
        type: 1,
        title:'修改通道名称',
        skin:'layui-layer-rim',
        area:['450px', 'auto'],

        content: '<div class="row" style="width: 420px;  margin-left:7px; margin-top:10px;">'
        +'<div class="col-sm-12">'
        +'<div class="input-group">'
        +'<span class="input-group-addon"> 通道名称  :</span>'
        +'<input id="typeName" type="text" class="form-control"  value="'+name+'"  placeholder="请输入通道名称">'
        +'</div>'
        +'</div>'
        +'</div>'
        ,
        btn:['保存','取消'],
        btn1: function (index,layero) {
                $.ajax({
                   url:prefix+"/update",
                    type:"post",
                    data:{
                       typeId :id,
                        typeName:$("#typeName").val()
                    },
                    success : function(r) {
                        if (r.code==0) {
                            layer.msg(r.msg);
                            var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
                            parent.layer.close(index);
                        }else{
                            layer.msg(r.msg);
                        }
                    }
                });
        },
        btn2:function (index,layero) {
            layer.close(index);
        }

    });
}
function remove(id) {
    layer.confirm('确定要删除选中的记录？', {
        btn : [ '确定', '取消' ]
    }, function() {
        $.ajax({
            url : prefix+"/remove",
            type : "post",
            data : {
                payChannelId:id
            },
            success : function(r) {
                if (r.code==0) {
                    layer.msg(r.msg);
                    reLoad();
                }else{
                    layer.msg(r.msg);
                }
            }
        });
    })
}

// function batchRemove() {
//     var rows = $('#exampleTable').bootstrapTable('getSelections'); // 返回所有选择的行，当没有选择的记录时，返回一个空数组
//     if (rows.length == 0) {
//         layer.msg("请选择要删除的数据");
//         return;
//     }
//     layer.confirm("确认要删除选中的'" + rows.length + "'条数据吗?", {
//         btn : [ '确定', '取消' ]
//         // 按钮
//     }, function() {
//         var companys = new Array();
//         var payMerchs = new Array();
//         var outChannels = new Array();
//         // 遍历所有选择的行数据，取每条数据对应的ID
//         $.each(rows, function(i, row) {
//
//         });
//         $.ajax({
//             type : 'POST',
//             data : {
//
//             },
//             url : prefix + '/batchRemove',
//             success : function(r) {
//                 if (r.code == 0) {
//                     layer.msg(r.msg);
//                     reLoad();
//                 } else {
//                     layer.msg(r.msg);
//                 }
//             }
//         });
//     }, function() {
//
//     });
// }

function  getTypeId() {
    $.ajax({
        url: prefix + "/getTypeId",
        type: "post",
        success: function (r) {
            if (r.code == 0) {
                $("#typeId").val(r.data+1);
                return r.data+1;
            } else {
                console.log(r.data);
                layer.msg(r.msg);
            }
        }
    });
}
