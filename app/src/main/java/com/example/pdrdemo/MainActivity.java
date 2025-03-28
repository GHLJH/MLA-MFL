package com.example.pdrdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.Indoor.HA.HAModel;
import com.Indoor.HA.Response.HEDestroyResponseEvent;
import com.Indoor.HA.Response.HEInitResponseEvent;
import com.Indoor.HA.Response.HEStartResponseEvent;
import com.Indoor.HA.Response.HEStopResponseEvent;
import com.Indoor.HA.Result.HEResultEvent;
import com.Indoor.ModeRecognition.Response.modeDestroyResponseEvent;
import com.Indoor.ModeRecognition.Response.modeInitResponseEvent;
import com.Indoor.ModeRecognition.Response.modeStartResponseEvent;
import com.Indoor.ModeRecognition.Response.modeStopResponseEvent;
import com.Indoor.ModeRecognition.Result.modeResultEvent;
import com.Indoor.ModeRecognition.modeModel;
import com.KF.KFModel;
import com.KF.Response.KFDestroyResponseEvent;
import com.KF.Response.KFInitResponseEvent;
import com.KF.Response.KFStartResponseEvent;
import com.KF.Response.KFStopResponseEvent;
import com.KF.Result.KFResultEvent;
import com.PDR.PDRposition.PDRmanager;
import com.PDR.Response.pdrDestroyResponseEvent;
import com.PDR.Response.pdrInitResponseEvent;
import com.PDR.Response.pdrStartResponseEvent;
import com.PDR.Response.pdrStopResponseEvent;
import com.PDR.Result.pdrResultEvent;
import com.PDR.pdrModel;
import com.Sensors.Response.SensorsDestroyResponseEvent;
import com.Sensors.Response.SensorsInitResponseEvent;
import com.Sensors.Response.SensorsStartResponseEvent;
import com.Sensors.Response.SensorsStopResponseEvent;
import com.Sensors.Result.SensorsResultEvent;
import com.Sensors.SensorsModel;
import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DateUtil;
import org.gispower.eventbus.Subscribe;
import org.gispower.eventbus.ThreadMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.ModelContext;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.ModelResultEvent;
import cn.edu.whu.lmars.loccore.event.LocEventFactory;
import cn.edu.whu.lmars.loccore.model.ILocContext;
import cn.edu.whu.lmars.loccore.model.ILocModel;

