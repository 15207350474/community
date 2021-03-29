$(function () {
    $("#uv").click(uv);
    $("#dau").click(dau);
})


function uv() {

    //前端验证
    var start = $("#uvStart").val();
    var end = $("#uvEnd").val();
    if (start == '' || end == '') {
        alert("请先填写日期!");
    } else if (start > end) {
        alert("日期前后设置错误!");
    } else {
        $.post(
            CONTEXT_PATH + "/data/uv",
            {
                start: start,
                end: end
            },
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    $("#uvResult").text(data.uvResult);
                } else {
                    alert(msg);
                }
            }
        )
    }


}


function dau() {

    //前端验证
    var start = $("#dauStart").val();
    var end = $("#dauEnd").val();
    if (start == '' || end == '') {
        alert("请先填写日期!");
    } else if (start > end) {
        alert("日期前后设置错误!");
    } else {
        $.post(
            CONTEXT_PATH + "/data/dau",
            {
                start: $("#dauStart").val(),
                end: $("#dauEnd").val()
            },
            function (data) {
                data = $.parseJSON(data);
                if (data.code == 0) {
                    $("#dauResult").text(data.dauResult);
                } else {
                    alert(msg);
                }
            }
        )
    }
}