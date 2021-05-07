package com.example.gbuddy.models.entities;

import java.util.Date;

abstract class BaseUser<T,V> {
    public V id;

    public Date lastLoginTime;

    public Date createdTime;

    public Date lastupdatedTime;

}
