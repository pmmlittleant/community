package com.example.community.dao.impl;

import com.example.community.dao.AlphaDao;
import org.springframework.stereotype.Repository;


@Repository("alphaHibernate")
public class AlphaDaoImpl implements AlphaDao {

    @Override
    public String select() {
        return "hibernate";
    }
}
