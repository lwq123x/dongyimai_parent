package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class SearchMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        //分布式系统,以后可能消息队列在很多模块都可能加,有的模块可能是用文本格式传的,有的可能是用其他格式
        if (message instanceof TextMessage){//习惯性的在做类型转换之前做类型的比较 强制类型转换的时候先做类型的比较 instanceof
            //1.获取消息 并做类型转换
            TextMessage textMessage = (TextMessage) message;
            try {
                String itemListStr = textMessage.getText();
                //2.将获取的消息做类型转换成操作对象
                List<TbItem> itemList = JSON.parseArray(itemListStr, TbItem.class);
                //3.执行导入solr
                itemSearchService.importItem(itemList);
                System.out.println("已接收消息中间件的消息,导入solr成功!");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }


    }
}
