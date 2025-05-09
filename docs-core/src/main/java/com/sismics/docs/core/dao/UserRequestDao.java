package com.sismics.docs.core.dao;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.model.jpa.UserRequest;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * User request DAO.
 */
public class UserRequestDao {
    private static final Logger log = LoggerFactory.getLogger(UserRequestDao.class);

    /**
     * Creates a new user request.
     *
     * @param userRequest User request
     * @return New ID
     */
    public String create(UserRequest userRequest) {
        // Create the UUID
        userRequest.setId(UUID.randomUUID().toString());
        
        // Create the user request
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(userRequest);
        
        return userRequest.getId();
    }
    
    /**
     * Gets a user request by its ID.
     *
     * @param id User request ID
     * @return User request
     */
    public UserRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(UserRequest.class, id);
    }
    
    /**
     * Gets all pending user requests.
     *
     * @return List of user requests
     */
    public List<UserRequest> getPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select u from UserRequest u where u.status = 'P' order by u.createDate");
        return q.getResultList();
    }
    
    /**
     * Finds all user requests with pagination and sorting.
     *
     * @param paginatedList Paginated list
     * @param sortCriteria Sort criteria
     */
    public void findAll(PaginatedList<UserRequest> paginatedList, SortCriteria sortCriteria) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        try {
            log.info("Finding all user requests with status: {}", Constants.USER_REQUEST_STATUS_PENDING);
            
            // Build the query
            StringBuilder sb = new StringBuilder("select u from UserRequest u where u.status = :status");
            
            // Add sort criteria
            if (sortCriteria != null) {
                sb.append(" order by u.createDate");
                if (!sortCriteria.isAsc()) {
                    sb.append(" desc");
                }
            }
            
            String queryString = sb.toString();
            log.debug("Executing query: {}", queryString);
            
            // Perform the search
            Query q = em.createQuery(queryString);
            q.setParameter("status", Constants.USER_REQUEST_STATUS_PENDING);
            
            // Set pagination
            q.setFirstResult(paginatedList.getOffset());
            q.setMaxResults(paginatedList.getLimit());
            
            // Get results
            @SuppressWarnings("unchecked")
            List<UserRequest> userRequestList = q.getResultList();
            log.info("Found {} user requests", userRequestList.size());
            paginatedList.setResultList(userRequestList);
            
            // Get total count
            Query qCount = em.createQuery("select count(u) from UserRequest u where u.status = :status");
            qCount.setParameter("status", Constants.USER_REQUEST_STATUS_PENDING);
            Long count = (Long) qCount.getSingleResult();
            log.info("Total count: {}", count);
            paginatedList.setResultCount(count.intValue());
        } catch (Exception e) {
            log.error("Error finding user requests: {}", e.getMessage(), e);
            throw new RuntimeException("Error finding user requests: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates a user request.
     *
     * @param userRequest User request
     */
    public void update(UserRequest userRequest) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(userRequest);
    }
    
    /**
     * Deletes a user request.
     *
     * @param id User request ID
     */
    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        UserRequest userRequest = em.find(UserRequest.class, id);
        em.remove(userRequest);
    }
} 