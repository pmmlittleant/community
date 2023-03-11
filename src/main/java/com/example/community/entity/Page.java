package com.example.community.entity;

import lombok.Data;
import lombok.Setter;

/**
 * 封装分页相关的信息。
 * */
@Data
public class Page {

    //当前页码
    private int current = 1;
    //显示上限
    private int limit = 10;

    //数据总数（用于计算总页数: 总数 / limit）
    private int rows;

    //查询路径（复用分页链接）
    private String path;

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行
     * @return
     */
    public int getOffSet() {
        // current * limit - limit
        return (current - 1) * limit;
    }
    /**
     * 获取总页数
     *@return
     */
    public int getTotal() {
        // rows / limit [+1]
        if (rows % limit == 0) return rows / limit;
        else return rows / limit + 1;
    }

    /**
     * 获取起始页码
     * @return
     * */
    public int getFrom() {
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取终止页码
     * @return
     * */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total ? total : to;
    }
}

