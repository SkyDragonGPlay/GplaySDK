package com.skydragon.gplay.callback;

/**
 * The listener of requesting game info list
 */
public interface OnRequestGameInfoListListener {
    void onSuccessOfRequestGames(String jsonResult);

    void onFailureOfRequestGames();
}