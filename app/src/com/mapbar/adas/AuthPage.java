package com.mapbar.adas;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.zbar.CaptureActivity;
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
    @ViewInject(R.id.scan)
    private View scanV;
    @ViewInject(R.id.sn_01)
    private EditText sn_01;
    @ViewInject(R.id.sn_02)
    private EditText sn_02;
    @ViewInject(R.id.sn_03)
    private EditText sn_03;
    @ViewInject(R.id.sn_04)
    private EditText sn_04;

    private String serialNumber;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("输入授权码");
        next.setOnClickListener(this);
        reportV.setOnClickListener(this);
        scanV.setOnClickListener(this);
        back.setOnClickListener(this);

        if (getDate() != null && null != getDate().get("sn")) {
            String sn = (String) getDate().get("sn");
            String[] sns = sn.split("-");
            if (sns.length < 4) {
                Toast.makeText(getContext(), "识别错误", Toast.LENGTH_LONG).show();
                return;
            }
            sn_01.setText(sns[0]);
            sn_02.setText(sns[1]);
            sn_03.setText(sns[2]);
            sn_04.setText(sns[3]);
        }
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.finishActivity(MainActivity.getInstance());
                break;
            case R.id.next:
                check();
                break;
            case R.id.scan:
                Intent intent = new Intent(GlobalUtil.getMainActivity(), CaptureActivity.class);
                GlobalUtil.getMainActivity().startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    private void check() {

        String sn01 = sn_01.getText().toString();
        String sn02 = sn_02.getText().toString();
        String sn03 = sn_03.getText().toString();
        String sn04 = sn_04.getText().toString();

        if (GlobalUtil.isEmpty(sn01) || GlobalUtil.isEmpty(sn02) || GlobalUtil.isEmpty(sn03) || GlobalUtil.isEmpty(sn04)) {
            Toast.makeText(getContext(), "请输入授权码", Toast.LENGTH_LONG).show();
            return;
        }
        next.setEnabled(false);
        final StringBuilder sn = new StringBuilder();
        sn.append(sn01).append("-").append(sn02).append("-").append(sn03).append("-").append(sn04);
        serialNumber = sn.toString();
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
