package org.superchat.server.user.domain.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 用户ip信息
 */
@Data
public class IpInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    //上次登录的ip
    private String lastIp="0.0.0.0";
    //最新登录的ip
    private String updateIp;
    //最新登录的ip详情
    private IpDetail updateIpDetail;

    public void refreshIpDetail(IpDetail updateIpDetail) {
        if(lastIp.equals(updateIp))
        {
            return ;
        }
        doRefreshIpDetail(updateIpDetail);
    }

    /**
     * 需要刷新的ip，这里判断更新ip就够，初始化的时候ip也是相同的，只需要设置的时候多设置进去就行
     *
     * @return
     */

    public void doRefreshIpDetail(IpDetail ipDetail) {
        if(Objects.nonNull(updateIpDetail))
        {
        lastIp= updateIpDetail.getIp();
        }
        updateIpDetail=ipDetail;
    }
}