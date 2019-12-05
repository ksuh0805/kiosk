package com.samilcts.util.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;


/**
 * SharedPreference wrapper class
 * use Default Preference
 * Created by mskim on 2015-07-31.
 */
public class Preference {

    private static Preference preference;

    private static SharedPreferences pref;


    public static Preference getInstance(Context context) {

        if ( preference == null) {

            return new Preference(context);
        }

        return preference;

    }

    private Preference(Context context) {

        pref =  PreferenceManager.getDefaultSharedPreferences(context);

       /* pref = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);*/
    }

    public boolean set(String key, String value) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean set(String key, boolean value) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public boolean set(String key, float value) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public boolean set(String key, int value) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean set(String key, long value) {

        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        return editor.commit();
    }


    /**
     * since android api 11
     * @param key key
     * @param value value
     * @return true, if committed
     */

    public boolean set(String key, Set<String> value) {

        SharedPreferences.Editor editor = pref.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet(key, value);
            return editor.commit();
        } else {

            return false;
        }

    }

    public Map<String,?> getAll() {

        return pref.getAll();

    }

    public String get(String key, String defValue) {

        return pref.getString(key, defValue);

    }

    public boolean get(String key, boolean defValue) {

        return pref.getBoolean(key, defValue);

    }

    public float get(String key, float defValue) {

        return pref.getFloat(key, defValue);

    }
    public long get(String key, long defValue) {

        return pref.getLong(key, defValue);

    }
    public int get(String key, int defValue) {

        return pref.getInt(key, defValue);

    }

    /**
     * Added in Android API level 11
     * @param key key
     * @param defValue default value
     * @return Set of values
     *
     */
    public Set<String> get (String key, Set<String> defValue) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return pref.getStringSet(key, defValue);
        } else {
            return defValue;
        }
    }


}
