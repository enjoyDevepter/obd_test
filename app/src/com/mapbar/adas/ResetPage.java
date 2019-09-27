package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
    private View infoV;
    private AnimationDrawable animationDrawable;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        resetTV.setOnClickListener(this);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset:
                resetTV.setVisibility(View.INVISIBLE);
                BlueManager.getInstance().send(ProtocolUtils.getOBDStatus(System.currentTimeMillis()));
                animationDrawable.start();
                statusV.setVisibility(View.VISIBLE);
                infoV.setVisibility(View.VISIBLE);
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


    private void reset(OBDStatusInfo obdStatusInfo) {
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
                    Log.d("sn_check failure " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.UNREGISTERED://未注册
                break;
            case OBDEvent.AUTHORIZATION: //未授权或者授权过期
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                reset((OBDStatusInfo) data);
                break;
            case OBDEvent.AUTHORIZATION_FAIL:
            case OBDEvent.NO_PARAM: // 无参数
                break;
            case OBDEvent.RESET:
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                AlarmManager.getInstance().play(R.raw.reset);
                break;
            default:
                break;
        }
    }
}
