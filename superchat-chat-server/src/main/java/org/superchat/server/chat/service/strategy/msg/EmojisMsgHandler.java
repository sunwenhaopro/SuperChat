package org.superchat.server.chat.service.strategy.msg;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.entity.msg.EmojisMsgDTO;
import org.superchat.server.chat.domain.entity.msg.MessageExtra;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;

import javax.annotation.Resource;
import java.util.Optional;


@Component
public class EmojisMsgHandler extends AbstractMsgHandler<EmojisMsgDTO>{
    @Resource
    private  MessageDao messageDao;


    @Override
    MessageTypeEnum getMsgTypeEnum() {
        return MessageTypeEnum.EMOJI;
    }

    @Override
    protected void saveMsg(Message message, EmojisMsgDTO body) {
        MessageExtra extra= Optional.ofNullable(message.getExtra()).orElse(new MessageExtra());
        Message update=new Message();
        message.setId(message.getId());
        message.setExtra(extra);
        extra.setEmojisMsgDTO(body);
        messageDao.updateById(message);
    }



    @Override
    public Object showMsg(Message msg) {
        return "表情";
    }

    @Override
    public Object showReplyMsg(Message msg) {
        return msg.getExtra().getEmojisMsgDTO();
    }

    @Override
    public String showContactMsg(Message msg) {
        return "[表情包]";
    }
}
