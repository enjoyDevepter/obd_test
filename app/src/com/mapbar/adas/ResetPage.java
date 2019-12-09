package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
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

@PageSetting(contentViewId = R.layout.auth_layout, toHistory = false)
public class ResetPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.reset)
    private TextView resetTV;
    @ViewInject(R.id.status)
    private View statusV;
    @ViewInject(R.id.info)
    private TextView infoV;

    @ViewInject(R.id.type)
    private TextView typeTV;

    @ViewInject(R.id.tire)
    private TextView tireTV;

    @ViewInject(R.id.obd)
    private TextView obdTV;

    @ViewInject(R.id.fm)
    private TextView fmTV;
    private AnimationDrawable animationDrawable;

    OBDStatusInfo obdStatusInfo;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        resetTV.setOnClickListener(this);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getOBDStatus(System.currentTimeMillis()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset:
                if (obdStatusInfo != null) {
                    resetTV.setVisibility(View.INVISIBLE);
                    animationDrawable.start();
                    infoV.setTextColor(R.color.text_color);
                    statusV.setVisibility(View.VISIBLE);
                    infoV.setVisibility(View.VISIBLE);
                    reset();
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        BlueManager.getInstance().removeCallBackListener(this);
    }


    private void reset() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("boxId", obdStatusInfo.getBoxId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("reset input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.RESET).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("reset failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        resetTV.setVisibility(View.VISIBLE);
                    }
                });
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        String code = result.optString("state");
                        if ("1".equals(code)) {
                            BlueManager.getInstance().send(ProtocolUtils.reset());
                        } else {
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(GlobalUtil.getContext(), "恢复出厂设置失败!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GlobalUtil.getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("reset failure " + e.getMessage());
                }
            }
        });
    }

    private void getOBDInfo() {
        if (obdStatusInfo == null) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("checkOBDRight input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.RIGHT_CHECK)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkOBDRight failure " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GlobalUtil.getContext(),"网络异常！",0).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkOBDRight success " + responese);
                final OBDRightInfo obdRightInfo = JSON.parseObject(responese, OBDRightInfo.class);
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        boolean supportTire = obdRightInfo.iSupportTire();
                        boolean supportCheck = obdRightInfo.iSupportCheck();
                        boolean supportFM = obdStatusInfo.isSupportFM();
                        String type = "";
                        switch (obdStatusInfo.getHudType()) {
                            case 0x00:
                                type = "T1-基础版";
                                break;
                            case 0x02:
                                if (!supportTire && !supportCheck) {
                                    type = "M2-基础版";
                                } else if (!supportTire && supportCheck) {
                                    type = "M2-OBD汽车医生版";
                                } else if (supportTire && supportCheck) {
                                    type = "M2-豪华版";
                                } else {
                                    type = "M2_未定义";
                                }
                                break;
                            case 0x03:
                                if (!supportTire && !supportCheck) {
                                    type = "M3-基础版";
                                } else if (!supportTire && supportCheck) {
                                    type = "M3-OBD汽车医生版";
                                } else if (supportTire && supportCheck) {
                                    type = "M3-豪华版";
                                } else {
                                    type = "M3_未定义";
                                }
                                break;
                            case 0x04:
                                if (!supportTire && !supportCheck) {
                                    type = "M4-基础版";
                                } else if (!supportTire && supportCheck) {
                                    type = "M4-OBD汽车医生版";
                                } else if (supportTire && supportCheck) {
                                    type = "M4-豪华版";
                                } else {
                                    type = "M4_未定义";
                                }
                                break;
                            case 0x22:
                                if (!supportTire && !supportCheck) {
                                    type = "F2-基础版";
                                } else if (!supportTire && supportCheck) {
                                    type = "F2-OBD汽车医生版";
                                } else if (supportTire && supportCheck) {
                                    type = "F2-豪华版";
                                } else {
                                    type = "F2_未定义";
                                }
                                break;
                            case 0x13:
                            case 0x23:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "F3-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "F3-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "F3-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "F3-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "F3-至尊";
                                } else {
                                    type = "F3_未定义";
                                }
                                break;
                            case 0x14:
                            case 0x24:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "F4-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "F4-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "F4-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "F4-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "F4-至尊";
                                } else {
                                    type = "F4_未定义";
                                }
                                break;
                            case 0x15:
                            case 0x25:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "F5-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "F5-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "F5-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "F5-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "F5-至尊";
                                } else {
                                    type = "F5_未定义";
                                }
                                break;
                            case 0x16:
                            case 0x26:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "F6-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "F6-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "F6-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "F6-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "F6-至尊";
                                } else {
                                    type = "F6_未定义";
                                }
                                break;
                            case 0x33:
                            case 0x43:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "P3-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "P3-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "P3-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "P3-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "P3-至尊";
                                } else {
                                    type = "P3_未定义";
                                }
                                break;
                            case 0x34:
                            case 0x44:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "P4-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "P4-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "P4-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "P4-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "P4-至尊";
                                } else {
                                    type = "P4_未定义";
                                }
                                break;
                            case 0x35:
                            case 0x45:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "P5-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "P5-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "P5-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "P5-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "P5-至尊";
                                } else {
                                    type = "P5_未定义";
                                }
                                break;
                            case 0x36:
                            case 0x46:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "P6-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "P6-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "P6-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "P6-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "P6-至尊";
                                } else {
                                    type = "P6_未定义";
                                }
                                break;
                            case 0x37:
                            case 0x47:
                                if (!supportTire && !supportCheck && !supportFM) {
                                    type = "P7-基础版";
                                } else if (!supportTire && supportCheck && !supportFM) {
                                    type = "P7-OBD汽车医生版";
                                } else if (supportTire && supportCheck && !supportFM) {
                                    type = "P7-豪华版";
                                } else if (!supportTire && supportCheck && supportFM) {
                                    type = "P7-FM版";
                                } else if (supportTire && supportCheck && supportFM) {
                                    type = "P7-至尊";
                                } else {
                                    type = "P7_未定义";
                                }
                                break;
                        }
                        typeTV.setText(type);

                        fmTV.setText(obdStatusInfo.isSupportFM() ? "支持" : "不支持");
                        tireTV.setText(obdRightInfo.iSupportTire() ? "支持" : "不支持");
                        obdTV.setText(obdRightInfo.iSupportCheck() ? "支持" : "不支持");
                    }
                });
            }
        });
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.UNREGISTERED://未注册
                break;
            case OBDEvent.AUTHORIZATION: //未授权或者授权过期
            case OBDEvent.AUTHORIZATION_SUCCESS:
            case OBDEvent.AUTHORIZATION_FAIL:
                obdStatusInfo = (OBDStatusInfo) data;
                getOBDInfo();
                break;
            case OBDEvent.NO_PARAM: // 无参数
                break;
            case OBDEvent.RESET:
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                statusV.setVisibility(View.INVISIBLE);
                infoV.setTextColor(android.R.color.holo_red_light);
                infoV.setText("恢复出厂设置成功！");
                AlarmManager.getInstance().play(R.raw.reset);
                break;
            default:
                break;
        }
    }
}
