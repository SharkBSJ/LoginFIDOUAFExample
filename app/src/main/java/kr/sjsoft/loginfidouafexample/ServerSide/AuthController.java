package kr.sjsoft.loginfidouafexample.ServerSide;

import android.content.Context;

import kr.sjsoft.loginfidouafexample.Cipher.CipherController;

public class AuthController {
    public static String challenge = "TEST123";
    public static String LOG_TAG = "AuthController";

    public static byte[] getChallenge(Context context, String key_nm) {
        // byte[] encrypted = CipherController.encrypt(challenge, context);
        String publicKey = CipherController.getPublicKeyString(context, key_nm);
        byte[] encrypted = CipherController.encryptWithStringKey(challenge, publicKey, context);

        //Log.d(LOG_TAG, encrypted.toString());
        return encrypted;
    }

    public static String isAuthSuccess(String response) {
        //Log.d(LOG_TAG, response);
        if (response.equals(challenge))
            return "로그인 성공";
        else
            return "로그인 실패";
    }
}
