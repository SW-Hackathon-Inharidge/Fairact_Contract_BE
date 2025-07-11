package org.inharidge.fairact_contract_be.util;

import org.inharidge.fairact_contract_be.entity.BaseUnixTimeEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class UnixTimestampEntityListener extends AbstractMongoEventListener<BaseUnixTimeEntity> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<BaseUnixTimeEntity> event) {
        BaseUnixTimeEntity entity = event.getSource();
        long now = System.currentTimeMillis() / 1000L;

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setModifiedAt(now);
    }
}
