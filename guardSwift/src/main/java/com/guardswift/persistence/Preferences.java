package com.guardswift.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.common.collect.Sets;
import com.guardswift.dagger.InjectingApplication.InjectingApplicationModule.ForApplication;

import java.util.Set;

import javax.inject.Inject;

public class Preferences {

    private final String TAG = Preferences.class.getName();

    protected final SharedPreferences sharedPrefs;
    private final Context context;

    @Inject
    public Preferences(@ForApplication Context context) {
        // getApplicationContext() in case it is created manually
        this.context = context.getApplicationContext();
        this.sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public Preferences(Context context, String name) {
        this.context = context.getApplicationContext();
        this.sharedPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public void clear() {
         {
            sharedPrefs.edit().clear().apply();
        }
    }

    public boolean has(String key) {
         {
            return sharedPrefs.contains(key);
        }
    }

    public void put(int resKey, int value) {
         {
            String key = context.getString(resKey);
            sharedPrefs.edit().putInt(key, value).apply();
        }
    }

    public void put(@NonNull String key, int value) {
         {
            sharedPrefs.edit().putInt(key, value).apply();
        }
    }

    public int getInt(int resource, int defaultValue) {
         {
            return sharedPrefs.getInt(context.getString(resource), defaultValue);
        }
    }

    public void putBoolean(@NonNull  String key, boolean value) {
         {
            sharedPrefs.edit().putBoolean(key, value).apply();
        }
    }

    public boolean getBoolean(@NonNull String key) {
         {
            return sharedPrefs.getBoolean(key, false);
        }
    }

    public boolean getBoolean(int resource) {
         {
            String key = context.getString(resource);
            return getBoolean(key);
        }
    }

    public boolean getBoolean(@NonNull String key, boolean b) {
         {
            return sharedPrefs.getBoolean(key, b);
        }
    }

    public void put(int resKey, String value) {
         {
            String key = context.getString(resKey);
            sharedPrefs.edit().putString(key, value).apply();
        }
    }

    public void put(String key, String value) {
         {
            sharedPrefs.edit().putString(key, value).apply();
        }
    }

    public String getString(int resKey) {
         {
            return sharedPrefs.getString(context.getString(resKey), "");
        }
    }

    public String getString(@NonNull String key) {
         {
            return getString(key, "");
        }
    }

    public String getString(@NonNull String key, String defaultValue) {
         {
            return sharedPrefs.getString(key, defaultValue);
        }
    }


    public int getInt(@NonNull String key, int defaultValue) {
         {
            return sharedPrefs.getInt(key, defaultValue);
        }
    }


    public void addUnique(@NonNull String key, String value) {
         {
            Set<String> currentSet = getStringSet(key);
            currentSet.add(value);
            sharedPrefs.edit().putStringSet(key, currentSet).apply();
        }
    }

    public void removeUnique(@NonNull String key, String value) {
         {
            Set<String> currentSet = getStringSet(key);
            currentSet.remove(value);
            sharedPrefs.edit().putStringSet(key, currentSet).apply();
        }
    }

    public void clearSet(@NonNull String key) {
         {
            sharedPrefs.edit().putStringSet(key, Sets.<String>newConcurrentHashSet()).apply();
        }
    }

    public Set<String> getStringSet(@NonNull String key) {
         {
            Set<String> defaultValue = Sets.newConcurrentHashSet();
            return sharedPrefs.getStringSet(key, defaultValue);
        }
    }

    public void remove(String key) {
         {
            sharedPrefs.edit().remove(key).apply();
        }
    }
}
