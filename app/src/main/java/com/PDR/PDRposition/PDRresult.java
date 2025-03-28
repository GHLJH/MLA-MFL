package com.PDR.PDRposition;


/**
 * Created by Pumpkin on 2018/12/9 0009.
 *  Modified by zbs 2023
 * 移动地理空间大数据云服务创新团队 www.dxkjs.com/
 */

class PDRresult {
	
	protected double[] StepInfo=new double[7];

	/** 0---StepStartLocation;(time)
     ** 1---StepPeakLocation;
     ** 2---StepEndLocation;
     ** 3---StepPeak;
     ** 4---StepPeriod; 步伐周期
     ** 5---StepVar; 步频
     ** 6---Heading; unit: degree
	 **/
	
	protected int[] StepSqNo=new int[4];

    /** 0---StepStartSqNo;
     ** 1---curStepPeakSqNo;
     ** 2---curStepEndSqNo;
	 ** 3---StepCount;
	 **/
	
	protected double steplength=0.8d;      // step length at each step
	
	protected double[] pos=new double[3];  //每步的位置信息: 0,X axis; 1, Y axis.     Z axis

}
