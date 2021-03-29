package com.example.community.entity;


import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    private int id;
    private int userId;
    private int entityId;
    private int entityType;
    private int targetId;
    private Date createTime;
    private String content;
    private int status;

}
