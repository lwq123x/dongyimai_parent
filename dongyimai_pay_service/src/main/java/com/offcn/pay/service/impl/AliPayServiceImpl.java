package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 生成支付宝支付二维码 支付宝支付接口
     * @param out_trade_no  订单号  注意:需要保证商家系统不重复。
     * @param total_fee     金额(分)
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        Map resultMap = new HashMap();
        //金额 分转元
        long total_fee_long = Long.parseLong(total_fee);
        BigDecimal total_fee_big = new BigDecimal(total_fee_long);
        BigDecimal chs = new BigDecimal(100L);
        BigDecimal money = total_fee_big.divide(chs);

        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest(); //创建API对应的request类
        request.setBizContent("{" +
                "\"out_trade_no\":\"" + out_trade_no + "\"," + //商户订单号
                "\"total_amount\":\"" + money.doubleValue() + "\"," +
                "\"subject\":\"商品01\"," +
                "\"store_id\":\"NJ_001\"," +
                "\"timeout_express\":\"90m\"}"); //订单允许的最晚付款时间
        AlipayTradePrecreateResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.print(response.getBody());

        //根据response中的结果继续业务逻辑处理
        //判断响应的状态码
        String code = response.getCode();
        System.out.println("状态码:" + code);
        if (null != code && code.equals("10000")) {
            //根据response中的结果继续业务逻辑处理
            resultMap.put("qrCode", response.getQrCode());            //二维码连接
            resultMap.put("out_trade_no", response.getOutTradeNo());  //订单号
            resultMap.put("total_fee", total_fee);                    //订单金额
        }
        return resultMap;
    }

    /**
     * 查询订单状态
     * 交易查询接口alipay.trade.query：
     * 	 获取指定订单编号的，交易状态
     *
     * @param out_trade_no 订单号  注意:需要保证商家系统不重复。
     * @return
     */
    @Override
    public Map queryPayStatus(String out_trade_no) {

        Map resultMap = new HashMap();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest(); //创建API对应的request类
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + out_trade_no + "\"," +
                "    \"trade_no\":\"\"}");  //设置业务参数
        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request); //通过alipayClient调用API，获得对应的response类
            System.out.println(response.getBody());
            String code = response.getCode();
            //根据response中的结果继续业务逻辑处理
            if (null!=code&&code.equals("10000")){
                resultMap.put("outTradeNo",response.getOutTradeNo());          //商户订单编号
                resultMap.put("tradeNo",response.getTradeNo());                //支付宝产生的交易流水号
                resultMap.put("tradeStatus",response.getTradeStatus());        //交易状态
            }

        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
