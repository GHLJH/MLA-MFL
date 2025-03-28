package com.example.pdrdemo;

import android.graphics.drawable.GradientDrawable;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.PDR.PDRposition.PDRmanager;

import java.util.List;
import java.util.Vector;

/**
 * Created by ZengBS on 2023/09
 * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */
public class SensorModelMethod {
    //测试数据
//    public static double initialX=-45.13;
//    public static double initialZ=0;
//    public static double initialY=18;
    //  public static double initialX=-23.43;
    //  public static double initialZ=0;
    //   public static double initialY=13.5;
     public static double initialX;
      public static double initialZ;
       public static double initialY;

    ///坐标转换
    //PDR坐标转换为实体坐标
    public static void PosTransform(double[] currentPos){
        //PDR默认初始坐标为0，0
        //取固定位置点初始为实际坐标点double[] initialPos
        double[] initialPos=new double[3];
        double[] TransPos=new double[3];//转换后的坐标
        //以五楼走廊端节点起始为例 数据库内的点为P Px=-45.13  Py=18 Pz=0
        //其中 Z=0表示在路网中心线
        //实际坐标点P0与PDR内点坐标A的关系为
        //   Ax=Ax0+Px0
        //   Ay=Ay0+ ( -Pz0 ）
        //   Az=Az0+Py0
        double Px=-45.13;
        double Py=-18;
        double Pz=0;
          //转换为实际模型的坐标值
        initialPos[0]=Px;  //=Ax0
        initialPos[1]=-Pz;  //=Ay0
        initialPos[2]=Py;  //=Az0

        double Ax=currentPos[0];
        double Ay=currentPos[1];
        double Az=currentPos[2];



    }


    ////传入查询到的MLA点位坐标  转换为PDR平面坐标值 用于匹配反馈至PDR实时坐标
    public static double[] PosTransformToPdr(double[] mla,double iniX,double iniZ,double iniY){
        double MLAx=mla[0];
        double MLAy=mla[1];
        double MLAz=mla[2];

        //已知MLA坐标
        //// X轴→  Y轴垂直于地面  Z轴↓
        double PDRx=MLAx-iniX;
        double PDRy=-(MLAy-iniZ);

        double PDRz=MLAz-iniY;

        double[]PDR=new double[3];
        PDR[0]=PDRx;
        PDR[1]=PDRy;
        PDR[2]=PDRz;

        return PDR;
    }
    ///二维情况
    public static double[] PosTransformToPdr(double[] mla,double iniX,double iniZ) {
        double MLAx = mla[0];
        double MLAy = mla[1];
        double MLAz = mla[2];

        //已知MLA坐标
        //// X轴→  Y轴垂直于地面  Z轴↓
        double PDRx = MLAx - iniX;
        double PDRy = -(MLAy - iniZ);


        double[] PDR = new double[3];
        PDR[0] = PDRx;
        PDR[1] = PDRy;
        PDR[2] = PDRmanager.TesterHeight;  ///二维情况下不修正PDR Z坐标

        return PDR;
    }


    /////PDR轨迹坐标转换为MLA坐标系坐标，用于距离判断
    public  static double[]PdrPosTransformToMla(double[]CurrentPdrPos ,double iniX,double iniZ){

        double CurrentPDRx=CurrentPdrPos[0];
        double CurrentPDRy=CurrentPdrPos[1];

        double Pdr2MlaX=iniX+CurrentPDRx;
        double Pdr2MlaY=iniZ+(-CurrentPDRy);

        double[]CurrentPdrPosToMla=new double[2];
        CurrentPdrPosToMla[0]=Pdr2MlaX;
        CurrentPdrPosToMla[1]=Pdr2MlaY;


        return CurrentPdrPosToMla;
    }
    ////三维坐标
    public  static double[]PdrPosTransformToMla(double[]CurrentPdrPos ,double iniX,double iniZ,double iniY){

        double CurrentPDRx=CurrentPdrPos[0];
        double CurrentPDRy=CurrentPdrPos[1];
        double CurrentPDRz=CurrentPdrPos[2];

        double Pdr2MlaX=iniX+CurrentPDRx;
        double Pdr2MlaY=iniZ+(-CurrentPDRy);

        double Pdr2MlaZ=iniY+CurrentPDRz;


        double[]CurrentPdrPosToMla=new double[3];
        CurrentPdrPosToMla[0]=Pdr2MlaX;
        CurrentPdrPosToMla[1]=Pdr2MlaY;
        CurrentPdrPosToMla[2]=Pdr2MlaZ;


        return CurrentPdrPosToMla;
    }


