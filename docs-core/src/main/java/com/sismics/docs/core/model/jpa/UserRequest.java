package com.sismics.docs.core.model.jpa;

import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.util.jpa.IdentifierGenerator;

import jakarta.persistence.*;
import java.util.Date;

/**
 * User request entity.
 */
@Entity
@Table(name = "T_USER_REQUEST", schema = "PUBLIC")
@NamedQueries({
    @NamedQuery(name = "UserRequest.findAllPending", 
                query = "select u from UserRequest u where u.status = :status order by u.createDate"),
    @NamedQuery(name = "UserRequest.countPending", 
                query = "select count(u) from UserRequest u where u.status = :status")
})
public class UserRequest {
    /**
     * User request ID.
     */
    @Id
    @Column(name = "URE_ID_C", length = 36)
    private String id;

    /**
     * Username.
     */
    @Column(name = "URE_USERNAME_C", nullable = false, length = 50)
    private String username;

    /**
     * Email.
     */
    @Column(name = "URE_EMAIL_C", nullable = false, length = 100)
    private String email;

    /**
     * Password.
     */
    @Column(name = "URE_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Status (PENDING, ACCEPTED, REJECTED).
     */
    @Column(name = "URE_STATUS_C", nullable = false, length = 1)
    private String status;

    /**
     * Creation date.
     */
    @Column(name = "URE_CREATEDATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    /**
     * Process date.
     */
    @Column(name = "URE_PROCESSDATE_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processDate;

    /**
     * Process user ID.
     */
    @Column(name = "URE_PROCESSUSERID_C", length = 36)
    private String processUserId;

    public UserRequest() {
        this.id = IdentifierGenerator.generate();
        this.createDate = new Date();
        this.status = Constants.USER_REQUEST_STATUS_PENDING;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public String getProcessUserId() {
        return processUserId;
    }

    public void setProcessUserId(String processUserId) {
        this.processUserId = processUserId;
    }
} 