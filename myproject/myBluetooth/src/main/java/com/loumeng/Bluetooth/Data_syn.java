package com.loumeng.Bluetooth;
public class Data_syn {

    // 将字节数组转化为16进制字符串，确定长度
    public static String bytesToHexString(byte[] bytes, int a) {
        String result = "";
        for (int i = 0; i < a; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);// 将高24位置0
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }
    // 将字节数组转化为16进制字符串，不确定长度
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i =0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
    // 将16进制字符串转化为字节数组
    public static byte[] hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;

        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + paramString.substring(j * 2, k)
                            + paramString.substring(k, l)).intValue());
            ++j;
        }
    }



}
