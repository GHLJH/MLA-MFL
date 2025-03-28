package com.PDR;

import android.content.Context;

import com.PDR.Control.pdrDestroyEvent;
import com.PDR.Control.pdrInitEvent;
import com.PDR.Control.pdrStartEvent;
import com.PDR.Control.pdrStopEvent;
import com.PDR.PDRposition.PDR;
import com.PDR.Response.pdrDestroyResponseEvent;
import com.PDR.Response.pdrInitResponseEvent;
import com.PDR.Response.pdrStartResponseEvent;
import com.PDR.Response.pdrStopResponseEvent;
import com.Sensors.SensorDataRecordEvent;
import com.example.pdrdemo.SensorModelMethod;

import org.gispower.eventbus.Subscribe;
import org.gispower.eventbus.ThreadMode;

import java.util.List;
import java.util.Vector;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelDestroyEvent;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelInitEvent;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelStartEvent;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelStopEvent;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Response.EnumModelState;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.ModelResultEvent;
import cn.edu.whu.lmars.loccore.event.LocEventFactory;
import cn.edu.whu.lmars.loccore.model.ILocContext;
import cn.edu.whu.lmars.loccore.model.ILocModel;


/**
 * Created by Pumpkin on 2018/12/9 0009.
 * Modified by ZBS on 2023/09
 * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class pdrModel implements ILocModel {
    private Context context;
    private boolean isInit=false;
    private boolean isStart=false;
    private boolean isStop=false;
    private boolean isDestroy=false;
    private static PDR pdr;
    private static Vector<double[]> AccDataList = new Vector<>(0,1);
    private static Vector<double[]> Ha = new Vector<>(0,1);
    private static Vector<double[]> KF = new Vector<>(0,1);

    private static Vector<double[]> MagDataList = new Vector<>(0,1);
    private static Vector<double[]> MagDataTYPE2List = new Vector<>(0,1);
    private static Vector<double[]> GyrDataList = new Vector<>(0,1);
    private static Vector<double[]> PreDataList = new Vector<>(0,1);


    private static Vector<String> Mode = new Vector<>(0,1);

    public float[] mAcceValues; // 加速度变更值的数组
    public float[] mMagnValues; // 磁场强度变更值的数组
    private float[] mAccVal=new float[3]; // 加速度变更值的数组
    private float[] mMagVal=new float[3]; // 磁场强度变更值的数组
    public static float m_Angle;


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void SensorEvent(SensorDataRecordEvent dataEvents) {

        switch (dataEvents.getDataType()) {
            case AccRecord:
                double[] accValue=new double[4];
                try {
                    accValue = dataEvents.getData();

                    mAcceValues= SensorModelMethod.ChangeDouble2FloatArray(accValue.clone());///获取加速度数据

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (AccDataList.size() >= 50) {
                    AccDataList.remove(0);
                    AccDataList.add(accValue.clone());
                } else {
                    AccDataList.add(accValue.clone());
                }
                break;
            case MagRecord://磁力计  TYPE_MAGNETIC_FIELD_UNCALIBRATED 14
                double[] magValue=new double[7];
                try {
                    magValue = dataEvents.getData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (MagDataList.size() >= 50) {
                    MagDataList.remove(0);
                    MagDataList.add(magValue.clone());
                } else {
                    MagDataList.add(magValue.clone());
                }
                break;

            case MagRecord_TYPE2:///磁力计 SENSOR_TYPE_MAGNETIC_FIELD 2

                double[] magValue_type2=new double[4];
                try {
                    magValue_type2 = dataEvents.getData();
                    mMagnValues= SensorModelMethod.ChangeDouble2FloatArray(magValue_type2.clone());///获取磁力计数据
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (MagDataTYPE2List.size() >= 50) {
                    MagDataTYPE2List.remove(0);
                    MagDataTYPE2List.add(magValue_type2.clone());
                } else {
                    MagDataTYPE2List.add(magValue_type2.clone());
                }
                break;

            case PressureRecord://气压计
                double[] PreValue=new double[2];
                try {
                    PreValue = dataEvents.getData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (PreDataList.size() >= 50) {
                    PreDataList.remove(0);
                    PreDataList.add(PreValue.clone());
                } else {
                    PreDataList.add(PreValue.clone());
                }
                break;
            case GyroRecord://陀螺仪
                double[] GyrValue=new double[4];
                try {
                    GyrValue = dataEvents.getData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (GyrDataList.size() >= 50) {
                    GyrDataList.remove(0);
                    GyrDataList.add(GyrValue.clone());
                } else {
                    GyrDataList.add(GyrValue.clone());
                }
                break;
        }


        if (mAcceValues != null && mMagnValues != null){
            //只取加速度计和磁力计数值不取时间
            mAccVal[0]=mAcceValues[0]; mAccVal[1]=mAcceValues[1]; mAccVal[2]=mAcceValues[2];
            mMagVal[0]=mMagnValues[0]; mMagVal[1]=mMagnValues[1]; mMagVal[2]=mMagnValues[2];

            m_Angle=SensorModelMethod.calculateOrientation(mAccVal,mMagVal); // 加速度和磁场强度两个都有了，才能计算磁极的方向
            String test="";
            if (MagDataList.size()>0){
                double[] tests=MagDataList.get(0);
                String sss="";
            }

        }

    }

    public static List<double[]> getAccDataList() {
        return AccDataList;
    }
    public static List<double[]> getMagDataList() {
        return MagDataList;
    }
    public static List<double[]> getMagDataTYPE2List() {
        return MagDataTYPE2List;
    }
    public static List<double[]> getPreDataList() {
        return PreDataList;
    }
    public static List<double[]> getGyrDataList() {
        return GyrDataList;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void ModeEvent(ModelResultEvent ModeEvents) {
        switch(ModeEvents.getRsltType()) {
            case CameraLoc:

                String ModeInfo = (String) ModeEvents.getResult();
                if (Mode.size() >=10) {
                    Mode.remove(0);
                    Mode.add(ModeInfo);
                } else {
                    Mode.add(ModeInfo);
                }
                break;
        }
    }

    public static Vector<String> getMode() {
        return Mode;
    }

    ///获取EKF
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void HAEvent(ModelResultEvent HAEvents) {
        switch(HAEvents.getRsltType()) {
            case BLELoc:

                double[] HaInfo = (double[]) HAEvents.getResult();
                if (Ha.size() >= 4) {
                    Ha.remove(0);
                    Ha.add(HaInfo.clone());
                } else {
                    Ha.add(HaInfo.clone());
                }
                break;
        }
    }
    ///获取KF的数值
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void KFEvents(ModelResultEvent KFEvents) {
        switch(KFEvents.getRsltType()) {
            case PhonePose:

                double[] KFInfo = (double[]) KFEvents.getResult();
                if (KF.size() >= 4) {
                    KF.remove(0);
                    KF.add(KFInfo.clone());
                } else {
                    KF.add(KFInfo.clone());
                }
                break;
        }
    }

    public static Vector<double[]> getHa() {
        return Ha;///EKF
    }

    public static Vector<double[]> getKF() {
        return KF;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrInitEvent(pdrInitEvent e) {
        //响应初始化事件，比如加载资源、初始化基站坐标

        pdr=new PDR();

        try{
            pdr.configure(100,context);
            isInit=true;
        } catch(Exception e1) {
            e1.printStackTrace();
            isInit=false;
        }

        if(isInit){
            pdrInitResponseEvent modelInitRsponse = new pdrInitResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelInitRsponse.setMsg("Init OK");       //需要传输的信息
            LocEventFactory.publishEvent(modelInitRsponse);
        }else {
            pdrInitResponseEvent modelInitRsponse = new pdrInitResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelInitRsponse.setMsg("Fail");
            LocEventFactory.publishEvent(modelInitRsponse);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStartEvent(pdrStartEvent e) {
        //响应开启定位事件

        try{
            pdr.onStart();
            isStart=true;
        } catch(Exception e1) {
            e1.printStackTrace();
            isStart=false;
        }

        if(isStart){
            pdrStartResponseEvent modelStartRsponse = new pdrStartResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelStartRsponse.setMsg(" Start OK");       //需要传输的信息
            LocEventFactory.publishEvent(modelStartRsponse);

        }else {
            pdrStartResponseEvent modelStartRsponse = new pdrStartResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelStartRsponse.setMsg("Fail");
            LocEventFactory.publishEvent(modelStartRsponse);
        }

    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrStopEvent(pdrStopEvent e) {
        //响应关闭定位事件
        try{
            pdr.onStop();
            isStop=true;
        } catch(Exception e1) {
            e1.printStackTrace();
            isStop=false;
        }

        if(isStop){
            pdrStopResponseEvent modelStopResponse = new pdrStopResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelStopResponse.setMsg(" Stop OK");       //需要传输的信息
            LocEventFactory.publishEvent(modelStopResponse);
        }else {
            pdrStopResponseEvent modelStopResponse = new pdrStopResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelStopResponse.setMsg("Fail");
            LocEventFactory.publishEvent(modelStopResponse);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void pdrDestroyEvent(pdrDestroyEvent e) {
        //响应销毁资源事件
        try{
            pdr.onDestroy();
            LocEventFactory.unregisterEventHandle(this);
            isDestroy=true;
        } catch(Exception e1) {
            e1.printStackTrace();
            isDestroy=false;
        }

        if(isDestroy){
            pdrDestroyResponseEvent modelDestroyResponse = new pdrDestroyResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelDestroyResponse.setMsg("Destroy OK");       //需要传输的信息
            LocEventFactory.publishEvent(modelDestroyResponse);
        }else {
            pdrDestroyResponseEvent modelDestroyResponse = new pdrDestroyResponseEvent(ModelName.PDR, EnumModelState.OK);
            modelDestroyResponse.setMsg("Fail");
            LocEventFactory.publishEvent(modelDestroyResponse);
        }
    }


    @Override
    public String getModelName() {
        return ModelName.PDR;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean modelLoad(ILocContext locContext) {
        LocEventFactory.registerEventHandle(this);  //必须注册
        // locContext供模块中需要Context调用android系统资源的地方使用，比如传感器注册
        this.context = locContext.getAppContext();
        return true;
    }

    @Override
    public ModelInitEvent getModelInitEvent() {
        return new pdrInitEvent(ModelName.PDR);
    }

    @Override
    public ModelStartEvent getModelStartEvent() {
        return new pdrStartEvent(ModelName.PDR);
    }

    @Override
    public ModelStopEvent getModelStopEvent() {
        return new pdrStopEvent(ModelName.PDR);
    }

    @Override
    public ModelDestroyEvent getModelDestroyEvent() {
        return new pdrDestroyEvent(ModelName.PDR);
    }
}


