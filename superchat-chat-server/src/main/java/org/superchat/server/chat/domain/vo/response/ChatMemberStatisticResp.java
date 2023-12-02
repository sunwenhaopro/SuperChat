package org.superchat.server.chat.domain.vo.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description: 群成员统计信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMemberStatisticResp {

    @ApiModelProperty("在线人数")
    private Long onlineNum;//在线人数
    @ApiModelProperty("总人数")
    @Deprecated
    private Long totalNum;//总人数
}
