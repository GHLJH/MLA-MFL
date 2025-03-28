package com.PDR.Response;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Response.EnumModelState;
import cn.edu.whu.lmars.loccore.event.AbstractEvent.Response.ModelStartResponseEvent;


/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrStartResponseEvent extends ModelStartResponseEvent {

    public pdrStartResponseEvent(String modelName, EnumModelState state) {
        super(modelName, state);
    }
}
