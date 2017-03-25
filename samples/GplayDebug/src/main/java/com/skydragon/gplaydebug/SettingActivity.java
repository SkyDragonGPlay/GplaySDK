package com.skydragon.gplaydebug;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.skydragon.gplaydebug.utils.Constants;
import com.skydragon.gplaydebug.utils.Utils;

/**
 * package : com.skydragon.gplaydebug
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.8 15:03.
 */
public class SettingActivity extends PreferenceActivity {
    private final static String PREFERENCE_SUFFIX = "_debug_config";
    private SharedPreferences mConfigSettings;
    public static String KEY_CHANNEL_ID = "channel_id";
    public static String KEY_GAME_KEY = "game_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.debug_config);
        mConfigSettings = Utils.getSettingSharedPreferences(getApplicationContext());
        initConfig();
    }

    private void initConfig() {
        EditTextPreference channelIdEtp = (EditTextPreference)findPreference(KEY_CHANNEL_ID);

        if(channelIdEtp != null) {
            channelIdEtp.setSummary(mConfigSettings.getString(KEY_CHANNEL_ID, Constants.DEFAULT_CHANNEL_ID));
            channelIdEtp.setText(mConfigSettings.getString(KEY_CHANNEL_ID, Constants.DEFAULT_CHANNEL_ID));
        }

        channelIdEtp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int intValue = Integer.parseInt((String) newValue);
                if(intValue < 100000 || 999999 < intValue){
                    return false;
                }

                preference.setSummary((String) newValue);
                return true;
            }
        });


        EditTextPreference gamekeyEtp = (EditTextPreference)findPreference(KEY_GAME_KEY);
        if(gamekeyEtp != null) {
            gamekeyEtp.setSummary(mConfigSettings.getString(KEY_GAME_KEY, Constants.DEFAULT_GAME_KEY));
            gamekeyEtp.setText(mConfigSettings.getString(KEY_GAME_KEY, Constants.DEFAULT_GAME_KEY));
        }

        gamekeyEtp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });
    }
}
