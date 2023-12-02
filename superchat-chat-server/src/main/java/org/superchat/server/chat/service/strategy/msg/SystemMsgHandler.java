package org.superchat.server.chat.service.strategy.msg;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;

/**
 * Description:系统消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class SystemMsgHandler extends AbstractMsgHandler<String> {

    @Autowired
    private MessageDao messageDao;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SYSTEM;
    }

    @Override
    public void saveMsg(Message msg, String body) {
        Message update = new Message();
        update.setId(msg.getId());
        update.setContent(body);
        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getContent();
    }

    @Override
    public String showContactMsg(Message msg) {
        return msg.getContent();
    }
}
