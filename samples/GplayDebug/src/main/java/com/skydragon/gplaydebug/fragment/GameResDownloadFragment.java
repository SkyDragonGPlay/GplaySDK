package com.skydragon.gplaydebug.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.skydragon.gplay.runtime.RuntimeLauncher;
import com.skydragon.gplay.runtime.callback.OnCallbackListener;
import com.skydragon.gplay.runtime.entity.game.GameInfo;
import com.skydragon.gplay.runtime.utils.FileConstants;
import com.skydragon.gplay.runtime.utils.FileUtils;
import com.skydragon.gplaydebug.HostActivity;
import com.skydragon.gplaydebug.R;
import com.skydragon.gplaydebug.model.RuntimeLoadingInfo;
import com.skydragon.gplaydebug.utils.GplayDebugDialog;
import com.skydragon.gplaydebug.utils.ReflectUtil;
import com.skydragon.gplaydebug.view.RuntimeInfoAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import cc.rooho.pageswitcher.AppCompatActivityBase;
import cc.rooho.pageswitcher.switcher.FragmentSwitcher;
import cc.rooho.pageswitcher.switcher.IFragmentHooker;

/**
 * package : com.skydragon.gplaydebug.fragment
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.8 9:45.
 */
public class GameResDownloadFragment extends Fragment implements IFragmentHooker {

    private static final String TAG = "GameResStartDLFragment";
    private FragmentSwitcher mFragmentSwitcher;
    private View mRootView;
    private GameInfo mGameInfo;
    private RuntimeLauncher mRuntimeLauncher;
//    private TextView mRuntimeLoadingInfo;

    private Button mLoadBootSceneBtn;
    private Button mGoGameStartBtn;
    private static boolean mCanLoadBootScene = false;
    private RuntimeInfoAdapter mRuntimeLoadingInfoAdapter;
    private List<RuntimeLoadingInfo> mRuntimeInfoList = new ArrayList<>();
    private ListView mRuntimeLoadinglistView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivityBase) getActivity()).setTitle(getString(R.string.title_start_download_res));
        ((AppCompatActivityBase) getActivity()).getSupportActionBar().hide();
        mRootView = inflater.inflate(R.layout.fragment_game_res_download, null);
        Button loadBeforeBootSceneBtn = (Button) mRootView.findViewById(R.id.start_dl);
        mRuntimeLoadinglistView = (ListView) mRootView.findViewById(R.id.runtime_loading_list);
        Button startSDLBtn = (Button) mRootView.findViewById(R.id.start_service_dl);
        mLoadBootSceneBtn = (Button) mRootView.findViewById(R.id.launch_boot_scene);
        mGoGameStartBtn = (Button) mRootView.findViewById(R.id.go_game_start);
