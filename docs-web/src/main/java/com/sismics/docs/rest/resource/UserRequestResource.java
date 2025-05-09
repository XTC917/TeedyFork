package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.dao.UserRequestDao;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.ValidationUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Date;

/**
 * User request REST resource.
 */
@Path("/userrequest")
public class UserRequestResource extends BaseResource {
    private static final Logger log = LoggerFactory.getLogger(UserRequestResource.class);

    /**
     * Creates a new user request.
     *
     * @param username Username
     * @param password Password
     * @param email Email
     * @return Response
     */
    @PUT
    public Response create(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("email") String email) {
        try {
            log.info("Creating new user request for username: {}", username);
            
            // Validate the input data
            username = ValidationUtil.validateLength(username, "username", 3, 50);
            ValidationUtil.validateUsername(username, "username");
            password = ValidationUtil.validateLength(password, "password", 8, 50);
            email = ValidationUtil.validateLength(email, "email", 1, 100);
            ValidationUtil.validateEmail(email, "email");

            // Check if username already exists
            UserDao userDao = new UserDao();
            if (userDao.getActiveByUsername(username) != null) {
                log.warn("Username {} already exists", username);
                throw new ClientException("AlreadyExistingUsername", "Username already exists");
            }

            // Create the user request
            UserRequest userRequest = new UserRequest();
            userRequest.setUsername(username);
            userRequest.setPassword(password);
            userRequest.setEmail(email);
            userRequest.setCreateDate(new Date());
            userRequest.setStatus(Constants.USER_REQUEST_STATUS_PENDING);

            UserRequestDao userRequestDao = new UserRequestDao();
            String id = userRequestDao.create(userRequest);
            log.info("Created user request with id: {}", id);

            // Always return OK
            JsonObjectBuilder response = Json.createObjectBuilder()
                    .add("status", "ok");
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            log.error("Error creating user request", e);
            throw new ServerException("UnknownError", "Error creating user request", e);
        }
    }

    /**
     * Gets all pending user requests.
     *
     * @return Response
     */
    @GET
    public Response list() {
        try {
            log.info("Listing user requests");
            
            if (!authenticate()) {
                log.warn("User not authenticated");
                throw new ForbiddenClientException();
            }
            
            log.info("Checking admin permissions for user: {}", principal.getId());
            if (!hasBaseFunction(BaseFunction.ADMIN)) {
                log.warn("User {} does not have ADMIN permission", principal.getId());
                throw new ForbiddenClientException();
            }
            log.info("User {} has ADMIN permission", principal.getId());

            // Get all pending requests
            UserRequestDao userRequestDao = new UserRequestDao();
            PaginatedList<UserRequest> paginatedList = PaginatedLists.create(100, 0);
            SortCriteria sortCriteria = new SortCriteria(1, false);
            
            try {
                log.info("Finding all pending user requests");
                userRequestDao.findAll(paginatedList, sortCriteria);
                log.info("Found {} pending requests", paginatedList.getResultCount());
            } catch (Exception e) {
                log.error("Error querying user requests: {}", e.getMessage(), e);
                throw new ServerException("DatabaseError", "Error querying user requests: " + e.getMessage(), e);
            }

            // Build the response
            JsonObjectBuilder response = Json.createObjectBuilder();
            JsonArrayBuilder requests = Json.createArrayBuilder();
            for (UserRequest userRequest : paginatedList.getResultList()) {
                try {
                    log.info("Processing request: id={}, username={}, status={}", 
                        userRequest.getId(), userRequest.getUsername(), userRequest.getStatus());
                    requests.add(Json.createObjectBuilder()
                            .add("id", userRequest.getId())
                            .add("username", userRequest.getUsername())
                            .add("email", userRequest.getEmail())
                            .add("create_date", userRequest.getCreateDate().getTime()));
                } catch (Exception e) {
                    log.error("Error processing request: {}", userRequest.getId(), e);
                }
            }
            response.add("requests", requests);

            return Response.ok().entity(response.build()).build();
        } catch (ForbiddenClientException e) {
            log.warn("Forbidden access to user requests", e);
            throw e;
        } catch (Exception e) {
            log.error("Error getting user requests: {}", e.getMessage(), e);
            throw new ServerException("UnknownError", "Error getting user requests: " + e.getMessage(), e);
        }
    }

    /**
     * Processes a user request.
     *
     * @param id Request ID
     * @param action Action (accept/reject)
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response process(
            @PathParam("id") String id,
            @FormParam("action") String action) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        checkBaseFunction(BaseFunction.ADMIN);

        // Validate the action
        if (!StringUtils.equals(action, "accept") && !StringUtils.equals(action, "reject")) {
            throw new ClientException("InvalidAction", "Invalid action");
        }

        // Get the request
        UserRequestDao userRequestDao = new UserRequestDao();
        UserRequest userRequest = userRequestDao.getById(id);
        if (userRequest == null) {
            throw new ClientException("RequestNotFound", "Request not found");
        }

        if (StringUtils.equals(action, "accept")) {
            // Create the user
            User user = new User();
            user.setRoleId(Constants.DEFAULT_USER_ROLE);
            user.setUsername(userRequest.getUsername());
            user.setPassword(userRequest.getPassword());
            user.setEmail(userRequest.getEmail());
            user.setStorageQuota(Constants.DEFAULT_STORAGE_QUOTA);
            user.setOnboarding(true);

            UserDao userDao = new UserDao();
            try {
                userDao.create(user, principal.getId());
            } catch (Exception e) {
                if ("AlreadyExistingUsername".equals(e.getMessage())) {
                    throw new ClientException("AlreadyExistingUsername", "Username already exists");
                } else {
                    throw new ServerException("UnknownError", "Unknown server error", e);
                }
            }
        }

        // Delete the request
        userRequestDao.delete(id);

        // Always return OK
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
} 