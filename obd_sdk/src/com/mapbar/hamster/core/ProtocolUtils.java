package com.mapbar.hamster.core;

import com.mapbar.hamster.log.Log;

/**
 * Created by guomin on 2018/6/2.
 */

public class ProtocolUtils {

    public static final int PROTOCOL_HEAD_TAIL = 0x7e;
    private static final int PROTOCAL_COMMON_00 = 0x80;

    public static byte[] getOBDStatus(long time) {
        Log.d("Protocol getOBDStatus ==");
        byte[] result = new byte[13];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) PROTOCAL_COMMON_00;
        result[2] = 01;
        byte[] bytes = HexUtils.longToByte(time);
        result[3] = bytes[0];
        int cr = result[1] ^ result[2] ^ result[3];
        for (int i = 1; i < bytes.length; i++) {
            result[3 + i] = bytes[i];
            cr = cr ^ bytes[i];
        }
        result[11] = (byte) cr;
        result[12] = PROTOCOL_HEAD_TAIL;
        return result;
    }

    public static byte[] reset() {
        Log.d("Protocol reset ==");
        byte[] result = new byte[6];
        result[0] = PROTOCOL_HEAD_TAIL;
        result[1] = (byte) 0x86;
        result[2] = 0x12;
        result[3] = 0;
        result[4] = (byte) (result[1] ^ result[2] ^ result[3]);
        result[5] = PROTOCOL_HEAD_TAIL;
        return result;
    }
}

