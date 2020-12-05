package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {

    /**
     * 生成支付宝支付二维码 支付宝支付接口
     * @param out_trade_no  订单号  注意:需要保证商家系统不重复。
     * @param total_fee     金额(分)
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询订单支付状态
     * @param out_trade_no  订单号  注意:需要保证商家系统不重复。
     * @return
     */
    public Map queryPayStatus(String out_trade_no);
}
