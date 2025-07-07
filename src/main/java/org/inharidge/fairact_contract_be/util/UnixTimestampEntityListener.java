package org.inharidge.fairact_contract_be.util;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.inharidge.fairact_contract_be.entity.BaseUnixTimeEntity;

import java.time.Instant;

public class UnixTimestampEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseUnixTimeEntity) {
            long now = Instant.now().getEpochSecond();
            ((BaseUnixTimeEntity) entity).setCreatedAt(now);
            ((BaseUnixTimeEntity) entity).setModifiedAt(now);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof BaseUnixTimeEntity) {
            long now = Instant.now().getEpochSecond();
            ((BaseUnixTimeEntity) entity).setModifiedAt(now);
        }
    }
}
