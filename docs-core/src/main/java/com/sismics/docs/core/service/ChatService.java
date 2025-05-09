package com.sismics.docs.core.service;

import com.sismics.docs.core.model.entity.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @PersistenceContext
    private EntityManager em;

    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void sendMessage(String senderId, String receiverId, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setCreateDate(new Date());
        message.setRead(false);

        em.persist(message);

        // Send to specific user
        messagingTemplate.convertAndSendToUser(
            receiverId,
            "/topic/private",
            message
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String userId) {
        return em.createQuery(
            "SELECT m FROM ChatMessage m WHERE m.receiverId = :userId OR m.senderId = :userId ORDER BY m.createDate DESC",
            ChatMessage.class)
            .setParameter("userId", userId)
            .getResultList();
    }

    @Transactional
    public void markAsRead(String messageId) {
        ChatMessage message = em.find(ChatMessage.class, messageId);
        if (message != null) {
            message.setRead(true);
            em.merge(message);
        }
    }
} 