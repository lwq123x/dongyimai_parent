package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.pojo.TbPayLog;

import java.util.List;

public interface PayLogService {

    /**
     * 查询全部支付日志
     * @return
     */
    public List<TbPayLog> findAllPayLog();

    /**
     * 返回分页列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage( int pageNum, int pageSize);
}
