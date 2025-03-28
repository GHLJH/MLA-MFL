package com.PDR.PDRposition;


import com.PDR.PDRposition.Utility.AvgDegree;
import com.PDR.pdrModel;
import com.example.pdrdemo.MainActivity;
import com.example.pdrdemo.MyDBOpenHelper;
import com.example.pdrdemo.SensorModelMethod;

import org.apache.log4j.chainsaw.Main;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 * Modified by zbs 2023
 * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class PDRmanager extends Observable implements Runnable {

    public float RealTimeOri=0;

    private int mode=1; //default mode: hand
    //hand=1, call=2, pocket=3, swing=4
    public double[] PDRpos={0d,0d,0d};///初始坐标
    private boolean isCreat=false;
    private boolean isProcess=false;
    private PDRstream demo=new PDRstream();
    private int accNo=1;

    private int SampleCount=1;
    private int Count=1;

    private List<double[]> accValue = new ArrayList<double[]>();
    private List<double[]> magValue = new ArrayList<double[]>();
    private List<double[]> magValue_type2 = new ArrayList<double[]>();

    private Vector<double[]> gyrValue = new Vector<>(0,1);
    private Vector<double[]> presValue = new Vector<>(0,1);

    private double accTimeStamp;
    private double accTimeStampLast=0d;
    private boolean isChanged=true;
    private boolean isWalk=true;
    protected Vector<double[]> HaCache = new Vector<>(0,1); //heading angle data
    private boolean updateStatus=false;
    protected double iniOrientation=0d;
    private int HacacheLength=30;
    private int CaliIniOrientLength=30;
    private static final String status1="swing";
    private static final String status2="calling";
    private static final String status3="hand";
    private static final String status4="pocket";

    protected Vector<Double> AccForVariance=new Vector<>(0,1);    //存储加速度计数据用于计算方差
    protected Vector<Double> PresForAvg=new Vector<>(0,1);    //存储气压计数据用于判断楼层
    protected Vector<Double> AccForVariance2=new Vector<>(0,1);    //存储加速度计数据用于计算方差

    public static double M_Variance=0;  //记录实时加速度计方差
    public static double M_Variance2=0;  //记录实时加速度计方差

    public static double ACC_Variance_Threshold=0.2;
    public static double InitialPressureValue=0;//初始气压
    public static double CurrentPressureValue=0;//实时气压均值
    private static double GyrThreshold_R=-0.8; ///陀螺仪左右转阈值
    private static double GyrThreshold_L=0.8;
    private static double PresThreshold=0.5;//气压计楼层变动阈值
    public static double Pres_Horizontal_Threshold=0.1;//气压计楼层水平变动阈值
    public static double Pres_Floor_Threshold=0.02;//气压计楼梯变动阈值 用于检测楼梯的值

    public static double MLA_Angle=0;
    public static Boolean IsAcc=false;
    public static int CurrentFloor=4;///初始楼层位置   根据蓝牙初始定位获取

    private static int HalfFloorCalCount=1; ///半层判定
    private static int FloorCalCount=0;

    public static double TesterHeight=0;//1.5d;//记录行人手机高度值  若是气压计变动

    private static boolean  IsPres=false;  //判断是否楼层变动
    private static boolean IsCornorMLA=false;////判断是否匹配至拐角MLA点 是则需要开启加速度计进门判定

    //start a new thread to deal with PDR process
    @Override
    public void run(){
        onStart(mode,PDRpos);
        onProcess();
        while(true){
            try {
                Thread.sleep(45);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Vector<String> Mode = new Vector<>(0,1);
            Vector<double[]> Heading = new Vector<>(0,1);

            Mode.clear();
            Mode.addAll(pdrModel.getMode());

            Heading.clear();
            //依据开关判定选用哪类数据
            if (MainActivity.SelectEKF==1){
                Heading.addAll(pdrModel.getHa());
            }
            else if(MainActivity.SelectKF==1){
                Heading.addAll(pdrModel.getKF());
            }


            if(Mode.size()> 0){
                String ModeInfo= Mode.get(Mode.size()-1);
                mode = getIndex(ModeInfo);
            }


            if(Heading.size() > 0){
                double[] HaInfo = Heading.get(0);
                updateHaCache(HaInfo[6],HaInfo[0]);
            }


            accValue.clear();
            accValue.addAll(pdrModel.getAccDataList());//获取加速度计数据

            magValue.clear();
            magValue.addAll(pdrModel.getMagDataList());//获取磁力计14数据

            gyrValue.clear();
            gyrValue.addAll(pdrModel.getGyrDataList());//获取陀螺仪数据

            presValue.clear();
            presValue.addAll(pdrModel.getPreDataList());//获取气压计数据
            if (presValue.size()>0){
                UpdatePresValue();//更新气压值
                if (InitialPressureValue==0 &&presValue.size()>=50){
                    InitialPressureValue=CurrentPressureValue;
                }

                if (InitialPressureValue!=0){
                    //监测气压计
                    //气压计模组的监测
                    //////气压计判断是否进行楼层变动  实时进行
                    IsPres=SensorModelMethod.PressureMonitor(CurrentPressureValue,InitialPressureValue,CurrentFloor,PresThreshold,M_Variance);
                    if (IsPres){FloorCalCount=1;}
                }

            }

            magValue_type2.clear();
            magValue_type2.addAll(pdrModel.getMagDataTYPE2List());//获取磁力计2数据
            // 获取实时数据后，针对不同传感器模组进行数值的监测，判断是否满足匹配条件

            double[] acc=new double[4];
            if(accValue.size()>0) {
                acc = accValue.get(0);
                accTimeStamp = acc[3];
//                UpdateAccValue();//记录加速度计方差值
                UpdateAccValue2();
            }

            if(Math.abs(accTimeStamp-accTimeStampLast)>10d){
                isChanged=true;
                accTimeStampLast=accTimeStamp;
            }else{
                isChanged=false;
            }
            if(isProcess&&isChanged){
                boolean[] getPDRinfo=demo.process(accNo,acc,(long)accTimeStamp);
                if(getPDRinfo[1]!=isWalk){
                    int i=-1;
                    isWalk=getPDRinfo[1];
                    if(isWalk)i=1;
                    setChanged();
                    Object message;
                    message=0+","+i;
                    notifyObservers(message);
                }
                Boolean detectflag=getPDRinfo[0];
                accNo=accNo+1;
                //Log.d("yangfan",String.valueOf(accTimeStamp));
                if(detectflag){
                    if(demo.StepResultArray.size()==1){
                        demo.StepResultArray.get(0).pos=PDRpos.clone();
                    }else{
                        ////参数
                        double[] DemPos=demo.StepResultArray.get(demo.StepResultArray.size()-2).pos;
                        double timeA=demo.StepResultArray.get(demo.StepResultArray.size()-1).StepInfo[2]+demo.firstAccTimeStamp;
                        double stepLength=demo.StepResultArray.get(demo.StepResultArray.size()-1).steplength;
                        double stepnum=demo.StepResultArray.get(demo.StepResultArray.size()-1).StepSqNo[3];
                        //计算实时坐标
                        demo.StepResultArray.get(demo.StepResultArray.size()-1).pos=
                                updatePos(demo.StepResultArray.get(demo.StepResultArray.size()-2).pos,
                                        demo.StepResultArray.get(demo.StepResultArray.size()-1).StepInfo[2]+demo.firstAccTimeStamp,
                                        demo.StepResultArray.get(demo.StepResultArray.size()-1).steplength);

                        double[] FinalPOS=demo.StepResultArray.get(demo.StepResultArray.size()-1).pos;

                        ///MLA检测开关
                        if (MainActivity.SelectMLA==1) {
                            ///增加MLA模组的匹配
                            MlaMatch(FinalPOS);
                        }

                    }


                    setChanged();
                    Object message1;
                    message1=demo.getPDRresult()[0]+","+demo.getPDRresult()[1]+","
                            +demo.getPDRresult()[2]+","+demo.getPDRresult()[3]+","+demo.getPDRresult()[4]
                            +","+demo.getPDRresult()[6];
                    notifyObservers(message1);
                }

            }
            onStart(mode,PDRpos);
        }
    }

    public PDRmanager() {
//        LocEventFactory.registerEventHandle(this);
    }

    //////MLA匹配方法
    private void MlaMatch(double[] FinalPOS){
        if (FloorCalCount==1) {
            double[] MLApos = MyDBOpenHelper.querySqlPres(MainActivity.sqLiteDatabase, FinalPOS, CurrentFloor);
            if (MLApos != null) {
                //坐标转换
                double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ, SensorModelMethod.initialY);
                //传入路径
                demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;
                TesterHeight = NewMlaPos[2];///高度变化时，记录用户高度值的变动   陀螺仪转弯不记录


                IsPres = false;
                FloorCalCount=0;///
                HalfFloorCalCount = 1; ///完成过楼层变动  才再次打开半层变化记录

            }
        }

        //楼梯平台判断
        if (HalfFloorCalCount == 1) ////只进行一次半楼层判定  下一次等完成过楼层变动后再复原为1
        {
            int IsFloorHalf = SensorModelMethod.PressureMonitor_HalfFloor(CurrentPressureValue, InitialPressureValue, CurrentFloor, PresThreshold, M_Variance);
            int WayTo = SensorModelMethod.WhichMethodToChangeFloor(M_Variance);//移动方式判定  行走/静止
            if (WayTo == 2) {//使用楼梯变动时才匹配；楼梯平台节点
                if (IsFloorHalf == 1) {
                    ////1上楼 查询当前楼层相关楼梯节点坐标
                    double[] MLApos = MyDBOpenHelper.querySqlPres(MainActivity.sqLiteDatabase, FinalPOS, CurrentFloor);
                    if (MLApos != null) {
                        //坐标转换
                        double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ, SensorModelMethod.initialY);
                        //传入路径
                        demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;
                        TesterHeight = NewMlaPos[2];///高度变化时，记录用户高度值的变动   陀螺仪转弯不记录
                        HalfFloorCalCount = 0;
                    }

                } else if (IsFloorHalf == 2) {
                    ////2下楼 查询当前楼层下一层相关楼梯节点坐标
                    int ToFloor = CurrentFloor - 1;
                    double[] MLApos = MyDBOpenHelper.querySqlPres(MainActivity.sqLiteDatabase, FinalPOS, ToFloor);
                    if (MLApos != null) {
                        //坐标转换
                        double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ, SensorModelMethod.initialY);
                        //传入路径
                        demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;
                        TesterHeight = NewMlaPos[2];///高度变化时，记录用户高度值的变动   陀螺仪转弯不记录

                        HalfFloorCalCount = 0;
                    }

                }
            }//
        }




        //陀螺仪
        Boolean IsGyr = SensorModelMethod.GyrValuesMonitor(gyrValue, GyrThreshold_R, GyrThreshold_L);
        if (IsGyr) {
            //匹配MLA  并计算实时坐标与相关MLA的距离关系  匹配至最合适的点 存入MLAPOS  用于后续坐标转换
            double[] MLApos = MyDBOpenHelper.querySqlGyro(MainActivity.sqLiteDatabase, FinalPOS, CurrentFloor);
            if (MLApos != null) {
                //坐标转换
                double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ);
                //传入路径
                demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;
            }
        }


        ///楼梯台阶点判定
        double stepnum=demo.StepResultArray.get(demo.StepResultArray.size()-1).StepSqNo[3];
        Boolean IsFSCheck=SensorModelMethod.StepDetectionMatch(stepnum,CurrentFloor,CurrentPressureValue,MyDBOpenHelper.IsFireDoor);
        if (IsFSCheck){
            //匹配MLA  并计算实时坐标与相关MLA的距离关系  匹配至最合适的点 存入MLAPOS  用于后续坐标转换
            int cFloor=CurrentFloor;
            if (SensorModelMethod.IsUpOrDownFloor==1){ cFloor= CurrentFloor-1 ; }
            double[] MLApos = MyDBOpenHelper.querySqlPresFloor(MainActivity.sqLiteDatabase, FinalPOS, cFloor);
            if (MLApos != null) {
                //坐标转换
                double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ, SensorModelMethod.initialY);
                //传入路径
                demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;
//                SensorModelMethod.IsFloorStepCheck=false;
                ///计算完成后
            }

        }

        ///加速度计 门节点判定
        if (IsAcc){
            //匹配MLA  并计算实时坐标与相关MLA的距离关系  匹配至最合适的点 存入MLAPOS  用于后续坐标转换
            double[] MLApos = MyDBOpenHelper.querySqlAcc(MainActivity.sqLiteDatabase, FinalPOS, CurrentFloor);
            if (MLApos != null) {
                //坐标转换
                double[] NewMlaPos = SensorModelMethod.PosTransformToPdr(MLApos, SensorModelMethod.initialX, SensorModelMethod.initialZ, SensorModelMethod.initialY);
                //传入路径
                demo.StepResultArray.get(demo.StepResultArray.size() - 1).pos = NewMlaPos;

                IsAcc = false;///计算完成后  加速度计判定重启

            }
        }

    }



    public void onStart(int inMode, double inPos[]){

//        sava data  20181030    for test   need  delete  later
//        File_IO_Pdr.Dir("ADPdr");

//        if(isCreat==false){
            //double[] iniPosXY={0d,0d};
            int windowLength=21;
            int cacheLength=22;
            double sampleFreq=15.36d; //15.36hz
            int smoothFactor=5;
            double DefaultStepPeriod=0.8d;
            double AccPeakPeriodLB = 0.26* 1000;
            double AccPeakMagnitudeLB = 0.7;
            double SFAccPeakPeriod = 1.8;
            mode=inMode;
            PDRpos=inPos;
            switch (mode) {
                //the statics below are obtained from experiments
                case 1:  //hand
                    smoothFactor = 5;
                    DefaultStepPeriod = 0.8d;
                    AccPeakPeriodLB = 0.26 * 1000;
                    AccPeakMagnitudeLB = 0.7;
                    SFAccPeakPeriod = 1.8;
                    break;
                case 2: //call
                    smoothFactor = 3;
                    DefaultStepPeriod = 0.8d;
                    AccPeakPeriodLB = 0.26 * 1000;
                    AccPeakMagnitudeLB = 0.7;
                    SFAccPeakPeriod = 1.8;
                    break;
                case 3: //pocket
                    smoothFactor = 3;
                    DefaultStepPeriod = 0.8d;
                    AccPeakPeriodLB = 0.26 * 1000;
                    AccPeakMagnitudeLB = 1;
                    SFAccPeakPeriod = 1.8;
                    break;
                case 4: //swing
                    smoothFactor = 3;
                    DefaultStepPeriod = 0.8d;
                    AccPeakPeriodLB = 0.26 * 1000;
                    AccPeakMagnitudeLB = 1.5;
                    SFAccPeakPeriod = 1.8;
                    break;
                default:
                    break;
            }

            demo.iniParameters(mode,sampleFreq,smoothFactor,windowLength,
                    cacheLength,DefaultStepPeriod,AccPeakMagnitudeLB,AccPeakPeriodLB,SFAccPeakPeriod);

            SampleControl();

            isCreat=true;
    }

    public void onProcess(){
        if(isCreat==true&&isProcess==false){
            isProcess=true;
        }else{}
    }

    public void onReset(int inMode, double inPos[]){
        if(isCreat==true){
            onExit();
            demo.PDRreset();
            accNo=1;
        onStart(inMode, inPos);
        onProcess();
    }else{}
}

    public void onRestart(int inMode, double inPos[]){
        demo.PDRreset();
        accNo=1;
        onStart(inMode, inPos);
        onProcess();
    }

    public void onExit(){
        if(isCreat==true){
            isProcess=false;
            isCreat=false;
        }
    }

    public void onDestroy(){
//        LocEventFactory.unregisterEventHandle(this);
    }



    private long GetSystemTimeByMill() {
        Date date=new Date();
        long value =date.getTime(); // obtain the ms
        return value;
    }

    private void SampleControl(){
        SampleCount=3;
    }

    // compute and update pos
    private double[] updatePos(double[] posA, double timeA,double steplength){
        double ha=getHA(timeA);
        ///开关检测
        if (MainActivity.SelectMLA==1){
            //        //加入模组对角度的八向划分 2023zbs
            ha=SensorModelMethod.OrientationTransFormer(ha);
            MLA_Angle=ha;
        }

        ha=Math.toRadians(ha);
        double[] posB=new double[3];
//      posB[0]=posA[0]+steplength*Math.sin(ha);
//		posB[1]=posA[1]+steplength*Math.cos(ha);
        ///原代码X=x+d*sin   Y=y+d*cos  调整为X→   Y↑ 箭头表示XY正方向
        posB[0]=posA[0]+steplength*Math.cos(ha);
        posB[1]=posA[1]+steplength*Math.sin(ha);

        posB[2]=TesterHeight;
        return posB;
    }

    // this is used to get heading angle while one step ends.
    public double getHA(double inTime) {
        // the return value is in unit of radian
        int size=0;
        if(HaCache.size()<1)return 0d;
        if(updateStatus){
            size=HaCache.size()-2;
        }else{
            size=HaCache.size()-1;
        }

        // find the heading angle that is most close to the inTimeEnd or inTimeStart as user requests
        double minimal=Math.abs(HaCache.get(size)[0]-inTime);
        int index=size;
        double dif=0d;

        for(int i=size-1;i>=0;i--){
            dif=Math.abs(HaCache.get(i)[0]-inTime);
            //be careful!
            if(dif>=minimal){
                index=i+1;
                break;
            }
            if(dif<minimal){
                minimal=dif;
                index=i;
            }
        }
        return HaCache.get(index)[1]-iniOrientation;
    }

    // only change the initial positions
    public void resetInitialPos(double posXY[]){
        demo.resetInitialPos(posXY);
    }

    public double[] Output() {
        double[] out = new double[9];
            if(demo.getPDRresult()[0]>0) {
                if(GetSystemTimeByMill() -demo.getPDRresult()[5] < 2000d) {
                    out[0] = demo.getPDRresult()[0];  // Start Time Stamp
                    out[1] = demo.getPDRresult()[5];  // End Time Stamp
                    out[2] = demo.getPDRresult()[4];  // Step length,unit:m
                    out[3] = demo.getPDRresult()[2];  // X pos,unit:m
                    out[4] = demo.getPDRresult()[3];  // Y pos,unit:m
//                    out[5] = HAOutput().get(HAOutput().size() - 2)[0]; // the latest heading,unit:degree
                    out[5] = HAOutput().get(0)[0];
                    out[6] = 1;         // Tag of Static or Walking,1:walking,0:static.
                    out[7] = demo.getPDRresult()[1];

                    out[8]=demo.getPDRresult()[6]; /////Z pos
                }else{
                    out[0] = demo.getPDRresult()[0];  //Time Stamp/start
                    out[1] = GetSystemTimeByMill();  //Time Stamp/end
                    out[2] = 0; //Step length
                    out[3] = demo.getPDRresult()[2]; // X pos  //or longitude
                    out[4] = demo.getPDRresult()[3]; // Y pos  //or latitude
//                    out[5] = HAOutput().get(HAOutput().size() - 2)[0]; // the latest yaw
                    out[5] = HAOutput().get(0)[0];
                    out[6] = 0;
                    out[7] = demo.getPDRresult()[1];

                    out[8]=demo.getPDRresult()[6]; /////Z pos
                }

                return out;
            }else{
                return null;
            }
    }


    private void updateHaCache(double inTime, double HA){
        updateStatus=true;
        HaCache.add(new double[]{inTime,HA});
        if(HaCache.size()==2)iniOrientation=HA;
        if(HaCache.size()>= HacacheLength)HaCache.remove(0);////后续代码作用待验证

        // update the initial Orientation;
        // user has to wait for a while, in a fixed pose to calibrate the initial orientation
        if(HaCache.size()==CaliIniOrientLength){
            double[] angles= new double[CaliIniOrientLength];
            for(int i=0;i<CaliIniOrientLength;i++){
                angles[i]=HaCache.get(i)[1];
            }
            iniOrientation= AvgDegree.num(angles);
        }
        updateStatus=false;
    }

    public Vector<double[]> HAOutput(){
        return HaCache;

    }

    private int getIndex(String input){

        if (input==status1) {
            return 4;
        } else if (input==status2) {
            return 2;
        } else if (input==status3) {
            return 1;
        } else if (input==status4) {
            return 3;
        } else {
            return 1;
        }

    }
    private void UpdateAccValue(){
        double[] list=accValue.get(0);
        double avg=avgAcc(list);
        AccForVariance.add(avg);
        if(AccForVariance.size()> 50)AccForVariance.remove(0);

        if (AccForVariance.size()==50){
            M_Variance=SensorModelMethod.Variance(Vector2double(AccForVariance));//计算实时加速度计方差
            String test="";
        }
    }
    private void UpdateAccValue2(){
        AccForVariance2.clear();

        for (int i=0;i<accValue.size();i++){
            double[] list=accValue.get(i);
            double avg=avgAcc(list);
            AccForVariance2.add(avg);
        }

        if (AccForVariance2.size()==50){
            M_Variance=SensorModelMethod.Variance(Vector2double(AccForVariance2));//计算实时加速度计方差
            String test="";
        }
    }

    private double avgAcc(double[] acc){
        double x=acc[0];
        double y=acc[1];
        double z=acc[2];

        double accavg=Math.sqrt(x*x+y*y+z*z);

        return accavg;
    }
    private double[] Vector2double(Vector<Double> vector){
        double[] arr=new double[vector.size()];
        for (int i=0;i<vector.size();i++){
            arr[i]=vector.get(i);
        }
        return arr;
    }

    private void UpdatePresValue(){
        double[] list=presValue.get(0);
        double pre=list[0];
        PresForAvg.add(pre);
        if(PresForAvg.size()> 50)PresForAvg.remove(0);

        if (PresForAvg.size()==50){
            CurrentPressureValue=SensorModelMethod.Avg(Vector2double(PresForAvg));

        }

    }

}
