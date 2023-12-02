package org.superchat.server.chat.service.strategy.msg;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.entity.msg.MessageExtra;
import org.superchat.server.chat.domain.entity.msg.SoundMsgDTO;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;

import java.util.Optional;

/**
 * Description:图片消息
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Component
public class SoundMsgHandler extends AbstractMsgHandler<SoundMsgDTO> {
    @Autowired
    private MessageDao messageDao;

    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.SOUND;
    }

    @Override
    public void saveMsg(Message msg, SoundMsgDTO body) {
        MessageExtra extra = Optional.ofNullable(msg.getExtra()).orElse(new MessageExtra());
        Message update = new Message();
        update.setId(msg.getId());
        update.setExtra(extra);
        extra.setSoundMsgDTO(body);
        messageDao.updateById(update);
    }

    @Override
    public Object showMsg(Message msg) {
        return msg.getExtra().getSoundMsgDTO();
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return "语音";
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[语音]";
    }
}
