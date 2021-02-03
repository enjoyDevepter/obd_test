package com.mapbar.hamster;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by guomin on 2018/3/7.
 */

public class Fail implements Serializable {
    private boolean find = false;
    private String msg;

    public Fail(boolean find, String msg) {
        this.find = find;
        this.msg = msg;
    }

    public boolean isFind() {
        return find;
    }

    public void setFind(boolean find) {
        this.find = find;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "Fail{" +
                "find=" + find +
                ", msg='" + msg + '\'' +
                '}';
    }
}
