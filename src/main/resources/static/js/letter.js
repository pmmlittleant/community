$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var to_name = $("#recipient-name").val()
	var letter_content = $("#message-text").val()
	$.post (
		CONTEXT_PATH + "/letter/send",
		{toName : to_name, content : letter_content},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0) {
				$("#hintBody").text("发送成功！");
			} else {
				$("#hintBody").text("发送失败！");
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);

}

function delete_msg() {
	$(this).parents(".media").remove();
}