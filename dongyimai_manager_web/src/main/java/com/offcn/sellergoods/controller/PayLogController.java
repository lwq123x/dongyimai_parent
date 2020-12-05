package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.PageResult;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbPayLog;
import com.offcn.sellergoods.service.PayLogService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/paylog")
public class PayLogController {

    @Reference
    private PayLogService payLogService;

    /**
     * 查询全部支付日志
     * @return
     */
    @RequestMapping("/findAllPayLog")
    public List<TbPayLog> findAllPayLog(){
        return payLogService.findAllPayLog();
    }

    /**
     * 按分页查询
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows){
        return payLogService.findPage(page,rows);
    }

    /*public PageResult search(@RequestBody TbPayLog payLog, int page, int rows) {
        return payLogService.findPage(payLog, page, rows);
    }*/
}
