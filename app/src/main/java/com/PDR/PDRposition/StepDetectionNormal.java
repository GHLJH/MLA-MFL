package com.PDR.PDRposition;

import java.util.Arrays;

/**
 * Created by Pumpkin on 2017/8/29 0009.
 *  Modified by zbs 2023
 *  移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

public class StepDetectionNormal {
	// single mode
	// private static StepDetectionNormal single=null;
    private PDRstream pdrStream=null;
    public StepDetectionNormal(PDRstream pdrStream){
        this.pdrStream=pdrStream;
    }

    protected boolean StepDetect(double AccSlideWin[][], int SlidWinMidSqNo){
    	
    	boolean StepDetectFlag=false;
    	PDRresult StepDetectResult=new PDRresult();
    	//

        //main body of the algorithm************
        
        //initialize
        Arrays.fill(StepDetectResult.StepInfo, 0d);
        Arrays.fill(StepDetectResult.StepSqNo, 0);
        //
        boolean FirstPeakFlag=pdrStream.stepDetect.FirstPeakFlag;
        boolean FindStepEndFlag=pdrStream.stepDetect.FindStepEndFlag;
        double curStepStartLocation=pdrStream.stepDetect.curStepStartLocation;
        double curStepPeakLocation=pdrStream.stepDetect.curStepPeakLocation;
        double curStepEndLocation=pdrStream.stepDetect.curStepEndLocation;
        double curStepPeakValue=pdrStream.stepDetect.curStepPeakValue;
        double preStepPeakLocation=pdrStream.stepDetect.preStepPeakLocation;
        double StepPeriod=pdrStream.stepDetect.preStepPeriod;
        double preStepVar=pdrStream.stepDetect.preStepVar;
        int StepCount=pdrStream.stepDetect.StepCount;
        int curStepStartSqNo=pdrStream.stepDetect.curStepStartSqNo;
        int curStepPeakSqNo=pdrStream.stepDetect.curStepPeakSqNo;
        int curStepEndSqNo=pdrStream.stepDetect.curStepEndSqNo;
        double AvgStepPeriod=pdrStream.stepDetect.AvgStepPeriod;
        double MaxAccValue=pdrStream.stepDetect.MaxAccValue;
        double MaxAccValueLocation=pdrStream.stepDetect.MaxAccValueLocation;
        int MaxAccValueSqNo=pdrStream.stepDetect.MaxAccValueSqNo;

        
        double curStepPeriod;
        double curStepVar;
        boolean Flag_GoToEnd = false;
        double AccWin[] = new double[AccSlideWin[0].length];
        for (int i = 0; i < AccSlideWin[0].length; i++) {
            AccWin[i] = AccSlideWin[1][i];
        } 

        int SlideWindowLen =AccWin.length; //sliding window length
        int MidNoWindow = (int) Math.floor(SlideWindowLen / 2) + 1; 
        double MidWindowTick = AccSlideWin[0][MidNoWindow - 1];
        int Pre_MidNoWindow = MidNoWindow - 1;
        int Next_MidNoWindow = MidNoWindow + 1;
        double AccWinMid = AccWin[MidNoWindow - 1];
        double AccWinMid_Pre = AccWin[Pre_MidNoWindow - 1];
        double AccWinMid_Next = AccWin[Next_MidNoWindow - 1];
        
        if(FindStepEndFlag==true){
        	if (AccWinMid > AccWinMid_Pre && AccWinMid > AccWinMid_Next && 
        	AccWinMid >curStepPeakValue && AccWinMid > MaxAccValue) {
        		MaxAccValue = AccWinMid;
        		MaxAccValueLocation = MidWindowTick;
        		MaxAccValueSqNo = SlidWinMidSqNo;
        	}
        	
        	int EndPointNo = (int) (MidWindowTick - curStepPeakLocation);  // ms,
            // To avoid the case of two peak in one step.
            // if current time interval is less than 1/4 of default period, then jump over the following procedure.
            boolean flag=EndPointNo > Math.round(1d/4d * AvgStepPeriod) && AccWinMid <= 0d && AccWinMid_Next
                    > 0d;
            // Swing mode will change the algorithm
            if(pdrStream.mode==4){
                flag=EndPointNo > Math.round( 1d/4d * AvgStepPeriod);
            }

        	if (flag==true) {
        		// find the end point
        		StepDetectFlag = true; 
        		curStepEndLocation = MidWindowTick;
        		curStepEndSqNo = SlidWinMidSqNo;  
                curStepPeriod = curStepEndLocation - curStepStartLocation + 1;
                
                if (MaxAccValue > curStepPeakValue) {
            		curStepPeakValue = MaxAccValue;
            		curStepPeakLocation = MaxAccValueLocation;
            		curStepPeakSqNo = MaxAccValueSqNo;
                }
                
                StepDetectResult.StepInfo[0]=curStepStartLocation;
                StepDetectResult.StepInfo[1]=curStepPeakLocation;
                StepDetectResult.StepInfo[2]=curStepEndLocation;
                StepDetectResult.StepInfo[3]=curStepPeakValue;
                StepDetectResult.StepInfo[4]=curStepPeriod;
                //StepDetectResult.StepVar=curStepVar;
                StepDetectResult.StepInfo[6]=0d;
                StepDetectResult.StepSqNo[0]=curStepStartSqNo;
                StepDetectResult.StepSqNo[1]=curStepPeakSqNo;
                StepDetectResult.StepSqNo[2]=curStepEndSqNo;
            	
                // finish, update the status for the next round
                FindStepEndFlag = false;
                StepPeriod = curStepPeriod;
                AvgStepPeriod = (AvgStepPeriod + StepPeriod) / 2;
                preStepPeakLocation = curStepPeakLocation;
                curStepStartLocation = MidWindowTick;
               
                curStepPeakLocation = 0;
                curStepEndLocation = 0;
                curStepPeakValue = 0;

                curStepStartSqNo = curStepEndSqNo;  
                curStepPeakSqNo = 0;
                curStepEndSqNo = 0;
                
                MaxAccValue = 0;
                MaxAccValueLocation = 0;
                MaxAccValueSqNo = 0;
                
        	}else if(EndPointNo > Math.round(5d/4d * AvgStepPeriod)){
        		// Period is too long to meet the requirement of 0-cross principle.
        		StepDetectFlag = true; 
                curStepEndLocation = curStepStartLocation + StepPeriod - 1;
                curStepEndSqNo = -1;
                curStepPeriod=StepPeriod;
                AvgStepPeriod = (AvgStepPeriod + StepPeriod) / 2;
                curStepVar=preStepVar;
                
                if (MaxAccValue > curStepPeakValue) {
                    curStepPeakValue = MaxAccValue;
                    curStepPeakLocation = MaxAccValueLocation;
                    curStepPeakSqNo = MaxAccValueSqNo;
                }
                
                StepDetectResult.StepInfo[0]=curStepStartLocation;
                StepDetectResult.StepInfo[1]=curStepPeakLocation;
                StepDetectResult.StepInfo[2]=curStepEndLocation;
                StepDetectResult.StepInfo[3]=curStepPeakValue;
                StepDetectResult.StepInfo[4]=curStepPeriod;
                StepDetectResult.StepInfo[5]=curStepVar;
                StepDetectResult.StepInfo[6]=0d;
                StepDetectResult.StepSqNo[0]=curStepStartSqNo;
                StepDetectResult.StepSqNo[1]=curStepPeakSqNo;
                StepDetectResult.StepSqNo[2]=curStepEndSqNo;
                
                FindStepEndFlag = false;
                //StepPeriod = curStepPeriod;
                // preStepVar=curStepVar;
                preStepPeakLocation = curStepPeakLocation;
                curStepStartLocation = MidWindowTick;
               
                curStepPeakLocation = 0;
                curStepEndLocation = 0;
                curStepPeakValue = 0;
                //curStepPeriod = 0;
                //curStepVar=0;
                curStepStartSqNo = (int) (curStepStartSqNo + Math.round(StepPeriod * pdrStream.sampleFreq/
                        1000d));  
                
                curStepPeakSqNo = 0;
                curStepEndSqNo = 0;
                
                MaxAccValue = 0;
                MaxAccValueLocation = 0;
                MaxAccValueSqNo = 0;
        	}	
        }else{
        	if ((AccWinMid >= AccWinMid_Pre && AccWinMid > AccWinMid_Next)) { 
                if (AccWinMid >= pdrStream.AccPeakMagnitudeLB) {
                    if (FirstPeakFlag == false) {   
                        FirstPeakFlag = true;  
                        StepCount = StepCount + 1;
                        curStepPeakLocation = MidWindowTick;
                        curStepPeakValue = AccWinMid;
                        curStepPeakSqNo = SlidWinMidSqNo;
                        
                        int StepStartWinNo = FindFirstStepStart(AccWin, MidNoWindow, 1);
                        curStepStartLocation = AccSlideWin[0][StepStartWinNo];
                        curStepStartSqNo = SlidWinMidSqNo - (MidNoWindow - StepStartWinNo);
                        FindStepEndFlag = true; 
                    }
                    else {  
                        double DiffPeakTime = MidWindowTick - preStepPeakLocation;
                        if (DiffPeakTime < pdrStream.AccPeakPeriodLB) {
                            //important! the interval of two step is too short?
                            Flag_GoToEnd = true;  
                        }
                        if (DiffPeakTime > (pdrStream.SFAccPeakPeriod * AvgStepPeriod) && Flag_GoToEnd ==
                                false) { 
                            StepCount = StepCount + 1;
                            curStepPeakLocation = MidWindowTick;
                            curStepPeakValue = AccWinMid;
                            curStepPeakSqNo = SlidWinMidSqNo;
                            int StepStartWinNo = FindFirstStepStart(AccWin, MidNoWindow, 1);
                            curStepStartLocation = AccSlideWin[0][StepStartWinNo];
                            curStepStartSqNo = SlidWinMidSqNo - (MidNoWindow - StepStartWinNo);
                            FindStepEndFlag = true; 
                        }
                        if (DiffPeakTime <= (pdrStream.SFAccPeakPeriod * AvgStepPeriod) && Flag_GoToEnd ==
                                false) { 
                            StepCount = StepCount + 1;
                            curStepPeakLocation = MidWindowTick;
                            curStepPeakValue = AccWinMid;
                            curStepPeakSqNo = SlidWinMidSqNo;
                            FindStepEndFlag = true; 
                        }
                    }  
                }  
            }  
        }
        
        // record all the state info for the next round
        pdrStream.stepDetect.FirstPeakFlag=FirstPeakFlag;
        pdrStream.stepDetect.FindStepEndFlag=FindStepEndFlag;
        pdrStream.stepDetect.curStepStartLocation=curStepStartLocation;
        pdrStream.stepDetect.curStepPeakLocation=curStepPeakLocation;
        pdrStream.stepDetect.curStepEndLocation=curStepEndLocation;
        pdrStream.stepDetect.curStepPeakValue=curStepPeakValue;
        pdrStream.stepDetect.preStepPeriod=StepPeriod;
        pdrStream.stepDetect.preStepPeakLocation=preStepPeakLocation;
        pdrStream.stepDetect.StepCount=StepCount;
        pdrStream.stepDetect.curStepStartSqNo=curStepStartSqNo;
        pdrStream.stepDetect.curStepPeakSqNo=curStepPeakSqNo;
        pdrStream.stepDetect.curStepEndSqNo=curStepEndSqNo;
        pdrStream.stepDetect.preStepVar=preStepVar;
        pdrStream.stepDetect.AvgStepPeriod=AvgStepPeriod;
        pdrStream.stepDetect.MaxAccValue=MaxAccValue;
        pdrStream.stepDetect.MaxAccValueLocation=MaxAccValueLocation;
        pdrStream.stepDetect.MaxAccValueSqNo=MaxAccValueSqNo;
    	
    	// update the heading angle, step length, and positions of current step.
        if(StepDetectFlag){
            // step length update
            StepLengthEstimation SLE=new StepLengthEstimation(pdrStream);
            StepDetectResult.steplength=SLE.StepLength(StepDetectResult);
            // update positions
        	if(pdrStream.StepResultArray.size()>=1){
        		// StepDetectResult.CurPos(pdrStream.StepResultArray.get(pdrStream.StepResultArray.size()-1).pos);
        		StepDetectResult.StepSqNo[3]=
        				pdrStream.StepResultArray.get(pdrStream.StepResultArray.size()-1).StepSqNo[3]+1;
        	}else{
        		// StepDetectResult.CurPos(pdrStream.iniPosXY);
        		StepDetectResult.StepSqNo[3]=1;
        	}
	    	
	    	if(pdrStream.StepResultArray.size()<pdrStream.maxStepResults){
	    		pdrStream.StepResultArray.add(StepDetectResult);
	    	}else {
	    		pdrStream.StepResultArray.add(StepDetectResult);
	    		pdrStream.StepResultArray.remove(0);
	    		pdrStream.StepResultArray.trimToSize();
			}
        }
    	
    	return StepDetectFlag;
    }

    /** MidNoWindow: from the point looking for the start of step backward.
     ** StopNo: means only find the zero-crossing point during
     ** [StopNo:MidNoWindow]
     ** Len=length(AccWin);
     **/
    private int FindFirstStepStart(double AccWin[], int MidNoWindow, int StopNo) {

        int StepStartWinNo = 0;
        for (int i = MidNoWindow-1; i >= StopNo-1; i--) {
            if (AccWin[i] == 0) {
                StepStartWinNo = i+1;
                break;
            }
            if (AccWin[i + 1] > 0 & AccWin[i] < 0) {
                StepStartWinNo = i + 2;
                break;
            }
        }
        return StepStartWinNo;
    }
}
