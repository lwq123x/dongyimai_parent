package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                Long goodsId = Long.parseLong(textMessage.getText());
                itemPageService.genItemPage(goodsId);
                System.out.println("已接收消息队列中的消息,完成生成静态页面");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
