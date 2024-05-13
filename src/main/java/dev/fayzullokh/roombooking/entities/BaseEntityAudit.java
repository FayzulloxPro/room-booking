package dev.fayzullokh.roombooking.entities;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntityAudit extends BaseEntity implements Serializable {
    private String createdBy;
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    public void onCreate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.createdBy = authentication == null ? "system" : authentication.getName();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.updatedBy = authentication == null ? "system" : authentication.getName();
        this.updatedAt = new Date();
    }
}