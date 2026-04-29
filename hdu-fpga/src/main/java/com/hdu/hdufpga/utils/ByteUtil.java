package com.hdu.hdufpga.utils;

public class ByteUtil {
  public static byte[] StringToBytes(String s) {
    if (s == null || s.isEmpty()) {
      return new byte[0];
    }

    // 长度必须是偶数
    if (s.length() % 2 != 0) {
      throw new IllegalArgumentException("Hex string length must be even");
    }

    byte[] finalBytes = new byte[s.length() / 2];

    for (int i = 0; i < s.length(); i += 2) {
      String hex = s.substring(i, i + 2);
      finalBytes[i / 2] = (byte) Integer.parseInt(hex, 16);
    }

    return finalBytes;
  }

  public static byte[] IntToBytes(long value, int length) {
    byte[] result = new byte[length];
    for (int i = 0; i < length; i++) {
      result[length - 1 - i] = (byte) ((value >> (8 * i)) & 0xFF);
    }
    return result;
  }
}