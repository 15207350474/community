package com.example.community.entity;


import lombok.Data;

import java.util.Date;

/**
 * 登录凭证实体类
 */

@Data
public class LoginTicket {
    private int id;
    private int userId;
    private int status;
    private String ticket;
    private Date expired;
}
