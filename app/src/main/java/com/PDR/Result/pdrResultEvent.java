package com.PDR.Result;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.EnumResultType;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Result.ModelResultEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrResultEvent extends ModelResultEvent {
    private  EnumResultType Type;

    private double[] rslt;

    public void setRslt(double[] rslt){
        this.rslt = rslt;
    }

    public void setRsltType(EnumResultType type) {
        this.Type = type;
    }   //模块对应的结果类型

    @Override
    public double[] getResult(){
        return rslt;
    }

    @Override
    public EnumResultType getRsltType() {
        return Type;
    }   //模块对应的结果类型


}
