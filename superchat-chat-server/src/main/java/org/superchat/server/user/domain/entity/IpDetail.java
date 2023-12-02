package org.superchat.server.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.geo.Point;

import java.io.Serializable;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 用户ip信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    //ip
    private String ip= "0.0.0.0";
    //市县
    private String city;
    //省
    private String province;
    //国家
    private String country;
    //经纬度
    //方便后续增加附近的人功能
    private Point point;

}