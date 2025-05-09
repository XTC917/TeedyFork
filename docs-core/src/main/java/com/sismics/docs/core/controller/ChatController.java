package com.sismics.docs.core.controller;

import com.sismics.docs.core.model.entity.ChatMessage;
import com.sismics.docs.core.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, String> payload) {
        String senderId = payload.get("senderId");
        String receiverId = payload.get("receiverId");
        String content = payload.get("content");
        
        chatService.sendMessage(senderId, receiverId, content);
    }

    @SubscribeMapping("/user/queue/messages")
    public List<ChatMessage> getMessages(String userId) {
        return chatService.getMessages(userId);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload String messageId) {
        chatService.markAsRead(messageId);
    }
} 