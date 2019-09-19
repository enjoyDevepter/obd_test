package com.mapbar.adas;

import android.content.pm.ActivityInfo;
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
public class AuthPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.sn_01)
    private TextView sn_01;
    @ViewInject(R.id.sn_02)
    private TextView sn_02;
    @ViewInject(R.id.sn_03)
    private TextView sn_03;
    @ViewInject(R.id.sn_04)
    private TextView sn_04;

    private String serialNumber;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("获取授权码");
        next.setOnClickListener(this);
        next.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        back.setOnClickListener(this);

//        if (getDate() != null && null != getDate().get("sn")) {
//            String sn = (String) getDate().get("sn");
//            String[] sns = sn.split("-");
//            if (sns.length < 4) {
//                Toast.makeText(getContext(), "识别错误", Toast.LENGTH_LONG).show();
//                return;
//            }
//            sn_01.setText(sns[0]);
//            sn_02.setText(sns[1]);
//            sn_03.setText(sns[2]);
//            sn_04.setText(sns[3]);
//        }
        getSN();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.finishActivity(MainActivity.getInstance());
                break;
            case R.id.next:
                getSN();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }


    private void getSN() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ty_right", AdasApplication.supportTrie ? 1 : 2);
            jsonObject.put("gc_right", AdasApplication.supportFault ? 1 : 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("getSN input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.GETSN).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("getSN failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        String serial_number = result.optString("serial_number");
                        final String[] sns = serial_number.split("-");
                        if (sns.length < 4) {
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "SN生成错误", Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {

                                sn_01.setText(sns[0]);
                                sn_02.setText(sns[1]);
                                sn_03.setText(sns[2]);
                                sn_04.setText(sns[3]);
                            }
                        });
                        check(serial_number);
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                next.setEnabled(true);
                                Toast.makeText(GlobalUtil.getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("getSN failure " + e.getMessage());
                }
            }
        });
    }

    private void check(final String serialNumber) {

        next.setEnabled(false);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", serialNumber);
            jsonObject.put("boxId", getDate().getString("boxId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("check sn input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.ACTIVATE).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("sn_check failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        // 激活成功
                        String code = result.optString("rightStr");
                        BlueManager.getInstance().send(ProtocolUtils.auth(serialNumber, code));
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                next.setEnabled(true);
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

    /**
     * 激活成功
     */
    private void activateSuccess() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", serialNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("activate input  " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE_SUCCESS)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("activate failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responese = response.body().string();
                Log.d("activate success " + responese);
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final JSONObject result = new JSONObject(responese);
                            if ("000".equals(result.optString("status"))) {
                                Toast.makeText(AuthPage.this.getContext(), "测试完成！", Toast.LENGTH_LONG).show();
                                AlarmManager.getInstance().play(R.raw.box);
                            } else {
                                Log.d("activate failure");
                            }
                        } catch (JSONException e) {
                            Log.d("activate failure " + e.getMessage());
                        }
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
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                activateSuccess();
                break;
            case OBDEvent.AUTHORIZATION_FAIL:
            case OBDEvent.NO_PARAM: // 无参数
                break;
            default:
                break;
        }
    }
}
