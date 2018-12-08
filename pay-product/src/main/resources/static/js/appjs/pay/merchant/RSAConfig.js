$().ready(function () {
    $("#privateKeyDiv").hide();
    $("#publicKey").val( merchant.publicKey)
    $("#createPrivateKey").click(createPrivateKey);
    $("#submit").click(update);
    $("#sendEmail").click(sendEmail);
});




function createPrivateKey() {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/pay/merchant/createPrivateKey",
        async: false,
        error: function (request) {
            parent.layer.alert("连接超时!");
        },
        success: function (data) {
            $("#publicKey").val(data[0]);
            $("#privateKey").val(data[1]);
            $("#privateKeyDiv").hide();
        }

    });
}

function update() {
    $.ajax({
        cache: true,
        type: "POST",
        url: "/pay/merchant/updatePKey",
        data: "merchNo=" +   merchant.merchNo + "&publicKey=" +   $("#publicKey").val()+"&privateKey=" +   $("#privateKey").val(),
        async: false,
        error: function (request) {
            parent.layer.alert("连接超时!");
        },
        success: function (data) {
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


// function validateRule() {
//     var icon = "<i class='fa fa-times-circle'></i> ";
//     $("#").validate({
//         rules: {
//             name: {
//                 required: true
//             }
//         },
//         messages: {
//             name: {
//                 required: icon + "请输入名字"
//             }
//         }
//     })
// }