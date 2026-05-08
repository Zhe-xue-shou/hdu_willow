package com.hdu.hdufpga.service.Impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import com.hdu.hdufpga.entity.constant.RedisConstant;
import com.hdu.hdufpga.entity.dto.UserStatisticDTO;
import com.hdu.hdufpga.entity.po.CircuitBoardPO;
import com.hdu.hdufpga.entity.vo.UserConnectionVO;
import com.hdu.hdufpga.exception.CircuitBoardException;
import com.hdu.hdufpga.mapper.CircuitBoardMapper;
import com.hdu.hdufpga.netty.NettySocketHolder;
import com.hdu.hdufpga.service.*;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.hdufpga.utils.CircuitBoardUtil;
import com.hdu.svccmn.service.UserStatisticService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class CircuitBoardServiceImpl extends MPJBaseServiceImpl<CircuitBoardMapper, CircuitBoardPO>
    implements CircuitBoardService {
  @Resource
  CbUseRecordService cbUseRecordService;

  @Resource
  CircuitBoardHistoryOperationService circuitBoardHistoryOperationService;

  @Resource
  RedisUtil redisUtil;

  @Resource
  UserStatisticService userStatisticService;

  @Value("${wait-queue.name}")
  String queueName;

  @Override
  public CircuitBoardPO getAFreeCircuitBoard() throws CircuitBoardException {
    LambdaQueryWrapper<CircuitBoardPO> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(CircuitBoardPO::getStatus, false);
    queryWrapper.eq(CircuitBoardPO::getIsReserved, false);
    queryWrapper.eq(CircuitBoardPO::getIsRecorded, false);
    List<CircuitBoardPO> circuitBoardPOS = baseMapper.selectList(queryWrapper);
    if (!circuitBoardPOS.isEmpty()) {
      CircuitBoardPO circuitBoardPO = circuitBoardPOS.get(RandomUtil.randomInt(circuitBoardPOS.size()));
      circuitBoardPO.setStatus(true);
      baseMapper.updateById(circuitBoardPO);
      return circuitBoardPO;
    } else {
//      throw new CircuitBoardException("暂无空闲板卡");
      log.info("暂无空闲板卡");
      return null;
    }
  }

  @Override
  public CircuitBoardPO getCircuitBoardByIp(String ip) {
    LambdaQueryWrapper<CircuitBoardPO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(CircuitBoardPO::getIp, ip);
    List<CircuitBoardPO> list = baseMapper.selectList(wrapper);
    if (!list.isEmpty()) {
      return list.get(0);
    }
    return null;
  }

  @Override
  public String freeCircuitBoard(String cbIp) {
    final String reg = "^(\\d+\\.){3}\\d+:\\d+$";
    if (ReUtil.isMatch(reg, cbIp)) {
      LambdaQueryWrapper<CircuitBoardPO> wrapper = new LambdaQueryWrapper<>();
      wrapper.eq(CircuitBoardPO::getIp, cbIp);
      List<CircuitBoardPO> circuitBoardPOList = baseMapper.selectList(wrapper);
      if (!circuitBoardPOList.isEmpty()) {
        circuitBoardPOList.forEach(e -> {
          String longId = e.getLongId();
          ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
          CircuitBoardUtil.sendEndToCB(ctx, longId);
          HashMap<String, Object> CBInfo = NettySocketHolder.getInfo(e.getLongId());
          if (Validator.isNull(CBInfo))
            CBInfo = new HashMap<>();
          CBInfo.put(CircuitBoardConstant.STATUS, false);
          CBInfo.put(CircuitBoardConstant.IS_RECORDED, false);
          CBInfo.remove(CircuitBoardConstant.FILE_PATH);
          CBInfo.remove(CircuitBoardConstant.COUNT);
          CBInfo.put(CircuitBoardConstant.BUTTON_STATUS, "");
          CBInfo.put(CircuitBoardConstant.LIGHT_STATUS, "");
          NettySocketHolder.put(e.getLongId(), CBInfo);
          e.setStatus(false);
          baseMapper.updateById(e);
        });
      }
      return cbIp;
    } else {
      return null;
    }
  }

  @Override
  public Long getFreeCircuitBoardCount() {
    LambdaQueryWrapper<CircuitBoardPO> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(CircuitBoardPO::getStatus, false);
    queryWrapper.eq(CircuitBoardPO::getIsReserved, false);
    queryWrapper.eq(CircuitBoardPO::getIsRecorded, false);
    return baseMapper.selectCount(queryWrapper);
  }

  @Override
  public CircuitBoardPO getByCBLongId(String longId) {
    LambdaQueryWrapper<CircuitBoardPO> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(CircuitBoardPO::getLongId, longId);
    List<CircuitBoardPO> list = baseMapper.selectList(wrapper);
    if (!list.isEmpty())
      return list.get(0);
    else
      return null;
  }

  @Override
  public Integer updateByLongId(CircuitBoardPO circuitBoardPO) {
    LambdaUpdateWrapper<CircuitBoardPO> wrapper = new LambdaUpdateWrapper<>();
    wrapper
        .eq(CircuitBoardPO::getLongId, circuitBoardPO.getLongId());
    return baseMapper.update(circuitBoardPO, wrapper);
  }

  @Override
  public void deleteByLongId(String longId) {
    LambdaUpdateWrapper<CircuitBoardPO> wrapper = new LambdaUpdateWrapper<>();
    wrapper.eq(CircuitBoardPO::getLongId, longId);
    baseMapper.delete(wrapper);
  }

  @Override
  public void recordBitToBitForTheFirstTime(String token, String filePath) {
    UserConnectionVO userConnectionVO = (UserConnectionVO)
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token);
    String longId = userConnectionVO.getLongId();
    if (Validator.isNotNull(longId) && !longId.isEmpty()) {
      // 获取缓存中对应的ChannelHandlerContext
      ChannelHandlerContext ctx = NettySocketHolder.getCtx(longId);
      HashMap<String, Object> info = NettySocketHolder.getInfo(longId);
      // 配置缓存信息
      info.put(CircuitBoardConstant.IS_RECORDED, false);
      info.put(CircuitBoardConstant.COUNT, 0);
      info.put(CircuitBoardConstant.FILE_PATH, filePath);
      // 保存缓存信息
      NettySocketHolder.put(longId, info);
      log.info("instance: {}", NettySocketHolder.getInfo(longId));
      // 烧录板卡
      CircuitBoardUtil.recordBitToCB(ctx, filePath, longId, 0);
    }
  }

  @Override
  public UserStatisticDTO clearUserRedisAndFreeCB(String token) throws Exception {
    UserConnectionVO connectionVO = (UserConnectionVO)
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token);
    if (Validator.isNull(connectionVO)) {
      return null;
    }

    UserStatisticDTO userStatisticDTO = userStatisticService.updateUserExptimeByToken(token);
    redisUtil.del(RedisConstant.REDIS_EXP_START_TIME_PREFIX + token);

    if (!cbUseRecordService.saveUseRecord(connectionVO, userStatisticDTO.getAddActiveTime())) {
      throw new SQLException("保存用户信息错误");
    }
    if (freeCircuitBoard(connectionVO.getCbIp()) == null) {
      throw new CircuitBoardException("释放板卡失败");
    }

    redisUtil.del(RedisConstant.REDIS_CONN_PREFIX + token, RedisConstant.REDIS_TTL_PREFIX + token,
        RedisConstant.REDIS_CONN_SHADOW_PREFIX + token);
    redisUtil.removeInZSet(queueName, token);
    log.info("用户{}使用信息已保存，redis记录已清除", token);
    circuitBoardHistoryOperationService.clearSteps(token);
    log.info("用户{}相关文件已清除", token);


    return userStatisticDTO;
  }

  @Override
  public Boolean getRecordStatus(String token, String cbIp) throws CircuitBoardException {
    // 为了兼容api,仍然接收cbIp, 也许未来有用
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    if (Validator.isNotNull(userConnectionVO)) {
      Boolean isRecorded = (Boolean) NettySocketHolder.getValue(userConnectionVO.getLongId(),
          CircuitBoardConstant.IS_RECORDED);
      if (Validator.isNotNull(isRecorded)) {
        return isRecorded;
      } else {
        throw new CircuitBoardException("isRecorded为空");
      }
    } else {
      throw new CircuitBoardException("找不到这个板卡");
    }
  }

  @Override
  public Boolean sendButtonString(String token, String switchButtonStatus, String tapButtonStatus)
      throws CircuitBoardException {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    // 处理字符串
    String finalString = CircuitBoardUtil.processButtonString(switchButtonStatus, tapButtonStatus);
    Boolean isRecorded = (Boolean) NettySocketHolder.getValue(longId, CircuitBoardConstant.IS_RECORDED);
    if (Validator.isNotNull(isRecorded) && isRecorded) {
      CircuitBoardUtil.sendButtonStringToCB(NettySocketHolder.getCtx(longId), finalString);
      // 保存缓存
      NettySocketHolder.putValue(longId, CircuitBoardConstant.BUTTON_STATUS, finalString);
      // 保存历史操作记录
      circuitBoardHistoryOperationService.saveOperationStep(token, finalString);
      return true;
    } else {
      throw new CircuitBoardException("板卡未烧录");
    }
  }

  @Override
  public String getLightString(String token) throws CircuitBoardException {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    String lightString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.LIGHT_STATUS);
    if (Validator.isNotNull(lightString)) {
      return lightString;
    } else {
      throw new CircuitBoardException("LED灯状态为空");
    }
  }

  @Override
  public String getNixieTubeString(String token) throws CircuitBoardException {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    String nixieTubeString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.NIXIE_TUBE_STATUS);
    if (Validator.isNotNull(nixieTubeString)) {
      return nixieTubeString;
    } else {
      throw new CircuitBoardException("数码管状态为空");
    }
  }

  @Override
  public String getProcessedBtnStr(String token) throws CircuitBoardException {
    UserConnectionVO userConnectionVO = Convert.convert(UserConnectionVO.class,
        redisUtil.get(RedisConstant.REDIS_CONN_PREFIX + token));
    String longId = userConnectionVO.getLongId();
    String buttonString = (String) NettySocketHolder.getValue(longId, CircuitBoardConstant.BUTTON_STATUS);
    if (Validator.isNotNull(buttonString)) {
      return buttonString;
    } else {
      throw new CircuitBoardException("按钮状态状态为空");
    }
  }

  @Override
  public String getNextLongId() {
    // 使用复合索引，直接按 long_id 降序取第一条
    LambdaQueryWrapper<CircuitBoardPO> wrapper = new LambdaQueryWrapper<>();
    wrapper.select(CircuitBoardPO::getLongId)
        .isNotNull(CircuitBoardPO::getLongId)
        .eq(CircuitBoardPO::getIsDeleted, 0)
        .orderByDesc(CircuitBoardPO::getLongId)
        .last("LIMIT 1");

    CircuitBoardPO po = this.getOne(wrapper, false);
    int maxId = 1000;
    if (po != null && po.getLongId() != null) {
      maxId = Integer.parseInt(po.getLongId());
    }

    return String.valueOf(maxId + 1);
  }
}
