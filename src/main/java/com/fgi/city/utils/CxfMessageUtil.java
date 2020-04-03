package com.fgi.city.utils;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;

import java.util.List;

public class CxfMessageUtil {

    // 获取webservice参数
    public MessageContentsList getContentsList(Message msg) {
        List<Object> o = CastUtils.cast(msg.getContent(List.class));
        if (o == null) {
            return null;
        }
        if (!(o instanceof MessageContentsList)) {
            MessageContentsList messageContentsList = new MessageContentsList(o);
            msg.setContent(List.class, messageContentsList);
            return messageContentsList;
        }
        return (MessageContentsList) o;
    }
}
