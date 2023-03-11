$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	// // 发送请求之前，将CSRF令牌设置到请求的消息头中
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e, xhr, options) {
	// 	xhr.setRequestHeader(header, token);
	// });
	// 获取标题和内容
	var post_title = $("#recipient-name").val();
	var post_content = $("#message-text").val();

	// 发送异步请求（post)
	$.post(
		CONTEXT_PATH + "/discuss/add",
		// 以表的形式向服务器发送数据
		{title : post_title,
		content : post_content},
		function (data) {
			data = $.parseJSON(data);
			// 在提示框中返回消息
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			// 显示提示框, 自动隐藏提示框
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if (data.code == 0) {
					// window.location.reload();
				}
			}, 2000);
		}
	);

}