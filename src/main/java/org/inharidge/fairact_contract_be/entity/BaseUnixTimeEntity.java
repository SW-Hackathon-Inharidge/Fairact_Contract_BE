package org.inharidge.fairact_contract_be.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
public abstract class BaseUnixTimeEntity {

    @Field("created_at")
    private Long createdAt;

    @Field("modified_at")
    private Long modifiedAt;
}


