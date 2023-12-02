package org.superchat.server.common.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.superchat.server.common.constant.MQConstant;
import org.superchat.server.common.domain.dto.PushMessageDTO;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.transaction.util.MqProducer;

import java.util.List;

@Service
@AllArgsConstructor
public class PushService {
    private final MqProducer mqProducer;
    public void sendPushMsg(WSBaseResp<?> msg, List<Long> uidList)
    {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC,new PushMessageDTO(uidList,msg));
    }
    public void sendPushMsg(WSBaseResp<?> msg,Long uid)
    {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC,new PushMessageDTO(uid,msg));
    }
    public void sendPushMsg(WSBaseResp<?> msg)
    {
        mqProducer.sendMsg(MQConstant.PUSH_TOPIC,new PushMessageDTO(msg));
    }
}
