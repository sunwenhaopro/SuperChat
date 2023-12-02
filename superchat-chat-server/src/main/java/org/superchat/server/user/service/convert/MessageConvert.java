package org.superchat.server.user.service.convert;

import cn.hutool.core.bean.BeanUtil;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.entity.MessageMark;
import org.superchat.server.chat.domain.enums.MessageMarkTypeEnum;
import org.superchat.server.chat.domain.enums.MessageStatusEnum;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;
import org.superchat.server.chat.domain.vo.request.ChatMessageReq;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.chat.domain.vo.response.msg.TextMsgResp;
import org.superchat.server.chat.service.strategy.msg.AbstractMsgHandler;
import org.superchat.server.chat.service.strategy.msg.MsgHandlerFactory;
import org.superchat.server.common.domain.enums.YesOrNoEnum;
import org.superchat.server.user.domain.entity.User;

import java.util.*;
import java.util.stream.Collectors;

public class MessageConvert {
    public static final int CAN_CALLBACK_GAP_COUNT = 100;

    public static ChatMessageReq toAgreeMsg(Long roomId) {
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        TextMsgResp textMsgResp=new TextMsgResp();
        textMsgResp.setContent("我们已经是好友了，开始聊天吧");
        chatMessageReq.setRoomId(roomId);
        chatMessageReq.setMsgType(MessageTypeEnum.TEXT.getType());
        chatMessageReq.setBody(textMsgResp);
        return chatMessageReq;
    }

    public static List<ChatMessageResp> toChatMessageResp(List<Message> msgList, List<MessageMark> marks, Long uid) {
        Map<Long, List<MessageMark>> markMap = marks.stream().collect(Collectors.groupingBy(MessageMark::getMsgId));
        return msgList.stream().map(a -> {
                    ChatMessageResp resp = new ChatMessageResp();
                    resp.setFromUser(buildFromUser(a.getFromUid()));
                    resp.setMessage(buildMessage(a, markMap.getOrDefault(a.getId(), new ArrayList<>()), uid));
                    return resp;
                })
                .sorted(Comparator.comparing(a -> a.getMessage().getSendTime()))//帮前端排好序，更方便它展示
                .collect(Collectors.toList());
    }
    private static ChatMessageResp.Message buildMessage(Message message, List<MessageMark> marks, Long receiveUid) {
        ChatMessageResp.Message messageVO = new ChatMessageResp.Message();
        BeanUtil.copyProperties(message, messageVO);
        messageVO.setSendTime(message.getCreateTime());
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategy(message.getType());
        if (Objects.nonNull(msgHandler)) {
            messageVO.setBody(msgHandler.showMsg(message));
        }
        //消息标记
        messageVO.setMessageMark(buildMsgMark(marks, receiveUid));
        return messageVO;
    }

    private static ChatMessageResp.MessageMark buildMsgMark(List<MessageMark> marks, Long receiveUid) {
        Map<Integer, List<MessageMark>> typeMap = marks.stream().collect(Collectors.groupingBy(MessageMark::getType));
        List<MessageMark> likeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.LIKE.getType(), new ArrayList<>());
        List<MessageMark> dislikeMarks = typeMap.getOrDefault(MessageMarkTypeEnum.DISLIKE.getType(), new ArrayList<>());
        ChatMessageResp.MessageMark mark = new ChatMessageResp.MessageMark();
        mark.setLikeCount(likeMarks.size());
        mark.setUserLike(Optional.ofNullable(receiveUid).filter(uid -> likeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid))).map(a -> YesOrNoEnum.YES.getStatus()).orElse(YesOrNoEnum.NO.getStatus()));
        mark.setDislikeCount(dislikeMarks.size());
        mark.setUserDislike(Optional.ofNullable(receiveUid).filter(uid -> dislikeMarks.stream().anyMatch(a -> Objects.equals(a.getUid(), uid))).map(a -> YesOrNoEnum.YES.getStatus()).orElse(YesOrNoEnum.NO.getStatus()));
        return mark;
    }

    private static ChatMessageResp.UserInfo buildFromUser(Long fromUid) {
        ChatMessageResp.UserInfo userInfo = new ChatMessageResp.UserInfo();
        userInfo.setUid(fromUid);
        return userInfo;
    }

    public static Message toMsgSave(ChatMessageReq request, Long uid) {
        Message message = new Message();
        message.setRoomId(request.getRoomId());
        message.setFromUid(uid);
        message.setStatus(MessageStatusEnum.NORMAL.getStatus());
        message.setType(request.getMsgType());
        return message;
    }

    public static TextMsgResp.ReplyMsg toReplyMsg(Message replyMessage, User replyUser, Integer gapCount) {
        TextMsgResp.ReplyMsg replyMsgVO = new TextMsgResp.ReplyMsg();
        replyMsgVO.setId(replyMessage.getId());
        replyMsgVO.setUid(replyMessage.getFromUid());
        replyMsgVO.setType(replyMessage.getType());
        replyMsgVO.setBody(MsgHandlerFactory.getStrategy(replyMessage.getType()).showReplyMsg(replyMessage));
        replyMsgVO.setUsername(replyUser.getName());
        replyMsgVO.setCanCallback(YesOrNoEnum.toStatus(Objects.nonNull(gapCount) && gapCount <= MessageConvert.CAN_CALLBACK_GAP_COUNT));
        replyMsgVO.setGapCount(gapCount);
        return replyMsgVO;
    }
}
