package kr.sjsoft.loginfidouafexample;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.security.spec.RSAKeyGenParameterSpec;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;

import kr.sjsoft.loginfidouafexample.Cipher.CipherController;
import kr.sjsoft.loginfidouafexample.Component.SharedPreferenceController;
import kr.sjsoft.loginfidouafexample.ServerSide.AuthController;


public class MainActivity extends AppCompatActivity {
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private boolean isLockedCheckedListener = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                try {
                    byte[] encryptedText = AuthController.getChallenge(MainActivity.this, CipherController.FINGER_KEY_NAME);
                    String resultText = AuthController.isAuthSuccess(new String(result.getCryptoObject().getCipher().doFinal(encryptedText)));
                    Toast.makeText(MainActivity.this, resultText, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "에러가 발생했습니다", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("지문 인증")
                .setSubtitle("기기에 등록된 지문으로 인증하십시오.")
                .setNegativeButtonText("취 소")
                .build();

        Button biometricLoginButton = findViewById(R.id.auth_finger_btn);
        biometricLoginButton.setOnClickListener(view -> {
            if (!SharedPreferenceController.getCheckedState(SharedPreferenceController.FINGER_USED, MainActivity.this)) {
                Toast.makeText(MainActivity.this, "먼저 지문을 등록하세요.", Toast.LENGTH_LONG).show();
                return;
            }

            Cipher cipher = CipherController.getCipherOfPrivateKey(MainActivity.this, CipherController.FINGER_KEY_NAME);
            if (cipher != null)
                biometricPrompt.authenticate(promptInfo,
                        new BiometricPrompt.CryptoObject(cipher));
            else
                Toast.makeText(MainActivity.this, "지문이 등록되어있지 않습니다.", Toast.LENGTH_LONG).show();
        });
    }

    public void onClickPatternBtn(View view) {
        if (SharedPreferenceController.getCheckedState(SharedPreferenceController.PATTERN_USED, MainActivity.this)==false) {
            Toast.makeText(MainActivity.this, "먼저 패턴을 등록하세요.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, PatternActivity.class);
        intent.putExtra(PatternActivity.MODE_NM, PatternActivity.AUTH_MODE);
        startActivity(intent);
    }

    public void onClickPinBtn(View view) {
        if (SharedPreferenceController.getCheckedState(SharedPreferenceController.PIN_USED, MainActivity.this)==false) {
            Toast.makeText(MainActivity.this, "먼저 PIN을 등록하세요.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, PinActivity.class);
        intent.putExtra(PinActivity.MODE_NM, PinActivity.AUTH_MODE);
        startActivity(intent);
    }

    public void initCheckBox() {
        isLockedCheckedListener = true;
        CheckBox finger, pin, pattern;
        finger = findViewById(R.id.register_finger_check);
        finger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isLockedCheckedListener)
                    return;

                if (isChecked == true) {
                    Executor executor = ContextCompat.getMainExecutor(MainActivity.this);
                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("지문 인증")
                            .setSubtitle("기기에 등록된 지문으로 인증하십시오.")
                            .setNegativeButtonText("취 소")
                            .build();
                    BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode,
                                                          @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(getApplicationContext(),
                                    "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onAuthenticationSucceeded(
                                @NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            CipherController.generateKeyPair(new KeyGenParameterSpec.Builder(
                                    CipherController.FINGER_KEY_NAME,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                                    .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                    .setUserAuthenticationRequired(true)
                                    .setInvalidatedByBiometricEnrollment(true)
                                    .build(), MainActivity.this);
                            SharedPreferenceController.setCheckedState(SharedPreferenceController.FINGER_USED, true, MainActivity.this);
                            Toast.makeText(MainActivity.this, "등록 완료", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(getApplicationContext(), "Authentication failed",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    SharedPreferenceController.setCheckedState(SharedPreferenceController.FINGER_USED, false, MainActivity.this);
                    CipherController.deleteKey(MainActivity.this, CipherController.FINGER_KEY_NAME);
                }
            }
        });
        finger.setChecked(SharedPreferenceController.getCheckedState(SharedPreferenceController.FINGER_USED, MainActivity.this));

        pin = findViewById(R.id.register_pin_check);
        pin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isLockedCheckedListener)
                    return;

                if (isChecked == true) {
                    Intent intent = new Intent(MainActivity.this, PinActivity.class);
                    intent.putExtra(PinActivity.MODE_NM, PinActivity.REGISTER_MODE);
                    startActivity(intent);
                }else {
                    SharedPreferenceController.setCheckedState(SharedPreferenceController.PIN_USED, false, MainActivity.this);
                    CipherController.deleteKey(MainActivity.this, CipherController.PIN_KEY_NAME);
                    SharedPreferenceController.deletePasswrod(SharedPreferenceController.PIN_PASSWORD, MainActivity.this);
                }
            }
        });
        pin.setChecked(SharedPreferenceController.getCheckedState(SharedPreferenceController.PIN_USED, MainActivity.this));

        pattern = findViewById(R.id.register_pattern_check);
        pattern.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if (isLockedCheckedListener)
                    return;

                if (isChecked == true) {
                    Intent intent = new Intent(MainActivity.this, PatternActivity.class);
                    intent.putExtra(PatternActivity.MODE_NM, PatternActivity.REGISTER_MODE);
                    startActivity(intent);
                }else {
                    SharedPreferenceController.setCheckedState(SharedPreferenceController.PATTERN_USED, false, MainActivity.this);
                    CipherController.deleteKey(MainActivity.this, CipherController.PATTERN_KEY_NAME);
                    SharedPreferenceController.deletePasswrod(SharedPreferenceController.PATTERN_PASSWORD, MainActivity.this);
                }
            }
        });
        pattern.setChecked(SharedPreferenceController.getCheckedState(SharedPreferenceController.PATTERN_USED, MainActivity.this));
        isLockedCheckedListener = false;
    }

    @Override
    protected void onResume() {
        initCheckBox();
        super.onResume();
    }
}