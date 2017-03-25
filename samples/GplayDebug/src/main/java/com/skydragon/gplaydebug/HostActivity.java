package com.skydragon.gplaydebug;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.skydragon.gplay.runtime.utils.FileUtils;
import com.skydragon.gplaydebug.fragment.RuntimeLoadingConfigFragment;
import com.skydragon.gplaydebug.utils.Constants;
import com.skydragon.gplaydebug.utils.GplayDebugDialog;
import com.skydragon.gplaydebug.utils.SelectorDialog;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import cc.rooho.pageswitcher.AppCompatActivityBase;

/**
 * package : com.skydragon.gplaydebug
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.8 1:05.
 */
public class HostActivity extends AppCompatActivityBase {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mPlanetTitles;

    public static Object mGplayServiceImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mPlanetTitles = getResources().getStringArray(R.array.select_dialog_items);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.view_drawer_list_item, R.id.item_txt, mPlanetTitles);
        mDrawerList.setAdapter(adapter);
    }

    //内容区显示PlanetFragment
    private void selectItem(int position) {
        Log.v("", "select Item : " + position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Fragment createFirstFragment() {
        return new RuntimeLoadingConfigFragment();
    }

    @Override
    public int getContainerResourceId() {
        return R.id.fragment_container;
    }

    @Override
    public int getContentViewId() {
        return R.layout.layout_base_activity_container;
    }
}
