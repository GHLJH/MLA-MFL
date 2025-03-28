package com.PDR.PDRposition.File;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class File_IO {


    private static String baseDir = "/storage/emulated/0/";
    private static File filerootdir;

    private static File saveFile_1;
    private static File saveFile_2;
    private static File saveFile_3;
    private static File saveFile_4;
    private static File saveFile_5;
    private static File saveFile_6;
    private static File saveFile_7;
    private static File saveFile_8;
    private static File FileName;
    private static String SelfFileName;

    private static boolean disableWrite = false;

    public static void Dir(String filename) {
        SelfFileName = filename ;
        if (disableWrite) return;

        filerootdir = new File(baseDir + SelfFileName);

        if (filerootdir.exists()) {
            deleteDir(filerootdir.toString());
            filerootdir.mkdirs();
        } else {
            filerootdir.mkdirs();
        }

        saveFile_1 = new File(filerootdir, "fusion.txt");
        saveFile_2 = new File(filerootdir, "fusionInitial.txt");
        saveFile_3 = new File(filerootdir, "KF.txt");
        saveFile_4 = new File(filerootdir, "KFInitial.txt");
        saveFile_5 = new File(filerootdir, "MeasurementData.txt");
        saveFile_6 = new File(filerootdir, "AllDataFusion.txt");
        saveFile_7 = new File(filerootdir, "AllDataKF.txt");
        saveFile_8 = new File(filerootdir, "Button.txt");

        try {
            if (!saveFile_1.exists()) {
                saveFile_1.createNewFile();
            } else {
                saveFile_1.delete();
                saveFile_1 = new File(filerootdir, "fusion.txt");
                saveFile_1.createNewFile();
            }
            if (!saveFile_2.exists()) {
                saveFile_2.createNewFile();
            } else {
                saveFile_2.delete();
                saveFile_2 = new File(filerootdir, "fusionInitial.txt");
                saveFile_2.createNewFile();
            }
            if (!saveFile_3.exists()) {
                saveFile_3.createNewFile();
            } else {
                saveFile_3.delete();
                saveFile_3 = new File(filerootdir, "KF.txt");
                saveFile_3.createNewFile();
            }
            if (!saveFile_4.exists()) {
                saveFile_4.createNewFile();
            } else {
                saveFile_4.delete();
                saveFile_4 = new File(filerootdir, "KFInitial.txt");
                saveFile_4.createNewFile();
            }
            if (!saveFile_5.exists()) {
                saveFile_5.createNewFile();
            } else {
                saveFile_5.delete();
                saveFile_5 = new File(filerootdir, "MeasurementData.txt");
                saveFile_5.createNewFile();
            }
            if (!saveFile_6.exists()) {
                saveFile_6.createNewFile();
            } else {
                saveFile_6.delete();
                saveFile_6 = new File(filerootdir, "AllDataFusion.txt");
                saveFile_6.createNewFile();
            }
            if (!saveFile_7.exists()) {
                saveFile_7.createNewFile();
            } else {
                saveFile_7.delete();
                saveFile_7 = new File(filerootdir, "AllDataKF.txt");
                saveFile_7.createNewFile();
            }
            if (!saveFile_8.exists()) {
                saveFile_8.createNewFile();
            } else {
                saveFile_8.delete();
                saveFile_8 = new File(filerootdir, "Button.txt");
                saveFile_8.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File GetFileName_fusion() {
        return saveFile_1;
    }

    public static File GetFileName_fusionInitial() {
        return saveFile_2;
    }

    public static File GetFileName_KF() {
        return saveFile_3;
    }

    public static File GetFileName_KFInitial() {
        return saveFile_4;
    }

    public static File GetFileName_MeasurementData() {
        return saveFile_5;
    }

    public static File GetFileName_AllDataFusion() {
        return saveFile_6;
    }

    public static File GetFileName_AllDataKF() {
        return saveFile_7;
    }

    public static File GetFileName_Button() {
        return saveFile_8;
    }

    public static File GetFileName(String s) {
        filerootdir = new File(baseDir + SelfFileName);

        FileName = new File(filerootdir, s);

        try {
            if (!FileName.exists()) {
                FileName.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileName;
    }

    public static boolean DirExists() {
        if (filerootdir.exists()) {
            return true;
        } else {
            return false;
        }
    }


    public static void deleteDir(final String pPath) {
        if (disableWrite) return;

        File dir = new File(pPath);
        deleteDirWihtFile(dir);
    }

    public static void deleteDirWihtFile(File dir) {
        if (disableWrite) return;

        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWihtFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


    public static void SaveData(double Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            writer.write(Double.toString(Data) + " ");
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(double[] Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            int n = Data.length;
            for (int i = 0; i < n; i++) {
                writer.write(Double.toString(Data[i]) + " ");
            }
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void SaveData(double[] Data, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));

            int n = Data.length;
            for (int i = 0; i < n; i++) {
                writer.write(Double.toString(Data[i]) + " ");
            }
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(String[] Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            int n = Data.length;
            for (int i = 0; i < n; i++) {
                writer.write(Data[i] + " ");
            }
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(String Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            writer.write(Data + " ");
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(double[][] Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            int n = Data.length;
            int m = Data[0].length;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                    writer.write(Double.toString(Data[i][j]) + " ");
                }
            }
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void SaveData_String(List<String> Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            int n = Data.size();
            for (int i = 0; i < n; i++) {
                writer.write(Data.get(i) + " ");
            }
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void SaveData(List<double[]> Data, String s, File filename, int a, int b, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));

            for (int i = a; i < b; i++) {
                writer.write(s + ":");
                for (int j = 0; j < Data.get(i).length; j++) {
                    writer.write(Double.toString(Data.get(i)[j]) + " ");
                }
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData_Status(List<Integer> Data, String s, File filename, int a, int b, boolean append) {
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));

            for (int i = a; i < b; i++) {
                writer.write(s + ":");
                writer.write(Double.toString(Data.get(i)) + " ");
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData_Status(List<Integer> Data, String s, File filename, boolean append) {
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));

            int size = Data.size();
            for (int i = 0; i < size; i++) {
                writer.write(s + ":");
                writer.write(Double.toString(Data.get(i)) + " ");
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void SaveData(int a, int b, String s, File filename, boolean append) {
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s + ":");
            writer.write(Double.toString(a) + " ");
            writer.write(Double.toString(b) + " ");
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(List<double[]> Data, String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            int num = Data.size();
            for (int i = 0; i < num; i++) {
                writer.write(s + ":");
                for (int j = 0; j < Data.get(i).length; j++) {
                    writer.write(Double.toString(Data.get(i)[j]) + " ");
                }
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(List<double[]> Data, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            int num = Data.size();
            for (int i = 0; i < num; i++) {
//                writer.write(s + ":");
                for (int j = 0; j < Data.get(i).length; j++) {
                    writer.write(Double.toString(Data.get(i)[j]) + " ");
                }
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(String s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(s);
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SaveData(double s, File filename, boolean append) {
        if (disableWrite) return;
        //write
        try {
            Writer writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
            writer.write(Double.toString(s));
            writer.write("\r\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}