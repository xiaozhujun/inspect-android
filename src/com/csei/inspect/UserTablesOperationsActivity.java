package com.csei.inspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.cesi.analysexml.DbModel;
import com.cesi.analysexml.ParseXml;
import com.csei.entity.Employer;
import com.example.viewpager.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class UserTablesOperationsActivity extends Activity {
	private ProgressDialog pdDialog;
	private Employer employer;
	private String fileDir = Environment.getExternalStorageDirectory()
			.toString();
	private String[] result;
	private ListView rolestablelist;
	private int count;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usertablesoperation);
		rolestablelist = (ListView) findViewById(R.id.activity_usertables_lv);

		// 加载文件对话框初始化
		ProgressInit();

		// 处理数据
		handledata();

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


	private void handledata() {
		employer = (Employer) getIntent().getExtras().getParcelable("employer");
		setEmployer(employer);

	}

	private void setEmployer(final Employer employer) {
		// ��ȡRolesTable.xml
		ParseXml p = new ParseXml();
		String filename = fileDir + "/RolesTable.xml";
		final List<DbModel> list = p.parseRolesTable(filename, Integer.parseInt(employer.getRoleNum()));
		pdDialog.setMax(list.size());
		pdDialog.show();
		new Thread(new Runnable() {
			@Override
			public void run() {
				result = new String[list.size()];
				count = 0;
				Iterator it = list.iterator();
				while (it.hasNext()) {
					DbModel d = (DbModel) it.next();
					result[count++] = d.getTableitem();
				}
				
				final ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
				for (count = 0; count < result.length; count++) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("ItemImage", R.drawable.item);
					map.put("ItemText", result[count]);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							pdDialog.setProgress(count);
						}
					});
					listItem.add(map);
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pdDialog.dismiss();
						SimpleAdapter listItemAdapter = new SimpleAdapter(
								UserTablesOperationsActivity.this, listItem,
								R.layout.rolestable, new String[] { "ItemImage", "ItemText" },
								new int[] { R.id.ItemImage, R.id.ItemText });
						rolestablelist.setAdapter(listItemAdapter);
						rolestablelist.setOnItemClickListener(new OnItemClickListener() {
							@SuppressWarnings({ "unchecked" })
							public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
								view.setSelected(true);
								ListView listview = (ListView) parent;
								HashMap<String, String> map = (HashMap<String, String>) listview
										.getItemAtPosition(position);
								String tbname = map.get("ItemText");
								Intent intent = new Intent(UserTablesOperationsActivity.this,
										TagValidateActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("tbname", tbname);
								bundle.putInt("count", 1);
								bundle.putString("username", employer.getName());
								bundle.putInt("uid", Integer.parseInt(employer.getNumber()));
								intent.putExtras(bundle);
								startActivity(intent);
							}
						});
					}
				});
			}
		}).start();
	}
}
