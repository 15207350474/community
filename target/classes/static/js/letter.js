$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	console.log("sadsad");


	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post(
		CONTEXT_PATH + "/letter/send",
		{
			"toName": toName,
			"content": content
		},
		function (data) {
			console.log(data);

			data = $.parseJSON(data); // 返回的data是普通的字符串，用jquery的parseJOSN转化为json对象
			if (data.code === 0) {
				$("#hintBody").text(data.msg);
			} else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload(); //重载当前页面
			}, 2000);
		}
	);




}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}

