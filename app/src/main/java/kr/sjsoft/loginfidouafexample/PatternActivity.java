package kr.sjsoft.loginfidouafexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;

import java.security.spec.RSAKeyGenParameterSpec;
import java.util.List;

import kr.sjsoft.loginfidouafexample.Cipher.CipherController;
import kr.sjsoft.loginfidouafexample.Component.SharedPreferenceController;
import kr.sjsoft.loginfidouafexample.ServerSide.AuthController;

public class PatternActivity extends AppCompatActivity {
    private int mode = -1;
    public final static int REGISTER_MODE = 1;
    public final static int AUTH_MODE = 2;
    public final static String MODE_NM = "MODE";

    private int step = -1;
    private final static int REGISTER_FIRST_STEP = 1;
    private final static int REGISTER_SECOND_STEP = 2;
    private String tempPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);

        Intent intent = getIntent();
        mode = intent.getIntExtra(MODE_NM, -1);
        step = REGISTER_FIRST_STEP;

        TextView textView = findViewById(R.id.pin_guide_text);

        PatternLockView patternLockView = findViewById(R.id.patternView);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List progressPattern) {

            }

            @Override
            public void onComplete(List pattern) {
                String pw = "";
                for (int i=0; i<pattern.size(); i++)
                    pw+=pattern.get(i);
                if (pattern.size() < 4 && mode == REGISTER_MODE) {
                    Toast.makeText(PatternActivity.this, "패턴은 최소 4개의 점을 사용해야 합니다.", Toast.LENGTH_LONG).show();
                    patternLockView.clearPattern();
                    return;
                }
                if (mode == REGISTER_MODE && step == REGISTER_FIRST_STEP) {
                    tempPattern = pw;
                    textView.setText("패턴을 한 번 더 입력해주세요.");
                    patternLockView.clearPattern();
                    step = REGISTER_SECOND_STEP;
                } else if (mode == REGISTER_MODE && step == REGISTER_SECOND_STEP) {
                    if (tempPattern.equals(pw)) {
                        SharedPreferenceController.setCheckedState(SharedPreferenceController.PATTERN_USED, true, PatternActivity.this);
                        SharedPreferenceController.setPassword(SharedPreferenceController.PATTERN_PASSWORD, pw, PatternActivity.this);
                        Toast.makeText(PatternActivity.this, "PIN 등록 완료", Toast.LENGTH_LONG).show();
                        CipherController.generateKeyPair(new KeyGenParameterSpec.Builder(
                                CipherController.PATTERN_KEY_NAME,
                                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                .build(), PatternActivity.this);
                        finish();
                    } else {
                        Toast.makeText(PatternActivity.this, "패턴이 다릅니다. 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                        patternLockView.clearPattern();
                    }
                } else if (mode==AUTH_MODE && SharedPreferenceController.isPasswordRight(SharedPreferenceController.PATTERN_PASSWORD, pw, PatternActivity.this)) {
                    byte[] encryptedText = AuthController.getChallenge(PatternActivity.this, CipherController.PATTERN_KEY_NAME);
                    String resultText = AuthController.isAuthSuccess(CipherController.decrypt(encryptedText, PatternActivity.this, CipherController.PATTERN_KEY_NAME));
                    Toast.makeText(PatternActivity.this, resultText, Toast.LENGTH_LONG).show();
                    finish();
                } else if (mode==AUTH_MODE && !SharedPreferenceController.isPasswordRight(SharedPreferenceController.PATTERN_PASSWORD, pw, PatternActivity.this)) {
                    Toast.makeText(PatternActivity.this, "등록된 패턴과 입력 패턴이 다릅니다.", Toast.LENGTH_LONG).show();
                    patternLockView.clearPattern();
                }
            }

            @Override
            public void onCleared() {

            }
        });
    }
}