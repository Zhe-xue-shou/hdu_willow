package com.hdu.hdufpga.utils;

import com.hdu.hdufpga.entity.constant.CircuitBoardConstant;
import com.hdu.hdufpga.util.MFileUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hdu.hdufpga.utils.ByteUtil.*;

@Slf4j
public class CircuitBoardUtil {
  public static void recordBitToCB(ChannelHandlerContext ctx, String filePath, String longId, Integer count) {
    log.info("文件烧录位置:{}", filePath);
    byte[] b = MFileUtil.fileToBytes(filePath);
    int limit = (int) Math.ceil((double) b.length / CircuitBoardConstant.SLICE_SIZE);
    List<byte[]> packList = new ArrayList<>();
    for (int i = 0; i < limit; i++) {
      int length = CircuitBoardConstant.SLICE_SIZE;
      if (i + 1 >= limit) {
        length = b.length % CircuitBoardConstant.SLICE_SIZE;
      }
      byte[] dest = new byte[CircuitBoardConstant.SLICE_SIZE];
      //截取数组
      System.arraycopy(b, i * CircuitBoardConstant.SLICE_SIZE, dest, 0, length);
      packList.add(dest);
    }
    if (count == 0) {
//      log.debug("b:{}", b);
      String sendString = "NNN #" + longId + " #";
      byte[] sendBytes = sendString.getBytes();
      sendToCbCtx(ctx, sendBytes); // 结束上一次的控制
      try {
        TimeUnit.MILLISECONDS.sleep(100); // 等待0.1s 让板卡复位
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      sendString = "CALL#" + longId + " #";
      sendBytes = sendString.getBytes();
      sendToCbCtx(ctx, sendBytes); // 重新连接
      log.info("the first time to send message");
    } else if (count == 1) {
      // b.length 文件字节数  传输前置
      int size = b.length / CircuitBoardConstant.SLICE_SIZE;
      byte[] packNumBytes = IntToBytes(size, 3);
      byte[] filesizeBytes = IntToBytes(b.length, 6);

      byte[] preBytes = "SIZE#".getBytes();
      byte[] sufBytes = "#".getBytes();

      byte[] sendBytes = BytesConcat(preBytes, packNumBytes, filesizeBytes, sufBytes);

      sendToCbCtx(ctx, sendBytes);
    } else if (count < limit + 2) {
      String preString = "FIL*#";
      byte[] preBytes = preString.getBytes();
      preBytes[3] = (byte) (count - 1);

      String sufString = "# #";
      byte[] sufBytes = sufString.getBytes();

      byte[] sendBytes = BytesConcat(preBytes, packList.get(count - 2), sufBytes);

      sendToCbCtx(ctx, sendBytes);
      log.info("第 " + count + " 次发送数据 " + packList.get(count - 2).length * 2);
    } else {
      log.info("烧录完毕");
    }
  }

  public static void sendButtonStringToCB(ChannelHandlerContext ctx, String buttonString) {
//    byte[] buttonBytes = StringToBytes(buttonString);
    String preString = "CTR#";
    byte[] preBytes = preString.getBytes();
    String sufString = "#";
    byte[] sufBytes = sufString.getBytes();
    byte[] sendBytes = BytesConcat(preBytes, buttonString.getBytes(), sufBytes);
    sendToCbCtx(ctx, sendBytes);
  }

  public static void sendEndToCB(ChannelHandlerContext ctx, String longId) {
    String sendString = "NNN #" + longId + " #";
    byte[] sendBytes = sendString.getBytes();
    sendToCbCtx(ctx, sendBytes); // 结束上一次的控制
  }

  public static String processButtonString(String switchButtonStatus, String tapButtonStatus) {
//    byte[] finalBytes = new byte[4 + 1];
    StringBuilder finalString = new StringBuilder();
    if (switchButtonStatus.length() > 32) {
      log.error("开关状态字符串超长");
    } else if (switchButtonStatus.length() == 32) {
      finalString.append(HexUtil.binaryToHex(switchButtonStatus));
//      System.arraycopy(HexUtil.binaryToHexBytes(switchButtonStatus), 0, finalBytes, 0, 4);
    } else {
      log.error("开关状态字符串缺失:{}", switchButtonStatus.length());
    }

    if (tapButtonStatus.length() == 6) {
      tapButtonStatus = tapButtonStatus + "00";
    }
    tapButtonStatus = "0000" + tapButtonStatus;
    if (tapButtonStatus.length() == 12) {
      finalString.append(HexUtil.binaryToHex(tapButtonStatus));
//      System.arraycopy(HexUtil.binaryToHexBytes(tapButtonStatus), 0, finalBytes, 4, 1);
    } else {
      log.error("tapButtonStatus 长度异常: {}", tapButtonStatus.length());
    }
//    return finalBytes;
    return finalString.toString();
  }

  public static void sendToCbCtx(ChannelHandlerContext ctx, byte[] sendBytes) {
    int checkv = 0;
    for (byte i : sendBytes) {
      checkv += (i & 0xFF);  // 使用无符号值计算
    }

//    byte[] checksumBytes = new byte[3];
//    checksumBytes[0] = (byte) ((checkv >> 16) & 0xFF);  // 最高8位
//    checksumBytes[1] = (byte) ((checkv >> 8) & 0xFF);   // 中间8位
//    checksumBytes[2] = (byte) (checkv & 0xFF);          // 最低8位

    // 合并原始数据
    byte[] resultBytes = sendBytes;
//    System.arraycopy(sendBytes, 0, resultBytes, 0, sendBytes.length);
//    System.arraycopy(checksumBytes, 0, resultBytes, sendBytes.length, checksumBytes.length);

    // 发送原始字节数据
    ctx.writeAndFlush(Unpooled.wrappedBuffer(resultBytes));

    // 打印完整发送数据的十六进制（包括校验和）
    StringBuilder hexDebug = new StringBuilder();
    for (byte b : resultBytes) {
      hexDebug.append(String.format("%02X ", b));
    }
    log.debug("send data (hex): {}", hexDebug.toString().trim());
  }
}
