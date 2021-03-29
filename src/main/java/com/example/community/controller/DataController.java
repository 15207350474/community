package com.example.community.controller;


import com.example.community.service.DataService;
import com.example.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 统计网站数据 (异步请求方式)
 */

@Controller
public class DataController {

    @Autowired
    private DataService dataService;


    //打开统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }


    //统计访问量
    @PostMapping("/data/uv")
    @ResponseBody
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        Map<String, Object> map = new HashMap<>();
        map.put("uvResult", dataService.calculateUV(start, end));
        map.put("uvStart", start);
        map.put("uvEnd", end);
        return CommunityUtil.getJsonString(0, null, map);
    }

    //统计活跃用户
    @PostMapping("/data/dau")
    @ResponseBody
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd") Date end) {
        Map<String, Object> map = new HashMap<>();
        map.put("dauResult", dataService.calculateUV(start, end));
        map.put("dauStart", start);
        map.put("dauEnd", end);
        return CommunityUtil.getJsonString(0, null, map);
    }

}
