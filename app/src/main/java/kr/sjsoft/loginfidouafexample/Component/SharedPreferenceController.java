package kr.sjsoft.loginfidouafexample.Component;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceController {
    public final static String FILE_ID = "STATE";
    public final static String FINGER_USED = "FINGER_USED";
    public final static String PIN_USED = "PIN_USED";
    public final static String PATTERN_USED = "PATTERN_USED";
    public final static String PIN_PASSWORD = "PIN_PW";
    public final static String PATTERN_PASSWORD = "PATTERN_PW";
    private final static String SALT = "BSJ";

    public static boolean getCheckedState(String item, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_ID, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(item, false);
    }

    public static void setCheckedState(String item, boolean isUsed, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(item, isUsed);
        editor.commit();
    }

    public static void deletePasswrod(String item, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(item);
        editor.commit();
    }

    public static void setPassword(String item, String password, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(item, Basic.sha256(password, SALT));
        editor.commit();
    }

    public static boolean isPasswordRight(String item, String input_pw, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(FILE_ID, Context.MODE_PRIVATE);
        String saved_pw = sharedPreferences.getString(item, "ERROR");
        if (saved_pw.equals(Basic.sha256(input_pw, SALT)))
            return true;
        else
            return false;
    }
}
