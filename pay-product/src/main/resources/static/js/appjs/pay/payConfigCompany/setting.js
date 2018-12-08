$().ready(function() {
	validateRule();
	
	$(":radio").each(function(i,obj){
		$(obj).click(function(){
			var configValue = $(this).val();
			update("outChannelConfig",configValue,$(this).attr("name"));
		});
	});
});

$.validator.setDefaults({
	submitHandler : function() {
		update("outChannelConfig",$("#companySinglePollMoney").val(),"company_single_poll_money");
	}
});
function update(parentItem,configValue,configItem) {
	$.ajax({
		cache : true,
		type : "POST",
		url : "/system/config/update",
		data : {configItem:configItem,
				configValue:configValue,
				parentItem:parentItem
				},// 你的formid
		async : false,
		error : function(request) {
			parent.layer.alert("Connection error");
		},
		success : function(data) {
			if (data.code == 0) {
				parent.layer.msg("操作成功");
				var index = parent.layer.getFrameIndex(window.name); // 获取窗口索引
//				parent.layer.close(index);

			} else {
				parent.layer.alert(data.msg)
			}

		}
	});

}
function validateRule() {
	var icon = "<i class='fa fa-times-circle'></i> ";
	$("#signupForm").validate({
		rules : {
			companySinglePollMoney : {
				required : true
			}
		},
		messages : {
			companySinglePollMoney : {
				required : icon + "请输入轮洵比例"
			}
		}
	})
}