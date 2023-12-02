package org.superchat.server.user.domain.vo.request.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;


/**
 * Description: 佩戴徽章
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdornBadgeReq {

    @NotNull
    @ApiModelProperty("徽章id")
    private Long badgeId;

}
