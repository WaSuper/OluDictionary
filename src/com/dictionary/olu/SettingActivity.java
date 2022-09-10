package com.dictionary.olu;

import com.dictionary.olu.tool.ShareprefenceTool;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;


public class SettingActivity extends Activity
		implements OnClickListener, OnCheckedChangeListener {

	public static final String TIME = "TIME";
	public static final String RANDOM = "RANDOM";
	public static final String STOP = "STOP";
	public static final String GOEDGE = "GOEDGE";
	public static final String OULU_GOOGLE = "OULU_GOOGLE";
	public static final String NOTIFY_NEW = "NOTIFY_NEW";
	
	private EditText mTimeText;
	private CheckBox mRandomPlayBox;
	private CheckBox mStopPlayBox;
	private CheckBox mGoEdgeBox;
	private CheckBox mGoogleBox;
	private CheckBox mNotifyBox;
	private ImageView mBackView;
	
	private Context mContext;
	private int mTime;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mContext = this;
        initUI();
        initData();
    }

    private void initUI() {
    	mTimeText = (EditText) findViewById(R.id.setting_edit_time);
    	mRandomPlayBox = (CheckBox) findViewById(R.id.setting_cb_play_random);
    	mStopPlayBox = (CheckBox) findViewById(R.id.setting_cb_play_stop);
    	mGoEdgeBox = (CheckBox) findViewById(R.id.setting_cb_wm_go_edge);
    	mGoogleBox = (CheckBox) findViewById(R.id.setting_cb_ouludict_google);
    	mNotifyBox = (CheckBox) findViewById(R.id.setting_cb_show_notify_new);
    	mBackView = (ImageView) findViewById(R.id.setting_img_back);
    	
    	mBackView.setOnClickListener(this);
    	mRandomPlayBox.setOnCheckedChangeListener(this);
    	mStopPlayBox.setOnCheckedChangeListener(this);
    	mGoEdgeBox.setOnCheckedChangeListener(this);
    	mGoogleBox.setOnCheckedChangeListener(this);
    	mNotifyBox.setOnCheckedChangeListener(this);
    	mTimeText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				String text = s.toString().trim();
				if (text.isEmpty()) {
					mTime = 0;
				} else {
					Integer time = Integer.parseInt(text);
					if (time != null && time > 0) {
						mTime = time;
					} else {
						mTime = 0;
					}
				}
				ShareprefenceTool.getInstance().setInt(TIME, mTime, mContext);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
    }

    private void initData() {
    	mTime = ShareprefenceTool.getInstance().getInt(TIME, mContext, 30);
    	mTimeText.setText(mTime + "");
    	boolean mRandomPlay = ShareprefenceTool.getInstance().getBoolean(RANDOM, mContext, false);
    	mRandomPlayBox.setChecked(mRandomPlay);
    	boolean mStopPlay = ShareprefenceTool.getInstance().getBoolean(STOP, mContext, false);
    	mStopPlayBox.setChecked(mStopPlay);
    	boolean mGoEdge = ShareprefenceTool.getInstance().getBoolean(GOEDGE, mContext, false);
    	mGoEdgeBox.setChecked(mGoEdge);
    	boolean mGoogle = ShareprefenceTool.getInstance().getBoolean(OULU_GOOGLE, mContext, false);
    	mGoogleBox.setChecked(mGoogle);
    	boolean mNotify = ShareprefenceTool.getInstance().getBoolean(NOTIFY_NEW, mContext, false);
    	mNotifyBox.setChecked(mNotify);
    }
    
	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		switch (view.getId()) {
		case R.id.setting_cb_play_random:
			ShareprefenceTool.getInstance().setBoolean(RANDOM, isChecked, mContext);
			break;
		case R.id.setting_cb_play_stop:
			ShareprefenceTool.getInstance().setBoolean(STOP, isChecked, mContext);
			break;
		case R.id.setting_cb_wm_go_edge:
			ShareprefenceTool.getInstance().setBoolean(GOEDGE, isChecked, mContext);
			break;
		case R.id.setting_cb_ouludict_google:
			ShareprefenceTool.getInstance().setBoolean(OULU_GOOGLE, isChecked, mContext);
			break;
		case R.id.setting_cb_show_notify_new:
			ShareprefenceTool.getInstance().setBoolean(NOTIFY_NEW, isChecked, mContext);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.setting_img_back:
			finish();
			break;
		default:
			break;
		}
	}

}
