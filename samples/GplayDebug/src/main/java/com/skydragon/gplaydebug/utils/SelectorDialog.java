package com.skydragon.gplaydebug.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.skydragon.gplaydebug.R;

/**
 * package : com.skydragon.gplaydebug.utils
 * <p/>
 * Description :
 *
 * @author Y.J.ZHOU
 * @date 2016.6.16 23:33.
 */
public class SelectorDialog {

    private static AlertDialog.Builder buildMultiChoiceDialog(Context context, final CharSequence[] items, final OnSelectedListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = context.getString(R.string.select_delete_game);
        builder.setTitle(title);
        builder.setIcon(R.drawable.png_title_warning);
        final boolean[] itemsClicked = new boolean[items.length];

        builder.setMultiChoiceItems(items, itemsClicked, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                itemsClicked[which] = isChecked;
            }
        });

        String positiveTxt = context.getString(R.string.dialog_confirm);
        builder.setPositiveButton(positiveTxt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int len = 0;
                for(boolean checked : itemsClicked){
                    if(checked){
                        len ++;
                    }
                }
                CharSequence[] itemsSelected = new CharSequence[len];
                if(itemsSelected.length > 0) {
                    for(int i = 0,j = 0; i < itemsClicked.length; i++) {
                        if(itemsClicked[i]){
                            itemsSelected[j ++] = items[i];
                        }
                    }
                }
                if(itemsSelected.length > 0)
                    listener.onItemSelected(itemsSelected);
            }
        });

        String negativeTxt = context.getString(R.string.dialog_cancele);
        builder.setNegativeButton(negativeTxt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        return builder;
    }

    public static void showMultiChoiceDialog(Context context, CharSequence[] items, final OnSelectedListener listener) {
        AlertDialog.Builder builder = buildMultiChoiceDialog(context, items, listener);
        builder.create().show();
    }

    public static void showSingleSelectorDialog(Context context, final CharSequence[] items, int selectIndex, final OnSelectedListener listener) {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String title = context.getString(R.string.choose_game);
        builder.setTitle(title);
        builder.setSingleChoiceItems(items, selectIndex, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onItemSelected(new CharSequence[]{items[which]});
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public interface OnSelectedListener{
        public void onItemSelected(CharSequence[] items);
    }
}
