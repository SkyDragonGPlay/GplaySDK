package com.skydragon.gplay.utils;

import android.text.TextUtils;
import android.util.Log;

import com.skydragon.gplay.constants.FileConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileUtils {

    public static final String TAG = "Gplay.FileUtils";

    public static void removeFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private static class FileComparator implements Comparator<File> {
        public int compare(File file1, File file2) {
            if (file1.lastModified() > file2.lastModified()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static FileFilter sdkFileFilter = new FileFilter() {
        public boolean accept(File file) {
            String tmp = file.getName().toLowerCase();
            return tmp.endsWith(".jar") || tmp.endsWith(".dex");
        }
    };


    private static ArrayList<File> getAllGplaySDKList(String sdkPath, FileFilter fiter) {
        File parentFile = new File(sdkPath);
        File[] files = parentFile.listFiles(fiter);
        ArrayList<File> fileList = new ArrayList<>();
        if (files != null) {
            Collections.addAll(fileList, files);
            Collections.sort(fileList, new FileComparator());
        }
        return fileList;
    }

    public static String getLatestGplaySDKPath(String sdkPath) {
        ArrayList<File> fileList = getAllGplaySDKList(sdkPath, sdkFileFilter);
        String latestJar = "";
        if (fileList.size() > 0) {
            latestJar = fileList.get(0).getAbsolutePath();
        }
        return latestJar;
    }

    public static void removeUnusedSDK(String sdkPath) {
        ArrayList<File> fileList = getAllGplaySDKList(sdkPath, sdkFileFilter);
        if (fileList.size() > 1) {
            for (int i = 1; i < fileList.size(); i++) {
                fileList.get(i).delete();
            }
        }
        fileList = getAllGplaySDKList(FileConstants.getGplayOptPath(), sdkFileFilter);
        if (fileList.size() > 1) {
            for (int i = 1; i < fileList.size(); i++) {
                fileList.get(i).delete();
            }
        }
    }

    public static boolean renameFile(String resFilePath, String newFilePath) {
        File resFile = new File(resFilePath);
        File newFile = new File(newFilePath);
        return resFile.renameTo(newFile);
    }

    public static String readFile(File file) {
        if (!file.exists())
            return null;
        try {
            FileInputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void copyFile(String srcFilePath, String destFilePath) {
        InputStream inputStream = null;
        FileOutputStream os = null;
        try {   
            int byteread;
            File oldfile = new File(srcFilePath);   
            if (oldfile.exists()) {
                inputStream = new FileInputStream(srcFilePath);
                File fDestFilePath = new File(destFilePath);
                if(fDestFilePath.isDirectory()) {
                    destFilePath = fDestFilePath.getAbsolutePath() + "/" + oldfile.getName();
                }
                os = new FileOutputStream(destFilePath);   
                byte[] buffer = new byte[1024];   
                while ( (byteread = inputStream.read(buffer)) != -1) {   
                    os.write(buffer, 0, byteread);   
                }
            }   
        }   
        catch (Exception e) {   
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static JSONObject readJsonFile(File file) {
        if (!file.exists()) {
            return null;
        }

        JSONObject ret = null;
        try {
            String fileStr = readFile(file);
            if (fileStr != null) {
                ret = new JSONObject(fileStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     *                 If a deletion fails, the method stops attempting to
     *                 delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }


    public static void deleteDir(String sFolder) {
        deleteDir(new File(sFolder));
    }

    public static String ensurePathEndsWithSlash(String path) {
        if (TextUtils.isEmpty(path))
            return "";

        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        return path;
    }

    public static String getFileMD5(String filePath) {
        FileInputStream fileInputStream = null;

        try {
            File file = new File(filePath);

            if (!file.exists())
                return null;

            fileInputStream = new FileInputStream(file);

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 10];

            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }

            return byteArrayToHex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String byteArrayToHex(byte[] byteArray) {
        char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        char[] resultCharArray = new char[byteArray.length * 2];

        int index = 0;

        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        return new String(resultCharArray);
    }

    public static boolean unpackZip(String zipPath, String unzipTo, String unzipFromPath, String[] exceptFiles) {
        unzipTo = FileUtils.ensurePathEndsWithSlash(unzipTo);
        boolean ret = false;

        ZipFile zf = null;
        InputStream zis = null;
        try {
            String filePath;
            zf = new ZipFile(zipPath);
            ZipEntry ze;
            byte[] buffer = new byte[1024 * 100];
            int count;

            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ze = entries.nextElement();
                zis = zf.getInputStream(ze);
                filePath = ze.getName();
                if (!TextUtils.isEmpty(unzipFromPath)) {
                    if (!filePath.contains(unzipFromPath)) {
                        continue;
                    } else {
                        filePath = filePath.replace(unzipFromPath, "");
                    }
                }
                if (exceptFiles != null) {
                    boolean contains = false;
                    for (String exceptFileName : exceptFiles) {
                        if (filePath.contains(exceptFileName)) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        continue;
                    }
                }

                filePath = unzipTo + filePath;
                File file = new File(filePath);

                if (ze.isDirectory()) {
                    file.mkdirs();
                } else {
                    // Make sure the directory exists
                    file.getParentFile().mkdirs();

                    FileOutputStream fout = new FileOutputStream(filePath);

                    // cteni zipu a zapis
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    tryClose(fout);
                }
            }
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tryClose(zis);
            tryClose(zf);
        }


        return ret;
    }

    public static void tryClose(InputStream is) {
        if(null != is) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tryClose(OutputStream os) {
        if(null != os) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tryClose(ZipFile zipFile) {
        if(null != zipFile) {
            try {
                zipFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copy(File fRootDir, File file) {
        File[] files = fRootDir.listFiles();
        if (!file.exists()) // 如果文件夹不存在
            file.mkdir(); // 建立新的文件夹
        for (File aFile : files) {
            if (aFile.isFile()) { // 如果是文件类型就复制文件
                try {
                    FileInputStream fis = new FileInputStream(aFile);
                    FileOutputStream out = new FileOutputStream(new File(file
                            .getPath()
                            + File.separator + aFile.getName()));

                    int count = fis.available();
                    byte[] data = new byte[count];
                    if ((fis.read(data)) != -1) {
                        out.write(data); // 复制文件内容
                    }
                    out.close(); // 关闭输出流
                    fis.close(); // 关闭输入流
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (aFile.isDirectory()) { // 如果是文件夹类型
                File des = new File(file.getPath() + File.separator
                        + aFile.getName());
                des.mkdir(); // 在目标文件夹中创建相同的文件夹
                copy(aFile, des); // 递归调用方法本身
            }
        }
    }

    public static boolean checkFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        File f = new File(filePath);
        if(f != null && f.exists()) {
            Log.d(TAG, filePath + " exist!");
            return true;
        } else {
            Log.d(TAG, filePath + " not exist!");
            return false;
        }
    }
}
