package org.superchat.server.user.service.serviceImpl;

import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.superchat.server.common.domain.vo.response.ApiResult;
import org.superchat.server.common.utils.IpUtil;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.IpDetail;
import org.superchat.server.user.domain.entity.IpInfo;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.service.IpService;
import org.superchat.server.user.service.convert.UserConvert;


import java.util.Objects;

@Slf4j
@Service
@AllArgsConstructor
public class IpServiceImpl implements IpService {

    private final UserDao userDao;


    @Override
    public void refreshIpDetailAsync(Long id) {

                  User user=userDao.getById(id);
                  IpInfo ipInfo=user.getIpInfo();
                  IpDetail ipDetail= IpUtil.getIpDetail(ipInfo.getUpdateIp());
                  if(Objects.nonNull(ipDetail))
                  {
                      ipInfo.refreshIpDetail(ipDetail);
                      User updateUser= UserConvert.toUpdateUser(user);
                      userDao.updateById(updateUser);
                  }
    };
}
