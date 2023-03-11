package com.example.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";

    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA ="kaptcch";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";


    // 某个实体的赞
    // like:entityEntityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId ->int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某用户关注的实体
    // followee:userId:entityType -> zset(entityId, now) //使用有序的集合储存某用户关注的对象实体的Id, 以关注时间作为分数：可以满足按关注先后顺序列举出来的需求
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    //用户在登录前还没无法获得用户身份的信息，所以对于该验证码，我们加上一个owner字符串作为凭证发到用户浏览器的Cookie中（用于短期标识使用该验证码的用户）
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
