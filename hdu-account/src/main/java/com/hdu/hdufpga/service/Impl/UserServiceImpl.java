package com.hdu.hdufpga.service.Impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.hdu.hdufpga.entity.po.DepartmentPO;
import com.hdu.hdufpga.entity.po.RolePO;
import com.hdu.hdufpga.entity.po.UserPO;
import com.hdu.hdufpga.entity.vo.UserVO;
import com.hdu.hdufpga.mapper.UserMapper;
import com.hdu.hdufpga.service.UserService;
import com.hdu.hdufpga.util.TimeUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@DubboService
public class UserServiceImpl extends MPJBaseServiceImpl<UserMapper, UserPO> implements UserService {
  @Resource
  UserMapper userMapper;

  @Override
  public List<Integer> getIdByUserName(List<String> userNameList, Integer departmentId) {
    LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
    wrapper
        .select(UserPO::getId)
        .in(UserPO::getUsername, userNameList)
        .eq(UserPO::getUserDepartmentId, departmentId);
    List<UserPO> poList = userMapper.selectList(wrapper);
    List<Integer> idList = new ArrayList<>();
    poList.forEach(e -> idList.add(e.getId()));
    return idList;
  }

  @Override
  public Long getUserCountByDate(Date startDate, Date endDate) {
    LambdaQueryWrapper<UserPO> wrapper = new LambdaQueryWrapper<>();
    wrapper.between(UserPO::getCreateTime, startDate, endDate);
    return userMapper.selectCount(wrapper);
  }

  @Override
  public boolean save(UserPO entity) {
    entity.setCreateTime(TimeUtil.getNowTime());
    entity.setUpdateTime(TimeUtil.getNowTime());
    entity.setPassword(SecureUtil.md5(entity.getPassword()));
    return super.save(entity);
  }

  @Override
  public boolean updateById(UserPO entity) {
    if (entity.getPassword() != null) {
      entity.setPassword(SecureUtil.md5(entity.getPassword()));
    }
    return super.updateById(entity);
  }

  @Override
  public UserPO getUserByUserName(String userName, Integer departmentId) {
    MPJLambdaWrapper<UserPO> wrapper = new MPJLambdaWrapper<>();
    wrapper
        .selectAll(UserPO.class)
        .selectAs(RolePO::getPrivilegeCharacter, UserPO::getUserRoleName)
        .selectAs(RolePO::getPrivilegeLevel, UserPO::getPrivilegeLevel)
        .selectAs(DepartmentPO::getName, UserPO::getUserDepartmentName)
        .leftJoin(RolePO.class, RolePO::getId, UserPO::getUserRoleId)
        .leftJoin(DepartmentPO.class, DepartmentPO::getId, UserPO::getUserDepartmentId)
        .eq(UserPO::getUsername, userName)
        .eq(UserPO::getUserDepartmentId, departmentId)
    ;
    return userMapper.selectJoinOne(UserPO.class, wrapper);
  }

  @Override
  public UserVO UserPO2UserVO(UserPO userPO) {
    if (userPO == null) {
      return null;
    }

    UserVO vo = new UserVO();

    // BaseEntity 字段（假设有 id / createTime 等）
    vo.setId(userPO.getId());
    vo.setCreateTime(userPO.getCreateTime());
    vo.setUpdateTime(userPO.getUpdateTime());

    vo.setUsername(userPO.getUsername());
    vo.setPassword(userPO.getPassword());
    vo.setRealName(userPO.getRealName());
    vo.setActiveTime(userPO.getActiveTime());

    vo.setUserDepartmentId(userPO.getUserDepartmentId());
    vo.setUserDepartmentName(userPO.getUserDepartmentName());

    vo.setUserRoleId(userPO.getUserRoleId());
    vo.setUserRoleName(userPO.getUserRoleName());

    vo.setPrivilegeLevel(userPO.getPrivilegeLevel());

    // ===== 特殊字段转换 =====

    // Long -> Duration
    if (userPO.getTotActiveTime() != null) {
      vo.setTotActiveTime(Duration.ofSeconds(userPO.getTotActiveTime()));
      // 如果你数据库存的是毫秒，改成：
      // Duration.ofMillis(userPO.getTotActiveTime())
    } else {
      vo.setTotActiveTime(Duration.ZERO);
    }

    vo.setTotExpCnt(
        userPO.getTotExpCnt() != null ? userPO.getTotExpCnt() : 0
    );

    return vo;
  }

  @Override
  public UserVO createThirdUser(String uid, String source) {
    UserPO userPO = new UserPO();
    userPO.setUsername("third part:" + uid);
    userPO.setPassword("114514");
    userPO.setRealName(uid);
    userPO.setUserDepartmentId(-1);
    userPO.setUserRoleId(1);
    save(userPO);
    return UserPO2UserVO(userPO);
  }
}
