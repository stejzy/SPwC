package org.example.dataexchangesystem.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class FileUploadProgressWebSocketHandler extends TextWebSocketHandler {

    private WebSocketSession session;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.session = session;
        System.out.println("WebSocket connected: " + session.getId());
    }

    public void sendProgress(long uploaded, long total) {
        if (session != null && session.isOpen()) {
            double progress = (double) uploaded / total * 100;
            String message = "Progress: " + progress + "%";
            System.out.println("sendProgress called, wysyłam wiadomość: " + message); // Debugging log
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (session == null) {
                System.out.println("Session is null in sendProgress");
            } else if (!session.isOpen()) {
                System.out.println("Session is closed in sendProgress");
            }
        }
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Jeśli chcesz obsługiwać wiadomości z frontend, np. do wysyłania postępu na żądanie:
        String payload = message.getPayload();
        if ("requestProgress".equals(payload)) {
            // Wyślij przykładowy postęp (np. 50%):
            sendProgress(500, 1000);  // Przykladowa logika
        }
    }
}