package com.mainthreadlab.weinv.model.base;


import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Getter
@Setter
@ToString
@NoArgsConstructor
@EntityListeners({BaseEntityListener.class})
public abstract class BaseEntity implements Serializable {

    @Column(name = "created_by", nullable = false, length = 50)
    @NotNull
    @EqualsAndHashCode.Exclude
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    @NotNull
    @EqualsAndHashCode.Exclude
    private Date createdAt;

    @Column(name = "updated_by", nullable = false, length = 50)
    @NotNull
    @EqualsAndHashCode.Exclude
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    @NotNull
    @EqualsAndHashCode.Exclude
    private Date updatedAt;


}
