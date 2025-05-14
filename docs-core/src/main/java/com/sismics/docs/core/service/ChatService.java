package com.sismics.docs.core.service;

import com.sismics.docs.core.model.entity.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public ChatMessage sendMessage(String senderUsername, String receiverUsername, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setSenderUsername(senderUsername);
        message.setReceiverUsername(receiverUsername);
        message.setContent(content);
        message.setCreateDate(new Date());
        message.setRead(false);

        em.persist(message);
        return message;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String username1, String username2) {
        if (username1 == null || username2 == null) {
            throw new IllegalArgumentException("Username parameters cannot be null");
        }

        try {
            log.info("Getting messages between users: {} and {}", username1, username2);
            
            List<ChatMessage> messages = em.createQuery(
                "SELECT m FROM ChatMessage m " +
                "WHERE (m.senderUsername = :username1 AND m.receiverUsername = :username2) " +
                "OR (m.senderUsername = :username2 AND m.receiverUsername = :username1) " +
                "ORDER BY m.createDate ASC",
                ChatMessage.class)
                .setParameter("username1", username1)
                .setParameter("username2", username2)
                .getResultList();

            log.info("Found {} messages between users", messages.size());

            // 如果没有消息，创建一个默认的欢迎消息
            if (messages.isEmpty()) {
                log.info("No messages found, creating welcome message");
                createWelcomeMessage(username1, username2);
                // 重新查询消息列表
                messages = em.createQuery(
                    "SELECT m FROM ChatMessage m " +
                    "WHERE (m.senderUsername = :username1 AND m.receiverUsername = :username2) " +
                    "OR (m.senderUsername = :username2 AND m.receiverUsername = :username1) " +
                    "ORDER BY m.createDate ASC",
                    ChatMessage.class)
                    .setParameter("username1", username1)
                    .setParameter("username2", username2)
                    .getResultList();
            }

            return messages;
        } catch (Exception e) {
            log.error("Error getting messages between users: {} and {}", username1, username2, e);
            throw new RuntimeException("Failed to get messages: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void createWelcomeMessage(String username1, String username2) {
        try {
            ChatMessage welcomeMessage = new ChatMessage();
            welcomeMessage.setId(UUID.randomUUID().toString());
            welcomeMessage.setSenderUsername(username2); // 对方发送欢迎消息
            welcomeMessage.setReceiverUsername(username1);
            welcomeMessage.setContent("我们开始聊天吧！");
            welcomeMessage.setCreateDate(new Date());
            welcomeMessage.setRead(false);

            em.persist(welcomeMessage);
            em.flush(); // 确保消息被立即写入数据库
            log.info("Welcome message created successfully");
        } catch (Exception e) {
            log.error("Error creating welcome message", e);
            throw new RuntimeException("Failed to create welcome message: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void markAsRead(String messageId) {
        try {
        ChatMessage message = em.find(ChatMessage.class, messageId);
        if (message != null) {
            message.setRead(true);
            em.merge(message);
                log.info("Message {} marked as read", messageId);
            } else {
                log.warn("Message {} not found", messageId);
            }
        } catch (Exception e) {
            log.error("Error marking message as read: {}", messageId, e);
            throw new RuntimeException("Failed to mark message as read: " + e.getMessage(), e);
        }
    }
} 