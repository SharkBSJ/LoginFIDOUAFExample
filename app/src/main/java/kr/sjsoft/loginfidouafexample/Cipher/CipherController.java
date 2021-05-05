package kr.sjsoft.loginfidouafexample.Cipher;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class CipherController {
    public static final String FINGER_KEY_NAME = "TEST_KEY";
    public static final String PATTERN_KEY_NAME = "TESST_KEY2";
    public static final String PIN_KEY_NAME = "TESST_KEY3";

    private static final String LOG_TAG = "CipherController";
    private static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void generateKeyPair(KeyGenParameterSpec keyGenParameterSpec, Context context) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance( KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
            keyPairGenerator.initialize(keyGenParameterSpec);
            KeyPair kp = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    public static byte[] encrypt(String text, Context context, String key_nm) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(key_nm).getPublicKey();
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            Log.d(LOG_TAG,"publicKey1"+publicKey);
            String pubkey3 = publicKey.getEncoded().toString();
            Log.d(LOG_TAG,"publicKey3"+ pubkey3 + "\t" +pubkey3.length());
            Log.d(LOG_TAG,"publicKey2"+ Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT) + "\t" + Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT).length());
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            byte[] publicKeyBytes = Base64.encode(publicKey.getEncoded(),0);
            String pubKey = new String(publicKeyBytes);
            Log.d(LOG_TAG,"publicKey4"+ pubKey + " \t" + pubKey.length() + "\t" + publicKeyBytes.length);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(Charset.defaultCharset()));
            Log.d(LOG_TAG, "encryt text"+encryptedBytes.toString());
            return encryptedBytes;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static String decrypt(byte[] encryptedText, Context context, String key_nm) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(key_nm, null);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedText = cipher.doFinal(encryptedText);
            Log.d(LOG_TAG, "end decrypt" + decryptedText.toString());
            return new String(decryptedText);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static Cipher getCipherOfPrivateKey(Context context, String key_nm) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(key_nm, null);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static String getPublicKeyString(Context context, String key_nm) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(key_nm).getPublicKey();
            byte[] publicKeyBytes = Base64.encode(publicKey.getEncoded(),0);
            String pubKey = new String(publicKeyBytes);
            return pubKey;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static byte[] encryptWithStringKey(String text, String publicKeyString, Context context) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA).generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyString, 0)));
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes(Charset.defaultCharset()));
            return encryptedBytes;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public static void deleteKey(Context context, String keyName) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(keyName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "에러가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }
}
