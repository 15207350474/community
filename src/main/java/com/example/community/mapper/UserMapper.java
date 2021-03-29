package com.example.community.mapper;

import com.example.community.entity.User;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface UserMapper {


    //根据id查用户
    User selectById(int id);

    //根据用户名查用户
    User selectByName(String username);

    //根据邮箱查用户
    User selectByEmail(String email);

    //保存一个用户
    void insertUser(User user);

    //更新用户状态
    void updateStatus(int id, int status);

    //更新用户头像
    int updateHeader(int id, String headerUrl);

    //更新用户密码
    int updatePassword(int id, String password);

}
