package com.example.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String headerUrl;
    private String email;
    private String salt;
    private int type; //用户类型 0-普通用户; 1-管理员; 2-版主;
    private int status; //用户状态 0-未激活; 1-已激活;
    private String activationCode;
    private Date createTime;


}
