package com.example.community.dao;

import com.example.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * @param userId 如果提供userId != 0,则查询用户主页该用户所发布的帖子，如果userId = 0 则显示社区首页所有发布的帖子
     * @param offset 每一页起始行的行号
     * @param limit 每一页最多显示的行数
     * */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);



    /** 动态拼接SQL语句时，如果参数只有一个，并且在<if>里使用，
     *  需要添加@Param注解为参数提供别名*/
    int selectDiscussPostRows(@Param("userId") int userId);

}
