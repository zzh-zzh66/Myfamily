package com.family.myfamily.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PostEvent extends ApplicationEvent {

    private final String type;
    private final Long postId;
    private final Long familyId;
    private final String authorName;
    private final String title;

    public PostEvent(Object source, String type, Long postId, Long familyId, String authorName, String title) {
        super(source);
        this.type = type;
        this.postId = postId;
        this.familyId = familyId;
        this.authorName = authorName;
        this.title = title;
    }
}
