package com.PDR.PDRposition;

import android.content.Context;

import com.PDR.Result.pdrResultEvent;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.EnumResultType;
import cn.edu.whu.lmars.loccore.event.AbstractLocEvent;
import cn.edu.whu.lmars.loccore.event.LocEventFactory;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 *  Modified by zbs 2023
 *  移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class PDR {
    protected long TimeInterval;
    protected boolean work=false;
    protected boolean isOBS=false;
    private PDRmanager pdRmanager;
    private double[] LocRes;


    protected long LastTimeStamp=-100;

    protected pdrResultEvent resultEvent=new pdrResultEvent();

    public void configure(long TimeInterval, Context context){
        pdRmanager = new PDRmanager();
        this.TimeInterval=TimeInterval;
    }

    public void onStart(){
        if(!work) {

            Thread t1 = new Thread(pdRmanager);
            t1.start();

            work=true;
        }

        if(!isOBS){
            isOBS=true;
            observer();
        }
    }

    public void onStop(){
        if(work) {
            //stop PDR
            pdRmanager.onExit();

            work=false;
        }
    }

    public void onReset(){
        pdRmanager.onRestart(1,new double[]{0d,0d});
        work=true;
    }

    public void onDestroy(){
        pdRmanager.onExit();
        isOBS=false;
        pdRmanager.onDestroy();
    }

    public void setLocRes(double[] locRes){
        LocRes=locRes;
    }

    public double[] getLocRes(){
       double [] loc =new double[8];
       try {
           loc=pdRmanager.Output();
       } catch(Exception e){
           e.printStackTrace();
       }

        return loc;
    }

    public long getLocTime(){
        double[] temp=getLocRes();
        if(temp==null) return LastTimeStamp;
        return (long) getLocRes()[0];
    }


    protected void publishLocStatus(AbstractLocEvent locEvents){
        LocEventFactory.publishEvent(locEvents);
    }

    public void observer(){
        new Thread(){
            public void run(){
                while (isOBS){
                    try {
                        Thread.sleep(TimeInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //对比时间戳
//                    long curTime = getLocTime();
//                    if( curTime == LastTimeStamp){
//
//                    }else{
//                        resultEvent.setRslt(getLocRes());
//                        resultEvent.setRsltType(EnumResultType.PDRHandLoc);
//                        LocEventFactory.publishEvent(resultEvent);
//                        LastTimeStamp=curTime;
//                    }
                    if(getLocRes()!=null){
                        resultEvent.setRslt(getLocRes());
                        resultEvent.setRsltType(EnumResultType.PDRHandLoc);
                        LocEventFactory.publishEvent(resultEvent);
}
                }

                        }
                        }.start();
                        }
}
