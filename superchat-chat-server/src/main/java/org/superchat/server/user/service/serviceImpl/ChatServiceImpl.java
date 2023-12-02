package org.superchat.server.user.service.serviceImpl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.superchat.server.annotation.RedissonLock;
import org.superchat.server.chat.dao.*;
import org.superchat.server.chat.domain.dto.MsgReadInfoDTO;
import org.superchat.server.chat.domain.entity.*;
import org.superchat.server.chat.domain.enums.MessageMarkActTypeEnum;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;
import org.superchat.server.chat.domain.vo.request.*;
import org.superchat.server.chat.domain.vo.request.member.MemberReq;
import org.superchat.server.chat.domain.vo.response.ChatMemberStatisticResp;
import org.superchat.server.chat.domain.vo.response.ChatMessageReadResp;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.chat.service.ContactService;
import org.superchat.server.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import org.superchat.server.chat.service.strategy.mark.MsgMarkFactory;
import org.superchat.server.chat.service.strategy.msg.AbstractMsgHandler;
import org.superchat.server.chat.service.strategy.msg.MsgHandlerFactory;
import org.superchat.server.chat.service.strategy.msg.RecallMsgHandler;
import org.superchat.server.common.cache.RoomCache;
import org.superchat.server.common.cache.RoomGroupCache;
import org.superchat.server.common.cache.UserCache;
import org.superchat.server.common.domain.enums.NormalOrNoEnum;
import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.event.MessageSendEvent;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.common.utils.ChatMemberHelper;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.ChatActiveStatusEnum;
import org.superchat.server.user.domain.enums.RoleEnum;
import org.superchat.server.user.domain.vo.response.ws.ChatMemberResp;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.user.service.RoleService;
import org.superchat.server.user.service.convert.MemberConvert;
import org.superchat.server.user.service.convert.MessageConvert;
import org.superchat.server.user.service.convert.RoomConvert;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserCache userCache;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final RoomCache roomCache;
    private final ContactDao contactDao;
    private final MessageMarkDao messageMarkDao;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RoleService roleService;
    private final RecallMsgHandler recallMsgHandler;
    private final ContactService contactService;
    private final RoomFriendDao roomFriendDao;
    private final RoomGroupCache roomGroupCache;
    private final GroupMemberDao groupMemberDao;
    private final RoomGroupDao roomGroupDao;

    /**
     * 发送信息
     *
     * @param request
     * @param uid
     * @return
     */
    @Override
    @Transactional
    public Long sendMsg(ChatMessageReq request, Long uid) {
        check(request, uid);
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategy(request.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(request, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this, msgId));
        return msgId;
    }

    private void check(ChatMessageReq request, Long uid) {
        Room room = roomCache.get(request.getRoomId());
        if (room.isHotRoom()) {//全员群跳过校验
            return;
        }
        if (room.isRoomFriend()) {
            RoomFriend roomFriend = roomFriendDao.getByRoomId(request.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if (room.isRoomGroup()) {
            RoomGroup roomGroup = roomGroupCache.get(request.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }
    }

    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq res, Long uid) {
        Long lastMsgId = getLastMsgId(res.getRoomId(), uid);
        CursorPageBaseResp<Message> cursorPageBaseResp = messageDao.getCursorPage(res.getRoomId(), res, lastMsgId);
        if (cursorPageBaseResp.isEmpty()) {
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPageBaseResp, getMsgRespBatch(cursorPageBaseResp.getList(), uid));
    }

    @Override
    @RedissonLock(key = "#uid")
    public void setMsgMark(Long uid, ChatMessageMarkReq req) {
        AbstractMsgMarkStrategy strategy = MsgMarkFactory.getStrategyNoNull(req.getMarkType());
        switch (MessageMarkActTypeEnum.of(req.getActType())) {
            case MARK:
                strategy.mark(uid, req.getMsgId());
                break;
            case UN_MARK:
                strategy.unMark(uid, req.getMsgId());
                break;
        }
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long receiveId) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, receiveId);
    }

    public ChatMessageResp getMsgResp(Message msg, Long receiveId) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(msg), receiveId));
    }

    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request) {
        Message message = messageDao.getById(request.getMsgId());
        AssertUtil.isNotEmpty(message, "消息id有误");
        AssertUtil.equal(uid, message.getFromUid(), "只能查看自己的消息");
        CursorPageBaseResp<Contact> pageBaseResp;
        if (request.getSearchType() == 1) {
            pageBaseResp = contactDao.getReadPage(message, request);
        } else {
            pageBaseResp = contactDao.getUnReadPage(message, request);
        }
        return CursorPageBaseResp.init(pageBaseResp, RoomConvert.toReadResp(pageBaseResp.getList()));
    }

    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request) {
        List<Message> messages = messageDao.listByIds(request.getMsgIds());
        messages.forEach(message -> AssertUtil.equal(uid, message.getFromUid(), "只能查询自己发送的消息"));
        return contactService.getMsgReadInfo(messages).values();
    }

    @Override
    @RedissonLock(key = "#uid")
    public void msgRead(Long uid, ChatMessageMemberReq request) {
        Contact contact = contactDao.get(uid, request.getRoomId());
        if (Objects.nonNull(contact)) {
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(new Date());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(request.getRoomId());
            insert.setReadTime(new Date());
            contactDao.save(insert);
        }
    }

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        System.out.println(Thread.currentThread().getName());
        Long onlineNum = userCache.getOnlineNum();
        Long offlineNum = userCache.getOfflineNum();
        ChatMemberStatisticResp resp = new ChatMemberStatisticResp();
        resp.setOnlineNum(onlineNum);
        resp.setTotalNum(onlineNum + offlineNum);
        return resp;
    }

    /**
     * 撤回消息
     *
     * @param uid
     * @param req
     */
    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq req) {
        Message message = messageDao.getById(req.getMsgId());
        checkRecall(uid, message);
        recallMsgHandler.recall(uid, message);
    }

    private void checkRecall(Long uid, Message message) {
        AssertUtil.isNotEmpty(message, "消息不存在");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        boolean hasPower = roleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
        if (hasPower) {
            return;
        }
        AssertUtil.isEqual(uid, message.getFromUid(), "抱歉！你没有权限");
        // 2分钟以内
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between < 2, "覆水难收");
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request) {
        Pair<ChatActiveStatusEnum, String> pair = ChatMemberHelper.getCursorPair(request.getCursor());
        ChatActiveStatusEnum activeStatusEnum = pair.getKey();
        String timeCursor = pair.getValue();
        List<ChatMemberResp> resultList = new ArrayList<>();//最终列表
        Boolean isLast = Boolean.FALSE;
        if (activeStatusEnum == ChatActiveStatusEnum.ONLINE) {//在线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.ONLINE);
            resultList.addAll(MemberConvert.toMember(cursorPage.getList()));//添加在线列表
            if (cursorPage.getIsLast()) {//如果是最后一页,从离线列表再补点数据
                activeStatusEnum = ChatActiveStatusEnum.OFFLINE;
                Integer leftSize = request.getPageSize() - cursorPage.getList().size();
                cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(leftSize, null), ChatActiveStatusEnum.OFFLINE);
                resultList.addAll(MemberConvert.toMember(cursorPage.getList()));//添加离线线列表
            }
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        } else if (activeStatusEnum == ChatActiveStatusEnum.OFFLINE) {//离线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.OFFLINE);
            resultList.addAll(MemberConvert.toMember(cursorPage.getList()));//添加离线线列表
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        }
        // 获取群成员角色ID
        List<Long> uidList = resultList.stream().map(ChatMemberResp::getUid).collect(Collectors.toList());
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        Map<Long, Integer> uidMapRole = groupMemberDao.getMemberMapRole(roomGroup.getId(), uidList);
        resultList.forEach(member -> member.setRoleId(uidMapRole.get(member.getUid())));
        //组装结果
        return new CursorPageBaseResp<>(ChatMemberHelper.generateCursor(activeStatusEnum, timeCursor), isLast, resultList);
    }


    private List<ChatMessageResp> getMsgRespBatch(List<Message> msgList, Long uid) {
        if (CollectionUtil.isEmpty(msgList)) {
            return new ArrayList<>();
        }
        List<MessageMark> marks = messageMarkDao.getValidMarkByMsgIdBatch(msgList.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageConvert.toChatMessageResp(msgList, marks, uid);

    }

    private Long getLastMsgId(Long roomId, Long uid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if (room.isHotRoom()) {
            return null;
        }
        AssertUtil.isNotEmpty(uid, "请先登录");
        Contact contact = contactDao.get(uid, roomId);
        return contact.getLastMsgId();
    }
}
