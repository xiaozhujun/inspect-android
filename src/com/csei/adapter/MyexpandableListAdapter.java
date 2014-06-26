package com.csei.adapter;
import java.util.ArrayList;
import java.util.List;
import com.example.viewpager.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
public class MyexpandableListAdapter extends BaseExpandableListAdapter {
	@SuppressWarnings("unused")
	private Context context;
	private LayoutInflater inflater;
	ArrayList<String> groupList;
	ArrayList<List<String>> childList;
	public MyexpandableListAdapter(Context context,ArrayList<String>groupList,ArrayList<List<String>> childList) {
		this.context = context;
		this.groupList=groupList;
		this.childList=childList;
		inflater = LayoutInflater.from(context);
	}
	// 返回父列表个数
	public int getGroupCount() {
		return groupList.size();
	}
	// 返回子列表个数
	public int getChildrenCount(int groupPosition) {
		return childList.get(groupPosition).size();
	}
	public Object getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}
	public Object getChild(int groupPosition, int childPosition) {
		return childList.get(groupPosition).get(childPosition);
	}
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}
	public boolean hasStableIds() {
		return true;
	}
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder groupHolder = null;
		if (convertView == null) {
			groupHolder = new GroupHolder();
			convertView = inflater.inflate(R.layout.group, null);
			groupHolder.textView = (TextView) convertView
					.findViewById(R.id.group);
			groupHolder.imageView = (ImageView) convertView
					.findViewById(R.id.image);
			groupHolder.textView.setTextSize(15);
			convertView.setTag(groupHolder);
		} else {
			groupHolder = (GroupHolder) convertView.getTag();
		}
		groupHolder.textView.setText(getGroup(groupPosition).toString());
		if (isExpanded)// ture is Expanded or false is not isExpanded
			{
			groupHolder.imageView.setImageResource(R.drawable.expanded);
		    convertView.setBackgroundColor(Color.GRAY);
			}else{
			groupHolder.imageView.setImageResource(R.drawable.collapse);
			convertView.setBackgroundColor(Color.LTGRAY);			
			}
			return convertView;
	}
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item, null);
		}
		TextView textView = (TextView) convertView.findViewById(R.id.item);
		textView.setTextSize(13);
		textView.setText(getChild(groupPosition, childPosition).toString());
		return convertView;
	}
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
class GroupHolder {
	TextView textView;
	ImageView imageView;
}


