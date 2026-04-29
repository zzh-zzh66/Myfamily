package com.family.myfamily.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 存储所有活跃的WebSocket连接，key为userId
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("WebSocket连接建立: userId={}", userId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.info("收到消息: userId={}, payload={}", userId, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("WebSocket连接关闭: userId={}", userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket传输错误: userId={}", userId, exception);
    }

    /**
     * 向指定用户发送消息
     */
    public void sendMessageToUser(Long userId, Object message) throws IOException {
        WebSocketSession session = SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
        }
    }

    /**
     * 向所有用户广播消息
     */
    public void broadcastMessage(Object message) throws IOException {
        String payload = objectMapper.writeValueAsString(message);
        for (WebSocketSession session : SESSIONS.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(payload));
            }
        }
    }

    /**
     * 向指定家族的所有成员发送消息
     */
    public void sendMessageToFamily(Long familyId, Object message) throws IOException {
        String payload = objectMapper.writeValueAsString(message);
        // 这里需要查询家族成员，暂时简化为广播
        broadcastMessage(message);
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("userId=")) {
                        return Long.parseLong(param.split("=")[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析userId失败", e);
        }
        return null;
    }
}
