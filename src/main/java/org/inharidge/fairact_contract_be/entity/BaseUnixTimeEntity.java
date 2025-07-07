package org.inharidge.fairact_contract_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.inharidge.fairact_contract_be.util.UnixTimestampEntityListener;

@MappedSuperclass
@EntityListeners(UnixTimestampEntityListener.class)
public abstract class BaseUnixTimeEntity {

    @Column(name = "created_at", updatable = false)
    private Long createdAt;

    @Column(name = "modified_at")
    private Long modifiedAt;

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getModifiedAt() {
        return modifiedAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setModifiedAt(Long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}

