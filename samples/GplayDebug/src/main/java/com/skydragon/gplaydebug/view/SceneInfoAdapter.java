package com.skydragon.gplaydebug.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.skydragon.gplay.runtime.RuntimeCore;
import com.skydragon.gplay.runtime.RuntimeLauncher;
import com.skydragon.gplay.runtime.RuntimeLocalRecord;
import com.skydragon.gplay.runtime.RuntimeScene;
import com.skydragon.gplay.runtime.entity.LocalSceneInfo;
import com.skydragon.gplay.runtime.entity.resource.GameResourceConfigInfo;
import com.skydragon.gplay.runtime.entity.resource.SceneInfo;
import com.skydragon.gplaydebug.R;
import com.skydragon.gplaydebug.model.SceneDLState;
import com.skydragon.gplaydebug.utils.GplayDebugDialog;
import com.skydragon.gplaydebug.utils.LoadingDialog;
import com.skydragon.gplaydebug.utils.LoadingProgressDialog;
import com.skydragon.gplaydebug.utils.ReflectUtil;
import com.zhy.view.HorizontalProgressBarWithNumber;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * package : com.skydragon.gplaydebug.view
 * <p>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.24 10:35.
 */
public class SceneInfoAdapter extends BaseAdapter {

    private static final String TAG = "SceneInfoAdapter";
    private List<SceneDLState> mRuntimeInfoList = new ArrayList<SceneDLState>();
    private Context mContext;
    private boolean mShowCheckBox = true;
    private RuntimeLocalRecord mRuntimeLocalRecord;
    private GameResourceConfigInfo mResourceConfigInfo;


    private static final int FAILURE = 0;
    private static final int PROCESSING = 1;
    private static final int COMPLETED = 2;

