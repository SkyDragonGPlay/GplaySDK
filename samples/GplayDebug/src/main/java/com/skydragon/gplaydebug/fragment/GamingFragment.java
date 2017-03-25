package com.skydragon.gplaydebug.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import com.skydragon.gplay.runtime.RuntimeCore;
import com.skydragon.gplay.runtime.RuntimeLauncher;
import com.skydragon.gplay.runtime.bridge.BridgeHelper;
import com.skydragon.gplay.runtime.entity.game.GameInfo;
import com.skydragon.gplay.runtime.entity.resource.GameResourceConfigInfo;
import com.skydragon.gplaydebug.R;
import com.skydragon.gplaydebug.utils.GplayDebugDialog;
import com.skydragon.gplaydebug.utils.ReflectUtil;
import com.skydragon.gplaydebug.view.SceneInfoAdapter;

import cc.rooho.pageswitcher.AppCompatActivityBase;
import cc.rooho.pageswitcher.switcher.FragmentSwitcher;
import cc.rooho.pageswitcher.switcher.IFragmentHooker;

/**
 * package : com.skydragon.gplaydebug.fragment
 *
 * @author Y.J.ZHOU
 * @date 2016.6.12 18:03.
 */
public class GamingFragment extends Fragment implements IFragmentHooker {

    private ViewGroup mRootView;
    private GameInfo mGameInfo;
    private RuntimeCore mRuntimeCore;
    private SceneInfoAdapter mSceneInfoAdapter;
    private boolean mIsSlientDownloading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((AppCompatActivityBase) getActivity()).setTitle(getString(R.string.title_gameing));
        ((AppCompatActivityBase) getActivity()).getSupportActionBar().hide();
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_gaming, null);

        Button startDLBtn = (Button) mRootView.findViewById(R.id.start_slient_dl);
        Button pauseDLBtn = (Button) mRootView.findViewById(R.id.pause_slient_dl);
        Button preloadBtn = (Button) mRootView.findViewById(R.id.start_preload_scene);
        Button checkBtn = (Button) mRootView.findViewById(R.id.check_local_scene);

        startDLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartSlientDL();
            }
        });

        pauseDLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPauseSlientDL();
            }
        });

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSceneInfoAdapter.checkScene();
            }
        });

        preloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsSlientDownloading){
                    GplayDebugDialog.showDialog(getActivity(), getString(R.string.please_pause_slient_downloading) + "?", null, null, null);
                    return;
                }
                GplayDebugDialog.showDialog(getActivity(), getString(R.string.preload_scene) + "?", null,
                        getString(R.string.dialog_confirm),
                        getString(R.string.dialog_cancele),
                        new GplayDebugDialog.OnDialogClickListener() {
                            @Override
                            public void onDialogButtonClick(int btnMode, DialogInterface dialog, int which) {
                                if(GplayDebugDialog.BTN_POSITIVE_CLICK == btnMode){
                                    mSceneInfoAdapter.preloadScene();
                                }
                            }
                        });
            }
        });

        mRuntimeCore = RuntimeCore.getInstance();
        BridgeHelper bridgeHelper = BridgeHelper.getInstance();

        mRuntimeCore.init(mGameInfo.mGameKey);
        ReflectUtil.setField(bridgeHelper, "mIsRunning", true);
        ReflectUtil.setField(mRuntimeCore, "mCurrentDownloadIndex", 0);

        final GridView gridView = (GridView) mRootView.findViewById(R.id.gridview);
        GameResourceConfigInfo resourceConfigInfo = RuntimeLauncher.getInstance().getResourceConfigInfo();
        mSceneInfoAdapter = new SceneInfoAdapter(getActivity());
        gridView.setAdapter(mSceneInfoAdapter);

        return mRootView;
    }

    private void onStartSlientDL() {
        GplayDebugDialog.showDialog(getActivity(), getString(R.string.start_slient_downloading) + "?", null,
                getString(R.string.dialog_confirm),
                getString(R.string.dialog_cancele),
                new GplayDebugDialog.OnDialogClickListener() {
                    @Override
                    public void onDialogButtonClick(int btnMode, DialogInterface dialog, int which) {
                        if(GplayDebugDialog.BTN_POSITIVE_CLICK == btnMode){
                            mIsSlientDownloading = true;
                            mRuntimeCore.startSilentDownload();
                            mSceneInfoAdapter.showCheckBox(false);
                            mSceneInfoAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void onPauseSlientDL() {
        GplayDebugDialog.showDialog(getActivity(), getString(R.string.pause_slient_downloading) + "?", null,
                getString(R.string.dialog_confirm),
                getString(R.string.dialog_cancele),
                new GplayDebugDialog.OnDialogClickListener() {
                    @Override
                    public void onDialogButtonClick(int btnMode, DialogInterface dialog, int which) {
                        if(GplayDebugDialog.BTN_POSITIVE_CLICK == btnMode){
                            mIsSlientDownloading = false;
                            mRuntimeCore.stopSilentDownload();
                            mSceneInfoAdapter.showCheckBox(true);
                            mSceneInfoAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public void hookSwitcher(FragmentSwitcher fragmentSwitcher) { }

    public void setGameInfo(GameInfo gameInfo) {
        mGameInfo = gameInfo;
    }
}
