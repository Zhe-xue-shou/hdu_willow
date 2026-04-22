package com.hdu.hdufpga.utils;

import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import com.hdu.hdufpga.util.MFileUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CircuitBoardUtil {
  public static void recordBitToCB(ChannelHandlerContext ctx, String filePath, String longId, Integer count) {
    log.info("文件烧录位置:{}", filePath);
    byte[] b = MFileUtil.fileToBytes(filePath);
    int limit = (int) Math.ceil((double) b.length / CircuitBoardConstant.SLICE_SIZE);
    List<String> stringList = new ArrayList<>();
    List<String> packNumList = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      int length = CircuitBoardConstant.SLICE_SIZE;
      if (i + 1 >= limit) {
        length = b.length % CircuitBoardConstant.SLICE_SIZE;
      }
      byte[] dest = new byte[CircuitBoardConstant.SLICE_SIZE];
      //截取数组
      System.arraycopy(b, i * CircuitBoardConstant.SLICE_SIZE, dest, 0, length);
      stringList.add(MFileUtil.bytesToHexString(dest)); // 存入链表
      packNumList.add(Integer.toHexString(length));
    }
    if (count == 0) {
      sendToCbCtx(ctx, "NNN"); // 结束上一次的控制
//      log.debug("send : {NNN}");
      sendToCbCtx(ctx, "CALL#" + longId + " #"); // 重新连接
//      log.debug("send : {{{}}}", "CALL#" + longId + " #");
      log.info("the first time to send message");
    } else if (count == 1) {
      // b.length 文件字节数  传输前置
      int size = b.length / CircuitBoardConstant.SLICE_SIZE;
      StringBuilder sizeString = new StringBuilder(Integer.toHexString(size));
      for (int i = sizeString.length(); i < 3; i++) {
        sizeString.insert(0, "0");  // 填充至三位 对应Byte[5..=7]
      }
      int c = b.length * 2;
      StringBuilder sizeString2 = new StringBuilder(Integer.toHexString(c));
      for (int i = sizeString2.length(); i < 6; i++) {
        sizeString2.insert(0, "0"); // 填充至六位 对应Byte[8..=13]
      }
      sizeString.append(sizeString2);
      sendToCbCtx(ctx, "SIZE#" + sizeString + "#");
//      log.debug("send : {{{}}}", "SIZE#" + sizeString + "#");
      log.info("发送文件字节大小:{}", sizeString);
    } else if (count < limit + 2) {

      sendToCbCtx(ctx, "FIL" + Integer.toString(count - 2) + "#" + stringList.get(count - 2) + "# #");
//      log.debug("send : {{{}}}", "FIL" + Integer.toString(count - 2) + "#" + stringList.get(count - 2) + "# #");
      log.info("第 " + count + " 次发送数据 " + stringList.get(count - 2).length() * 2);
    } else {
      log.info("烧录完毕");
    }
  }

  public static void sendButtonStringToCB(ChannelHandlerContext ctx, String buttonString) {
    sendToCbCtx(ctx, "CTR #" + buttonString + "#");
  }

  public static void sendEndToCB(ChannelHandlerContext ctx) {
    sendToCbCtx(ctx, "NNN");
  }

  public static String processButtonString(String switchButtonStatus, String tapButtonStatus) {
    StringBuilder finalString = new StringBuilder();
    if (switchButtonStatus.length() > 32) {
      log.error("开关状态字符串超长");
    } else if (switchButtonStatus.length() == 32) {
      finalString.append(HexUtil.binaryToHex(switchButtonStatus));
    } else {
      log.error("开关状态字符串缺失:{}", switchButtonStatus.length());
    }

    if (tapButtonStatus.length() == 6) {
      tapButtonStatus = tapButtonStatus + "00";
    }
    if (tapButtonStatus.length() == 8) {
      finalString.append(HexUtil.binaryToHex(tapButtonStatus));
    } else {
      log.error("tapButtonStatus 长度异常: {}", tapButtonStatus.length());
    }
    return finalString.toString();
  }

  public static void sendToCbCtx(ChannelHandlerContext ctx, String sendString) {
    int checkv = 0;
    for (byte i : sendString.getBytes()) {
      checkv += i;
    }
    byte[] checksumBytes = new byte[3];
    checksumBytes[0] = (byte) ((checkv >> 16) & 0xFF);  // 最高8位
    checksumBytes[1] = (byte) ((checkv >> 8) & 0xFF);   // 中间8位
    checksumBytes[2] = (byte) (checkv & 0xFF);          // 最低8位
    ctx.writeAndFlush(sendString + Arrays.toString(checksumBytes));
    log.debug("send : {{}}", sendString + Arrays.toString(checksumBytes));
  }
}
