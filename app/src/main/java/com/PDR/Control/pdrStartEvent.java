package com.PDR.Control;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelStartEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrStartEvent extends ModelStartEvent {
    public pdrStartEvent (String modelName){
        super(modelName);
    }
}
