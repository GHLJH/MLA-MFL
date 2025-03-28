package com.PDR.PDRposition.Utility;

import java.util.Date;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */
public class TimeSystem {
    static public long GetSystemTimeByMill() {
        Date date=new Date();
        long value =date.getTime(); // obtain the ms
        //String a="aaaa";
        return value;
    }
}
