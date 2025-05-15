package com.sismics.docs.rest.resource;

import com.sismics.docs.core.model.entity.ChatMessage;
import com.sismics.docs.core.service.ChatService;
import com.sismics.rest.exception.ForbiddenClientException;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Path("/chat")
public class ChatResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(ChatResource.class);

    private final ChatService chatService = new ChatService();


    @GET
    @Path("/messages")
    public Response getMessages(
            @QueryParam("username1") String username1,
            @QueryParam("username2") String username2) {
        try {
            if (!authenticate()) {
                throw new ForbiddenClientException();
            }

            if (username1 == null || username2 == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required parameters: username1 and username2")
                        .build();
            }
            log.info("Getting messages between users: {} and {}", username1, username2);
            List<ChatMessage> messages = chatService.getMessages(username1, username2);
            JsonArrayBuilder comments = Json.createArrayBuilder();
            int count = 0;
            for (ChatMessage message : messages) {
                count ++;
                comments.add(Json.createObjectBuilder()
                        .add("id", message.getId())
                        .add("senderUsername", message.getSenderUsername())
                        .add("receiverUsername", message.getReceiverUsername())
                        .add("content", message.getContent())
                        .add("createDate", message.getCreateDate() + "")
                        .add("read", message.isRead() + ""));
            }

            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("chatMessages", comments)
                    .add("count", count);
            return Response.ok().entity(response.build()).build();
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
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMessage(
            @FormParam("senderUsername") String senderUsername,
            @FormParam("receiverUsername") String receiverUsername,
            @FormParam("content") String content) {
        try {
            log.info("sendMessage senderUsername: {} ,receiverUsername {} ,receiverUsername {}", senderUsername, receiverUsername, content);

            if (senderUsername == null || receiverUsername == null || content == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Missing required fields: senderUsername, receiverUsername, and content")
                        .build();
            }

            log.info("Sending message from {} to {}", senderUsername, receiverUsername);
            ChatMessage message = chatService.sendMessage(senderUsername, receiverUsername, content);
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("id", message.getId())
                    .add("senderUsername", message.getSenderUsername())
                    .add("receiverUsername", message.getReceiverUsername())
                    .add("content", message.getContent())
                    .add("createDate", message.getCreateDate() + "")
                    .add("read", message.isRead() + "");
            return Response.ok().entity(response.build()).build();
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