/**
 * Created by Pumpkin on 2020/9/9.
 * Modified by Zbs on 202309
 * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class MainActivity extends AppCompatActivity  {
    private ILocModel locSensor;
    private ILocModel locMode;
    private ILocModel locHA;
    private ILocModel locKF;
    private  Context context;
    private ILocModel locPDR;

    // 画布信息
    private ImageView iv_map;
    private Bitmap baseBitmap;
    private Paint paint = new Paint();
    private Canvas canvas;
    private static int height = 1000;
    private static int wide = 720;
    private static int m2pixel = 5;   // One meter corresponding pixel distance 一米对应像素距离
    private Vector vpos = new Vector();

    DbUtils db;//数据库   用来存储获得的坐标
    int m_StartSave=0;  //是否存储坐标到数据库
    int sensor_id=1;
    private Button btn_Save;
    float Ekf_angle;//存储角度值
    float Kf_angle;
    float Origin_angle;//不适用滤波时的行进角度
    String str_Ekf_angle;
    String str_Kf_angle;
    // 动态获取存储权限，存储日志需要
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    private TextView tv_ekf, tv_kf, tv_mode, tv_pdr, tv_x, tv_y,tv_step,  tv_NormalAngle  ,tv_z,tv_pres ,tv_floor,tv_AccVar;
    private MyDBOpenHelper DbHelper;
    private static final int VERSION=1;
    private static final String DBNAME="MIPNS.db";
    public static SQLiteDatabase sqLiteDatabase;



    private static Context mainActivity_context;
    ///用于标题的小菜单 选择了哪一种方式进行测算
    public static int SelectEKF=1;
    public static int SelectKF=0;
    public static int SelectMLA=1;
    private Menu menu;

    /**
     * tv_ekf: ekf航向角度
     * tv_kf：kf航向角
     * tv_mode：手机模式
     * tv_pdr：步长信息
     * tv_x：坐标X
     * tv_y: 坐标Y
     * tv_step: 步数
     * tv_z:坐标Z
     * tv_pres：气压
     * tv_floor：所在楼层
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity_context=getApplicationContext();
        // 动态请求存储的读写权限
        verifyStoragePermissions(this);
        LocEventFactory.registerEventHandle(this);
        // 日志模块初始化；供调试用； 存储路径为 文件夹：“根目录/Logger”
        Logger.addLogAdapter(new AndroidLogAdapter());
        // 保存日志到本地
        Logger.addLogAdapter(new DiskLogAdapter());
        Logger.i("测试日志存储！");        // 打印日志同时会存储到文件

        db = DbUtils.create(getApplicationContext());//数据库
        ///每次启动时  清除残存数据
        checkAndClearDatabase(db);

        bindViews();



        try {
           ///创建并连接SQLITE数据库  离线时可以考虑使用excel等作为代替使用
        DbHelper= new MyDBOpenHelper(this,DBNAME,null,VERSION);
        sqLiteDatabase= DbHelper.getWritableDatabase();
        if ( ! queryDbExist(sqLiteDatabase)) {////如果数据库内没有数据  则进行插入数据
            readExcelFileFromAssets(sqLiteDatabase);
        }
        } catch (IOException e) {
            e.printStackTrace();
        }




    }
    ///标题右上角选项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        this.menu=menu;
        return true;
    }
    ////标题栏菜单选项点击事件
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){////EKF  与  KF  只能选其一
            case R.id.menuEKF:
                Toast.makeText(getApplicationContext(),"EKF",Toast.LENGTH_SHORT).show();
                if (SelectEKF==0){
//                    menuEKF.setText("✔EKF");
                    menu.findItem(R.id.menuEKF).setTitle("✔EKF");
                    SelectEKF=1;
                    menu.findItem(R.id.menuKF).setTitle("KF");
                    SelectKF=0;
                }
                else {
//                    menuEKF.setText("EKF");
                    menu.findItem(R.id.menuEKF).setTitle("EKF");
                    SelectEKF=0;
                    menu.findItem(R.id.menuKF).setTitle("✔KF");
                    SelectKF=1;
                }
                break;
            case R.id.menuKF:
                Toast.makeText(getApplicationContext(),"KF",Toast.LENGTH_SHORT).show();
                if (SelectKF==0){
                    menu.findItem(R.id.menuKF).setTitle("✔KF");
                    SelectKF=1;
                    menu.findItem(R.id.menuEKF).setTitle("EKF");
                    SelectEKF=0;
                }
                else {
                    menu.findItem(R.id.menuEKF).setTitle("✔EKF");
                    SelectEKF=1;
                    menu.findItem(R.id.menuKF).setTitle("KF");
                    SelectKF=0;
                }
                break;
            case R.id.menuMLA:
                Toast.makeText(getApplicationContext(),"MLA",Toast.LENGTH_SHORT).show();
                if (SelectMLA==0){
                    menu.findItem(R.id.menuMLA).setTitle("✔MLA");
                    SelectMLA=1;
                }
                else {
                    menu.findItem(R.id.menuMLA).setTitle("MLA");
                    SelectMLA=0;
                }
                break;
            case R.id.menuFloor:
                Toast.makeText(getApplicationContext(),"",Toast.LENGTH_SHORT).show();
                if (PDRmanager.CurrentFloor==4){//testing
                    menu.findItem(R.id.menuFloor).setTitle("✔5F");
                    SensorModelMethod.initialX=-45.13;
                    SensorModelMethod.initialZ=0;
                    SensorModelMethod.initialY=18;
                    PDRmanager.CurrentFloor=5;
                }
                else if (PDRmanager.CurrentFloor==5){//testing
                    menu.findItem(R.id.menuFloor).setTitle("✔4F");
                    SensorModelMethod.initialX=-23.43;
                    SensorModelMethod.initialZ=0;
                    SensorModelMethod.initialY=13.5;
                    PDRmanager.CurrentFloor=4;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    // 绑定控件
    private void bindViews() {
        //ekf航向角度
        tv_ekf = findViewById(R.id.tv_ukf);
        //kf航向角
        tv_kf = findViewById(R.id.tv_kf);
        //手机模式
        tv_mode = findViewById(R.id.tv_mode);
        //步长信息
        tv_pdr =  findViewById(R.id.tv_pdr);
        //坐标X
        tv_x =  findViewById(R.id.tv_x);
        //坐标Y
        tv_y =  findViewById(R.id.tv_y);
        //步数
        tv_step =  findViewById(R.id.tv_step);
        //未滤波方向角度
        tv_NormalAngle=findViewById(R.id.tv_NormalAngle);
        tv_z =  findViewById(R.id.tv_z);
        tv_pres =  findViewById(R.id.tv_pres);
        tv_floor=findViewById(R.id.tv_floor);
        iv_map = findViewById(R.id.iv_map);
        tv_AccVar=findViewById(R.id.tv_AccVar);
        btn_Save=findViewById(R.id.btn_Save);




        // Create a blank picture创建空白图片
        baseBitmap = Bitmap.createBitmap(wide, height, Bitmap.Config.ARGB_8888);
        // Create a canvas 创建一个
        canvas = new Canvas(baseBitmap);
        // Canvas背景是白色
        canvas.drawColor(Color.WHITE);

        paint.setColor(Color.BLACK);
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);

        // Fill the background color with white
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        canvas.drawLine(wide/2, 0, wide/2, height, paint);
        canvas.drawLine(0, height/2, wide,height/2, paint);
        // Set the brush color to green将笔刷颜色设置为绿色
        paint.setColor(Color.GREEN);
        // Set the brush width to 5 pixels将画笔宽度设置为5像素
        paint.setStrokeWidth(2);

        iv_map.setImageBitmap(baseBitmap);


    }

    /**
     * 传感器数据采集模块
     */

    // 传感器模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorInitEvent(SensorsInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }

    }
    // 传感器模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorStartEvent(SensorsStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorStopEvent(SensorsStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorDestroyEvent(SensorsDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 传感器模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorResultEvent(SensorsResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("SensorData:"+e.getRslt());  //打印模块输出结果
    }

    /**
     * 手机模式识别模块
     */

    // 模式识别模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeInitEvent(modeInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeStartEvent(modeStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeStopEvent(modeStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeDestroyEvent(modeDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // 模式识别模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeResultEvent(modeResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("phonePose:"+e.getResult());   //打印模块输出结果
    }

    /**
     * KF航向估计模块
     */

    // KF航向估计模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFInitEvent(KFInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFStartEvent(KFStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    //打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFStopEvent(KFStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFDestroyEvent(KFDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // KF航向估计模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFResultEvent(KFResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("KF:"+e.getResult()[0]);        // 打印模块输出结果
    }

    /**
     * EKF航向估计模块
     */

    // EKF航向估计模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAInitEvent(HEInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                //事件响应失败
                break;
        }
    }
    // EKF航向估计模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAStartEvent(HEStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAStopEvent(HEStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HADestroyEvent(HEDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    //打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // EKF航向估计模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAResultEvent(HEResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("HA:"+e.getResult()[0]);        // 打印模块输出结果
    }

    /**
     * PDR定位模块
     */

    // PDR定位模块初始化事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrInitEvent(pdrInitResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块开启事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStartEvent(pdrStartResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块关闭事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStopEvent(pdrStopResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块销毁事件
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrDestroyEvent(pdrDestroyResponseEvent e) {
        // 事件响应
        switch (e.getState()){
            case OK:
                // 事件响应成功
                System.out.println(e.getMsg());    // 打印附带的信息
                break;
            case Fail:
                // 事件响应失败
                break;
        }
    }
    // PDR定位模块返回结果
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrResultEvent(pdrResultEvent e) {
        System.out.println(e.getRsltType());    // 打印结果类型
        System.out.println("StepLen: "+e.getResult()[2]);
        System.out.println("posX："+e.getResult()[3]);
        System.out.println("posY："+e.getResult()[4]); // 打印模块输出结果
    }

    // 请求写文件权限
    public static void verifyStoragePermissions(Activity activity) {
        try {
            // 检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 返回结果可视化
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Result(ModelResultEvent e) {
        switch (e.getRsltType()) {

            // EKF航向角解算
            case BLELoc:
                double[] HA = ((double[]) e.getResult()).clone();
                DecimalFormat df3 = new DecimalFormat("#0.000");

                String str1= df3.format(Double.parseDouble(String.valueOf(HA[0])));
                Message HA_msg = new Message();
                HA_msg.what = 1;
                HA_msg.obj = str1;
                Ekf_angle=(float) Double.parseDouble(String.valueOf(HA[0]));//保存EKF值
                str_Ekf_angle=str1;

                this.mhandler.sendMessage(HA_msg);

                tv_pres.setText("气压[hPa]："+String.format("%.4f",PDRmanager.CurrentPressureValue));
                tv_floor.setText("楼层："+PDRmanager.CurrentFloor);
                tv_AccVar.setText("加速度方差："+String.format("%.4f",PDRmanager.M_Variance));

                break;

            // KF航向角解算
            case PhonePose:
                double[] KF = ((double[]) e.getResult());
                DecimalFormat dff3 = new DecimalFormat("#0.000");

                String str2= dff3.format(Double.parseDouble(String.valueOf(KF[0])));
                Kf_angle=(float)Double.parseDouble(String.valueOf(KF[0]));//保存KF值
                str_Kf_angle=str2;

                Message KF_msg = new Message();
                KF_msg.what = 2;
                KF_msg.obj = str2;
                this.mhandler.sendMessage(KF_msg);

                break;

            case CameraLoc:

                String mode= null;
                if( e.getResult() != null) {
                    mode = (String) e.getResult();
                }

                Message mode_msg = new Message();
                mode_msg.what = 3;
                mode_msg.obj = mode;
                this.mhandler.sendMessage(mode_msg);

                break;


            case PDRHandLoc:
                double[] pdr = ((double[]) e.getResult()).clone();
                DecimalFormat df = new DecimalFormat("#0.00");
                Message str3_msg = new Message();
                Message str4_msg = new Message();
                Message str5_msg = new Message();
                Message str6_msg = new Message();

                Message str7_msg = new Message();//Z 2023


                String str3 = df.format(Double.parseDouble(String.valueOf(pdr[2]))); // 步长
                String str4 = df.format(Double.parseDouble(String.valueOf(pdr[3]))); // 坐标X
                String str5 = df.format(Double.parseDouble(String.valueOf(pdr[4]))); // 坐标Y
                String str6 = String.valueOf((int)pdr[7]); // 步数

                String str7 = df.format(Double.parseDouble(String.valueOf(pdr[8]))); // 坐标Z 2023

                float steplength=(float)Double.parseDouble(String.valueOf(pdr[2]));
                int stepcount=(int)pdr[7];


                str3_msg.what = 4;
                str3_msg.obj = str3;
                this.mhandler.sendMessage(str3_msg);

                str4_msg.what = 5;
                str4_msg.obj = str4;
                this.mhandler.sendMessage(str4_msg);

                str5_msg.what = 6;
                str5_msg.obj = str5;
                this.mhandler.sendMessage(str5_msg);

                str6_msg.what = 7;
                str6_msg.obj = str6;
                this.mhandler.sendMessage(str6_msg);

                str7_msg.what = 8;
                str7_msg.obj = str7;
                this.mhandler.sendMessage(str7_msg);

                Pos pos = new Pos();
                pos.x = (float) Double.parseDouble(str4);
                pos.y = (float) Double.parseDouble(str5);

                pos.z=(float) Double.parseDouble(str7);

                vpos.add(pos);
                repaint(vpos, canvas, paint, baseBitmap, iv_map);

                //保存坐标数据
                Save_PDR_pos(pos.x,pos.y,pos.z,steplength,stepcount);


                break;

        }
    }

    @SuppressLint("HandlerLeak")
    public Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case 1:
                    tv_ekf.setText("EKF角度[度]：" + msg.obj);
                    break;
                case 2:
                    tv_kf.setText("KF角度[度]：" + msg.obj);
                    break;
                case 3:
                    tv_mode.setText("手机模式：" + msg.obj);
                    break;
                case 4:
                    tv_pdr.setText("步长[米]：" + msg.obj);
                    break;
                case 5:
                    tv_x.setText("X坐标[米]：" + msg.obj);
                    break;
                case 6:
                    tv_y.setText("Y坐标[米]：" + msg.obj);
                    break;
                case 7:
                    tv_step.setText("步数：" + msg.obj);
                    break;

                case 8: //2023 Z
                    tv_z.setText("Z坐标[米]：" + msg.obj);
                    break;
            }


        }
    };


    // Button Click 测试
    public void InitModel(View view) {

        // 初始化模块的Context
        ILocContext locContext = new ModelContext(this);

        locSensor = new SensorsModel();
        locSensor.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locSensor.getModelInitEvent());

        locHA = new HAModel();
        locHA.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locHA.getModelInitEvent());

        locMode = new modeModel(context);
        locMode.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locMode.getModelInitEvent());

        locKF = new KFModel();
        locKF.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locKF.getModelInitEvent());


        locPDR = new pdrModel();
        locPDR.modelLoad(locContext);   // 初始系统环境
        LocEventFactory.publishEvent(locPDR.getModelInitEvent());

        tv_ekf.setText("EKF角度[度]："+ 0.0);
        tv_kf.setText("KF角度[度]："+ 0.0);
        tv_mode.setText("手机模式："+ null);
        tv_pdr.setText("步长[米]："+ 0.0);
        tv_x.setText("X坐标[米]："+ 0.0);
        tv_y.setText("Y坐标[米]："+ 0.0);
        tv_step.setText("步数：" + 0);

        tv_NormalAngle.setText("常规角度："+0.0);
        tv_z.setText("Z坐标[米]："+ 0.0);
        tv_pres.setText("气压[hPa]："+0.0);

        tv_floor.setText("楼层："+0);

    }

    public void StartModel(View view) {
        LocEventFactory.publishEvent(locSensor.getModelStartEvent());
        LocEventFactory.publishEvent(locHA.getModelStartEvent());
        LocEventFactory.publishEvent(locMode.getModelStartEvent());
        LocEventFactory.publishEvent(locKF.getModelStartEvent());
        LocEventFactory.publishEvent(locPDR.getModelStartEvent());

        // 清理轨迹
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // 清理位置
        while (vpos.size() >= 1) {
            vpos.remove(0);
        }

        // update cavas and brush
        canvas = new Canvas(baseBitmap);
        canvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        canvas.drawLine(wide/2, 0, wide/2, height, paint);
        canvas.drawLine(0, height/2, wide, height/2, paint);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        iv_map.setImageBitmap(baseBitmap);
    }

    public void StopModel(View view) {
        LocEventFactory.publishEvent(locPDR.getModelStopEvent());
        LocEventFactory.publishEvent(locKF.getModelStopEvent());
        LocEventFactory.publishEvent(locMode.getModelStopEvent());
        LocEventFactory.publishEvent(locHA.getModelStopEvent());
        LocEventFactory.publishEvent(locSensor.getModelStopEvent());

        tv_ekf.setText("EKF角度[度]："+ 0.0);
        tv_kf.setText("KF角度[度]："+ 0.0);
        tv_mode.setText("手机模式："+ null);
        tv_pdr.setText("步长[米]："+ 0.0);
        tv_x.setText("X坐标[米]："+ 0.0);
        tv_y.setText("Y坐标[米]："+ 0.0);
        tv_step.setText("步数：" + 0);
        tv_NormalAngle.setText("常规角度："+0.0);
        tv_z.setText("Z坐标[米]："+ 0.0);
        tv_pres.setText("气压[hPa]："+0.0);
        tv_floor.setText("楼层："+0);

        // Clear the walking track
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // Clear Vpos
        while (vpos.size() >= 1) {
            vpos.remove(0);
        }

        // update cavas and brush
        canvas = new Canvas(baseBitmap);
        canvas.drawColor(Color.WHITE);
        paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        canvas.drawLine(wide/2, 0, wide/2, height, paint);
        canvas.drawLine(0, height/2, wide, height/2, paint);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
        iv_map.setImageBitmap(baseBitmap);
    }

    public void DestroyModel(View view) {

        LocEventFactory.publishEvent(locPDR.getModelDestroyEvent());
        LocEventFactory.publishEvent(locKF.getModelDestroyEvent());
        LocEventFactory.publishEvent(locMode.getModelDestroyEvent());
        LocEventFactory.publishEvent(locHA.getModelDestroyEvent());
        LocEventFactory.publishEvent(locSensor.getModelDestroyEvent());

        tv_ekf.setText("EKF角度[度]：");
        tv_kf.setText("KF角度[度]：");
        tv_mode.setText("手机模式：");
        tv_pdr.setText("步长[米]：");
        tv_x.setText("X坐标[米]：");
        tv_y.setText("Y坐标[米]：");
        tv_step.setText("步数：");
        tv_NormalAngle.setText("常规角度：");
        tv_z.setText("Z坐标[米]：");
        tv_pres.setText("气压[hPa]：");
        tv_floor.setText("楼层：");

    }
    public static Context getContext(){
        return mainActivity_context;
    }
    //保存按钮
    public void SaveModel(View view) {

        if (m_StartSave==0){
            btn_Save.setText("停止记录");
            m_StartSave=1;
        }
        else {
            m_StartSave=0;
            btn_Save.setText("保存数据");
        }

        AccThread acchread=new AccThread();
        Thread Acctd = new Thread(acchread, "加速度计监测线程");
        Acctd.start();
    }
    //导出按钮
    public void OutPutModel(View view) {
        OutPutExcel();

    }

    // Define location(x,y)
    public class Pos {
        float x;
        float y;

        float z;
    }

    public void repaint(Vector vector, Canvas canvas, Paint paint, Bitmap baseBitmap, ImageView iv) {

        if (vector.size() == 3) {
            // Delete the first element
            vpos.remove(0);
        }

        if (vector.size() >= 2) {
            int size = vector.size();
            Pos pos1 = new Pos();
            Pos pos2 = new Pos();
            pos1 = (Pos) vpos.get(size - 2);
            pos2 = (Pos) vpos.get(size - 1);
            // Draw the trajectory of walking
            canvas.drawLine(wide/2 + m2pixel * pos1.y, height / 2 - m2pixel * pos1.x,
                    wide/2 + m2pixel * pos2.y, height / 2 - m2pixel * pos2.x, paint);
        }

        iv.setImageBitmap(baseBitmap);
    }

    public void  Save_PDR_pos(float PosX,float PosY,float PosZ,float StepLength,float StepCount){

        float MagAngle=pdrModel.m_Angle;
        float mlaAngle=(float)PDRmanager.MLA_Angle;
        tv_NormalAngle.setText("常规角度："+MagAngle+"");

        try {

            ////如果此时已经点击保存按钮  则保存数值
            if (m_StartSave==1){
                //保存到数据库  X,Y,Z  KF,EKF,步数  步长  ID  未滤波方向角→ 磁力计模组八分后角度
                Accelerate_info accelerate_info = new Accelerate_info(System.currentTimeMillis(), PosX, PosY,PosZ,Kf_angle,Ekf_angle,StepCount,StepLength,sensor_id,MagAngle);
                db.save(accelerate_info);  ///若是修改字段  需要卸载APP后重装
            }

        }
        catch (DbException dbe) {
            // TODO Auto-generated catch block
            dbe.printStackTrace();
            System.out.println("保存失败");
        }

    }
    /*
    导出表格
     */
    public  void  OutPutExcel(){

        //普通对话框
        final AlertDialog.Builder normalDia=new AlertDialog.Builder(MainActivity.this);
        normalDia.setTitle("正在写入磁盘操作");
        normalDia.setMessage("正在存储，请耐心等待1-2分钟\n将写入最近3000条记录");
        final AlertDialog dialog = normalDia.create();
        final AsyncTask<Void, Void, Void> task =  new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                // TODO Auto-generated method stub
                List<Accelerate_info> outList = null;
                try {
                    outList = db.findAll(Selector.from(Accelerate_info.class).where("sensor", "=", sensor_id).orderBy("time",true).limit(3000));
                } catch (DbException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    WriteExcel.writeList(outList);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(MainActivity.this, "没有sd卡权限或手机内存已满", Toast.LENGTH_LONG).show();
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                // TODO Auto-generated method stub
                super.onPostExecute(result);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "恭喜你写入成功", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder normalDia2=new AlertDialog.Builder(MainActivity.this);
                normalDia2.setTitle("写入完毕");

                normalDia2.setMessage("成功写入到手机内置存储根目录下\n1PDR文件夹");
                normalDia2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ///每次导出完成清除数据
                        checkAndClearDatabase(db);

                    }
                });
                AlertDialog dialog2 = normalDia2.create();
                dialog2.show();
            }
        };

        dialog.show();
        task.execute();
    }


    private void querySQL(SQLiteDatabase database){
        // 执行 SQL 查询语句
        String query = "SELECT * FROM geomla";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor!=null){

            int count=cursor.getCount();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                double X = cursor.getDouble(cursor.getColumnIndex("X"));
                double Y = cursor.getDouble(cursor.getColumnIndex("Y"));
                double Z = cursor.getDouble(cursor.getColumnIndex("Z"));
                String describe = cursor.getString(cursor.getColumnIndex("describe"));
                int floor = cursor.getInt(cursor.getColumnIndex("floor"));
                String sensors = cursor.getString(cursor.getColumnIndex("sensors"));
            }
            cursor.close();
        }

        String ss="ss";
    }
    //判断数据库内是否有数据
    private boolean queryDbExist(SQLiteDatabase database){
        // 执行 SQL 查询语句
        String query = "SELECT * FROM geomla";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor!=null){
            int count=cursor.getCount();

            if (count>0)
            {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;

        }
        cursor.close();
        return false;

    }

    ////读取excel中的数值  存储到Sqlite中
    private void readExcelFileFromAssets(SQLiteDatabase sqLiteDatabase) throws IOException {
        InputStream inputStream = getAssets().open("geomla.xls");//打开资源文件夹下数据
        // 将文件流解析成 POI 文档
        POIFSFileSystem fs = new POIFSFileSystem(inputStream);
        HSSFWorkbook workbook = new HSSFWorkbook(fs);
        HSSFSheet sheet = workbook.getSheetAt(0);
        // 得到这一行一共有多少列
        int totalColumns = sheet.getRow(0).getPhysicalNumberOfCells();
        // 得到最后一行的坐标
        int lastRowNum = sheet.getLastRowNum();

        ///从第二行开始读取
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            HSSFRow row = sheet.getRow(i);
            int id = (int) row.getCell(0).getNumericCellValue();
            double X = row.getCell(1).getNumericCellValue();
            double Y = row.getCell(2).getNumericCellValue();
            double Z = row.getCell(3).getNumericCellValue();
            String describe = GetCellType(row.getCell(4));
            int floor = (int) row.getCell(5).getNumericCellValue();
            String sensors = row.getCell(6).getStringCellValue();

            ContentValues contentValues = new ContentValues();
            contentValues.put("id", id);
            contentValues.put("X", X);
            contentValues.put("Y", Y);
            contentValues.put("Z", Z);
            contentValues.put("describe", describe);
            contentValues.put("floor", floor);
            contentValues.put("sensors", sensors);

            sqLiteDatabase.insert("geomla", null, contentValues);
        }

    }
    private String GetCellType(HSSFCell Cell){
        //自己去类型就可以,代码可以自己修改
        String cellValue = "";
        int cellType=Cell.getCellType();
        switch (cellType) {
            case HSSFCell.CELL_TYPE_STRING: //字符串类型
                cellValue= Cell.getStringCellValue().trim();
                break;
            case HSSFCell.CELL_TYPE_BOOLEAN:  //布尔类型
                cellValue = String.valueOf(Cell.getBooleanCellValue());
                break;
            case HSSFCell.CELL_TYPE_NUMERIC: //数值类型
                if (HSSFDateUtil.isCellDateFormatted(Cell)) {  //判断日期类型
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    Date date = HSSFDateUtil.getJavaDate(Cell.getNumericCellValue());
                    String dateValue = sdf.format(date);
                    cellValue = dateValue;
                } else {  //否
                    cellValue = new DecimalFormat("#.######").format(Cell.getNumericCellValue());
                }
                break;
            default: //其它类型，取空串吧
                cellValue = "";
                break;
        }
        return cellValue;
    }

    // 检查并清空数据库
    private void checkAndClearDatabase(DbUtils db) {
        try {
            // 获取数据数量
            long dataCount = db.count(Selector.from(Accelerate_info.class));

            // 如果有数据，则清空数据库
            if (dataCount > 0) {
                clearDatabase(db);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    // 清空数据库
    private void clearDatabase(DbUtils db) {
        try {
            db.deleteAll(Accelerate_info.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    ///加速度计进门判定
    private class AccThread implements Runnable{
        private boolean TimeCount = true;//
        //在run方法里复写需要进行的操作:
        @Override
        public void run(){
            while (TimeCount){
                try {
=                    if (PDRmanager.M_Variance>PDRmanager.ACC_Variance_Threshold){
                        Thread.sleep(1000);
                        if (PDRmanager.M_Variance<PDRmanager.ACC_Variance_Threshold){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 在主线程中执行操作
                                }
                            });
                            Thread.sleep(1000);
                            if (PDRmanager.M_Variance>PDRmanager.ACC_Variance_Threshold){
                                System.out.println("满足加速度进门判断条件3");
                                TimeCount=false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        PDRmanager.IsAcc=true;
                                    }
                                });
                            }

                        }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
