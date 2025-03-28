package com.PDR.PDRposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 *  Modified by zbs 2023
 *  移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

class PDRstream {
	
	//决定PDR质量的参数
	protected double sampleFreq=15.36d;
	//protected double[] iniPosXY={0d,0d};
	private int smoothFactor;
	private int windowLength; // it is better to be odd
	private int cacheLength; // be equal to one sec data等于一秒的数据
	protected int mode=1; //default mode: hand
	//hand=1, call=2, pocket=3, swing=4
    protected double AccPeakPeriodLB;
    protected double AccPeakMagnitudeLB;
    protected double SFAccPeakPeriod;
    protected double DefaultStepPeriod=0.8d; // unit: second
    protected double DefaultSL=0.70d;  // step length by default 默认步长

    //Arrays to store step results
	protected Vector<PDRresult> StepResultArray = new Vector<>(0,1);
	//max step results 
	protected final int maxStepResults=20;
	
	//global variables
	public double firstAccTimeStamp; //record the start time
	private double gravity=9.70157d; //initial value of gravity
	private double gravityNew=0d; //update the gravity value according to the collected sensor data
	private boolean cachefull=false;
	private ArrayList<double[]> RawAccCache = new ArrayList<double[]>();
	protected ArrayList<double[]> SlideWindow = new ArrayList<double[]>();
	protected StepDetectState stepDetect=new StepDetectState(this);
    private boolean reset=false;
	
	//temporal variables
	//private int accNo;  //No of the input acc data
	private double accValue; // the RMS of the 3-axis acc data.
	private double accTimeStamp; //time stamp of the input acc data. system time format


	//Constructor: default parameter setting
	public PDRstream(){
        mode=1;
        smoothFactor=5;
        windowLength=17;
        cacheLength=18;
        sampleFreq=15.36d; //15.36hz
        DefaultStepPeriod=0.8d;
        AccPeakPeriodLB = 0.26* 1000;
        AccPeakMagnitudeLB = 0.7;
        SFAccPeakPeriod = 1.8;
        stepDetect.ini();
        if(windowLength % 2==0)windowLength=windowLength+1;
	}


	// used to reset the whole PDR process while using in real time Android application
	public void PDRreset(){
        reset=true;
        gravity=9.70157d; // initial value of gravity
        gravityNew=0d; // update the gravity value according to the collected sensor data
        cachefull=false;
        while(StepResultArray.size()>0){
            StepResultArray.remove(0);
        }
        while(RawAccCache.size()>0){
            RawAccCache.remove(0);
        }
        while(SlideWindow.size()>0){
            SlideWindow.remove(0);
        }
        stepDetect.ini();
        reset=false;
    }


	// manually set the parameters: PDRstream.iniParameters(..)手动设置参数
	public void iniParameters(int inMode, double sampleFreqIn, int smoothFactorIn,
			int windowLengthIn, int cacheLengthIn, double stepPeriodIn, double AccPMLBin, double AccPPLBin,
             double SFAccPPin){
		mode=inMode;
		//iniPosXY=iniPosIn;
		sampleFreq=sampleFreqIn;
		smoothFactor=smoothFactorIn;
		windowLength=windowLengthIn;
		cacheLength=cacheLengthIn;
        DefaultStepPeriod=stepPeriodIn;
        AccPeakMagnitudeLB=AccPMLBin;
        AccPeakPeriodLB=AccPPLBin;
        SFAccPeakPeriod=SFAccPPin;
		if(windowLength%2==0)windowLength=windowLength+1;
	}
	
	// process starts
	// the accTimeStamp is time with respect to 1970.1.1 (unit: ms)
	// public boolean process(int accNo, double[] accValue, String[] accTimeStamp){
	public boolean[] process(int accNo, double[] accValue, long accTimeStamp){
		//this();
		//if reset?
        if(reset)return new boolean[]{false,false};

		// pass the time
		if(accNo==1){
			this.accTimeStamp=accTimeStamp;
			firstAccTimeStamp=this.accTimeStamp;
			this.accTimeStamp=0d;
		}else {
			this.accTimeStamp=accTimeStamp;
			this.accTimeStamp=this.accTimeStamp-firstAccTimeStamp;
		}
		
		// pass the acc data
		this.accValue=Math.sqrt(accValue[0] * accValue[0] + accValue[1] * accValue[1] + accValue[2] * accValue[2]);

		
		// data pre-processing
		updateCache();
		
		updateSlidWinSmoothed();
		
		// process starts!
		int SlidWinMidSqNo=accNo-1; // serial NO. of the middle Data in Sliding window
		int SlidWinMidNo=(windowLength+1)/2; // the middle of slide window, be careful!
		
		if(cachefull==false)return new boolean[]{false,false};
		
		double AccSlideWin[][]=new double[2][windowLength];
		
		for(int i=0;i<SlidWinMidNo+1;i++){
			for(int j=0; j<2; j++){
				AccSlideWin[j][i]=SlideWindow.get(windowLength-SlidWinMidNo-1+i)[j];
			}
		}
		
		for(int i=SlidWinMidNo+1;i<windowLength;i++){
			for(int j=0; j<2; j++){
				AccSlideWin[j][i]=0d;
			}
		}
		
		// step detection
        StepDetectionNormal SDN=new StepDetectionNormal(this);
		boolean StepDetectFlag=SDN.StepDetect(AccSlideWin, SlidWinMidSqNo);
		// judge: walking--true? static--false?
		return new boolean[]{StepDetectFlag,walkingStatus()};
	}
	
