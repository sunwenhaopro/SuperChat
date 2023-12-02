package org.superchat.server.chat.service.strategy.msg;

import cn.hutool.core.bean.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;
import org.superchat.server.chat.domain.vo.request.ChatMessageReq;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.user.service.convert.MessageConvert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;



public abstract class AbstractMsgHandler<Req> {
    @Resource
    private  MessageDao messageDao;

    private  Class<Req> bodyClass;

    @PostConstruct
    private void init()
    {
      ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
      this.bodyClass=(Class<Req>) genericSuperclass.getActualTypeArguments()[0];
      MsgHandlerFactory.register(getMsgTypeEnum().getType(),this);

    }

    abstract MessageTypeEnum getMsgTypeEnum() ;

    protected void checkMsg(Req body,Long roomId,Long uid)
    {

    }


    @Transactional
    public Long checkAndSaveMsg(ChatMessageReq request, Long uid) {
        Req body = this.toBean(request.getBody());
        //统一校验
        AssertUtil.allCheckValidateThrow(body);
        //子类扩展校验
        checkMsg(body, request.getRoomId(), uid);
        Message insert = MessageConvert.toMsgSave(request, uid);
        //统一保存
        messageDao.save(insert);
        //子类扩展保存
        saveMsg(insert, body);
        return insert.getId();
    }

    private Req toBean(Object body) {
        if (bodyClass.isAssignableFrom(body.getClass())) {
            return (Req) body;
        }
        return BeanUtil.toBean(body, bodyClass);
    }

    protected abstract void saveMsg(Message message, Req body);

    /**
     * 展示消息
     */
    public abstract Object showMsg(Message msg);

    /**
     * 被回复时——展示的消息
     */
    public abstract Object showReplyMsg(Message msg);

    /**
     * 会话列表——展示的消息
     */
    public abstract String showContactMsg(Message msg);
}
