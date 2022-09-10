package com.dictionary.olu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.dictionary.olu.tool.IndexTool;
import com.dictionary.olu.tool.LightTool;
import com.dictionary.olu.tool.ShareprefenceTool;
import com.dictionary.olu.tool.ToastTool;
import com.dictionary.olu.tool.Util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class LockActivity extends Activity implements OnClickListener {

	private Context mContext;
	
	private SimpleDateFormat mWeekDayFormat = new SimpleDateFormat("EEEE", Locale.CHINA);
	private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
	
	private TextView mWeekDayText;
	private TextView mTimeText;
	private TextView mDateText;
	private TextView mCountText;
	private TextView mLine1Text;
	private TextView mLine2Text;
	private TextView mLine3Text;
	private ImageView mPlayView;
	private ImageView mExitView;
	
	private boolean canSearch = false;
    private Intent searchWordIntent;
    private int periodSecond;
    private boolean isRandomPlay;
    private boolean isStopPlay;
    private boolean isGoogle;
    private Handler mHandler = new Handler();
    private boolean isRunning = false;
    private IndexTool mIndexTool = IndexTool.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.translucentStatusbar(this);
		setContentView(R.layout.activity_lock);
		fullScreen();
		setStatusBarHeight();
		mContext = this;
		LightTool lightTool = new LightTool(this);
		initUI();
		initData();
		refreshTime();
		sendBroadcast(true);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		sendBroadcast(false);
		mHandler.removeCallbacks(mTimeRunnable);
		mHandler.removeCallbacks(mViewRunnable);
	}
	
	private void fullScreen() {
		// 全屏展示
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		        // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
		        getWindow().getDecorView().setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
		                        | View.SYSTEM_UI_FLAG_FULLSCREEN
		                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		    } else {
		        // 全屏显示，隐藏状态栏
		        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		    }
		}
	}

	private void sendBroadcast(boolean isShow) {
		Intent intent=new Intent(ToastService.Action);
		intent.putExtra(ToastService.BroadCaseType, ToastService.LockView);
		intent.putExtra(ToastService.LockView, isShow);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}
	
	private void setStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int height = getResources().getDimensionPixelSize(resourceId);
            View statusBar = findViewById(R.id.statusbar_view);
            ViewGroup.LayoutParams layoutParams = statusBar.getLayoutParams();
            layoutParams.height = height;
        }
    }
	
	private void initUI() {
		mWeekDayText = (TextView) findViewById(R.id.lock_tv_weekday);
		mTimeText = (TextView) findViewById(R.id.lock_tv_time);
		mDateText = (TextView) findViewById(R.id.lock_tv_date);
		mCountText = (TextView) findViewById(R.id.lock_tv_count);
		mLine1Text = (TextView) findViewById(R.id.lock_tv_line1);
		mLine2Text = (TextView) findViewById(R.id.lock_tv_line2);
		mLine3Text = (TextView) findViewById(R.id.lock_tv_line3);
		mPlayView = (ImageView) findViewById(R.id.lock_img_play);
		mExitView = (ImageView) findViewById(R.id.lock_img_exit);
		
		mLine1Text.setOnClickListener(this);
		mLine2Text.setOnClickListener(this);
		mLine3Text.setOnClickListener(this);
		mPlayView.setOnClickListener(this);
		mExitView.setOnClickListener(this);
	}
	
	private void initData() {
		periodSecond = ShareprefenceTool.getInstance().getInt(SettingActivity.TIME, mContext, 30);
		isRandomPlay = ShareprefenceTool.getInstance().getBoolean(SettingActivity.RANDOM, mContext, false);
		isStopPlay = ShareprefenceTool.getInstance().getBoolean(SettingActivity.STOP, mContext, false);
		isGoogle = ShareprefenceTool.getInstance().getBoolean(SettingActivity.OULU_GOOGLE, mContext, false);
		canSearch = false;
		// 用于查询单词，跳转到欧路词典
		searchWordIntent = new Intent();
		searchWordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		searchWordIntent.setAction("colordict.intent.action.SEARCH");
		ComponentName name;
		if (isGoogle) {
			name = new ComponentName("com.qianyan.eudic",
					"com.eusoft.dict.activity.dict.LightpeekActivity");
		} else {
			name = new ComponentName("com.eusoft.eudic",
					"com.eusoft.dict.activity.dict.LightpeekActivity");
		}
		searchWordIntent.setComponent(name);
		
		mIndexTool.setRandomPlay(isRandomPlay);
		if (mIndexTool.getCount() > 0) {
			refreshView();
		} else {
			mCountText.setText(getString(R.string.word_count_string, 
					0, 0, 0));
			mLine1Text.setText(getString(R.string.nothing_play));
			mLine2Text.setText("");
			mLine3Text.setText("");
		}
	}
	
	private void refreshView() {
		isRunning = true;
		if (!mIndexTool.isFinish()) {
			WordItem item = mIndexTool.getNextWordItem();
			mCountText.setText(getString(R.string.word_count_string, 
					mIndexTool.getCurIndex() + 1, mIndexTool.getCount(), mIndexTool.getLeftCount()));
			mLine1Text.setText(item.word + "\t\t" + item.type);
			mLine2Text.setText(item.accent);
			mLine3Text.setText(item.mean);
			searchWordIntent.putExtra("EXTRA_QUERY", item.word);
			canSearch = true;
			mHandler.postDelayed(mViewRunnable, periodSecond * 1000);
		} else {
			canSearch = false;
			mCountText.setText(getString(R.string.word_count_string, 
					0, mIndexTool.getCount(), 0));
			if (isStopPlay) {
				mLine1Text.setText(getString(R.string.play_finish));
				mLine2Text.setText("");
				mLine3Text.setText("");
				isRunning = false;
			} else {
				mIndexTool.restart();
				mLine1Text.setText(getString(R.string.this_round_play_end));
				mLine2Text.setText(getString(R.string.begin_new_play_round));
				mLine3Text.setText("");
				mHandler.postDelayed(mViewRunnable, 3000);
			}
		}
	}
	
	private Runnable mViewRunnable = new Runnable() {
		
		@Override
		public void run() {
			refreshView();
		}
	};
	
	private void refreshTime() {
		long time = System.currentTimeMillis();
		mWeekDayText.setText(mWeekDayFormat.format(new Date(time)));
		mTimeText.setText(mTimeFormat.format(new Date(time)));
		mDateText.setText(mDateFormat.format(new Date(time)));
		mHandler.postDelayed(mTimeRunnable, 1000);
	}
	
	private Runnable mTimeRunnable = new Runnable() {
		
		@Override
		public void run() {
			refreshTime();
		}
	};
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.lock_tv_line1:
		case R.id.lock_tv_line2:
		case R.id.lock_tv_line3:
			try {
				if (canSearch) {
					startActivity(searchWordIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
				ToastTool.showToast(LockActivity.this, R.string.please_install_app);
			}
			break;
		case R.id.lock_img_play:
			if (isRunning) {
				mPlayView.setImageResource(R.drawable.play);
				mHandler.removeCallbacks(mViewRunnable);
				isRunning = false;
			} else {
				if (mIndexTool.isFinish() && isStopPlay) {
					return;
				}
				mPlayView.setImageResource(R.drawable.pause);
				refreshView();
				isRunning = true;
			}
			break;
		case R.id.lock_img_exit:
			finish();
			break;
		default:
			break;
		}
	}
}
