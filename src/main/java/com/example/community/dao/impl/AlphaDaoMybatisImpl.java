package com.example.community.dao.impl;

import com.example.community.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao {


    @Override
    public String select() {
        return "Mybatis";
    }
}
