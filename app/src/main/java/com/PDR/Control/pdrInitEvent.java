package com.PDR.Control;

import cn.edu.whu.lmars.loccore.event.AbstractEvent.Control.ModelInitEvent;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class pdrInitEvent extends ModelInitEvent {
    public pdrInitEvent(String modelName) {
        super(modelName);
    }
}
