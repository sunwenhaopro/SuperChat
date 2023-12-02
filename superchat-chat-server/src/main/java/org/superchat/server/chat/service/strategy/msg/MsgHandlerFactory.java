package org.superchat.server.chat.service.strategy.msg;

import io.swagger.models.auth.In;
import org.superchat.server.common.exception.Enum.CommonErrorEnum;
import org.superchat.server.common.utils.AssertUtil;

import java.util.HashMap;
import java.util.Map;

public class MsgHandlerFactory {
    private static final Map<Integer,AbstractMsgHandler> STRATEGY_MAP=new HashMap<>();
    public static void register(Integer type,AbstractMsgHandler abstractMsgHandler)
    {
        STRATEGY_MAP.put(type,abstractMsgHandler);
    }
    public static AbstractMsgHandler getStrategy(Integer type)
    {
        AbstractMsgHandler strategy=STRATEGY_MAP.get(type);
        AssertUtil.isNotEmpty(strategy, CommonErrorEnum.PARAM_VALID);
        return strategy;
    }

}