    private static double[] FormerGyrValue;
    private static boolean IsTurning=false;
    private static int TurnNum=0; //判断 陀螺仪数值中超过阈值|1|的次数
    /***
     * 陀螺仪模组
     * @param GyrValue 陀螺仪实时数据
     * @param GyrThreshold_R 右转阈值
     * @param GyrThreshold_L 左转阈值
     *                       转弯返回真
     */
    public static Boolean GyrValuesMonitor(double[] GyrValue,double GyrThreshold_R,double GyrThreshold_L){

        if (FormerGyrValue==null){ //首次运行时 保存陀螺仪读数据
            FormerGyrValue=new double[3];
            FormerGyrValue[0]=GyrValue[0];
            FormerGyrValue[1]=GyrValue[1];
            FormerGyrValue[2]=GyrValue[2];
            return false;
        }
        //三轴数据
        double x=GyrValue[0];
        double y=GyrValue[1];
        double z=GyrValue[2];

        double Former_x=FormerGyrValue[0];
        double Former_y=FormerGyrValue[1];
        double Former_z=FormerGyrValue[2];
//
//        double Dif_x=x-Former_x;
//        double Dif_y=y-Former_y;
//        double Dif_z=z-Former_z;

        //1.首先  出现陀螺仪z数值超过阈值|1|时  判定开始转弯

        //2.转弯后续情况下  当陀螺仪数值中  首次低于1时  判定为转弯完毕   重启转弯判定，避免出现一个转弯判定多次


        if (z>=GyrThreshold_L)//数值大于左转阈值
        {
            //超过阈值判定为以此转弯运动  匹配MLA转角节点  后续结合罗盘判定转弯后方向
            IsTurning=true;
            TurnNum++;

            if (IsTurning && TurnNum>1){ //当出现转弯且未结束转弯时 结束后续判断
                return false;
            }

            if (TurnNum==1){
                //=1表示进行了转弯
                //此时查询同楼层陀螺仪MLA节点  判断是否进行匹配
                return true;
//                MatchGyroMLA();
            }

        }
        else if (z<=GyrThreshold_R)//数值小于右转阈值
        {
            IsTurning=true;
            TurnNum++;

            if (IsTurning && TurnNum>1){ //当出现转弯且未结束转弯时 结束后续判断
                return false;
            }
            if (TurnNum==1){
                //=1表示进行了转弯
                //此时查询同楼层陀螺仪MLA节点  判断是否进行匹配
                return true;
//                MatchGyroMLA();
            }

        }
        else {//当数值在左右转阈值范围内
            IsTurning=false;
            TurnNum=0;
            return false;
        }

        return false;

    }
    public static Boolean GyrValuesMonitor(Vector<double[]>GyrValue, double GyrThreshold_R, double GyrThreshold_L){

        for (int i=0;i<GyrValue.size();i++   ) {
            double[] gyr=GyrValue.get(i);
            //三轴数据
            double x = gyr[0];
            double y = gyr[1];
            double z = gyr[2];
            if (z>=GyrThreshold_L)//数值大于左转阈值
            {
                //超过阈值判定为以此转弯运动  匹配MLA转角节点  后续结合罗盘判定转弯后方向
                IsTurning=true;
                TurnNum++;

                if (IsTurning && TurnNum>1){ //当出现转弯且未结束转弯时 结束后续判断
                    return false;
                }

                if (TurnNum==1){
                    //=1表示进行了转弯
                    //此时查询同楼层陀螺仪MLA节点  判断是否进行匹配
                    return true;
//                MatchGyroMLA();
                }

            }
            else if (z<=GyrThreshold_R)//数值小于右转阈值
            {
                IsTurning=true;
                TurnNum++;

                if (IsTurning && TurnNum>1){ //当出现转弯且未结束转弯时 结束后续判断
                    return false;
                }
                if (TurnNum==1){
                    //=1表示进行了转弯
                    //此时查询同楼层陀螺仪MLA节点  判断是否进行匹配
                    return true;
//                MatchGyroMLA();
                }

            }
            else {//当数值在左右转阈值范围内
                IsTurning=false;
                TurnNum=0;
                continue;
            }

        }
        return false;

    }

