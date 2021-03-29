$(function () { //页面加载完以后绑定事件

    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful)
    $("#deleteBtn").click(setDelete)
});


function setTop() {
    $.post(
        CONTEXT_PATH + "/top",
        {
            "id": $("#discussPostId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#topBtn").attr("disabled", "disabled") //操作成功把按钮改为不可用
            } else {
                alert(data.msg);
            }

        }
    );
}

function setWonderful() {
    $.post(
        CONTEXT_PATH + "/wonderful",
        {
            "id": $("#discussPostId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $("#wonderfulBtn").attr("disabled", "disabled") //操作成功把按钮改为不可用
            } else {
                alert(data.msg);
            }

        }
    );
}

function setDelete() {
    $.post(
        CONTEXT_PATH + "/delete",
        {
            "id": $("#discussPostId").val()
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                location.href = CONTEXT_PATH + "/index"; 删除成功跳回主页
            } else {
                alert(data.msg);
            }

        }
    );
}





function like(btn, entityType, entityId, entityUserId, discussPostId) {

    $.post(
        CONTEXT_PATH + "/like",
        {
            "entityType": entityType,
            "entityId": entityId,
            "entityUserId": entityUserId,
            "discussPostId": discussPostId
        },
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? '已赞' : '赞');


            } else {
                alert(data.msg);
            }

        }
    );
}