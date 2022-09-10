package com.dictionary.olu;

import java.util.List;
import com.dictionary.olu.tool.DBManager;
import com.dictionary.olu.tool.ExcelUtils;
import com.dictionary.olu.tool.IndexTool;
import com.dictionary.olu.tool.ShareprefenceTool;
import com.dictionary.olu.tool.ToastTool;
import com.dictionary.olu.ui.AttachLayoutWindow;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.TextView;

public class ToastService extends Service {

	public static final String BookDir = "BookDir";
	public static final String Action = "com.dictionary.olu.LOCAL_BROADCAST";
	public static final String BroadCaseType = "BroadCaseType";
	public static final String LockView = "LockView";
	public static final String PlayPause = "PlayPause";
	
	/**
     * Toast的规则
     */
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    /**
     * WindowManager管理者对象
     */
    private WindowManager mWM;
    /**
     * Toast上的View
     */
    private View mToastView;
    private TextView mLine1Text;
    private TextView mLine2Text;
    private TextView mLine3Text;
    
    private MyBinder binder = new MyBinder();
    private String bookDir;
    private int periodSecond;
    private boolean isRandomPlay;
    private boolean isStopPlay;
    private boolean isGoEdge;
    private boolean isGoogle;
    private boolean isNotifyNew;
    private Handler mHandler = new Handler();
    private boolean isRunning = false;
    private IndexTool mIndexTool = IndexTool.getInstance();
    
    //通知栏
    private int nfID = 1111;
    private NotificationManager nfManager;
    private Notification myNotification;
    private RemoteViews remoteViews;
    private Builder mBuilder;
//    private PendingIntent pendingIntent;
    
    private boolean canSearch;
    private Intent searchWordIntent;
    
    private LocalBroadcastManager localBroadcastManager;
    private MyReceiver localReceiver = new MyReceiver();
    
    @Override
    public void onCreate() {
    	mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
    	super.onCreate();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	startForeground(nfID, new Notification());
    	return super.onStartCommand(intent, flags, startId);
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		bookDir = intent.getStringExtra(BookDir);
		List<WordItem> list = null;
		if (bookDir.endsWith(".db")) {
			DBManager dbManager = new DBManager(bookDir);
			list = dbManager.queryWord();
			dbManager.closeDB();
		} else if (bookDir.endsWith(".xls")) {
			list = ExcelUtils.readExcelToWords(bookDir);
		}
		initData();
		mIndexTool.setData(list);
		mIndexTool.setRandomPlay(isRandomPlay);
    	// 通知栏
    	createNotification();
    	
    	registerReciver();
    	
		return binder;
	}
	
