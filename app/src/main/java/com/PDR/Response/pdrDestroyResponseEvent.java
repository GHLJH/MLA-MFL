package com.PDR.Response;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Response.EnumModelState;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Response.ModelDestroyResponseEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrDestroyResponseEvent extends ModelDestroyResponseEvent {
    public pdrDestroyResponseEvent(String modelName, EnumModelState state){
        super(modelName, state);
    }
}
