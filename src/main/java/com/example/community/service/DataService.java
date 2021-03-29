package com.example.community.service;


import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * 统计UV和DAU，统计网站的访客和日活跃用户
 */
@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    //以当前时间为key, 把访问的ip地址存入redis
    public void recordUV(String ip) {
        String uvKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey, ip);

    }

    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        List<String> keyList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getUVKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }
        String uvKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey, keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(uvKey);

    }

    public void recordDAU(int userId) {
        String duvKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(duvKey, userId, true);
    }

    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        List<byte[]> keyList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtil.getDAUKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }
        return (long)redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String dUvKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, dUvKey.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(dUvKey.getBytes());
            }
        });
    }



}
