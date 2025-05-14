package com.sismics.docs.core.resource;

import com.sismics.docs.core.model.entity.ChatMessage;
import com.sismics.docs.core.service.ChatService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Path("/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChatResource {
    private static final Logger log = LoggerFactory.getLogger(ChatResource.class);

    @Autowired
    private ChatService chatService;

    @GET
    @Path("/messages")
    public Response getMessages(
            @QueryParam("username1") String username1,
            @QueryParam("username2") String username2) {
        try {
            if (username1 == null || username2 == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required parameters: username1 and username2")
                        .build();
            }

            log.info("Getting messages between users: {} and {}", username1, username2);
            List<ChatMessage> messages = chatService.getMessages(username1, username2);
            return Response.ok(messages).build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid parameters: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error getting messages", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to get messages: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/send")
    public Response sendMessage(Map<String, String> request) {
        try {
            String senderUsername = request.get("senderUsername");
            String receiverUsername = request.get("receiverUsername");
            String content = request.get("content");

            if (senderUsername == null || receiverUsername == null || content == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required fields: senderUsername, receiverUsername, and content")
                        .build();
            }

            log.info("Sending message from {} to {}", senderUsername, receiverUsername);
            ChatMessage message = chatService.sendMessage(senderUsername, receiverUsername, content);
            return Response.ok(message).build();
        } catch (Exception e) {
            log.error("Error sending message", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to send message: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/read/{messageId}")
    public Response markAsRead(@PathParam("messageId") String messageId) {
        try {
            if (messageId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required parameter: messageId")
                        .build();
            }

            log.info("Marking message as read: {}", messageId);
            chatService.markAsRead(messageId);
            return Response.ok().build();
        } catch (Exception e) {
            log.error("Error marking message as read", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to mark message as read: " + e.getMessage())
                    .build();
        }
    }
} 