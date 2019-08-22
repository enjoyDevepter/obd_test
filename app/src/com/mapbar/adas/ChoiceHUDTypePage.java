package com.mapbar.adas;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.hud_type_layout, toHistory = false)
public class ChoiceHUDTypePage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.type)
    private TextView typeTV;
    @ViewInject(R.id.choice)
    private View choiceV;
    @ViewInject(R.id.next)
    private View nextV;

    final String[] items = {"M2", "M3", "M4", "F2", "F3", "F4", "F5", "F6", "P3", "P4", "P5", "P6", "P7","T1"};
    private int index;
    private int type;
    private boolean auto;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        title.setText("设置HUD类型");
        choiceV.setOnClickListener(this);
        BlueManager.getInstance().addBleCallBackListener(this);
        type = getDate().getInt("type");
        auto = type != -1;
        Log.d("auto  " + auto);
        if (auto) {
            BlueManager.getInstance().send(ProtocolUtils.choiceHUD(type));
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choice:
                AlertDialog.Builder builder = new AlertDialog.Builder(ChoiceHUDTypePage.this.getContext());
                builder.setTitle("选择HUD类型");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        index = which;
                        switch (which) {
                            case 0: // M2
                                type = 0x02;
                                break;
                            case 1: // M3
                                type = 0x03;
                                break;
                            case 2: // M4
                                type = 0x04;
                                break;
                            case 3: // F2
                                type = 0x22;
                                break;
                            case 4: // F3
                                type = 0x23;
                                break;
                            case 5: // F4
                                type = 0x24;
                                break;
                            case 6:
                                type = 0x25;
                                break;
                            case 7:
                                type = 0x26;
                                break;
                            case 8:
                                type = 0x43;
                                break;
                            case 9:
                                type = 0x44;
                                break;
                            case 10:
                                type = 0x45;
                                break;
                            case 11:
                                type = 0x46;
                                break;
                            case 12:
                                type = 0x47;
                                break;
                            case 13:
                                type = 0x00;
                                break;
                            default:
                                break;
                        }
                        BlueManager.getInstance().send(ProtocolUtils.choiceHUD(type));
                    }
                });
                builder.show();
                break;
            case R.id.next:
                PageManager.go(new HUDTestPage());
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.CHOICE_HUD_TYPE:
                if (type == (Integer) data) {
                    typeTV.setText(items[index]);
                    // 开始测试
                    nextV.setVisibility(View.VISIBLE);
                    nextV.setOnClickListener(this);
                    AdasApplication.type = type;
                    if (auto) {
                        PageManager.go(new HUDTestPage());
                    }
                } else {
                    // 重置
                    typeTV.setText("");
                    Toast.makeText(ChoiceHUDTypePage.this.getContext(), "设置异常，请重新选择！", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }
}
