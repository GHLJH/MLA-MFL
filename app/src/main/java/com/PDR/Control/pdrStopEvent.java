package com.PDR.Control;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelStopEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrStopEvent extends ModelStopEvent {
    public pdrStopEvent (String modelName){
        super(modelName);
    }
}
