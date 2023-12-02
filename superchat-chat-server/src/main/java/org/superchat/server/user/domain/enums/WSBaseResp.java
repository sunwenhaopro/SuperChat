package org.superchat.server.user.domain.enums;

import lombok.Data;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: ws的基本返回信息体
 */
@Data
public class WSBaseResp<T> {
    /**
     * ws推送给前端的消息
     *
     * @see WSRespTypeEnum
     */
    private Integer type;
    private T data;
}
