package com.example.community.entity;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
@Data
@Accessors(chain = true)
@ToString
public class Event {
    private String topic;
    private int userId;
    private int entityType;
    private int entityId;
    private int entityUserId;

    private Map<String, Object> data = new HashMap<>(); // 使事件更具扩展性，以后需要扩展能够增加事件属性可以存入map中

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }
}