    public SceneInfoAdapter(Context context){
        mContext = context;
        mResourceConfigInfo = RuntimeLauncher.getInstance().getResourceConfigInfo();
        mRuntimeLocalRecord = RuntimeLocalRecord.getInstance();

        RuntimeCore runtimeCore = RuntimeCore.getInstance();
        ReflectUtil.setField(runtimeCore, "DEBUG_SCENE_LOADING_LISTENER", mScenesLoadingListener);

        List<SceneInfo> allSceneInfo = mResourceConfigInfo.getAllSceneInfos();
        for(Iterator<SceneInfo> ite = allSceneInfo.iterator(); ite.hasNext();){
            SceneInfo sceneInfo = ite.next();
            mRuntimeInfoList.add(new SceneDLState(sceneInfo));
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_layout_scene_info, null);
            viewHolder = new ViewHolder();
            viewHolder.sceneInfo = (TextView) convertView.findViewById(R.id.scene_info_txt);
            viewHolder.resultTxt = (TextView) convertView.findViewById(R.id.result_status);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.check_box);
            viewHolder.sceneInfoProgress = (HorizontalProgressBarWithNumber) convertView.findViewById(R.id.scene_loading_progress);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        fillValues(viewHolder, position);
        return convertView;
    }

    private void fillValues(ViewHolder viewHolder, final int position) {
        viewHolder.sceneInfo.setText(getSceneDLInfo(position));
        viewHolder.sceneInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GplayDebugDialog.showDialog(mContext, mContext.getString(R.string.dialog_title_scene_info) , getScnenConfigInfo(position),
                        mContext.getString(R.string.dialog_confirm), null);
            }
        });

        final SceneDLState sceneDLState = mRuntimeInfoList.get(position);
        switch (sceneDLState.getStatus()){
            case FAILURE:
                viewHolder.resultTxt.setText(mContext.getString(R.string.failure));
                viewHolder.resultTxt.setBackgroundColor(mContext.getResources().getColor(R.color.color_light_red_FFFF0000));
                break;
            case PROCESSING:
                viewHolder.resultTxt.setText("");
                viewHolder.resultTxt.setBackgroundColor(mContext.getResources().getColor(R.color.color_light_grey_FFE8E8E0));
                break;
            case COMPLETED:
                viewHolder.resultTxt.setText(mContext.getString(R.string.completed));
                viewHolder.resultTxt.setBackgroundColor(mContext.getResources().getColor(R.color.color_light_green_FF99CC00));
                break;
        }

        if(mShowCheckBox){
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mRuntimeInfoList.get(position).setChecked(isChecked);
                }
            });
        } else {
            viewHolder.checkBox.setVisibility(View.GONE);
        }

        int progress = sceneDLState.getProgress();

        if(!mShowCheckBox) {
            viewHolder.sceneInfoProgress.setVisibility(View.VISIBLE);
            viewHolder.sceneInfoProgress.setProgress(progress);
        } else {
            viewHolder.sceneInfoProgress.setVisibility(View.INVISIBLE);
        }
    }

    private List<SceneDLState> mPreLoadScenes = new ArrayList<SceneDLState>();
    public void preloadScene() {
        JSONArray scenesArray = new JSONArray();
        for(Iterator<SceneDLState> ite = mRuntimeInfoList.iterator(); ite.hasNext(); ){
            SceneDLState sceneDLState = ite.next();
            Log.v("", "preloadScene sceneName "+sceneDLState.getName()+" isCheck " + sceneDLState.isChecked());
            if(sceneDLState.isChecked()) {
                scenesArray.put(sceneDLState.getName());
                mPreLoadScenes.add(sceneDLState);
            }
        }

        if(scenesArray.length() <= 0){
            Toast.makeText(mContext, "please select a scene!", Toast.LENGTH_LONG).show();
            return;
        }

        RuntimeCore runtimeCore = RuntimeCore.getInstance();
        LoadingProgressDialog.showLoadingDialog(mContext, mContext.getString(R.string.preloading_scene));
        ReflectUtil.invokeMethod(runtimeCore, "preloadScenes", new Class[]{ JSONArray.class }, new Object[]{ scenesArray });
    }

    private class ViewHolder{
        TextView sceneInfo;
        TextView resultTxt;
        CheckBox checkBox;
        HorizontalProgressBarWithNumber sceneInfoProgress;
    }

    private String getSceneDLInfo(int position){
        SceneDLState sceneDLState = mRuntimeInfoList.get(position);
        String sceneName = mRuntimeInfoList.get(position).getName();
        int newestVersion = mResourceConfigInfo.getVersionCode();
        StringBuffer msg = new StringBuffer();
        msg.append("["+sceneName+"]   Num. "+sceneDLState.getSceneInfo().getOrder()+"\n");

        LocalSceneInfo localSceneInfo = getRecordSceneInfo(sceneName);
        if(localSceneInfo != null) {
            int localVersion = localSceneInfo.getVerison();
            msg.append("本地版本 v " + localVersion + "   最新版本v " + newestVersion + "\n");
        } else {
            msg.append("待更新...最新版本(" + newestVersion + ")\n");
        }

        RuntimeScene runtimeScene = RuntimeScene.getRuntimeScene(sceneName);

        Boolean canUpdateByPatch = (Boolean) ReflectUtil.invokeMethod(runtimeScene, "canUpdateByPatch");

        if(canUpdateByPatch)
            msg.append("更新方式:增量更新 \n");
        else
            msg.append("更新方式:完整更新 \n");
        return msg.toString();
    }

    private String getScnenConfigInfo(int position){
        final SceneDLState sceneDLState = mRuntimeInfoList.get(position);
        String sceneMsg = "" + sceneDLState.getName() + "  V. " + mResourceConfigInfo.getVersionCode() + "\n";

        List<String> groupList = sceneDLState.getSceneInfo().getAllGroupInfos();
        if(groupList.size() > 0){
            sceneMsg += "Groups :\n";
            for(Iterator<String> ite = groupList.iterator(); ite.hasNext();){
                sceneMsg += "  " + ite.next() + "\n";
            }
        }

        List<String> patchList = sceneDLState.getSceneInfo().getAllPatchInfos();
        if(patchList.size() > 0){
            sceneMsg += "Patchs :\n";
            for(Iterator<String> ite = patchList.iterator(); ite.hasNext();){
                sceneMsg += "   " + ite.next() + "\n";
            }
        }
        return sceneMsg;
    }

    private RuntimeCore.ScenesLoadingListener mScenesLoadingListener = new RuntimeCore.ScenesLoadingListener() {
        private SceneDLState mSceneDLState;

        @Override
        public void onScenesLoadingCallback(long alreadyDownload, long totallySize) {
            // clean all selected

            if (totallySize == 0) {
                for(Iterator<SceneDLState> ite = mRuntimeInfoList.iterator();
                    ite.hasNext();
                    ite.next().setChecked(false));
                LoadingProgressDialog.closeLoadingDialog();
                Toast.makeText(mContext, "No scene need to be downloading!", Toast.LENGTH_LONG).show();
                return;
            }

            LoadingProgressDialog.onProgress(alreadyDownload, totallySize);

            if (totallySize == alreadyDownload) {
                for (SceneDLState aMRuntimeInfoList : mRuntimeInfoList) {
                    aMRuntimeInfoList.setChecked(false);
                }

                if(mPreLoadScenes != null && mPreLoadScenes.size() > 0){
                    for(SceneDLState sceneDLState : mPreLoadScenes){
                        sceneDLState.setProgress(100);
                        sceneDLState.setSceneStatus(COMPLETED);
                    }
                }

                notifyDataSetChanged();
                LoadingProgressDialog.closeLoadingDialog();
            }
        }

        @Override
        public void onSilentsSceneLoadingCallback(int status, String sceneName, long alreadyDownloadSize, long totallySize, String msg) {
            Log.v(TAG, "onSilentsSceneLoadingCallback 0 sceneName " + status + "  ,  " +  sceneName + "  ,  " + alreadyDownloadSize + "  ,  " + totallySize + "  ,  " + msg);

            int percent = 0;
            switch (status) {
                case 0:
                    for(Iterator<SceneDLState> ite = mRuntimeInfoList.iterator(); ite.hasNext(); ) {
                        mSceneDLState = ite.next();
                        if(mSceneDLState.getName().equals(sceneName)){
                            break;
                        }
                    }
                    break;
                case 1:
                    percent = (int)((alreadyDownloadSize) * 100 / totallySize);
                    mSceneDLState.setSceneStatus(PROCESSING);
                    break;
                case 2:
                    percent = 100;
                    if(mSceneDLState == null){
                        Toast.makeText(mContext, "nothing download !", Toast.LENGTH_LONG).show();
                        return;
                    }
                    mSceneDLState.setSceneStatus(COMPLETED);
                    break;
            }
            mSceneDLState.setProgress(percent);
            notifyDataSetChanged();
        }

    };

    public void checkScene() {
        LoadingDialog.showLoadingDialog(mContext, mContext.getString(R.string.checking), 700, new LoadingDialog.TimeoutListener() {
            @Override
            public void onTimeout() {}
        });

        for(Iterator<SceneDLState> ite = mRuntimeInfoList.iterator(); ite.hasNext(); ){
            SceneDLState scnenDLStat = ite.next();
            List<String> waittingForDownloadGroupList = RuntimeScene.getRuntimeScene(scnenDLStat.getSceneInfo().getName()).calculateLackOfGroups();
            Log.v("", "SceneName : " + scnenDLStat.getSceneInfo().getName() + " isSceneUpdate : " + waittingForDownloadGroupList.size());
            if(waittingForDownloadGroupList.size() == 0) {
                scnenDLStat.setSceneStatus(COMPLETED); // MSG_SUCCESS
            } else {
                scnenDLStat.setSceneStatus(PROCESSING); // MSG_FAILURE
            }
        }
        notifyDataSetChanged();
    }

    private LocalSceneInfo getRecordSceneInfo(String sceneName){
        return (LocalSceneInfo) ReflectUtil.invokeMethod(mRuntimeLocalRecord,
                "getLocalSceneRecord", new Class[]{ String.class }, new Object[]{ sceneName });
    }

    public void showCheckBox(boolean show){
        mShowCheckBox = show;
    }

    @Override
    public int getCount() {
        return mRuntimeInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return mRuntimeInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
