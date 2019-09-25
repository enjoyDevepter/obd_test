package com.mapbar.adas.utils;

import com.miyuan.obd.BuildConfig;

/**
 * Created by guomin on 2018/6/3.
 */

public class URLUtils {
    public static final String HOST = BuildConfig.HOST;
    public static final String ACTIVATE = HOST + "service/lisense/activationT";
    public static final String GETSN = HOST + "service/lisense/getSN";
    public static final String ACTIVATE_SUCCESS = HOST + "service/lisense/activationResult";
}
