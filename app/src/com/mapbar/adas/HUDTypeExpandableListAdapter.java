package com.mapbar.adas;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.miyuan.obd.R;

import java.util.List;

/**
 * Created by guomin on 2018/6/4.
 */

public class HUDTypeExpandableListAdapter extends BaseExpandableListAdapter {

    private List<HUDInfo> hudInfos;

    public HUDTypeExpandableListAdapter(List<HUDInfo> hudInfos) {
        this.hudInfos = hudInfos;
    }

    public List<HUDInfo> getCarModels() {
        return hudInfos;
    }

    public void setCarModels(List<HUDInfo> hudInfos) {
        this.hudInfos = hudInfos;
    }

    @Override
    public int getGroupCount() {
        return hudInfos.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<HUDInfo.HUDItem> hudItems = hudInfos.get(groupPosition).getHudItems();
        return hudItems == null ? 0 : hudItems.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return hudInfos.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<HUDInfo.HUDItem> hudItems= hudInfos.get(groupPosition).getHudItems();
//        return carStyleList == null ? 0 : carStyleList.size();
        return hudItems == null ? null : hudItems.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.model_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = convertView.findViewById(R.id.car_name);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(hudInfos.get(groupPosition).getType());
        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(GlobalUtil.getContext()).inflate(R.layout.model_style_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = convertView.findViewById(R.id.car_name);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        HUDInfo.HUDItem item = hudInfos.get(groupPosition).getHudItems().get(childPosition);
        childViewHolder.tvTitle.setTextColor(item.isChoice() ? Color.RED : Color.parseColor("#FF5D5D5D"));
        childViewHolder.tvTitle.setText(item.getName());
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView tvTitle;
    }

    static class ChildViewHolder {
        TextView tvTitle;
    }
}