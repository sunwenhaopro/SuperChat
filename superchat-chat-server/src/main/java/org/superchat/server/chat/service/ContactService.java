package org.superchat.server.chat.service;

import org.superchat.server.chat.domain.dto.MsgReadInfoDTO;
import org.superchat.server.chat.domain.entity.Contact;
import org.superchat.server.chat.domain.entity.Message;

import java.util.List;
import java.util.Map;

public interface ContactService {
    Map<Long, MsgReadInfoDTO> getMsgReadInfo(List<Message> messages);
    Contact createContact(Long uid, Long roomId);
    Integer getMsgReadCount(Message message);
    Integer getMsgUnReadCount(Message message);
}
