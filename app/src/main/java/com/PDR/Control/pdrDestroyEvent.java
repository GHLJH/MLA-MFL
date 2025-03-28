package com.PDR.Control;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelDestroyEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrDestroyEvent extends ModelDestroyEvent {
    public pdrDestroyEvent(String modelName) {
        super(modelName);
    }
}
