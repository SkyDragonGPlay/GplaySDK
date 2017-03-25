package com.skydragon.gplay.sprite;

public interface OnGplaySpriteDragonListener {
    /**
     * 拖拉开始
     * @param startX
     * @param startY
     */
    void onGplayWhitePointDragonStart(int startX, int startY);

    /**
     * 拖拉移动中
     * @param dragonX
     * @param dragonY
     */
    void onGplayWhitePointDragonMove(int dragonX, int dragonY);

    /**
     * 拖拉松手回调
     * @param dragonX
     * @param dragonY
     */
    void onGplayWhitePointDragonUp(int dragonX, int dragonY);

    /**
     * 小白点移动结束回调
     * @param endX
     * @param endY
     */
    void onGplayWhitePointDragonEnd(int endX, int endY);
}
