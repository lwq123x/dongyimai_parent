package com.offcn.sellergoods.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.mapper.TbPayLogMapper;
import com.offcn.pojo.TbPayLog;
import com.offcn.sellergoods.service.PayLogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PayLogServiceImpl implements PayLogService {


    @Autowired
    private TbPayLogMapper tbPayLogMapper;

    /**
     * 查询全部支付日志
     * @return
     */
    @Override
    public List<TbPayLog> findAllPayLog() {
        return tbPayLogMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage( int pageNum, int pageSize) {

        PageHelper.startPage(pageNum,pageSize);
        Page<TbPayLog> page = (Page<TbPayLog>) tbPayLogMapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
