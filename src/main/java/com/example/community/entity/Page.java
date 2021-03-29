package com.example.community.entity;


import lombok.Data;


/**
 * 封装分页相关信息
 */

@Data
public class Page {
    //当前页码
    private int current = 1;
    //每页显示条数
    private int limit = 10;
    //数据总数(用于计算总页数)
    private int rows;
    //查询路径(用户复用分页路径)
    private String path;


    //获取当前页的起始行
    public int getOffset() {
        return current * limit - limit;
    }

    //分页列表起点
    public int getFrom() {
        return Math.max(current - 2, 1);
    }

    //分页列表终点
    public int getTo() {
        return Math.min(current + 2, getTotalPage());
    }

    //获取总页数
    public int getTotalPage() {
        if (rows % limit == 0) return rows / limit;
        else return rows / limit + 1;
    }

}
