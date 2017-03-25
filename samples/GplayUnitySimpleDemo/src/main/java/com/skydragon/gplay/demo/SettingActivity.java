package com.skydragon.gplay.demo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.skydragon.gplay.Gplay;
import com.skydragon.gplay.demo.utils.Utils;

public class SettingActivity extends PreferenceActivity {

    private SharedPreferences settings;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        settings = Utils.getSharedPreferences(this);

        EditTextPreference rootpath = (EditTextPreference) findPreference("rootpath");
        if (rootpath != null) {
            rootpath.setSummary(settings.getString("rootpath", Utils.getGplayDefaultCacheDir()));
            rootpath.setText(settings.getString("rootpath", Utils.getGplayDefaultCacheDir()));
        }

        rootpath.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });

        EditTextPreference channelId = (EditTextPreference)findPreference("channel_id");
        if(channelId != null) {
            channelId.setSummary(settings.getString("channel_id", Constants.DEFAULT_CHANNEL_ID));
            channelId.setText(settings.getString("channel_id", Constants.DEFAULT_CHANNEL_ID));
        }

        channelId.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });

        ListPreference productMode = (ListPreference)findPreference("product_mode");
        if( null != productMode ) {
            productMode.setSummary(settings.getString("product_mode", Gplay.getProductMode()));
        }

        productMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary((String) newValue);
                return true;
            }
        });

        final ListPreference sdk_choose = (ListPreference) findPreference("sdk_choose");

        if (sdk_choose != null) {
            String summary = GPALYH5PAYSDK;
            int key_choose = Integer.parseInt((String) settings.getString("sdk_choose", "0"));
            if(key_choose == Constants.USE_GPLAY_PAYSDK){
                summary = GPALYPAYSDK;
            }
            sdk_choose.setSummary(summary);

            sdk_choose.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int key_choose = Integer.parseInt((String) newValue);
                    String summary = GPALYH5PAYSDK;
                    preference.setSummary((String) newValue);

                    if(key_choose == Constants.USE_GPLAY_PAYSDK){
                        summary = GPALYPAYSDK;
                    }
                    sdk_choose.setSummary(summary);
                    return true;
                }
            });
        }

        final ListPreference channel_progress = (ListPreference) findPreference("channel_progress");

        if (channel_progress != null) {
            String summary = STR_TRUE;
            int key_choose = Integer.parseInt((String) settings.getString("channel_progress", "0"));
            if(key_choose == Constants.ZERO){
                summary = STR_FALSE;
            }
            channel_progress.setSummary(summary);

            channel_progress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int key_choose = Integer.parseInt((String) newValue);
                    String summary = STR_FALSE;
                    if(key_choose == Constants.ONE){
                        summary = STR_TRUE;
                    }
                    channel_progress.setSummary(summary);
                    return true;
                }
            });
        }
    }

    private final static String STR_TRUE = "true";
    private final static String STR_FALSE = "false";

    private final static String GPALYPAYSDK = "GplayPaySDK";
    private final static String GPALYH5PAYSDK = "GplayH5PaySDK";

}