    // 计算指南针的方向
    public static float calculateOrientation(float[] mAcceValues,float[] mMagnValues) {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, mAcceValues, mMagnValues);
        SensorManager.getOrientation(R, values);
        Log.d("Main","values[0] :"+Math.toDegrees(values[0]));

        values[0] = (float) Math.toDegrees(values[0]);//弧度转换角度

        String value11=(values[0]+360)%360+"";
        float ori=(values[0]+360)%360;

        float Modified_Ori=0;
        String tv_direction;

        if (  (ori>=337.5 && ori<=360)  || (ori>=0 && ori<22.5) )
        {
            Modified_Ori=0;
            tv_direction="北";
        }
        else if (  ori>=22.5 && ori<67.5   )
        {
            Modified_Ori=45;
            tv_direction="东北";

        }
        else if (  ori>=67.5 && ori<112.5   )
        {
            Modified_Ori=90;
            tv_direction="东";
        }
        else if (  ori>=112.5 && ori<157.5   )
        {
            Modified_Ori=135;
            tv_direction="东南";
        }
        else if (  ori>=157.5 && ori<202.5   )
        {
            Modified_Ori=180;
            tv_direction="南";
        }
        else if (  ori>=202.5 && ori<247.5   )
        {
            Modified_Ori=225;
            tv_direction="西南";
        }
        else if (  ori>=247.5 && ori<292.5   )
        {
            Modified_Ori=270;
            tv_direction="西";
        }
        else if (  ori>=292.5 && ori<337.5   )
        {
            Modified_Ori=315;
            tv_direction="西北";
        }


