package com.sismics.docs.rest.resource;

import com.sismics.docs.core.model.entity.ChatMessage;
import com.sismics.docs.core.service.ChatService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/chat")
public class ChatResource {

    private final ChatService chatService;

    public ChatResource(ChatService chatService) {
        this.chatService = chatService;
    }

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(Map<String, String> payload) {
        String senderId = payload.get("senderId");
        String receiverId = payload.get("receiverId");
        String content = payload.get("content");
        
        ChatMessage message = chatService.sendMessage(senderId, receiverId, content);
        return Response.ok(message).build();
    }

    @GET
    @Path("/messages/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@PathParam("username") String username) {
        List<ChatMessage> messages = chatService.getMessages(username);
        return Response.ok(messages).build();
    }

    @POST
    @Path("/read/{messageId}")
    public Response markAsRead(@PathParam("messageId") String messageId) {
        chatService.markAsRead(messageId);
        return Response.ok().build();
    }
} 