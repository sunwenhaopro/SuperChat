package org.superchat.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.entity.RoomGroup;

import java.util.List;

@Getter
public class GroupMemberAddEvent extends ApplicationEvent {
    private final RoomGroup roomGroup;
   private final List<GroupMember> groupMemberList;
    private final Long uid;
    public GroupMemberAddEvent(Object source,RoomGroup roomGroup,List<GroupMember> groupMemberList,Long uid) {
        super(source);
        this.groupMemberList=groupMemberList;
        this.roomGroup=roomGroup;
        this.uid=uid;
    }
}