		return ori;
    }
    ////  方向八分化
    public static double OrientationTransFormer(double ori){
        double Modified_Ori=0;
        String tv_direction;
        if (  (ori>=337.5 && ori<=360)  || (ori>=0 && ori<22.5) )
        {
            Modified_Ori=0;
            tv_direction="北";
        }
        else if (  ori>=22.5 && ori<67.5   )
        {
            Modified_Ori=45;
            tv_direction="东北";
        }
        else if (  ori>=67.5 && ori<112.5   )
        {
            Modified_Ori=90;
            tv_direction="东";
        }
        else if (  ori>=112.5 && ori<157.5   )
        {
            Modified_Ori=135;
            tv_direction="东南";
        }
        else if (  ori>=157.5 && ori<202.5   )
        {
            Modified_Ori=180;
            tv_direction="南";
        }
        else if (  ori>=202.5 && ori<247.5   )
        {
            Modified_Ori=225;
            tv_direction="西南";
        }
        else if (  ori>=247.5 && ori<292.5   )
        {
            Modified_Ori=270;
            tv_direction="西";
        }
        else if (  ori>=292.5 && ori<337.5   )
        {
            Modified_Ori=315;
            tv_direction="西北";
        }
        return  Modified_Ori;

    }
    // BLEMonitor code is now redevelopded by other labman for his experiments,and will be open-sourced later.
    private double[] BLEMonitor(){
        double[] xyz=new double[3];
        return xyz;
    }


    private int CountDoorMla=0;
    /***
     * 加速度计模组
     * @param AccValue    加速度数据
     * @param AccThreshold    加速度方差阈值
     */
    private void AccValueMonitor(double[] AccValue,double AccThreshold,double[] PosXY,double AccVariance){
        double Acc_x=AccValue[0];
        double Acc_y=AccValue[1];
        double Acc_z=AccValue[2];
        double AccSqrtThreeAxis= Math.sqrt(Acc_x*Acc_x+Acc_y*Acc_y+Acc_z*Acc_z) ;//合加速度

        //如果匹配至门节点相关位置  出现“走-停-走”变化情况，则判断进入房间  绑定至门节点坐标

        //1.第一层判定 如果陀螺仪节点匹配到了门节点  则开启判定
        if (IsDoor){
            //2.第二层判定
            //获取当前加速度计方差，若是大于行走阈值  则开启下一阶段判定
            if (AccVariance>AccThreshold){

                //3.第三层判定
                //线程启动调取

            }

        }

    }



    private boolean IsDoor=false;
    ////监测是否匹配至门节点
    private boolean IsMatchToDoor(double[] PosXY,double floor){

        double x=PosXY[0];   double y=PosXY[1];
        //查询数据库内坐标信息
        //查询sqlite
        //select * from door  where  floor=4  and x=   y=;

        int count=1;
        //如果当前匹配至门相关节点  则返回T
        if (count>0){
            IsDoor=true;
            return true;
        }
        IsDoor=false;
        return  false;
    }





    ///转换DOUBLE数组至Float数组
 public static float[] ChangeDouble2FloatArray(double[] Model){
        float[] floats=new float[Model.length];

        for (int i=0;i<Model.length;i++){
            floats[i]=(float) (Model[i]);
        }
        return floats;
 }

    //方差s^2=[(x1-x)^2 +...(xn-x)^2]/n 或者s^2=[(x1-x)^2 +...(xn-x)^2]/(n-1)
    public static double Variance(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        return dVar/m;
    }
    public static double Avg(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值

        return dAve;
    }

    ///传入数组与均值
    public static double Variance(double[] x,double dAve) {
        int m=x.length;
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            double a=x[i];
            if (a==0){
                continue;
            }
            double aa=(a-dAve)*(a-dAve);
            if (aa>1)
            {
                a=x[i];
            }
            dVar=dVar+aa;
        }
        double v=dVar/m;
        return v;
    }
    //标准差σ=sqrt(s^2)
    public static double StandardDiviation(double[] x) {
        int m=x.length;
        double sum=0;
        for(int i=0;i<m;i++){//求和
            sum+=x[i];
        }
        double dAve=sum/m;//求平均值
        double dVar=0;
        for(int i=0;i<m;i++){//求方差
            dVar+=(x[i]-dAve)*(x[i]-dAve);
        }
        //reture Math.sqrt(dVar/(m-1));
        return Math.sqrt(dVar/m);
    }


    /////气压计监测
    public static Boolean PressureMonitor(double CurrentPressure,double InitialPressure,int FloorNum,double threshold_Pressure,double M_Variance){

        if (InitialPressure!=0){
            double PreDiff=Math.abs(InitialPressure-CurrentPressure);
            if (PreDiff>=threshold_Pressure){
                //出现气压差值大于阈值，判定为楼层已经变动
//                Toast.makeText(DeviceScanActivityV2_locate,"判定楼层进行变动",Toast.LENGTH_LONG).show();
                if (InitialPressure>CurrentPressure)//气压减小说明是上楼
                {
                    FloorNum++;
//                    textViewFloor.setText(String.format("所在楼层：%s",FloorNum+""));
//                    ToastUtil.show(MainActivity.getContext(),String.format("楼层向上变动，所在楼层：%s",FloorNum));
                    PDRmanager.InitialPressureValue=CurrentPressure;//变动初始气压值，用于后续楼层判定
                    PDRmanager.CurrentFloor=FloorNum;

//                    textViewIniPressure.setText(String.format("楼层变动后气压：%f",InitialPressure));
                    //当加速度的值变动超过阈值时,通过气压导致楼层变动时加速度计速度值变化的方差来判断
//                    WhichMethodToChangeFloor(M_Variance);
                    return true;
                }
                else{//气压上升说明是下楼

                    FloorNum--;
//                    textViewFloor.setText(String.format("所在楼层：%s",FloorNum+""));
//                    ToastUtil.show(MainActivity.getContext(),String.format("楼层向下变动，所在楼层：%s",FloorNum));
                    PDRmanager.InitialPressureValue=CurrentPressure;//变动初始气压值，用于后续楼层判定
                    PDRmanager.CurrentFloor=FloorNum;
                    // textViewIniPressure.setText(String.format("楼层变动后气压：%f",InitialPressure));
                    //当加速度的值变动超过阈值时,通过气压导致楼层变动时加速度计速度值变化的方差来判断
//                    WhichMethodToChangeFloor(M_Variance);
                    return true;
                }

            }

            return false;
        }
        return false;

    }
    /////气压计监测  楼梯平台情况
    public static int PressureMonitor_HalfFloor(double CurrentPressure,double InitialPressure,int FloorNum,double threshold_Pressure,double M_Variance){

        if (InitialPressure!=0){
            double PreDiff=Math.abs(InitialPressure-CurrentPressure);
            ///气压大等于二分之一阈值  小于阈值
            if (PreDiff>=threshold_Pressure/2  && PreDiff<threshold_Pressure){
                //出现气压差值大等于二分之一阈值，判定为楼层移动至楼梯平台的情况
                if (InitialPressure>CurrentPressure)//气压减小说明是上楼
                {
//                    WhichMethodToChangeFloor(M_Variance);
                    return 1;
                }
                else{//气压上升说明是下楼
                    //当加速度的值变动超过阈值时,通过气压导致楼层变动时加速度计速度值变化的方差来判断
                    //当加速度的值变动超过阈值时,通过气压导致楼层变动时加速度计速度值变化的方差来判断
//                    WhichMethodToChangeFloor(M_Variance);
                    return 2;
                }

            }

            return 0;
        }
        return 0;

    }
    /* 判断是通过哪种方式进行的楼层变动，通过对加速度计的方差值变化情况来进行判断
     */
    public static int WhichMethodToChangeFloor(double M_Variance){
        //当加速度的值变动超过阈值时,通过气压导致楼层变动时加速度计速度值变化的方差来判断
        int isWhichMethod=isOverweightOrWeightless2(M_Variance);
        switch (isWhichMethod){
            case 0:
                break;
            case 1:
//                Toast.makeText(MainActivity.getContext(),"正在使用电梯进行楼层变动",Toast.LENGTH_SHORT).show();
//                ToastUtil.show(MainActivity.getContext(),"正在使用电梯进行楼层变动");
                break;
            case 2:
//                ToastUtil.show(MainActivity.getContext(),"正在使用楼梯进行楼层变动");
//                Toast.makeText(MainActivity.getContext(),"正在使用楼梯进行楼层变动",Toast.LENGTH_SHORT).show();
                break;
        }

        return isWhichMethod;
    }

    /***依据当前的加速度方差判断如何进行楼层移动
     *
     * @return 超过阈值返回T   反之F
     */
    public static double VarianceThreshold_Floor=0.2;//楼梯加速度计方差阈值
    public static double VarianceThreshold_Lift=0.0019;//电梯加速度计方差阈值 依据实测
    public static double WhichWayToChangeFloor=0;///楼层变动方式  0不变  1电梯  2楼梯
    public static  int isOverweightOrWeightless2(double M_Variance) {
        // 判断变化幅度是否超过阈值
        if (M_Variance > VarianceThreshold_Lift && M_Variance<VarianceThreshold_Floor) {
            // 更新上一时刻的加速度计读数,将当前的加速度读数赋予
            return 1;  ///表示电梯
        }
        else if (M_Variance>VarianceThreshold_Floor) {
            return 2;   //表示楼梯
        } else {
            return 0;
        }
    }

    private static double FirstPressure=0;
    private static double StepCounter=0;
    public static boolean IsFloorStepCheck=false;
    public static int IsUpOrDownFloor=0;  ///0上楼，1下楼
    ///气压计与步伐检测的楼梯匹配
    public static Boolean StepDetectionMatch(double stepnum,double CurrentFloor,double CurrentPres,boolean IsFloorDoor){
        //1.首先要满足匹配门节点  并且是楼梯前防火门
        if (!IsFloorDoor){
            return false;
        }
        //2.匹配后开始监测当前气压是否超过水平阈值
        if (FirstPressure==0 || StepCounter==0 ){
            FirstPressure=CurrentPres;
            StepCounter=stepnum;
            return false;///首次进行函数时，赋值
        }
        else
        {
            //气压值降低
            if (  ( CurrentPres-FirstPressure > PDRmanager.Pres_Floor_Threshold ) && (stepnum-StepCounter>=1) )
            {
                //是否气压计值变化超过楼梯气压阈值  并且  步数增加
                //超过后则依据步伐检测  一步匹配一个台阶节点
                //优先级较楼层定位其他MLA低，当满足其他的节点条件则优先匹配其他节点
                ///此时匹配楼梯节点
                IsFloorStepCheck=true;
                IsUpOrDownFloor=1;////现气压大于初始气压  说明下楼
                return IsFloorStepCheck;
            }
            else if ( ( FirstPressure-CurrentPres>  PDRmanager.Pres_Floor_Threshold ) && (stepnum-StepCounter>=1) ){

                ///此时匹配楼梯节点
                IsUpOrDownFloor=0;////现气压小于初始气压  说明上楼   目前值设置只针对当前实验要求的楼梯行走情况
                IsFloorStepCheck=true;
                return IsFloorStepCheck;
            }

        }
        return false;


    }








}///////最下
