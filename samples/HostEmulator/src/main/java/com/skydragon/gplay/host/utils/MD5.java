package com.skydragon.gplay.host.utils;

import java.security.MessageDigest;

public class MD5 {
    
    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    public static String fromStr(String strIn) {
        String strOut = null;
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(strIn.getBytes());
            strOut = toHexString(md5.digest());   
            strOut = strOut.toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strOut;
    }
}
