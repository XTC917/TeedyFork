package com.sismics.docs.core.service;

import com.sismics.docs.core.model.entity.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public ChatMessage sendMessage(String senderId, String receiverId, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setCreateDate(new Date());
        message.setRead(false);

        em.persist(message);
        return message;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String username) {
        return em.createQuery(
            "SELECT m FROM ChatMessage m WHERE m.receiverId = :username OR m.senderId = :username ORDER BY m.createDate DESC",
            ChatMessage.class)
            .setParameter("username", username)
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