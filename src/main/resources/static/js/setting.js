$(function () {
    $("#upload").submit(upload);
});


function upload() {
    $.ajax({
        url: "http://upload-z2.qiniup.com",
        method: "post",
        processData: false,  //不要把表单的内容转成字符串，这是上传文件
        contentType: false,
        data: new FormData($("#upload")[0]),
        success: function (data) {
            if (data && data.code == 0) {
                $.post(
                    CONTEXT_PATH + "/update/header",
                    {
                        fileName: $("input[name='key']").val()
                    },
                    function (data) {
                        data = $.parseJSON(data);
                        if (data.code == 0) {
                            window.location.reload();
                        } else {
                            alert(data.msg);
                        }

                    }
                );
                alert("修改头像成功！")
            } else {
                alert("上传失败！")
            }
        }



    });
    return false;
}