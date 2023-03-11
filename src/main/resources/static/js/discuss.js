function like(btn, entityType, entityId, entityUserId, post_id) {
    $.post(
        CONTEXT_PATH + "/like",
        {entity_type: entityType, entity_id:entityId, entity_user_id:entityUserId, postId:post_id},
        function (data) {
            data = $.parseJSON(data);
            if (data .code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?'已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    );

}