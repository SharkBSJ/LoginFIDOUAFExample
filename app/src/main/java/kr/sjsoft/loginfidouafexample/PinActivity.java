package kr.sjsoft.loginfidouafexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import java.security.spec.RSAKeyGenParameterSpec;

import kr.sjsoft.loginfidouafexample.Cipher.CipherController;
import kr.sjsoft.loginfidouafexample.Component.SharedPreferenceController;
import kr.sjsoft.loginfidouafexample.ServerSide.AuthController;

public class PinActivity extends AppCompatActivity {
    private int mode = -1;
    public final static int REGISTER_MODE = 1;
    public final static int AUTH_MODE = 2;
    public final static String MODE_NM = "MODE";

    private int step = -1;
    private final static int REGISTER_FIRST_STEP = 1;
    private final static int REGISTER_SECOND_STEP = 2;
    private String tempPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        PinLockView mPinLockView = (PinLockView) findViewById(R.id.pin_lock_view);

        Intent intent = getIntent();
        mode = intent.getIntExtra(MODE_NM, -1);
        step = REGISTER_FIRST_STEP;

        TextView textView = findViewById(R.id.pin_guide_text);

        PinLockListener mPinLockListener = new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (mode == REGISTER_MODE && step == REGISTER_FIRST_STEP) {
                    tempPin = pin;
                    textView.setText("PIN을 한 번 더 입력해주세요.");
                    mPinLockView.resetPinLockView();
                    step = REGISTER_SECOND_STEP;
                } else if (mode == REGISTER_MODE && step == REGISTER_SECOND_STEP) {
                    if (tempPin.equals(pin)) {
                        SharedPreferenceController.setCheckedState(SharedPreferenceController.PIN_USED, true, PinActivity.this);
                        SharedPreferenceController.setPassword(SharedPreferenceController.PIN_PASSWORD, pin, PinActivity.this);
                        Toast.makeText(PinActivity.this, "PIN 등록 완료", Toast.LENGTH_LONG).show();
                        CipherController.generateKeyPair(new KeyGenParameterSpec.Builder(
                                CipherController.PIN_KEY_NAME,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                .build(), PinActivity.this);
                        finish();
                    } else {
                        Toast.makeText(PinActivity.this, "PIN 번호가 다릅니다. 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                        mPinLockView.resetPinLockView();
                    }
                } else if (mode==AUTH_MODE && SharedPreferenceController.isPasswordRight(SharedPreferenceController.PIN_PASSWORD, pin, PinActivity.this)) {
                    byte[] encryptedText = AuthController.getChallenge(PinActivity.this, CipherController.PIN_KEY_NAME);
                    String resultText = AuthController.isAuthSuccess(CipherController.decrypt(encryptedText, PinActivity.this, CipherController.PIN_KEY_NAME));
                    Toast.makeText(PinActivity.this, resultText, Toast.LENGTH_LONG).show();
                    finish();
                } else if (mode==AUTH_MODE && !SharedPreferenceController.isPasswordRight(SharedPreferenceController.PIN_PASSWORD, pin, PinActivity.this)) {
                    Toast.makeText(PinActivity.this, "등록된 PIN과 입력 PIN이 다릅니다.", Toast.LENGTH_LONG).show();
                    mPinLockView.resetPinLockView();
                }
            }

            @Override
            public void onEmpty() {
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
            }
        };

        IndicatorDots mIndicatorDots = (IndicatorDots) findViewById(R.id.indicator_dots);
        mPinLockView.attachIndicatorDots(mIndicatorDots);
        mPinLockView.setPinLockListener(mPinLockListener);
    }
}