//        mRuntimeLoadingInfo = (TextView) mRootView.findViewById(R.id.runtime_loading_info);
//        mRuntimeLoadingInfo.setMovementMethod(ScrollingMovementMethod.getInstance());

        startSDLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onStartServiceDownloadResClicked();
            }
        });

        loadBeforeBootSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onLoadBeforeBootClicked(); } });

        mLoadBootSceneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  onLoadBootClicked(); } });

        mGoGameStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onGoGameStartClicked(); } });

        mRuntimeLauncher = RuntimeLauncher.getInstance();

        beInRuntimeLauncherDebuging(mRuntimeLauncher, true);

        initDebugDetector();

        mRuntimeLoadingInfoAdapter = new RuntimeInfoAdapter(getActivity(), mRuntimeInfoList);

        mRuntimeLoadinglistView.setAdapter(mRuntimeLoadingInfoAdapter);

        return mRootView;
    }

    private void initDebugDetector(){
        try {
            Field debugDetector = ReflectUtil.getField(RuntimeLauncher.class, "FIELD_DEBUG_DETECTOR");
            debugDetector.set(mRuntimeLauncher, mDebugDetector);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void onLoadBeforeBootClicked() {
        mRuntimeInfoList.clear();
        mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
        if(isServiceDownloading){
            cancleServiceResDownload();
        }
        mRuntimeLauncher.reset();

        // invork notifyCurrentStepFinished
        ReflectUtil.invokeMethod(mRuntimeLauncher, "start", new Class[]{ Context.class , GameInfo.class, OnCallbackListener.class }
                , new Object[]{ getActivity(), mGameInfo, new OnCallbackListener<GameInfo>(){
                    @Override
                    public void onCallBack(GameInfo data) { } }} );
    }

    private void onLoadBootClicked() {
        if(!mCanLoadBootScene) {
            GplayDebugDialog.showDialog(getActivity(), "请先下载Runtime资源", null, null, null);
            return;
        }
        mRuntimeInfoList.clear();
        mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
        ReflectUtil.setField(mRuntimeLauncher, "mIsPreDownloadRuntime", false);
        // invork notifyCurrentStepFinished
        ReflectUtil.invokeMethod(mRuntimeLauncher, "updateBootScene");
    }

    private boolean isServiceDownloading;
    private void onStartServiceDownloadResClicked() {
        isServiceDownloading = true;
        mRuntimeInfoList.clear();
        mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
        ReflectUtil.setField(mRuntimeLauncher, "n", 0);
        // invork notifyCurrentStepFinished
        ReflectUtil.invokeMethod(mRuntimeLauncher, "startDownloadRuntime", new Class[]{ Context.class }, new Object[]{ getActivity() } );
    }

    private void cancleServiceResDownload() {
        // invork notifyCurrentStepFinished
        Object serviceImpl = ((HostActivity)getActivity()).mGplayServiceImpl;
        if(serviceImpl != null){
            ReflectUtil.invokeMethod(serviceImpl, "cancelPrepareRuntime");
        }
    }

    private String mFlowName = "";
    private RuntimeLoadingInfo mCurrentLoadingInfo;
    private RuntimeLauncher.DebugDetector mDebugDetector = new RuntimeLauncher.DebugDetector() {
        @Override
        public void onRuntimeFlowCallBack(String flowName, int alreadyProcessPercent, int msgCode, String msg, int retryTimes) {
            Log.v(TAG, "GplayDebuging onRuntimeFlowCallBack " + flowName + " , " + alreadyProcessPercent + " , " + msgCode + " , " + msg + " , " + retryTimes);
            Context context;
            if(!mFlowName.equals(flowName) && msgCode == 1) { //MES_BEGIN
                mFlowName = flowName;
                mCurrentLoadingInfo = new RuntimeLoadingInfo();
                mCurrentLoadingInfo.name = flowName;
                mCurrentLoadingInfo.percent = alreadyProcessPercent;
                mCurrentLoadingInfo.msg = msg;
                mRuntimeInfoList.add(mCurrentLoadingInfo);
                mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
            } else if(msgCode == 2 && mCurrentLoadingInfo != null){ //MES_PROCESS
                mCurrentLoadingInfo.percent = alreadyProcessPercent;
                mCurrentLoadingInfo.msg = msg;
                mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
            } else if(msgCode == 3 && mCurrentLoadingInfo != null){ //MES_FINISHED
                mCurrentLoadingInfo.percent = alreadyProcessPercent;
                mCurrentLoadingInfo.msg = msg;
                mFlowName = "";
                mRuntimeLoadingInfoAdapter.notifyDataSetChanged();
            } else if(msgCode == 4 && (context = getActivity()) != null){ //MES_FLOW_FINISHED
                if(flowName.equals("SERVICE_UPDATE")){
                    GplayDebugDialog.showDialog(context, "服务加载Runtime资源完毕！", null, null, null);
                } else if(flowName.equals("RUNTIME_RES")){
                    GplayDebugDialog.showDialog(context, "Runtime资源已加载完毕！", null, null, null);
                    mCanLoadBootScene = true;
                } else if(flowName.equals("BOOT_SCENE_UPDATE")){
                    GplayDebugDialog.showDialog(context, "Boot场景加载完毕！", null, null, null);
                }
            }
        }
    };

    private void appendTextLog(TextView tv, SpannableString msg){
        tv.append(msg + "\n");
        int offset = tv.getLineCount() * tv.getLineHeight();
        if(offset > tv.getHeight()){
            tv.scrollTo(0, offset - tv.getHeight());
        }
    }

    private void onGoGameStartClicked() {

        if(!mCanLoadBootScene) {
            GplayDebugDialog.showDialog(getActivity(), "请先下载Runtime资源", null, null, null);
            return;
        }
        // check game root dir
        FileUtils.ensureDirExists(FileConstants.getGameRootDir(mGameInfo.mPackageName));

        // goto game running page
        gotoGameRunningPage();
    }

    private void gotoGameRunningPage(){
        GamingFragment ghf = new GamingFragment();
        ghf.setGameInfo(mGameInfo);
        mFragmentSwitcher.switchFragment(ghf, FragmentSwitcher.MODE_PUSH_STACK);
    }

    @Override
    public void hookSwitcher(FragmentSwitcher fragmentSwitcher) {
        mFragmentSwitcher = fragmentSwitcher;
    }

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }

    private void beInRuntimeLauncherDebuging(RuntimeLauncher runtimeLauncher, boolean debug){
        // 设置为加载本地资源的模式。
        try {
            Field IN_GPLAY_DEBUGING = ReflectUtil.getField(RuntimeLauncher.class, "IN_GPLAY_DEBUGING");
            IN_GPLAY_DEBUGING.set(runtimeLauncher, debug);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
