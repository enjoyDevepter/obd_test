package com.mapbar.adas;

import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@PageSetting(contentViewId = R.layout.hud_type_layout, toHistory = false)
public class ChoiceHUDTypePage extends AppBasePage implements View.OnClickListener, BleCallBackListener, ExpandableListView.OnGroupExpandListener, ExpandableListView.OnChildClickListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.next)
    private View nextV;
    @ViewInject(R.id.count)
    private EditText countET;
    @ViewInject(R.id.expandablelistView)
    private ExpandableListView groupLV;

    private HUDTypeExpandableListAdapter hudTypeExpandableListAdapter;

    private boolean auto;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        nextV.setOnClickListener(this);
        title.setText("设置HUD类型");

        BlueManager.getInstance().addBleCallBackListener(this);
        auto = AdasApplication.currentHUDItem != null;
        Log.d("auto  " + auto);
        if (auto) {
            BlueManager.getInstance().send(ProtocolUtils.choiceHUD(AdasApplication.currentHUDItem.getType()));
        }

        initData();
        hudTypeExpandableListAdapter = new HUDTypeExpandableListAdapter(AdasApplication.hudInfos);
        groupLV.setAdapter(hudTypeExpandableListAdapter);
        groupLV.setOnGroupExpandListener(this);
        groupLV.setOnChildClickListener(this);
    }


    private void initData() {
        if (AdasApplication.hudInfos.size() > 0) {
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(GlobalUtil.getContext().getAssets().open("hudinfos")));
            String tempS = "";
            while ((tempS = br.readLine()) != null) {
                HUDInfo info = new HUDInfo();
                String[] type = tempS.split("#");
                info.setType(type[0]);
                String[] itemStr = type[1].split("@");
                List<HUDInfo.HUDItem> items = new ArrayList<>();
                for (int i = 0; i < itemStr.length; i++) {
                    HUDInfo.HUDItem item = new HUDInfo.HUDItem();
                    String[] temp = itemStr[i].split("&");
                    item.setName(temp[0]);
                    item.setType(Integer.valueOf(temp[1]));
                    item.setSupportTire(Boolean.valueOf(temp[2]));
                    item.setSupportOBD(Boolean.valueOf(temp[3]));
                    items.add(item);
                    info.setHudItems(items);
                }
                AdasApplication.hudInfos.add(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            case R.id.next:
                String count = countET.getText().toString().trim();
                if ("".equals(count)) {
                    Toast.makeText(GlobalUtil.getContext(), "请输入待出厂盒子个数!", Toast.LENGTH_LONG).show();
                    return;
                }
                AdasApplication.count = Integer.valueOf(count);
                BlueManager.getInstance().send(ProtocolUtils.choiceHUD(AdasApplication.currentHUDItem.getType()));
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.CHOICE_HUD_TYPE:
                if (AdasApplication.currentHUDItem.getType() == (Integer) data && AdasApplication.count > 0) {
                    // 开始测试
                    PageManager.go(new HUDTestPage());
                } else if (AdasApplication.currentHUDItem.getType() == (Integer) data && AdasApplication.count <= 0) {
                    AdasApplication.currentHUDItem = null;
                    Toast.makeText(GlobalUtil.getContext(), "已完成！请重新输入待出厂盒子个数!", Toast.LENGTH_LONG).show();
                } else {
                    // 重置
                    Toast.makeText(ChoiceHUDTypePage.this.getContext(), "设置异常，请重新选择！", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onGroupExpand(int groupPosition) {
        if (groupLV == null) return;
        for (int i = 0; i < hudTypeExpandableListAdapter.getGroupCount(); i++) {
            if (i != groupPosition) {
                groupLV.collapseGroup(i);
            }
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        HUDInfo hudInfo = (HUDInfo) hudTypeExpandableListAdapter.getGroup(groupPosition);
        HUDInfo.HUDItem hudItem = hudInfo.getHudItems().get(childPosition);
        hudItem.setChoice(!hudItem.isChoice());
        for (HUDInfo.HUDItem item : hudInfo.getHudItems()) {
            if (item != hudItem) {
                item.setChoice(false);
            }
        }
        AdasApplication.currentHUDItem = new HUDInfo.HUDItem();
        convert(AdasApplication.currentHUDItem, hudItem);
        hudTypeExpandableListAdapter.notifyDataSetChanged();
        nextV.setVisibility(View.VISIBLE);
        return true;
    }

    private void convert(HUDInfo.HUDItem origin, HUDInfo.HUDItem source) {
        origin.setName(source.getName());
        origin.setChoice(source.isChoice());
        origin.setSupportOBD(source.isSupportOBD());
        origin.setSupportTire(source.isSupportTire());
        origin.setType(source.getType());
    }
}
