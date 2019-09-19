package com.mapbar.adas;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
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
    @ViewInject(R.id.next)
    private View nextV;
    @ViewInject(R.id.trie)
    private ViewGroup trieVG;
    @ViewInject(R.id.fault)
    private ViewGroup faultVG;

    final String[] items = {"M2", "M3", "M4", "F2", "F3", "F3-FM", "F4", "F4-FM", "F5", "F5-FM", "F6", "F6-FM", "P3", "P3-FM", "P4", "P4-FM", "P5", "P5-FM", "P6", "P6-FM", "P7", "P7-FM", "T1"};
    private int index;
    private int type;
    private boolean supportTrie;
    private boolean supoortFault;
    private boolean auto;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        title.setText("设置HUD类型");
        typeTV.setOnClickListener(this);
        trieVG.setOnClickListener(this);
        faultVG.setOnClickListener(this);
        trieVG.setSelected(false);
        faultVG.setSelected(false);

        updateUI();

        BlueManager.getInstance().addBleCallBackListener(this);
        type = getDate().getInt("type");
        auto = (type != -1);
        Log.d("auto  " + auto);
        if (auto) {
            BlueManager.getInstance().send(ProtocolUtils.choiceHUD(type));
        }
    }


    private void updateUI() {
        TextView trieFirstTV = (TextView) trieVG.getChildAt(0);
        TextView trieSecondTV = (TextView) trieVG.getChildAt(1);
        TextView faultFirstTV = (TextView) faultVG.getChildAt(0);
        TextView faultSecondTV = (TextView) faultVG.getChildAt(1);

        if (trieVG.isSelected()) {
            trieFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            trieFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            trieSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            trieSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            trieSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            trieSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            trieFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            trieFirstTV.setBackgroundColor(Color.TRANSPARENT);
        }

        if (faultVG.isSelected()) {
            faultFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            faultFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            faultSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            faultSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            faultSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            faultSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            faultFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            faultFirstTV.setBackgroundColor(Color.TRANSPARENT);
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
            case R.id.type:
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
                                type = 0x13;
                                break;
                            case 5: // F3-FM
                                type = 0x23;
                                break;
                            case 6: // F4
                                type = 0x14;
                                break;
                            case 7: // F4-FM
                                type = 0x24;
                                break;
                            case 8: // P5
                                type = 0x15;
                                break;
                            case 9: // P5-FM
                                type = 0x25;
                                break;
                            case 10: // P6
                                type = 0x16;
                                break;
                            case 11: // P6-FM
                                type = 0x26;
                                break;
                            case 12: // P3
                                type = 0x33;
                                break;
                            case 13: // P3-FM
                                type = 0x43;
                                break;
                            case 14: // P4
                                type = 0x34;
                                break;
                            case 15: // P4-FM
                                type = 0x44;
                                break;
                            case 16: // P5
                                type = 0x35;
                                break;
                            case 17: //P5-FM
                                type = 0x45;
                                break;
                            case 18: // P6
                                type = 0x36;
                                break;
                            case 19: // P6-FM
                                type = 0x46;
                                break;
                            case 20: // P7
                                type = 0x37;
                                break;
                            case 21: // P7-FM
                                type = 0x47;
                                break;
                            case 22: // T1
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
            case R.id.trie:
                trieVG.setSelected(!trieVG.isSelected());
                AdasApplication.supportTrie = trieVG.isSelected();
                updateUI();
                break;
            case R.id.fault:
                faultVG.setSelected(!faultVG.isSelected());
                AdasApplication.supportFault = faultVG.isSelected();
                updateUI();
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
