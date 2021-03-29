package com.example.community.util;


public class RedisKeyUtil {

    private static final String SPLIT = ":";


    //点赞关注
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";


    //缓存登录验证码
    private static final String PREFIX_LOGIN_CODE= "loginCode";
    //缓存找回密码验证码
    private static final String PREFIX_FIND_BACK_PASSWORD_CODE = "findBackPasswordCode";
    //缓存登录凭证
    private static final String PREFIX_LOGIN_TICKET = "loginTicket";
    //缓存用户信息
    private static final String PREFIX_USER = "user";

    //缓存热门帖子
    private static final String PREFIX_HOT_POST = "hostPost";
    private static final String PREFIX_POST_ROWS = "postRows";

    //缓存帖子分数
    private static final String PREFIX_POST = "post";

    //统计网站数据
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";

    //热门帖子
    public static String getHotPostKey() {
        return PREFIX_HOT_POST;
    }

    //帖子行数
    public static String getPostRowsKey() {
        return PREFIX_POST_ROWS;
    }

    //某个实体的赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
    //某个用户的赞
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //某个用户关注的实体
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //某个实体的粉丝
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_LOGIN_CODE+ SPLIT + owner;
    }

    //用户信息
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    //登录凭证
    public static String getLoginTicketKey(String loginTicket) {
        return PREFIX_LOGIN_TICKET + SPLIT + loginTicket;
    }


    //单日uv 独立用户，访客的意思，统计IP
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    //区间uv
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    //单日DAU 日活跃用户，有效的登录用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    //区间DAU
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    //找回密码Key
    public static String getFindBackPasswordCodeKey() {
        return PREFIX_FIND_BACK_PASSWORD_CODE;
    }

}
