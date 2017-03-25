package com.skydragon.gplaydebug.model;

import com.skydragon.gplay.runtime.entity.resource.SceneInfo;

/**
 * package : com.skydragon.gplaydebug.model
 * <p>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.16 11:55.
 */
public class SceneDLState {
    // 下载进度
    private int mProgress = -1;
    //
    private SceneInfo mSceneInfo;

    private boolean mChecked;

    private int mStatus = 1;

    public SceneDLState(SceneInfo sceneInfo){
        mSceneInfo = sceneInfo;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setSceneStatus(int status) {
        mStatus = status;
    }

    public String getName(){
        return mSceneInfo.getName();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }


    public SceneInfo getSceneInfo(){
        return mSceneInfo;
    }


    public void setChecked(boolean checked) {
        this.mChecked = checked;
    }

    public boolean isChecked() {
        return mChecked;
    }
}
