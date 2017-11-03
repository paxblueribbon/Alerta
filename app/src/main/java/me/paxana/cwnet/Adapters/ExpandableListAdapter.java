package me.paxana.cwnet.Adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.paxana.cwnet.Model.Category;
import me.paxana.cwnet.Model.Trigger;
import me.paxana.cwnet.R;

/**
 * Created by paxie on 10/30/17.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private ArrayList<Category> _listDataHeader; // header titles

    public ExpandableListAdapter(Context context, ArrayList<Category> listDataHeader) {
        this._context = context;
        this._listDataHeader = listDataHeader;
    }

    @Override
    public Trigger getChild(int groupPosition, int childPosititon) {
        Category category = _listDataHeader.get(groupPosition);
        ArrayList<Trigger> categoryArrayList = category.getTriggerList();

        return categoryArrayList.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Category category = _listDataHeader.get(groupPosition);
        ArrayList<Trigger> triggerList = category.getTriggerList();

        return triggerList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        Category category = _listDataHeader.get(groupPosition);
        String headerTitle = category.getCategoryName();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        Category category = _listDataHeader.get(i);
        ArrayList<Trigger> categoryArrayList = category.getTriggerList();

        Trigger trigger = categoryArrayList.get(i1);
        final String childText = trigger.getTriggerName();

        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.trigger_list_item, null);
        }

        TextView txtListChild = view.findViewById(R.id.triggerName);

        txtListChild.setText(childText);
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}