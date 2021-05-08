package com.example.gbuddy.models.entities;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
abstract class BaseEntity<T> extends BaseId<T> implements Serializable {

    @Temporal(TemporalType.DATE)
    @Column(name = "created_time")
    private Date createdTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_updated_time")
    private Date lastUpdatedTime;

    private String hostname;
}
