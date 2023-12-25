package com.mainthreadlab.weinv.model.base;

import org.springframework.stereotype.Component;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Component
public class BaseEntityListener {

    private static final String SYSTEM = "System";

    @PrePersist
    public void setCreationDate(BaseEntity baseEntity) {
        baseEntity.setCreatedAt(new Date());
        baseEntity.setCreatedBy(SYSTEM);
        baseEntity.setUpdatedBy(SYSTEM);
        baseEntity.setUpdatedAt(new Date());
    }

    @PreUpdate
    public void setUpdateDate(BaseEntity baseEntity) {
        baseEntity.setUpdatedBy(SYSTEM);
        baseEntity.setUpdatedAt(new Date());
    }

}
