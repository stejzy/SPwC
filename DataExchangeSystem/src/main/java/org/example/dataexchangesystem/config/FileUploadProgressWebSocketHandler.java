package org.example.dataexchangesystem.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class FileUploadProgressWebSocketHandler extends TextWebSocketHandler {

    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        this.session = session;
    }

    public void sendProgress(long uploaded, long total) {
        if (session != null && session.isOpen()) {
            double progress = (double) uploaded / total * 100;
            try {
                session.sendMessage(new TextMessage("Progress: " + progress + "%"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
