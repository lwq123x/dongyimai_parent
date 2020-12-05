package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class DeleteMessageListenerImpl implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                //删除静态页面操作
                itemPageService.deleteItemHtml(ids);
                System.out.println("已接收消息队列的消息,删除"+ Arrays.asList(ids) +"页面");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
