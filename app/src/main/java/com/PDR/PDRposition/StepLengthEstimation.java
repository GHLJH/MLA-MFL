package com.PDR.PDRposition;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 *  * Modified by zbs 2023
 *  移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */
//步长估计
class StepLengthEstimation {
    // single instance mode
    // private static StepLengthEstimation single=null;
    // key parameter
    private PDRstream pdrStream=null;
    private double EP=0d;

    static List<double[]> Info= new ArrayList();

    // constructor
    public StepLengthEstimation(PDRstream pdrStream){
        this.pdrStream=pdrStream;
    }


    public double  StepEpTraining(double [] RefSL,double [] StepPeak,double [] StepValley){

        double Kval = 0d;
        double [] res = new double [RefSL.length];
        double [] X = new double [RefSL.length];
        double [] F = new double [RefSL.length];
        double SingleStepPeak = 0d;
        double SingleStepValley = 0d;
        double sum = 0d;

        for (int i=0;i < RefSL.length;i++){
            double temp = 0d;
            SingleStepPeak = StepPeak [i];
            SingleStepValley = StepValley [i];

            X [i] = Math.pow((SingleStepPeak-SingleStepValley),0.25);
            F [i] = RefSL [i];

            temp = F [i] / X [i];
            sum = sum + temp;
        }

        Kval = sum / RefSL.length;

        for (int i=0;i < RefSL.length;i++){
            res [i] = RefSL [i] - (Kval * X [i]);
        }

        return Kval;
    }

    // main process starts
    // to Train: true --> train; false ---> no training
    protected double StepLength(PDRresult StepResult){
        //get K value
        if(EP<0.00001d) EP=getKvalue();

        //calculate the step length
        //initialization
        //double DefaultSL = 0.75d;
        double StepPeak = 0d;
        double StepValley = 0d;
        double SL=0d;

        int StepStartSqNo = StepResult.StepSqNo[0];
        int StepPeakSqNo = StepResult.StepSqNo[1];
        int StepEndSqNo = StepResult.StepSqNo[2];

        int b=StepEndSqNo-StepStartSqNo+3;

        //potential bug fixed here
        if (StepStartSqNo == -1 | StepPeakSqNo == -1 | StepEndSqNo == -1| b>=pdrStream.SlideWindow.size()) {
            SL = pdrStream.DefaultSL;
        } else {
            double []StepAcc=new double[StepEndSqNo-StepStartSqNo+1];
            double []DifStepTick=new double[StepEndSqNo-StepStartSqNo+3];
            for(int j=StepStartSqNo-1;j<StepEndSqNo;j++){
                //StepAcc[j-StepStartSqNo+1]=AccArray[1][j];
                StepAcc[j-StepStartSqNo+1]=
                        pdrStream.SlideWindow.get(pdrStream.SlideWindow.size()-StepEndSqNo+j-1)[1];
            }
            for(int j=StepStartSqNo-2;j<=StepEndSqNo;j++){
                //DifStepTick[j-StepStartSqNo+2]=AccArray[0][j+1]-AccArray[0][j];
                DifStepTick[j-StepStartSqNo+2]=
                        pdrStream.SlideWindow.get(pdrStream.SlideWindow.size()-StepEndSqNo+j-1)[0]-
                                pdrStream.SlideWindow.get(pdrStream.SlideWindow.size()-StepEndSqNo+j-2)[0];
            }



            if (max(DifStepTick) >= 100) {
                SL = pdrStream.DefaultSL;
            } else {
                StepPeak= StepResult.StepInfo[3];
                StepValley= min(StepAcc);
                SL= EP *Math.pow((StepPeak - StepValley), 0.25);
                if (Math.abs(SL - pdrStream.DefaultSL) > 0.2d) {
                    SL = pdrStream.DefaultSL;
                }
                pdrStream.DefaultSL= SL;
            }

            double [] temp = new double[3];
            temp[0] = StepPeak;
            temp[1] = StepValley;
            temp[2] = SL;

            if(Info.size() < 5) {

                Info.add(temp);
            }else {
                Info.remove(0);
                Info.remove(0);
                Info.remove(0);

                Info.add(temp);
            }
        }
        return SL;
    }

    // get the K value from the database or from the default K;
    private double getKvalue(){

        double [] RefSL = new double[5];
        double [] StepPeak = new double[5];
        double [] StepValley = new double[5];
        double Kval=0d;
        //default value
        if(Info.size() < 5) {
            Kval = 0.55606d;
        }else {  //get K value from training

            for(int i = 0;i < 5;i++){
                RefSL[i] = Info.get(i)[2];
                StepPeak[i] = Info.get(i)[0];
                StepValley[i] = Info.get(i)[1];

            }

            Kval = StepEpTraining(RefSL,StepPeak,StepValley);
        }
        return Kval;
    }


    private double min(double[] array){
        //get minimal value from a vector获取向量的最小值
        double minValue = array[0];
        for (int i = 0; i<array.length;i++){
            if (array[i]<minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    private double max(double[] array){
        //get maximal value from a vector
        double maxValue = array[0];
        for (int i = 0; i<array.length;i++){
            if (array[i]>maxValue)
                maxValue = array[i];
        }
        return maxValue;
    }

}