	private void registerReciver() {
		localBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Action);
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
	}
	
	private void initData() {
		isRunning = true;
		// 用于查询单词，跳转到欧路词典
		searchWordIntent = new Intent();
		searchWordIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		searchWordIntent.setAction("colordict.intent.action.SEARCH");
		loadDefaultData();
		canSearch = false;
	}
	
	private void loadDefaultData() {
		periodSecond = ShareprefenceTool.getInstance().getInt(SettingActivity.TIME, this, 30);
		isRandomPlay = ShareprefenceTool.getInstance().getBoolean(SettingActivity.RANDOM, this, false);
		isStopPlay = ShareprefenceTool.getInstance().getBoolean(SettingActivity.STOP, this, false);
		isGoEdge = ShareprefenceTool.getInstance().getBoolean(SettingActivity.GOEDGE, this, false);
		isGoogle = ShareprefenceTool.getInstance().getBoolean(SettingActivity.OULU_GOOGLE, this, false);
		isNotifyNew = ShareprefenceTool.getInstance().getBoolean(SettingActivity.NOTIFY_NEW, this, false);

		ComponentName name;
		if (isGoogle) {
			name = new ComponentName("com.qianyan.eudic",
					"com.eusoft.dict.activity.dict.LightpeekActivity");
		} else {
			name = new ComponentName("com.eusoft.eudic",
					"com.eusoft.dict.activity.dict.LightpeekActivity");
		}
		searchWordIntent.setComponent(name);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		isRunning = false;
		canSearch = false;
		removeToast();
		removeNotification();
		unregisterReceiver();
		return true;
	}
	
	private void unregisterReceiver() {
		localBroadcastManager.unregisterReceiver(localReceiver);
	}
	
	public void showToast() {
        if (mToastView == null) {
            //宽高
            final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;

            params.format = PixelFormat.TRANSLUCENT;
            //在响铃的时候显示吐司,和电话类型一致
            if (Build.VERSION.SDK_INT >= 26) {
            	params.type = 2038; // WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
            	params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE   默认是不可以触摸的
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH 
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;;

            //指定Toast所在位置
            params.gravity = Gravity.LEFT + Gravity.TOP;
            params.x = 0;
            params.y = 100;

            //吐司显示效果(吐司布局文件) ,xml->view(吐司),将吐司挂在windowManager窗体上
        	mToastView = View.inflate(this, R.layout.toast_view, null);
        	AttachLayoutWindow window = (AttachLayoutWindow) mToastView.findViewById(R.id.toast_ll_parent);
        	window.setIsNeedGoEdge(isGoEdge);
        	window.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						if (canSearch) {
							startActivity(searchWordIntent);
						}
					} catch (Exception e) {
						e.printStackTrace();
						ToastTool.showToast(ToastService.this, R.string.please_install_app);
					}
				}
			});
        	mLine1Text = (TextView) mToastView.findViewById(R.id.toast_line1);
        	mLine2Text = (TextView) mToastView.findViewById(R.id.toast_line2);
        	mLine3Text = (TextView) mToastView.findViewById(R.id.toast_line3);
        	mWM.addView(mToastView, mParams);
		} 
        if (mIndexTool.getCount() > 0) {
        	refreshToast();
		} else {
			mWM.removeView(mToastView);
			mToastView = null;
		}
    }
	
	private void refreshToast() {
		if (!isRunning) return;
		loadDefaultData();
		AttachLayoutWindow window = (AttachLayoutWindow) mToastView
				.findViewById(R.id.toast_ll_parent);
    	window.setIsNeedGoEdge(isGoEdge);
		if (!mIndexTool.isFinish()) {
			WordItem item = mIndexTool.getNextWordItem();
			mLine1Text.setText(item.word + "\t\t" + item.type);
			mLine2Text.setText(item.accent);
			mLine3Text.setText(item.mean);
//			if (Build.VERSION.SDK_INT >= 26) {
				if (isNotifyNew) removeNotification();
				createNewNotification(
						item.word + "\t\t" + item.accent + "\t\t" + item.type, 
						item.mean);
//			} else {
//				remoteViews.setTextViewText(R.id.notify_line1, 
//						item.word + "\t\t" + item.accent + "\t\t" + item.type);
//				remoteViews.setTextViewText(R.id.notify_line2, item.mean);
//			}			
			nfManager.notify(nfID, myNotification);
			searchWordIntent.putExtra("EXTRA_QUERY", item.word);
			canSearch = true;
			mHandler.postDelayed(mToastRunnable, periodSecond * 1000);
		} else {
			if (isStopPlay) {
				mWM.removeView(mToastView);
				mToastView = null;
			} else {
				mIndexTool.restart();
				mLine1Text.setText(getString(R.string.this_round_play_end));
				mLine2Text.setText(getString(R.string.begin_new_play_round));
				mLine3Text.setText("");
//				if (Build.VERSION.SDK_INT >= 26) {
					if (isNotifyNew) removeNotification();
					createNewNotification(getString(R.string.this_round_play_end), 
							getString(R.string.begin_new_play_round));
//				} else {
//					remoteViews.setTextViewText(R.id.notify_line1, 
//							getString(R.string.this_round_play_end));
//					remoteViews.setTextViewText(R.id.notify_line2, 
//							getString(R.string.begin_new_play_round));
//				}
				nfManager.notify(nfID, myNotification);
				canSearch = false;
				mHandler.postDelayed(mToastRunnable, 3000);
			}
		}
	}

	private Runnable mToastRunnable = new Runnable() {
		
		@Override
		public void run() {
			refreshToast();
		}
	};
	
	private void removeToast() {
		mHandler.removeCallbacks(mToastRunnable);
		if (mToastView != null) {
			mWM.removeView(mToastView);
			mToastView = null;
		}
	}
	
	public class MyBinder extends Binder {
		
        public void done() {
        	showToast();
        }
    }
	
	private void createNotification() {
		// 获得通知管理器
		nfManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		remoteViews = new RemoteViews(getPackageName(),R.layout.layout_notification);
		// 设置通知点击的动作
//		Intent intent = new Intent(this, MainActivity.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// 一个未来(预先)的动作,,用于点击或清除通知时的动作，
//		pendingIntent = PendingIntent.getActivity(this, 0, 
//				searchWordIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		//设置Channel ID和Name以及 importance
		if (Build.VERSION.SDK_INT >= 26) {
			String channelId = "oluDict";
	        String channelName = "oluDictionary";
	        int channelImportance = NotificationManager.IMPORTANCE_HIGH;
	        //构建Channel 通知通道
	        NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
	        //是否绕过请勿打扰模式
	        //channel.canBypassDnd();
	        //锁屏显示通知
	        channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
	        //闪光灯
	        channel.enableLights(false);
	        //闪关灯的灯光颜色
//	        channel.setLightColor(Color.WHITE);
	        //桌面launcher的消息角标
	        channel.setShowBadge(false);
	        //是否允许震动
	        channel.enableVibration(false);
	        //获取系统通知响铃声音的配置
	        channel.setSound(null, null);
	        //channel.getAudioAttributes();
	        //获取通知取到组
	        //channel.getGroup();
	        //设置可绕过  请勿打扰模式
	        //channel.setBypassDnd(true);
	        //设置震动模式
	        //channel.setVibrationPattern(new long[]{100, 100, 200});
	        //是否会有灯光
	        //channel.shouldShowLights();
	        nfManager.createNotificationChannel(channel);
	        mBuilder = new Notification.Builder(this, channelId)
		        .setWhen(System.currentTimeMillis())
		        .setSmallIcon(R.drawable.smallicon_gray)
//		        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
//	        	.setContentIntent(pendingIntent)
//	        	.setCustomContentView(remoteViews)
//	        	.setCustomBigContentView(remoteViews)
		        .setAutoCancel(true)
		        .setOnlyAlertOnce(true);
	        myNotification = mBuilder.build();
	        createNewNotification("欢迎使用欧拉词典", "");
		} else {
			myNotification = new Notification();
//			myNotification.tickerText = "欢迎使用欧拉词典";
			myNotification.when = System.currentTimeMillis();
			myNotification.icon = R.drawable.smallicon_gray;
//			myNotification.contentIntent = pendingIntent;
			myNotification.contentView = remoteViews;
			myNotification.flags = Notification.FLAG_AUTO_CANCEL;
			myNotification.priority = Notification.PRIORITY_MAX;
		}
		nfManager.notify(nfID, myNotification);
    }
	
	private void createNewNotification(String title, String content) {
    	myNotification = mBuilder
    		.setContentTitle(title)
    		.setContentText(content)
    		.setStyle(new Notification.BigTextStyle().bigText(content))
    		.build();
	}
	
	private void removeNotification() {
		if (nfManager != null) {
			nfManager.cancel(nfID);
		}
	}
	
	class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String type = intent.getStringExtra(BroadCaseType);
				if (type.equals(LockView)) {
					boolean isShowLockView = intent.getBooleanExtra(LockView, false);
					if (isShowLockView) {
						removeToast();
						removeNotification();
					} else {
						showToast();
					}
				} else if (type.equals(PlayPause)) {
					boolean isPlaying = intent.getBooleanExtra(PlayPause, false);
					if (isPlaying) {
						mHandler.removeCallbacks(mToastRunnable);
					} else {
						showToast();
					}
				}
			}
		}
		
	}
	
}
