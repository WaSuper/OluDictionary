package com.dictionary.olu;

import java.util.List;

import com.dictionary.olu.tool.DBManager;
import com.dictionary.olu.tool.ExcelUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WordActivity extends Activity implements OnClickListener {
	
	public static final String BookName = "BookName";
	public static final String BookDir = "BookDir";

	private Context mContext;
	
	private ImageView mBackView;
	private TextView mTitleView;
	private ListView mListView;
	private WordAdapter mWordAdapter;
	
	private String title;
	private String dir;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_word);
		mContext = this;
		title = getIntent().getStringExtra(BookName);
		dir = getIntent().getStringExtra(BookDir);
		initUI();
	}
	
	private void initUI() {
		mBackView = (ImageView) findViewById(R.id.word_img_back);
		mTitleView = (TextView) findViewById(R.id.word_tv_title);
		mTitleView.setText(title);
		mListView = (ListView) findViewById(R.id.word_listview);
		List<WordItem> list = null;
		if (dir.endsWith(".db")) {
			DBManager dbManager = new DBManager(dir);
			list = dbManager.queryWord();
			dbManager.closeDB();
		} else if (dir.endsWith(".xls")) {
			list = ExcelUtils.readExcelToWords(dir);
		}		
		mWordAdapter = new WordAdapter(mContext, list);
		mListView.setAdapter(mWordAdapter);
		
		mBackView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.word_img_back:
			finish();
			break;
		default:
			break;
		}
	}
}
