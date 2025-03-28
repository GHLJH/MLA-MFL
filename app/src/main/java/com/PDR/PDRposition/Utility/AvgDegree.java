package com.PDR.PDRposition.Utility;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class AvgDegree{

    public static double num(double[] angles){
        double ava=0;
        double last=angles[0];
        double sum=angles[0];
        double diff=0d;
        for(int i=1;i<=angles.length-1;i++){
            diff=angle_diff(angles[i], angles[i-1]);
            last+=diff;
            sum+=last;
        }
        ava=sum/angles.length;
        if(ava<0)ava+=360;
        return ava;
    }

    static double angle_diff(double x2, double x1){
        double diff=x2-x1+180;
        if(diff<0){
            diff=diff+360-180;
        }else if(diff>360){
            diff=diff-360-180;
        }else{
            diff=diff-180;
        }
        return diff;
    }

}
