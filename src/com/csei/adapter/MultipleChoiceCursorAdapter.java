package com.csei.adapter;

import com.csei.inspect.ActionHistoryActivity;
import com.example.viewpager.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class MultipleChoiceCursorAdapter extends SimpleCursorAdapter {
	private int visibility;
	private LayoutInflater inflater;
	private Context mContext;

	public MultipleChoiceCursorAdapter(Context context, int layout,
			Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		inflater = LayoutInflater.from(context);
		visibility=View.INVISIBLE;
		mContext=context;
	}
	
	//itemlayout点击事件
	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.history_item, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.history_item_igv);
			holder.tablename = (TextView) convertView.findViewById(R.id.history_item_tv_tablename);
			holder.devicename = (TextView)convertView.findViewById(R.id.history_item_tv_devicename);
			holder.finishtime = (TextView) convertView.findViewById(R.id.history_item_tv_finishtime);
			holder.upload=(Button)convertView.findViewById(R.id.history_item_btn_finishflag);
			holder.cb = (CheckBox) convertView.findViewById(R.id.history_item_cb);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();	
		}
		holder.upload.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) {
					v.setBackgroundResource(R.drawable.btn_uploadfile_down);
//					((ActionHistoryActivity)mContext).pdDialog.show();
//					new Thread(new UploadFileThread()).start();
				}
				if (event.getAction()==MotionEvent.ACTION_UP) {
					v.setBackgroundResource(R.drawable.btn_uploadfile);
				}
				return false;
			}
		});
		if (visibility==View.INVISIBLE) {
			holder.cb.setVisibility(visibility);
			holder.upload.setVisibility(View.VISIBLE);
		}else {
			holder.cb.setVisibility(visibility);
			holder.upload.setVisibility(View.GONE);
		}
		
		
		return convertView;
	}
	
	public void setVisibility(int visibility){
		this.visibility = visibility;
	}
	
	public static class ViewHolder {  
		ImageView imageView;
		TextView tablename; 
		TextView devicename;
		TextView finishtime;
		Button upload;
		CheckBox cb;  
	}
}


