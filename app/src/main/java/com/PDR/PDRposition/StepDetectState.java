package com.PDR.PDRposition;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 *  * Modified by zbs 2023
 *   * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class StepDetectState {
    protected boolean FirstPeakFlag;
    protected boolean FindStepEndFlag;
    protected double curStepStartLocation;
    protected double curStepPeakLocation;
    protected double curStepEndLocation;
    protected double curStepPeakValue;
    protected double preStepPeriod;
    protected double preStepPeakLocation;
    protected int StepCount;
    protected int curStepStartSqNo;
    protected int curStepPeakSqNo;
    protected int curStepEndSqNo;
    protected double preStepVar;
    protected double AvgStepPeriod;
    protected double MaxAccValue;
    protected double MaxAccValueLocation;
    protected int MaxAccValueSqNo;

    private PDRstream pdrstream;
    public StepDetectState(PDRstream pdrstream){
        this.pdrstream=pdrstream;
    }

    protected void ini(){
        FirstPeakFlag=false;
        FindStepEndFlag=false;
        curStepStartLocation=0d;
        curStepPeakLocation=0d;
        curStepEndLocation=0d;
        curStepPeakValue=0d;
        preStepPeriod=pdrstream.DefaultStepPeriod*1000d;
        preStepPeakLocation=0d;
        StepCount=0;
        curStepStartSqNo=0;
        curStepPeakSqNo=0;
        curStepEndSqNo=0;
        preStepVar=0d;
        AvgStepPeriod=pdrstream.DefaultStepPeriod*1000d;
        MaxAccValue=0d;
        MaxAccValueLocation=0d;
        MaxAccValueSqNo=0;
    }
}
