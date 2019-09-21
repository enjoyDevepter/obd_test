package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mapbar.hamster.OBDEvent.TEST_CAN_ERROR;
import static com.mapbar.hamster.OBDEvent.TEST_C_ERROR;
import static com.mapbar.hamster.OBDEvent.TEST_FM_ERROR;
import static com.mapbar.hamster.OBDEvent.TEST_K_ERROR;
import static com.mapbar.hamster.OBDEvent.TEST_OK;
import static com.mapbar.hamster.OBDEvent.TEST_V_ERROR;

@PageSetting(contentViewId = R.layout.hud_test_layout, toHistory = false)
public class HUDTestPage extends AppBasePage implements BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.status)
    private TextView statusTV;
    @ViewInject(R.id.type)
    private TextView typeTV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        title.setText("测试");
        typeTV.setText(AdasApplication.currentHUDItem.getName());
        BlueManager.getInstance().send(ProtocolUtils.getTest());
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }


    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case TEST_V_ERROR:
                AlarmManager.getInstance().play(R.raw.warm);
                statusTV.setText("电压异常!");
                break;
            case TEST_C_ERROR:
                AlarmManager.getInstance().play(R.raw.warm);
                statusTV.setText("传感器异常！");
                break;
            case TEST_CAN_ERROR:
                AlarmManager.getInstance().play(R.raw.warm);
                statusTV.setText("CAN 通信故障！");
                break;
            case TEST_K_ERROR:
                AlarmManager.getInstance().play(R.raw.warm);
                statusTV.setText("K 线通信故障");
                break;
            case TEST_FM_ERROR:
                AlarmManager.getInstance().play(R.raw.warm);
                statusTV.setText("FM 异常");
                break;
            case TEST_OK:
                updateBoxID(HexUtils.formatHexString((byte[]) data));
                break;
            default:
                break;
        }
    }

    private void updateBoxID(final String boxId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("boxId", boxId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateBoxID input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .url("http://47.92.101.179:8020/service/box/add")
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        statusTV.setText("updateBoxID fail!");
                    }
                });
                Log.d("updateBoxID failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateBoxID success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status")) || "102".equals(result.optString("status"))) {
                        AuthPage authPage = new AuthPage();
                        Bundle bundle = new Bundle();
                        bundle.putString("boxId", boxId);
                        authPage.setDate(bundle);
                        PageManager.go(authPage);
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                statusTV.setText(result.optString("message") + "，\n 继续测试请更换OBD，程序将自动开始测试");
                            }
                        });
                    }
                } catch (JSONException e) {
                    AlarmManager.getInstance().play(R.raw.warm);
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            statusTV.setText("数据异常！请联系开发人员!");
                        }
                    });
                }
            }
        });
    }
}
