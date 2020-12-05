package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.entity.Result;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.pojo.TbPayLog;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制层
 */
@RestController
@RequestMapping("/alipay")
public class AliPayController {

    @Reference(timeout = 30000)
    private AliPayService aliPayService;

    @Autowired
    private IdWorker idWorker;

    @Reference
    private OrderService orderService;

    /**
     * 生成二维码
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis中查询支付日志
        TbPayLog tbPayLog = orderService.searchPayLogFromRedis(userId);
        //判断日志存在
        if (tbPayLog!=null){
            return aliPayService.createNative(tbPayLog.getOutTradeNo(), tbPayLog.getTotalFee()+"");
        }else {
            return new HashMap();
        }

    }

    /**
     * 查询订单状态
     * @param out_trade_no  订单号  注意:需要保证商家系统不重复
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {

        Result result = null;
        int x = 0;
        while (true) {
            //调用查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("调用查询服务出错");
            }
            if (map == null) {  //出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("tradeStatus") != null && map.get("tradeStatus").equals("TRADE_SUCCESS")) { //如果成功
                result = new Result(true,"支付成功!");
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no,map.get("tradeNo"));
                break;
            }
            if (map.get("tradeStatus")!=null&&map.get("tradeStatus").equals("TRADE_CLOSED")){   //如果成功
                result = new Result(true,"未付款交易超时关闭,或交付完成后全额退款");
                break;
            }
            if (map.get("tradeStatus")!=null&&map.get("tradeStatus").equals("TRADE_FINISHED")){  //如果成功
                result = new Result(true,"交易结束,不可退款");
            }


            try {
                Thread.sleep(3000);   //间隔3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //为了不让循环无休止地运行，定义一个循环变量，如果这个变量超过了这个值则退出循环，设置时间为5分钟
            x++;
            if (x>=100){
                result = new Result(false,"二维码超时");
                break;
            }

        }
        return result;

    }
}
