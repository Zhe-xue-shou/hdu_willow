package com.hdu.hdufpga.netty;

import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

//线程安全的懒汉模式
@Slf4j
public class NettySocketHolder {
  // token -> Hashmap{
  // IP , LightStatus, ButtonStatus, ...
  // }
  private static final ConcurrentHashMap<String, HashMap<String, Object>> nettySocketHolder = new ConcurrentHashMap<>(100);

  /**
   * 将信息存入map
   */
  public static void put(String longId, HashMap<String, Object> info) {
    nettySocketHolder.put(longId, info);
    log.debug("添加缓存:{} -> {}", longId, info);
  }

  public static void putValue(String longId, String key, Object value) {
    nettySocketHolder.get(longId).put(key, value);
  }

  /**
   * 从map中替换
   */
  public static void replace(String longId, HashMap<String, Object> info) {
    nettySocketHolder.replace(longId, info);
  }

  /**
   * 从map中删除
   */
  public static void remove(String longId) {
    nettySocketHolder.remove(longId);
  }

  /**
   * 返回 info
   */
  public static HashMap<String, Object> getInfo(String longId) {
    return nettySocketHolder.get(longId);
  }

  public static Object getValue(String longId, String key) {
    return nettySocketHolder.get(longId).get(key);
  }


  /**
   * 返回 ctx
   */
  public static ChannelHandlerContext getCtx(String longId) {
    log.debug("读取:{}", nettySocketHolder);
    return (ChannelHandlerContext) NettySocketHolder.getInfo(longId).get(CircuitBoardConstant.CTX);
  }
}