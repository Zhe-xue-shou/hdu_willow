package com.hdu.vboard.listener;

import com.hdu.hdufpga.config.RedisConfiguration;
import com.hdu.hdufpga.util.RedisUtil;
import com.hdu.vboard.entity.constant.VbRedisConstant;
import com.hdu.vboard.service.VirtualBoardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
@Import(RedisConfiguration.class)
public class VbRedisKeyExpiredListener extends KeyExpirationEventMessageListener {
  private static final Topic TOPIC_ALL_KEYEVENTS = new PatternTopic("__keyevent@1");

  @Resource
  private RedisUtil redisUtil;

  @Resource
  private VirtualBoardService virtualBoardService;

  public VbRedisKeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
    super(listenerContainer);
  }

  // 只监听db1的键
  protected void doRegister(RedisMessageListenerContainer listenerContainer) {
    listenerContainer.addMessageListener(this, new PatternTopic("__keyevent@1__:expired"));
  }

  @Override
  public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
    String expiredKey = message.toString();

    try {
      log.debug("expiredKey:{}", expiredKey);
      String[] split = expiredKey.split(":");
      String token = split[1];
      if (VbRedisConstant.REDIS_VB_TTL_PREFIX.contains(split[0])) {
        log.debug("key: {} 过期,清理工作区!", token);
        freeVirtualBoard(token);
      }

    } catch (Exception e) {
      log.error(e.toString());
      log.error("{} 有key过期了，但是没有成功执行监听方法", expiredKey);
    }
  }

  private void freeVirtualBoard(String token) {
    try {
      virtualBoardService.stopWorkbench(token, 1);
      log.info("Time out to free workbench for token:{} successfully", token);
    } catch (Exception e) {
      log.warn(e.toString());
    }
  }
}
