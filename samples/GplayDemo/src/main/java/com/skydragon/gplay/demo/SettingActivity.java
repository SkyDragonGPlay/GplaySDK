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
//预下载后台挂起
        final ListPreference bghang_option = (ListPreference) findPreference("bghang_option");
        if(bghang_option!=null){
            String summary = STR_OPEN;
            int key_choose = Integer.parseInt((String) settings.getString("bghang_option", "0"));
            if(key_choose == Constants.ZERO){
                summary = STR_CLOSE;
            }
            bghang_option.setSummary(summary);

            bghang_option.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int key_choose = Integer.parseInt((String) newValue);
                    String summary = STR_CLOSE;
                    if(key_choose == Constants.ONE){
                        summary = STR_OPEN;
                    }
                    bghang_option.setSummary(summary);
                    return true;
                }
            });
        }
//预下载策略
        final ListPreference predowngame_strategy = (ListPreference) findPreference("predowngame_strategy");
        if(predowngame_strategy!=null){
            String summary = PRD_STRATEGY2;
            int key_choose = Integer.parseInt((String) settings.getString("predowngame_strategy", "2"));
            if(key_choose == Constants.TWO){
                summary = PRD_STRATEGY2;
            }else if(key_choose == Constants.ONE){
                summary = PRD_STRATEGY1;
            }else if(key_choose == Constants.THREE){
                summary = PRD_STRATEGY3;
            }else if(key_choose == Constants.FOUR){
                summary = PRD_STRATEGY4;
            }

            predowngame_strategy.setSummary(summary);

            predowngame_strategy.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    int key_choose = Integer.parseInt((String) newValue);
                    String summary = PRD_STRATEGY1;
                    if(key_choose == Constants.ONE){
                        summary = PRD_STRATEGY1;
                    }if(key_choose == Constants.TWO){
                        summary = PRD_STRATEGY2;
                    }if(key_choose == Constants.THREE){
                        summary = PRD_STRATEGY3;
                    }if(key_choose == Constants.FOUR){
                        summary = PRD_STRATEGY4;
                    }
                    predowngame_strategy.setSummary(summary);
                    return true;
                }
            });
        }
    }

    private final static String STR_TRUE = "true";
    private final static String STR_FALSE = "false";

    private final static String STR_OPEN = "打开";
    private final static String STR_CLOSE = "关闭";

    private final static String PRD_STRATEGY1 = "加载游戏首包";
    private final static String PRD_STRATEGY2 = "加载完整游戏";
    private final static String PRD_STRATEGY3 = "加载指定资源包";
    private final static String PRD_STRATEGY4 = "先加载指定资源包，完成后继续加载其他资源包";

    private final static String GPALYPAYSDK = "GplayPaySDK";
    private final static String GPALYH5PAYSDK = "GplayH5PaySDK";

}