	private void updateCache(){
		// A data cache for storing the raw acc data and calculating gravity at static mode.
		if(RawAccCache.size()<cacheLength){
			// add data
			RawAccCache.add(new double[]{accTimeStamp, accValue});
			return;
		}else if(RawAccCache.size()==cacheLength&&cachefull==false){
			// compute the gravity or give the gravity a constant value
			
			cachefull=true;
			RawAccCache.add(new double[]{accTimeStamp, accValue});
			RawAccCache.remove(0);
			RawAccCache.trimToSize();
			return;
		}else if(RawAccCache.size() == cacheLength&&cachefull==true){
			RawAccCache.add(new double[]{accTimeStamp, accValue});
			RawAccCache.remove(0);
			RawAccCache.trimToSize();
			return;
		}else{
			// output error info
			return;
		}
	}
	
	private void updateSlidWinSmoothed(){
		// smooth the acc data, and update the sliding window.
		double AccCurSmoothed=0d;
		// obtain the current smoothed acc data
		if(RawAccCache.size()<smoothFactor){
			AccCurSmoothed=RawAccCache.get(RawAccCache.size()-1)[1];
		}else{
			//smooth	
			for(int i=smoothFactor-1;i>=0;i--){
				AccCurSmoothed=AccCurSmoothed+RawAccCache.get(RawAccCache.size()-1-i)[1];
			}
			AccCurSmoothed=AccCurSmoothed/(double)(smoothFactor);
			// sum up the new gravity
			
		}
		gravityNew=gravityNew+AccCurSmoothed;
		// subtract the gravity
		AccCurSmoothed=AccCurSmoothed-gravity;
		
		if(RawAccCache.size()==(int) (sampleFreq)&&cachefull==false){
			gravityNew=gravityNew/(double)Math.floor(sampleFreq) ;
			gravity=gravityNew;
		}
		
		// update the window
		if(SlideWindow.size()<windowLength){
			SlideWindow.add(new double[]{accTimeStamp, AccCurSmoothed});
		}else{
			SlideWindow.remove(0);
			SlideWindow.add(new double[]{accTimeStamp, AccCurSmoothed});
			RawAccCache.trimToSize();
		}
		
	}

	private boolean walkingStatus(){
		// 这是为了确定用户是否正在行走.
		double stdlim=0.1d;
		double sum=0.0d;
		double mean=0.d;
		double numi=0d;
		double std=0d;
		int shrink=0;

		for (int i=windowLength-1;i>shrink;i--){
			sum+=SlideWindow.get(i)[1];
		}
		mean=sum/windowLength;
		for (int i=windowLength-1;i>shrink;i--){
			numi+=Math.pow((SlideWindow.get(i)[1]-mean),2);
		}
		std=Math.sqrt(numi/(windowLength-shrink));
		if(std<=stdlim){
			return false;   // 没有在行走
		}else{
			return true;
		}
	}
	// manually reset the positions while the PDR error is intolerant.当PDR错误不可容忍时，手动重置位置。
	public void resetInitialPos(double posXY[]){
		//judge if the step exist?
		StepResultArray.get(StepResultArray.size()-1).pos=posXY;
	}
	
	// ****** get pos ***************
	public double[] getPDRresult(){
		double[] result=new double[7];
		if(StepResultArray.size()>0){
			result[0]=StepResultArray.get(StepResultArray.size()-1).StepInfo[0]+firstAccTimeStamp; //unit: ms
			result[1]=(double)StepResultArray.get(StepResultArray.size()-1).StepSqNo[3];  //Step Count
			result[2]=StepResultArray.get(StepResultArray.size()-1).pos[0];  // X pos
			result[3]=StepResultArray.get(StepResultArray.size()-1).pos[1];  // Y pos
			result[4]=StepResultArray.get(StepResultArray.size()-1).steplength; // Step length
			result[5]=StepResultArray.get(StepResultArray.size()-1).StepInfo[2]+firstAccTimeStamp; //unit: ms

			result[6]=StepResultArray.get(StepResultArray.size()-1).pos[2];  // Z pos
			String aa="";
		}else {

			Arrays.fill(result, 0d);
		}
		return result;
	}
}
	
	
	
