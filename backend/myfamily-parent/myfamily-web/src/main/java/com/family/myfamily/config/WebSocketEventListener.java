package com.family.myfamily.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.family.myfamily.event.CommentEvent;
import com.family.myfamily.event.PostEvent;
import com.family.myfamily.websocket.MyWebSocketHandler;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MyWebSocketHandler webSocketHandler;

    @Async
    @EventListener
    public void handlePostEvent(PostEvent event) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", event.getType());
            message.put("data", Map.of(
                "postId", event.getPostId(),
                "familyId", event.getFamilyId(),
                "authorName", event.getAuthorName(),
                "title", event.getTitle()
            ));
            message.put("timestamp", System.currentTimeMillis());

            webSocketHandler.broadcastMessage(message);
            log.info("广播动态事件: type={}, postId={}", event.getType(), event.getPostId());
        } catch (Exception e) {
            log.error("广播动态事件失败: {}", e.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleCommentEvent(CommentEvent event) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", event.getType());
            message.put("data", Map.of(
                "commentId", event.getCommentId(),
                "postId", event.getPostId(),
                "authorName", event.getAuthorName(),
                "content", event.getContent()
            ));
            message.put("timestamp", System.currentTimeMillis());

            webSocketHandler.broadcastMessage(message);
            log.info("广播评论事件: type={}, postId={}", event.getType(), event.getPostId());
        } catch (Exception e) {
            log.error("广播评论事件失败: {}", e.getMessage());
        }
    }
}
