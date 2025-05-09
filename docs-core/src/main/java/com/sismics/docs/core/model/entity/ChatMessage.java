package com.sismics.docs.core.model.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "T_CHAT_MESSAGE")
public class ChatMessage {
    @Id
    @Column(name = "MSG_ID_C", length = 36)
    private String id;

    @Column(name = "MSG_SENDER_ID_C", length = 36, nullable = false)
    private String senderId;

    @Column(name = "MSG_RECEIVER_ID_C", length = 36, nullable = false)
    private String receiverId;

    @Column(name = "MSG_CONTENT_C", nullable = false)
    private String content;

    @Column(name = "MSG_CREATEDATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "MSG_READ_B", nullable = false)
    private boolean read;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
} 