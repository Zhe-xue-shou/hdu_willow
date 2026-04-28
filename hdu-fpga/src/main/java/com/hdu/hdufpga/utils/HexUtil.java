package com.hdu.hdufpga.utils;

public class HexUtil {
  public static String binaryToHex(String binary) {
    StringBuilder hex = new StringBuilder();
    for (int i = 0; i < binary.length(); i += 4) {
      String fourBits = binary.substring(i, i + 4);
      int decimal = Integer.parseInt(fourBits, 2);
      String temp = Integer.toHexString(decimal);
      hex.append(temp);
    }
    return hex.toString();
  }

  public static byte[] binaryToHexBytes(String binary) {
    byte[] finalBytes = new byte[binary.length() / 8];
    for (int i = 0; i < binary.length(); i += 8) {
      String fourBits = binary.substring(i, i + 8);
      int decimal = Integer.parseInt(fourBits, 2);
      finalBytes[i / 8] = (byte) decimal;
    }
    return finalBytes;
  }
}
