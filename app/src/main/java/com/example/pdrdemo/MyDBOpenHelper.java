package com.example.pdrdemo;
/**
 * Created by zbs on 2023.
 *  * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.PDR.PDRposition.PDRmanager;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

public class MyDBOpenHelper extends SQLiteOpenHelper {

    private static final int VERSION=1;
    private static final String DBNAME="MIPNS.db";

    private Context mContext;

    public MyDBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext=context;
    }
    // 创建MLA表
    public static final String CREATE_GEOMLA = "create table geomla ("
            + "id integer primary key autoincrement, "
            + "X real, "
            + "Y real, "
            + "Z real, "
            + "describe text, "
            + "floor integer, "
            + "sensors text)";
    // 创建步行节点表
    public static final String CREATE_StepPoint = "create table StepPoint ("
            + "id integer primary key autoincrement, "
            + "X real, "
            + "Y real, "
            + "Z real, "
            + "describe text, "
            + "floor integer)";

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_GEOMLA);
        db.execSQL(CREATE_StepPoint);
        Toast.makeText(mContext, "CreateDB succeed", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //匹配陀螺仪MLA
    public static double[] querySqlGyro(SQLiteDatabase database,double[] pos,int Whichfloor){
        // 执行 SQL 查询语句
//        String query = "SELECT * FROM geomla where sensors='gyro' and floor='5'";
        String query=String.format("SELECT * FROM geomla where sensors='gyro' and floor='%d'",Whichfloor);
        Cursor cursor = database.rawQuery(query, null);
        double knownX=pos[0];
        double knownY=pos[1];
        double finalPos[];

        if (cursor!=null){
            int count=cursor.getCount();
            double dif=0;
            double[][] coordinates=new double[count][count];
            int J=0;

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double X = cursor.getDouble(cursor.getColumnIndex("X"));
                double Y = cursor.getDouble(cursor.getColumnIndex("Y"));
                double Z = cursor.getDouble(cursor.getColumnIndex("Z"));
                String describe = cursor.getString(cursor.getColumnIndex("describe"));
                int floor = cursor.getInt(cursor.getColumnIndex("floor"));
                String sensors = cursor.getString(cursor.getColumnIndex("sensors"));

                coordinates[J][0]=X;
                coordinates[J][1]=Z;//依据已知数据  设定建筑物模型的Z坐标为地面平面     Y为垂直地面的坐标

                J++;
            }
            cursor.close();
            ///判断最短距离的点  并记录对应坐标 返回
            finalPos= CalDif(coordinates,knownX,knownY,pos);

            if (finalPos!=null){
                return  finalPos;
            }
        }

        String ss="ss";
        return null;
    }

    ///计算已知坐标点与查询到的点的距离最短的坐标值。
    ////计算三维
    private static double[] CalDif(double[][] coordinates,double knownX,double knownY,double knownZ,double[] knownPos){
        double[] finalpos=new double[3];
        /////将实时PDR坐标依据初始位置  计算为建筑物模型坐标系对应坐标  用于计算距离
        double[]knownPdrToMla  =SensorModelMethod.PdrPosTransformToMla(knownPos,SensorModelMethod.initialX,SensorModelMethod.initialZ,SensorModelMethod.initialY);
        double knownPdrToMlaX=knownPdrToMla[0];
        double knownPdrToMlaY=knownPdrToMla[1];
        double knownPdrToMlaZ=knownPdrToMla[2];


        int minDifferenceIndex = -1;  // 最小差值的索引
        double minDifference = Double.MAX_VALUE;  // 最小差值

        // 遍历坐标数组
        for (int i = 0; i < coordinates.length; i++) {

            double currentX = coordinates[i][0];
            double currentY = coordinates[i][1];
            double currentZ = coordinates[i][2];

            double difference = Math.sqrt(Math.pow((currentX - knownPdrToMlaX), 2) + Math.pow((currentY - knownPdrToMlaY), 2)+ Math.pow((currentZ - knownPdrToMlaZ), 2) );

            // 更新最小差值和索引
            if (difference < minDifference) {
                minDifference = difference;
                minDifferenceIndex = i;
            }
        }
        if (minDifferenceIndex != -1) {
            double closestX = coordinates[minDifferenceIndex][0];
            finalpos[0]=closestX;
            double closestY = coordinates[minDifferenceIndex][1];
            finalpos[1]=closestY;
            double closestZ = coordinates[minDifferenceIndex][2];
            finalpos[2]=closestZ;
            System.out.println("最接近已知坐标的数据为：" + closestX + ", " + closestY+ ", " + closestZ);
        } else {
            System.out.println("坐标数组为空");
            finalpos=null;//没有值则返回空
        }


        return finalpos;

    }
    ////计算二维时
    private static double[] CalDif(double[][] coordinates,double knownX,double knownY,double[] knownPos){

        double[] finalpos=new double[3];

        /////将实时PDR坐标依据初始位置  计算为建筑物模型坐标系对应坐标  用于计算距离
        double[]knownPdrToMla  =SensorModelMethod.PdrPosTransformToMla(knownPos,SensorModelMethod.initialX,SensorModelMethod.initialZ);
        double knownPdrToMlaX=knownPdrToMla[0];
        double knownPdrToMlaY=knownPdrToMla[1];


        int minDifferenceIndex = -1;  // 最小差值的索引
        double minDifference = Double.MAX_VALUE;  // 最小差值

        // 遍历坐标数组
        for (int i = 0; i < coordinates.length; i++) {
            //获得到的coordinates为数据库内坐标，需要转换为PDR对应坐标进行距离比较

            double currentX = coordinates[i][0];
            double currentY = coordinates[i][1];

            double difference = Math.sqrt(Math.pow((currentX - knownPdrToMlaX), 2) + Math.pow((currentY - knownPdrToMlaY), 2));


            // 更新最小差值和索引
            if (difference < minDifference) {
                minDifference = difference;
                minDifferenceIndex = i;
            }
        }
        if (minDifferenceIndex != -1) {
            double closestX = coordinates[minDifferenceIndex][0];
            finalpos[0]=closestX;
            double closestY = coordinates[minDifferenceIndex][1];
            finalpos[1]=closestY;

            finalpos[2]= PDRmanager.TesterHeight;

            System.out.println("最接近已知坐标的数据为：" + closestX + ", " + closestY);
        } else {
            System.out.println("坐标数组为空");
            finalpos=null;//没有值则返回空
        }


        return finalpos;

    }



    ///////查询气压计MLA
    public static double[] querySqlPres(SQLiteDatabase database,double[] pos,int Whichfloor){
        String query=String.format("SELECT * FROM geomla where sensors='pres' and floor='%d'",Whichfloor);
        Cursor cursor = database.rawQuery(query, null);

        double knownX=pos[0];
        double knownY=pos[1];
        double knownZ=pos[2];

        double finalPos[];
        if (cursor!=null){
            int count=cursor.getCount();
            double dif=0;
            double[][] coordinates=new double[count][count];
            int J=0;

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double X = cursor.getDouble(cursor.getColumnIndex("X"));
                double Y = cursor.getDouble(cursor.getColumnIndex("Y"));
                double Z = cursor.getDouble(cursor.getColumnIndex("Z"));
                String describe = cursor.getString(cursor.getColumnIndex("describe"));
                int floor = cursor.getInt(cursor.getColumnIndex("floor"));
                String sensors = cursor.getString(cursor.getColumnIndex("sensors"));

                coordinates[J][0]=X;
                coordinates[J][1]=Z;//建筑物模型的Z坐标为地面平面  Y为垂直地面的坐标，暂不用于计算
                coordinates[J][2]=Y;
                J++;
            }
            cursor.close();
            ///判断最短距离的点  并记录对应坐标 返回
            finalPos= CalDif(coordinates,knownX,knownY,knownZ,pos);

            if (finalPos!=null){
                return  finalPos;
            }

        }

        String ss="ss";
        return null;

    }

    ///////查询加速度计MLA  2023
    public static Boolean IsFireDoor=false;
    public static double[] querySqlAcc(SQLiteDatabase database,double[] pos,int Whichfloor){
        String query=String.format("SELECT * FROM geomla where sensors='acc' and floor='%d'",Whichfloor);
        Cursor cursor = database.rawQuery(query, null);

        double knownX=pos[0];
        double knownY=pos[1];
        double knownZ=pos[2];

        double finalPos[];
        if (cursor!=null){
            int count=cursor.getCount();
            double dif=0;
            double[][] coordinates=new double[count][count];
            int J=0;

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double X = cursor.getDouble(cursor.getColumnIndex("X"));
                double Y = cursor.getDouble(cursor.getColumnIndex("Y"));
                double Z = cursor.getDouble(cursor.getColumnIndex("Z"));
                String describe = cursor.getString(cursor.getColumnIndex("describe"));
                int floor = cursor.getInt(cursor.getColumnIndex("floor"));
                String sensors = cursor.getString(cursor.getColumnIndex("sensors"));

                coordinates[J][0]=X;
                coordinates[J][1]=Z;//建筑物模型的Z坐标为地面平面  Y为垂直地面的坐标，暂不用于计算
                coordinates[J][2]=Y;
                J++;
            }
            cursor.close();
            ///判断最短距离的点  并记录对应坐标 返回
            finalPos= CalDif(coordinates,knownX,knownY,knownZ,pos);

            if (finalPos!=null){
                /// 测试数据
                if (finalPos[0]==7 && finalPos[1]==-1.5 && finalPos[2]==13.5){
                    IsFireDoor=true;
                }
                /// 测试数据
                if (finalPos[0]==7 && finalPos[1]==-1.5 && finalPos[2]==18){
                    IsFireDoor=false;
                }

                return  finalPos;
            }

        }

        String ss="ss";
        return null;

    }

    ///////查询楼梯台阶相关MLA
    public static double[] querySqlPresFloor(SQLiteDatabase database,double[] pos,int Whichfloor){
        String query=String.format("SELECT * FROM geomla where sensors='floor' and floor='%d'",Whichfloor);
        Cursor cursor = database.rawQuery(query, null);

        double knownX=pos[0];
        double knownY=pos[1];
        double knownZ=pos[2];

        double finalPos[];
        if (cursor!=null){
            int count=cursor.getCount();
            double dif=0;
            double[][] coordinates=new double[count][count];
            int J=0;

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double X = cursor.getDouble(cursor.getColumnIndex("X"));
                double Y = cursor.getDouble(cursor.getColumnIndex("Y"));
                double Z = cursor.getDouble(cursor.getColumnIndex("Z"));
                String describe = cursor.getString(cursor.getColumnIndex("describe"));
                int floor = cursor.getInt(cursor.getColumnIndex("floor"));
                String sensors = cursor.getString(cursor.getColumnIndex("sensors"));

                coordinates[J][0]=X;
                coordinates[J][1]=Z;//建筑物模型的Z坐标为地面平面  Y为垂直地面的坐标，暂不用于计算
                coordinates[J][2]=Y;
                J++;
            }
            cursor.close();
            ///判断最短距离的点  并记录对应坐标 返回
            finalPos= CalDif(coordinates,knownX,knownY,knownZ,pos);

            if (finalPos!=null){
                ///写死的特定5楼楼梯最高点坐标，后续改为describe条件判定
                if (finalPos[0]==5 && finalPos[1]==-4 && finalPos[2]==18){
                    IsFireDoor=false;
                    SensorModelMethod.IsFloorStepCheck=false;
                }

                return  finalPos;
            }

        }

        String ss="ss";
        return null;

    }

}////代码末端
