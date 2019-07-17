package com.mapbar.adas;


import android.os.Bundle;

import com.mapbar.hamster.BlueManager;

/**
 * 免责声明
 */
public class DisclaimerTask extends BaseTask {

    @Override
    public void excute() {

        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) GlobalUtil.getMainActivity()).hideSplash();
            }
        });
        if (BlueManager.getInstance().isConnected()) {
            ChoiceHUDTypePage hudTypePage = new ChoiceHUDTypePage();
            Bundle bundle = new Bundle();
            bundle.putInt("type", AdasApplication.type);
            hudTypePage.setDate(bundle);
            PageManager.go(hudTypePage);
        } else {
            BlueManager.getInstance().stopScan(false);
            PageManager.go(new ConnectPage());
        }
        complate();
    }
}
