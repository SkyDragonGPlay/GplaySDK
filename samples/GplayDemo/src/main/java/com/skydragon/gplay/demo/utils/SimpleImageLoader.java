package com.skydragon.gplay.demo.utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class SimpleImageLoader {

    private static final String sPath = Environment.getExternalStorageDirectory()+"/gplay/img/";
    final Handler mHandler = new Handler();
    private HashMap<String, SoftReference<Drawable>> mImgCache = new HashMap<String, SoftReference<Drawable>>();   
    private ExecutorService mExecutorService;
    
    public interface OnImageLoadCallback {
        public void onImageLoad(int pos, String url);
    }
    
    public SimpleImageLoader()
    {
        File dir = new File(sPath);
        if(!dir.exists())
            dir.mkdirs();
        mExecutorService = Executors.newFixedThreadPool(4);
    }
    
    public void clearAllImages() {
        Iterator<Entry<String, SoftReference<Drawable>>> iter = mImgCache.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, SoftReference<Drawable>> entry = iter.next();
            BitmapDrawable dr = (BitmapDrawable)(entry.getValue().get());
            if(dr != null && dr.getBitmap() != null && !dr.getBitmap().isRecycled()){  
                dr.getBitmap().recycle();  
            }
            entry.getValue().clear();
        }
        mImgCache.clear();
    }
    
    public void loadImage(int pos, ImageView iv, String url, OnImageLoadCallback l) {
        
        if (mImgCache.containsKey(url))
        {  
            SoftReference<Drawable> softReference = mImgCache.get(url);  
            Drawable d = softReference.get();  
            if (d != null && iv != null) 
            {  
                iv.setImageDrawable(d);
                return;  
            }  
        }  
        
        File f = new File(sPath + MD5.fromStr(url));
        if(f.exists())
        {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(f);
                Drawable d = Drawable.createFromStream(fis, "src");
                if (d != null) 
                {  
                    mImgCache.put(url, new SoftReference<Drawable>(d));
                    if (iv != null) 
                    {  
                        iv.setImageDrawable(d);
                    }  
                    return;  
                } 
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if(fis != null)
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        
        if(l != null)
        {
            DownloadImageRunnable r = new DownloadImageRunnable(pos, url, l);
            mExecutorService.execute(r);
        }
    }
    
    
    class DownloadImageRunnable implements Runnable
    {
        String mUrl;
        OnImageLoadCallback mListener;
        int mIndex;
        
        DownloadImageRunnable(int index, String url, OnImageLoadCallback l)
        {
            mUrl = url;
            mIndex = index;
            mListener = l;
        }

        @Override
        public void run() {
            try {
                Drawable d = loadImageFromUrl(mUrl);
                if(d != null){
                    mImgCache.put(mUrl, new SoftReference<Drawable>(d));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onImageLoad(mIndex, mUrl);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
    }
         
    public static Drawable loadImageFromUrl(String strUrl) throws IOException 
    {
        Log.d("SimpleImageLoader", "imageUrl: " + strUrl);
        URL url = new URL(strUrl);
        InputStream i = (InputStream) url.getContent();
        File imgFile = new File(sPath+MD5.fromStr(strUrl));
        DataInputStream in = new DataInputStream(i);
        FileOutputStream out = new FileOutputStream(imgFile);
        int len = 0;
        byte[] buffer = new byte[1024];
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
        
        FileInputStream fis = new FileInputStream(imgFile);
        Drawable d = Drawable.createFromStream(fis, "src");
        fis.close();
        return d;
    }
}
