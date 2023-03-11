package com.example.community.dao;

import com.example.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;


@Mapper
@Deprecated //不推荐使用的组件
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId}, #{ticket},#{status},#{expired})"
    })
    //声明SQL相关的机制使用Options,插入数据后回填自动生成的主键值到实体类对应属性上
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select("select * from login_ticket where ticket=#{ticket}")
    LoginTicket selectByTicket(String ticket);

    //在注解中实现动态SQL（if条件拼接sql语句）则需要加上<script>标签，使用if标签判断是否需要拼接语句。ps:test=""需要加转义符号\"\"
    @Update({
            "<script>",
            "update login_ticket set status = #{status} where ticket = #{ticket} ",
            "<if test=\"ticket!=null\">",
                "and 1=1",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
