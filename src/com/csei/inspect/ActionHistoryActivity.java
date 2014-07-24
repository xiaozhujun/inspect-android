package com.csei.inspect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.csei.database.service.imp.TaskCellServiceDao;
import org.json.JSONException;

import com.cesi.client.CasClient;
import com.csei.entity.Employer;
import com.csei.util.JsonParser;
import com.csei.util.Tools;
import org.whut.inspect.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class ActionHistoryActivity extends Activity {
	private ListView listView;
	private Cursor cursor;
	private ProgressDialog pdDialog;
	private Employer employer;
	private TextView textView;
	private FrameLayout fav_bottom_bar1;
	private MultipleChoiceAdapter mCursorAdapter;
	private Button btnCancelAll;
	private Button btnSelAll;
	private Button btnDelAll;
	private TaskCellServiceDao userServiceDao;
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.activity_history);
		listView=(ListView) findViewById(R.id.actionhistory_lv);
		
		btnCancelAll = (Button) findViewById(R.id.btnCancelAll);
		btnSelAll = (Button) findViewById(R.id.btnSelAll);
		btnDelAll = (Button) findViewById(R.id.btnDelAll);
		
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		ProgressInit();
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
		View emptyView = inflater.inflate(R.layout.history_empty, (ViewGroup)findViewById(R.id.emptyList));
		emptyView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((ViewGroup)listView.getParent()).addView(emptyView); 
		listView.setEmptyView(emptyView);
		fav_bottom_bar1 = (FrameLayout) findViewById(R.id.fav_bottom_bar1);
		textView=(TextView) findViewById(R.id.history_tv_edit);
		textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(textView.getText().toString().equals("编辑")){
					fav_bottom_bar1.setVisibility(View.VISIBLE);
					textView.setText("完成");
					mCursorAdapter.setCb_visibleflag(View.VISIBLE);
					dataChanged();
				}else{
					fav_bottom_bar1.setVisibility(View.GONE);
					textView.setText("编辑");
					mCursorAdapter.setCb_visibleflag(View.GONE);
					dataChanged();
			}
			}
			});
		
		try {
			userServiceDao=new TaskCellServiceDao(getApplicationContext());
			cursor=userServiceDao.QueryHistory1(employer.getName(),Tools.GetCurrentDate());
			} catch (Exception e) {
			e.printStackTrace();
		}
		mCursorAdapter = new MultipleChoiceAdapter(
				ActionHistoryActivity.this , R.layout.history_item, cursor 
				, new String[]{"tablename" , "devicename","finishtime","uploadflag"}
				, new int[]{R.id.history_item_tv_tablename , R.id.history_item_tv_devicename,R.id.history_item_tv_finishtime,R.id.history_item_btn_finishflag});
			//显示数据
		listView.setAdapter(mCursorAdapter);
		
		//设置底部操作
		btnSelAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(int i=0;i<mCursorAdapter.getCount();i++){
					mCursorAdapter.getIsSelected().put(i, true);
				}
				dataChanged();			
			}
		});
		
		btnCancelAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(int i=0;i<mCursorAdapter.getCount();i++){
					mCursorAdapter.getIsSelected().put(i, false);
				}
				dataChanged();
			}
		});
		
		btnDelAll.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				List<Integer> selectedItem = new ArrayList<Integer>();
				for(int i=0;i<cursor.getCount();i++){
					if(mCursorAdapter.getIsSelected().get(i)){
						selectedItem.add(i);
					}
				}
				for(int j=0;j<selectedItem.size();j++){
					cursor.moveToPosition(selectedItem.get(j));
					userServiceDao.DeleteRecord(cursor.getString(cursor.getColumnIndex("_id")));
				}
				cursor=userServiceDao.QueryHistory1(employer.getName(),Tools.GetCurrentDate());
				mCursorAdapter = new MultipleChoiceAdapter(
						ActionHistoryActivity.this , R.layout.history_item, cursor 
						, new String[]{"tablename" , "devicename","finishtime","uploadflag"}
						, new int[]{R.id.history_item_tv_tablename , R.id.history_item_tv_devicename,R.id.history_item_tv_finishtime,R.id.history_item_btn_finishflag});
				listView.setAdapter(mCursorAdapter);
				fav_bottom_bar1.setVisibility(View.GONE);
				textView.setText("编辑");
				mCursorAdapter.setCb_visibleflag(View.GONE);
				Toast.makeText(getApplicationContext(), "删除成功！", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	class UploadFileThread implements Runnable
	{
		int position;
		public UploadFileThread(int position)
		{
			this.position=position;
		}
		@Override
		public void run() {
			try {
				cursor.moveToPosition(position);
				final String msg = CasClient.getInstance().doSendFile2(getResources().getString(R.string.UPLOAD_FILE),cursor.getString(cursor.getColumnIndex("filesavepath")) );
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							if (JsonParser.UploadIsSuccess(msg)) {//上传成功
								//插入数据库操作
								pdDialog.dismiss();
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
							}
							else{//上传失败
								pdDialog.dismiss();
								Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			} 						
		}
	}
	
	private void ProgressInit() {
		pdDialog = new ProgressDialog(this);
		pdDialog.setMax(100);
		pdDialog.setTitle(getResources().getString(R.string.dialog_title));
		pdDialog.setMessage(getResources().getString(R.string.dialog_loadfile));
		pdDialog.setCancelable(false);
		pdDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdDialog.setIndeterminate(false);
	}
	
	
	private void dataChanged() {  
		
		mCursorAdapter.notifyDataSetChanged();  
	}
	
	class MultipleChoiceAdapter extends SimpleCursorAdapter{

		private int cb_visibleflag=View.GONE;
		private HashMap<Integer,Boolean> isSelected;
		private int listSize;
		
		
		public void setCb_visibleflag(int cb_visibleflag) {
			this.cb_visibleflag = cb_visibleflag;
		}

		public MultipleChoiceAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			isSelected = new HashMap<Integer, Boolean>();
			listSize=c.getCount();
			initData();
		}
		
		private void initData(){
			for(int i=0;i<listSize;i++){
				getIsSelected().put(i,false);
			}
		}
		
		public HashMap<Integer, Boolean> getIsSelected() {  
			return isSelected;  
		}
		
		public void getIsSelected(HashMap<Integer,Boolean> isSelected){
			this.isSelected = isSelected;
		}
		
			//itemlayout点击事件
			@Override
			public View getView(final int position, View convertView,
					final ViewGroup parent) {
				convertView =  super.getView(position, convertView, parent);
				
				final Button btn_finishflag = (Button) convertView.findViewById(R.id.history_item_btn_finishflag);
				CheckBox checkBox=(CheckBox) convertView.findViewById(R.id.history_item_cb);
				if (cb_visibleflag==View.GONE) {
					checkBox.setVisibility(cb_visibleflag);
					btn_finishflag.setVisibility(View.VISIBLE);
				}
				else {
					checkBox.setVisibility(cb_visibleflag);
					btn_finishflag.setVisibility(View.GONE);
				}
				if (btn_finishflag.getText().equals("未上传")) {
					btn_finishflag.setBackgroundResource(R.color.myred);
				}
				else btn_finishflag.setBackgroundResource(R.drawable.btn_uploadfile);
				btn_finishflag.setOnTouchListener(new View.OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction()==MotionEvent.ACTION_DOWN) {
							pdDialog.show();
							new Thread(new UploadFileThread(position)).start();
						}
						if (event.getAction()==MotionEvent.ACTION_UP) {
						}
						return false;
					}
		});
		checkBox.setChecked(isSelected.get(position));
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getIsSelected().put(position, isChecked);
			}
		});
		return convertView;
		}
	}
}
