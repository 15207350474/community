package com.example.community.entity;


import lombok.Data;

import java.util.Date;

@Data
public class Message {

    private int id;
    private int fromId;
    private int toId;
    private String content;
    private int status;
    private Date createTime;
    private String coversationId;
}
