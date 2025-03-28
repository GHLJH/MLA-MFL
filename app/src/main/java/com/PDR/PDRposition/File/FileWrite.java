package com.PDR.PDRposition.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class FileWrite {
    boolean mExternalStorageAvailable = false;        // 可写可读标记
    boolean mExternalStorageWriteable = false;

    public static String baseDir = "/storage/emulated/0/";
    private static File filerootdir;

    public static File newDir(String dirname) {
        filerootdir = new File(baseDir + "/" + "HIPEold");
        if (!filerootdir.exists()) {
            filerootdir.mkdir();
        }
        File filepath = new File(filerootdir + "/" + dirname);
        if (!filepath.exists()) {
            filepath.mkdir();
            return filepath;
        }
        return filepath;
    }

    public static File newFile(String filepath, String filename) {
        File saveFile = new File(filepath, filename);
        try {
            if (!saveFile.exists()) {
                saveFile.createNewFile();
                return saveFile;
            }else {
                saveFile.delete();
                saveFile = new File(filepath, filename);
                saveFile.createNewFile();
                return saveFile;        //如果文件已存在，则返回null
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkIfExternalStorageIsAvailable() {
        String state = Environment.getExternalStorageState();    //获取外部存储状态
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //SD卡正常挂载，且可写可读
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //SD卡正常挂载，只可读
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            //其他状态下，皆不可写不可读
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        return mExternalStorageAvailable;
    }

    //以StringBuilder的形式写入文本
    static void writeFileToStorage(StringBuilder contents, File file) {
        String logEntry = contents + "";
        writeFileToStorage(logEntry, file);
    }

    public static void writeFileToStorage(String contents, File file) {
        writeFileToStorage(contents, file, true);
    }

    public static void writeFileToStorage(String contents, File file, boolean append) {
        //获取系统分隔符

        //写入文本
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
            //
            writer.write(contents);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String[] readFileofStorage(File file){
        String temp;
        String s="";
        try {
            FileReader fileReader=new FileReader(file);
            BufferedReader reader=new BufferedReader(fileReader);
            try {
                while((temp=reader.readLine())!=null){
                    s+=temp+"\n";
                }
                return s.split("\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return s.split("\n");
    }

    //拷贝文件
    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public static void scanNewFiles(File file, Context context) {
        String paths[] = {file.getPath()};
        MediaScannerConnection.scanFile(context, paths, null, new MediaScannerConnection
                .OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
    }

    //Utility function to find and recovery files that may be "lost" on the internal storage.
    public static void findFiles() {
        final File dir = new File("/data/data/com.contextawareness.contextawaresensors/");

        File recoveryDir = newDir("recovery");
        recoveryDir.mkdirs();
        for (final File file : dir.listFiles()) {
            String filePath = file.getPath();
            String fileName = file.getName();
            Log.d("findFiles", filePath);
            try {
                copyFile(file, new File(recoveryDir, fileName));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
