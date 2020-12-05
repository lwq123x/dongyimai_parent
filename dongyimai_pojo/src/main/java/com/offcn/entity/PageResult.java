package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页的复合实体类
 */
public class PageResult implements Serializable {

    private List rows;  //分段查询的集合
    private Long total; //总记录数
    //注意:一旦声明有参的构造方法,就一定要声明无参的构造方法

    public PageResult() {
    }

    public PageResult(Long total,List rows) {
        this.rows = rows;
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
