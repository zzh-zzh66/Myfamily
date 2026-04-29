package com.family.myfamily.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CommentEvent extends ApplicationEvent {

    private final String type;
    private final Long commentId;
    private final Long postId;
    private final String authorName;
    private final String content;

    public CommentEvent(Object source, String type, Long commentId, Long postId, String authorName, String content) {
        super(source);
        this.type = type;
        this.commentId = commentId;
        this.postId = postId;
        this.authorName = authorName;
        this.content = content;
    }
}
