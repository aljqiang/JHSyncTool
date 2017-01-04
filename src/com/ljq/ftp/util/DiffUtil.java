package com.ljq.ftp.util;

import java.io.*;
import java.security.MessageDigest;

/**
 * 判断两个文件是否相同(MD5)
 * User: Larry Lai
 * Date: 2017-01-04
 * Time: 9:18
 * Version: 1.0
 */

public class DiffUtil {

    public static void main(String[] args) {
        File file1=new File("Y:\\20170103\\5826_094353");
        File file2=new File("E:\\voicerecord-history\\lyvoc\\qlgj\\20170103\\5827_093328.wav");
        System.out.println(DiffUtil.check(file1,file2));
    }

    /**
     * 判断两个文件是否相同
     *
     * @param file1
     * @param file2
     * @return
     */
    public static boolean check(File file1, File file2) {
        boolean isSame = false;
        String img1Md5 = getMD5(file1);
        String img2Md5 = getMD5(file2);
        if (img1Md5.equals(img2Md5)) {
            isSame = true;
        } else {
            isSame = false;
        }
        return isSame;
    }

    public static byte[] getByte(File file) {
        // 得到文件长度
        byte[] b = new byte[(int) file.length()];
        try {
            InputStream in = new FileInputStream(file);
            try {
                in.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return b;
    }

    public static String getMD5(byte[] bytes) {
        // 16进制字符
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] strTemp = bytes;
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            // 移位 输出字符串
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMD5(File file) {
        return getMD5(getByte(file));
    }
}
