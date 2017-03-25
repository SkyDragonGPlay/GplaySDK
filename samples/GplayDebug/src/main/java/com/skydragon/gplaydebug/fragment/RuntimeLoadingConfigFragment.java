package com.skydragon.gplaydebug.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.skydragon.gplay.IGplayService;
import com.skydragon.gplay.callback.OnPrepareRuntimeListener;
import com.skydragon.gplay.channel.plugin.h5.GplayChannelPayH5SDKPlugin;
import com.skydragon.gplay.constants.FileConstants;
import com.skydragon.gplay.loader.DexLoader;
import com.skydragon.gplay.runtime.callback.ProtocolCallback;
import com.skydragon.gplay.runtime.entity.ResultInfo;
import com.skydragon.gplay.runtime.entity.game.GameInfo;
import com.skydragon.gplay.runtime.protocol.ProtocolController;
import com.skydragon.gplay.runtime.utils.FileUtils;
import com.skydragon.gplay.service.IRuntimeBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKBridge;
import com.skydragon.gplay.thirdsdk.IChannelSDKServicePlugin;
import com.skydragon.gplaydebug.HostActivity;
import com.skydragon.gplaydebug.R;
import com.skydragon.gplaydebug.SettingActivity;
import com.skydragon.gplaydebug.utils.Constants;
import com.skydragon.gplaydebug.utils.GplayDebugDialog;
import com.skydragon.gplaydebug.utils.JsonFormator;
import com.skydragon.gplaydebug.utils.LoadingDialog;
import com.skydragon.gplaydebug.utils.ReflectUtil;
import com.skydragon.gplaydebug.utils.SelectorDialog;
import com.skydragon.gplaydebug.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cc.rooho.pageswitcher.AppCompatActivityBase;
import cc.rooho.pageswitcher.switcher.FragmentSwitcher;
import cc.rooho.pageswitcher.switcher.IFragmentHooker;

/**
 * package : com.skydragon.gplaydebug.fragment
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.8 11:34.
 */
public class RuntimeLoadingConfigFragment extends Fragment implements IFragmentHooker {

    private static final int MSG_DELETE_GAME_RES = 0;
    private TextView mTextChannelId;
    private TextView mTextGameKey;
    String mCacheDir;
    private IChannelSDKServicePlugin ichannelServicePluginProxy = null;
    private FragmentSwitcher mFragmentSwitcher;
    private View mRootView;
    private Object mGplayServiceImpl;
    private GameInfo mGameInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // init base config
        ichannelServicePluginProxy = new GplayChannelPayH5SDKPlugin();
        mCacheDir = Environment.getExternalStorageDirectory() + "/gplay_debug";
        FileConstants.init(this.getActivity());

        ((AppCompatActivityBase) getActivity()).setTitle(getString(R.string.title_loading_runtime_and_config));
        ((AppCompatActivityBase) getActivity()).getSupportActionBar().show();
        mRootView = inflater.inflate(R.layout.fragment_runtime_loading, null);
        mTextChannelId = (TextView) mRootView.findViewById(R.id.game_info_channel_id);
        mTextGameKey = (TextView) mRootView.findViewById(R.id.game_info_game_key);
        String channelIdStr = Utils.getSettingSharedPreferences(getActivity()).getString(SettingActivity.KEY_CHANNEL_ID, Constants.DEFAULT_CHANNEL_ID);
        mTextChannelId.setText(channelIdStr);
        String gameKeyStr = Utils.getSettingSharedPreferences(getActivity()).getString(SettingActivity.KEY_GAME_KEY, Constants.DEFAULT_GAME_KEY);
        mTextGameKey.setText(gameKeyStr);

        Button loadandinitbtn = (Button) mRootView.findViewById(R.id.load_and_init_runtime);
        Button nextResBtn = (Button) mRootView.findViewById(R.id.next_step_runtime_res);
        Button configDebugBtn = (Button) mRootView.findViewById(R.id.config_gplay_debug);
        Button cleanRuntimeBtn = (Button) mRootView.findViewById(R.id.clean_runtime_res);
        Button cleanResBtn = (Button) mRootView.findViewById(R.id.clean_game_res);
        Button loadConfigByIdAndKey = (Button) mRootView.findViewById(R.id.game_info_by_idandkey);

        mTextGameKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGameKeyClick();
            }
        });

        loadandinitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadandInitRuntimel();
            }
        });

        loadConfigByIdAndKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConfigByIdAndKey();
            }
        });

        nextResBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onnNxtResBtnClicked();
            }
        });

        configDebugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onnConfigDebugBtnClicked();
            }
        });

        cleanRuntimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onnCleanRuntimeBtnClicked();
            }
        });

        cleanResBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCleanGameResBtnClicked();
            }
        });

        return mRootView;
    }

    private void onnConfigDebugBtnClicked() {
        startActivity(new Intent(getActivity(), SettingActivity.class));
    }

    private void onnCleanRuntimeBtnClicked() {
        LoadingDialog.showLoadingDialog(this.getActivity(), getString(R.string.cleaning), 300, new LoadingDialog.TimeoutListener() {
            @Override
            public void onTimeout() {}
        });
        // 删除原有 Runtime 信息
        com.skydragon.gplay.utils.FileUtils.deleteDir(FileConstants.getLibsDir());
        mGplayServiceImpl = null;
        ((HostActivity)getActivity()).mGplayServiceImpl = null;
        mGameInfo = null;
    }

    private void onCleanGameResBtnClicked() {

        String debugDirPath = Constants.getGplayDebugDefaultDir();
        File debugDir = new File(debugDirPath);

        File[] files;;
        if(!debugDir.exists() || (files = debugDir.listFiles()).length <= 0){
            GplayDebugDialog.showDialog(getActivity(), getString(R.string.no_downloaded_game), null, null, null);
        } else {
            CharSequence[] items = new CharSequence[files.length];
            for(int i = 0; i < files.length; i++){
                items[i] = files[i].getName();
            }
            SelectorDialog.showMultiChoiceDialog(getActivity(), items, new SelectorDialog.OnSelectedListener() {
                @Override
                public void onItemSelected(CharSequence[] items) {
                    new DeleteGameRes(items).start();
                }
            });
        }
    }

    private class DeleteGameRes extends Thread {
        private CharSequence[] items;
        public DeleteGameRes(CharSequence[] items){
            this.items = items;
        }

        @Override
        public void run() {
            mHander.post(new Runnable() {
                @Override
                public void run() {

                    LoadingDialog.showLoadingDialog(getActivity(), getString(R.string.cleaning), 2000, null);
                }
            });
            for(CharSequence item : items){
                File dir = new File(Constants.getGplayDebugDefaultDir() + File.separator + item);
                if(dir.exists()){
                    try {
                        FileUtils.deleteFile(dir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void onGameKeyClick() {
        if(mGplayServiceImpl == null){
            showMsg("please load runtime!!");
            return;
        }

        LoadingDialog.showLoadingDialog(this.getActivity(), getString(R.string.searching), 6000, new LoadingDialog.TimeoutListener() {
            @Override
            public void onTimeout() {}
        });

        ProtocolController.requestChannelGameList(mTextChannelId.getText().toString(), 0, 1024, new ProtocolCallback<String>() {
            @Override
            public void onSuccess(String obj) {
                LoadingDialog.closeLoadingDialog();
                final Map<String, String> gameMap = new HashMap<String, String>();
                try {
                    JSONObject jsonObject = new JSONObject(obj);
                    JSONArray gameArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < gameArray.length(); i ++) {
                        JSONObject gameObj = gameArray.getJSONObject(i);

                        String key = gameObj.getString("game_name");
                        String gameKey = gameObj.getString("client_id");
                        Log.v("RuntimeLoading", "key " + key + "  ,  " + gameKey);
                        gameMap.put(key, gameKey);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(gameMap.size() <= 0){
                    showMsg(getString(R.string.search_failure));
                    return;
                }

                showGameListDialog(gameMap, new SelectorDialog.OnSelectedListener() {
                    @Override
                    public void onItemSelected(CharSequence[] items) {
                        if(items.length > 0){
                            String gameId = gameMap.get(items[0]);
                            mTextGameKey.setText(gameId);
                        }
                    }
                });
            }

            @Override
            public void onFailure(ResultInfo err) {
                LoadingDialog.closeLoadingDialog();
                showMsg(getString(R.string.search_failure));
            }
        });
    }

    private void showGameListDialog(final Map<String, String> gameMap, SelectorDialog.OnSelectedListener listener) {

        CharSequence[] keyArrays = new CharSequence[gameMap.size()];
        int i = 0;
        int defaultIndex = 0;
        for(Iterator<String> ite = gameMap.keySet().iterator(); ite.hasNext(); ){
            keyArrays[i] = ite.next();

            if(gameMap.get(keyArrays[i]).equals(mTextGameKey.getText().toString())){
                defaultIndex = i;
            }
            i++;
        }

        SelectorDialog.showSingleSelectorDialog(getActivity(), keyArrays, defaultIndex, listener);
    }

    private void loadandInitRuntimel() {
        mGplayServiceImpl = null;
        LoadingDialog.showLoadingDialog(this.getActivity(), getString(R.string.loading), 1000, new LoadingDialog.TimeoutListener() {
            @Override
            public void onTimeout() {}
        });

        setDebugLoadingModel(true);
        IRuntimeBridge runtimeBridge = DexLoader.loadRuntimeBridgeImpl();

        if(runtimeBridge == null){
            GplayDebugDialog.showDialog(getActivity(), getString(R.string.runtime_load_fail), null, null, null);
            return;
        }

        mGplayServiceImpl = runtimeInit(runtimeBridge);

        ((HostActivity)getActivity()).mGplayServiceImpl = mGplayServiceImpl;
        if(mGplayServiceImpl != null){
            GplayDebugDialog.showDialog(getActivity(), getString(R.string.runtime_init_success), null, null, null);
        } else {
            GplayDebugDialog.showDialog(getActivity(), getString(R.string.runtime_init_failure), null, null, null);
        }
    }

    private void loadConfigByIdAndKey(){

        if(mGplayServiceImpl == null){
            GplayDebugDialog.showDialog(getActivity(), "Runtime 未加载成功！", null, null, null);
            return;
        }

        LoadingDialog.showLoadingDialog(this.getActivity(), getString(R.string.loading), 5000, new LoadingDialog.TimeoutListener() {
            @Override
            public void onTimeout() {
            }
        });

        String gameKey = mTextGameKey.getText().toString();
        String channelId = mTextChannelId.getText().toString();

        ProtocolController.requestGameInfoByKey(channelId, gameKey, new ProtocolCallback<GameInfo>() {
            @Override
            public void onSuccess(GameInfo obj) {
                LoadingDialog.closeLoadingDialog();
                String gameInfoStr = JsonFormator.formatJson(obj.toJson().toString());
                GplayDebugDialog.showDialog(getActivity(), getString(R.string.dialog_title_game_info), gameInfoStr, null, null, null);
                mGameInfo = obj;
            }

            @Override
            public void onFailure(ResultInfo err) {
                LoadingDialog.closeLoadingDialog();
                GplayDebugDialog.showDialog(getActivity(), getString(R.string.dialog_title_game_info), err.getMsg(), null, null, null);
            }
        });
    }

    private Object runtimeInit(final IRuntimeBridge runtimeBridge){
        final String channelId = mTextChannelId.getText().toString();

        Object serviceImpl = ReflectUtil.newInstance("com.skydragon.gplay.GplayServiceImpl",
                new Class[]{ IRuntimeBridge.class }, new Object[]{ runtimeBridge });

        Context context = getActivity();
        IChannelSDKServicePlugin channelSDKServicePlugin = new GplayChannelPayH5SDKPlugin();
        String runtimeDir = Constants.getGplayRuntimeDefaultDir(context);
        String debugDir = Constants.getGplayDebugDefaultDir();

        ReflectUtil.invokeMethod(serviceImpl, "init"
                , new Class[]{ Context.class, String.class, String.class, String.class, IChannelSDKBridge.class, IChannelSDKServicePlugin.class, OnPrepareRuntimeListener.class }
                , new Object[]{ context, channelId , runtimeDir, debugDir, null, channelSDKServicePlugin, mPrepareRuntimeListener});

        return serviceImpl;
    }

    private OnPrepareRuntimeListener mPrepareRuntimeListener = new OnPrepareRuntimeListener() {
        @Override
        public void onPrepareRuntimeStart() { }

        @Override
        public void onPrepareRuntimeProgress(long downloadedSize, long totalSize) { }

        @Override
        public void onPrepareRuntimeSuccess(IGplayService service) { }

        @Override
        public void onPrepareRuntimeFailure(String msg) { }

        @Override
        public void onPrepareRuntimeCancel() { }
    };

    private void setDebugLoadingModel(Boolean debug) {
        // 设置为加载本地资源的模式。
        try {
            Field debugField = ReflectUtil.getField(DexLoader.class, "DEBUG");
            debugField.set(null, debug);

            Log.v("RuntimeLoaderFragment", "setDebugLoadingModel  DEBUG value  " + debugField.getBoolean(null));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void onnNxtResBtnClicked() {
        if(mGplayServiceImpl == null || mGameInfo == null){
            GplayDebugDialog.showDialog(getActivity(), "Runtime 或游戏信息未加载成功！", null, null, null);
            return;
        }

        GameResDownloadFragment grsdf = new GameResDownloadFragment();
        grsdf.setGameInfo(mGameInfo);
        mFragmentSwitcher.switchFragment(grsdf, FragmentSwitcher.MODE_PUSH_STACK);
    }

    @Override
    public void onResume() {
        super.onResume();
        String channelIdStr = Utils.getSettingSharedPreferences(getActivity()).getString(SettingActivity.KEY_CHANNEL_ID, Constants.DEFAULT_CHANNEL_ID);
        mTextChannelId.setText(channelIdStr);
    }

    @Override
    public void hookSwitcher(FragmentSwitcher fragmentSwitcher) {
        mFragmentSwitcher = fragmentSwitcher;
    }

    private void showMsg(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
    private Handler mHander = new Handler(){};
}
