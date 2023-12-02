package org.superchat.server.user.service.serviceImpl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.superchat.server.annotation.RedissonLock;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.request.PageBaseReq;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.domain.vo.response.PageBaseResp;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.user.dao.UserApplyDao;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.dao.UserFriendDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.entity.UserApply;
import org.superchat.server.user.domain.entity.UserFriend;
import org.superchat.server.user.domain.vo.request.friend.FriendApplyReq;
import org.superchat.server.user.domain.vo.request.friend.FriendApproveReq;
import org.superchat.server.user.domain.vo.request.friend.FriendCheckReq;
import org.superchat.server.user.domain.vo.request.friend.FriendDeleteReq;
import org.superchat.server.user.domain.vo.response.friend.FriendApplyResp;
import org.superchat.server.user.domain.vo.response.friend.FriendCheckResp;
import org.superchat.server.user.domain.vo.response.friend.FriendResp;
import org.superchat.server.user.domain.vo.response.friend.FriendUnreadResp;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.user.service.FriendService;
import org.superchat.server.user.service.RoomService;
import org.superchat.server.user.service.convert.FriendConvert;
import org.superchat.server.user.service.convert.MessageConvert;
import org.superchat.server.user.service.convert.UserApplyConvert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.superchat.server.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;

@Service
@AllArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {
    private final UserFriendDao userFriendDao;
    private final UserApplyDao userApplyDao;
    private final RoomService roomService;
    private final ChatService chatService;
    private final UserDao userDao;


    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq req) {
        List<Long> uidList = req.getUidList();
        List<Long> friendUidList = userFriendDao.getByFriends(uid, uidList).stream().map(UserFriend::getFriendUid).collect(Collectors.toList());
        List<FriendCheckResp.FriendCheck> result = req.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(uid);
            friendCheck.setIsFriend(friendUidList.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return new FriendCheckResp(result);
    }

    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq req) {
        //判断是否已经是好友
        UserFriend friend = userFriendDao.getByFriend(uid, req.getTargetUid());
        AssertUtil.isEmpty(friend, "已经是好友了哦！");
        //判断是否在自己申请列表中
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, req.getTargetUid());
        AssertUtil.isEmpty(selfApproving, "已经申请过了哦");
        //判断别人是否申请过自己
        UserApply otherApproving = userApplyDao.getFriendApproving(req.getTargetUid(), uid);
        //双方是否互相申请过好友
        if (Objects.nonNull(otherApproving)) {
            ((FriendService) AopContext.currentProxy()).apply(uid, req);
            return;
        }
        UserApply insert = UserApplyConvert.toUserApply(uid, req);
        userApplyDao.save(insert);
    }

    @Override
    @Transactional
    public void deleteFriend(Long uid, FriendDeleteReq req) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, req.getTargetUid());
        AssertUtil.isFalse(CollectionUtil.isEmpty(userFriends), "没有好友关系！无法删除");
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        //禁用房间
        roomService.disableFriendRoom(Arrays.asList(uid, req.getTargetUid()));
    }

    /**
     * 同意申请
     *
     * @param uid
     * @param req
     */
    @Override
    @RedissonLock(key = "#uid")
    public void approve(Long uid, FriendApproveReq req) {
        UserApply userApply = userApplyDao.getById(req.getApplyId());
        AssertUtil.isNotEmpty(userApply, "不存在申请记录");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), WAIT_APPROVAL.getCode(), "已同意好友申请");
        userApplyDao.agree(req.getApplyId());
        createFriend(uid, userApply.getUid());
        RoomFriend roomFriend = roomService.createFriendRoom(Arrays.asList(uid, userApply.getUid()));
        chatService.sendMsg(MessageConvert.toAgreeMsg(roomFriend.getRoomId()), uid);
    }

    /**
     * @param uid
     * @param req
     * @return 分页查询好友申请列表
     */
    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq req) {
        IPage<UserApply> userApplyIpage = userApplyDao.friendApplyPage(uid, req.plusPage());
        if (CollectionUtil.isEmpty(userApplyIpage.getRecords())) {
            return PageBaseResp.empty();

        }
        readApplies(uid, userApplyIpage);
        return PageBaseResp.init(userApplyIpage, FriendConvert.toFriendApplyList(userApplyIpage.getRecords()));
    }

    @Override
    public FriendUnreadResp unRead(Long uid) {
        return new FriendUnreadResp(userApplyDao.getUnReadCount(uid));
    }

    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq req) {
        CursorPageBaseResp<UserFriend> friendPage=userFriendDao.getFriendPage(uid,req);
        if(CollectionUtil.isEmpty(friendPage.getList()))
        {
            CursorPageBaseResp.empty();
        }
        List<Long> friendIds=friendPage.getList().stream().map(UserFriend::getFriendUid).collect(Collectors.toList());
        List<User> userList=userDao.getFriendList(friendIds);
        return CursorPageBaseResp.init(friendPage,FriendConvert.toFriendRespList(userList));
    }

    private void readApplies(Long uid, IPage<UserApply> userApplyIpage) {
        List<Long> applyIds = userApplyIpage.getRecords().stream().map(UserApply::getId).collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    private void createFriend(Long uid, Long friendId) {
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setFriendUid(uid);
        userFriend1.setUid(friendId);
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setFriendUid(friendId);
        userFriend2.setUid(uid);
        userFriendDao.saveBatch(Arrays.asList(userFriend1, userFriend2));
    }
}
