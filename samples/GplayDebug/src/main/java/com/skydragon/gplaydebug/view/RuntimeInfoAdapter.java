package com.skydragon.gplaydebug.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skydragon.gplaydebug.R;
import com.skydragon.gplaydebug.model.RuntimeLoadingInfo;
import com.zhy.view.HorizontalProgressBarWithNumber;

import java.util.List;
import java.util.zip.Inflater;

/**
 * package : com.skydragon.gplaydebug.view
 * <p>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.23 16:59.
 */
public class RuntimeInfoAdapter extends BaseAdapter {

    private List<RuntimeLoadingInfo> mRuntimeInfoList;
    private Context mContext;
    public RuntimeInfoAdapter(Context context, List<RuntimeLoadingInfo> list){
        mRuntimeInfoList = list;
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView =  LayoutInflater.from(mContext).inflate(R.layout.item_layout_runtime_info, null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView) convertView.findViewById(R.id.item_info);
            viewHolder.itemMsg = (TextView) convertView.findViewById(R.id.item_msg);
            viewHolder.progressBarWithNumber = (HorizontalProgressBarWithNumber) convertView.findViewById(R.id.loading_progress);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        fillInfo(viewHolder, position);

        return convertView;
    }

    private void fillInfo(ViewHolder viewHolder, int position) {
        viewHolder.itemName.setText(mRuntimeInfoList.get(position).name);
        viewHolder.itemMsg.setText(mRuntimeInfoList.get(position).msg);
        viewHolder.progressBarWithNumber.setProgress(mRuntimeInfoList.get(position).percent);
    }

    private static class ViewHolder {
        TextView itemName;
        TextView itemMsg;
        HorizontalProgressBarWithNumber progressBarWithNumber;
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
