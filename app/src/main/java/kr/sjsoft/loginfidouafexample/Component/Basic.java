package kr.sjsoft.loginfidouafexample.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Basic {
    public static String sha256(String str, String salt) {
        String strWithSalt = str + salt;
        String SHA = "";
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(strWithSalt.getBytes());
            byte byteData[] = sh.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++)
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            SHA = sb.toString();
        }catch(NoSuchAlgorithmException e) { e.printStackTrace(); SHA = null; }
        return SHA;
    }
}
