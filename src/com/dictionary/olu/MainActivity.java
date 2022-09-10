package com.dictionary.olu;

import java.util.ArrayList;
import java.util.List;

import com.dictionary.olu.BookAdapter.OnBookItemClickListener;
import com.dictionary.olu.ToastService.MyBinder;
import com.dictionary.olu.tool.ShareprefenceTool;
import com.dictionary.olu.tool.ToastTool;
import com.dictionary.olu.ui.CommonDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends Activity
		implements OnClickListener, OnRequestPermissionsResultCallback, 
				   OnItemClickListener, OnItemLongClickListener {
	
	private static final int Request_Add = 1;
	private static final String BOOKS = "BOOKS";
	
	private Context mContext;
	
	private boolean isPlaying = false;
	
	private ImageView mExitView;
	private ImageView mPlayView;
	private ImageView mSettingView;
	private ImageView mLockView;
	private ImageView mAddView;
	private ListView mListView;
	private BookAdapter mBookAdapter;
	private List<BookItem> mBookList = new ArrayList<BookItem>();
	
	private Gson mGson = new Gson();
	
	private int mSelectPos;
	private Intent mServiceIntent;
	private ToastService.MyBinder binder;
	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			binder = (MyBinder) service;
			binder.done();
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initUI();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (mServiceIntent != null) {
			unbindService(conn);
		}
    }
    
    @Override
    public void onBackPressed() {
    	//方式一：将此任务转向后台
    	moveTaskToBack(false);

    	//方式二：返回手机的主屏幕
//    	  Intent intent = new Intent(Intent.ACTION_MAIN);
//    	  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    	  intent.addCategory(Intent.CATEGORY_HOME);
//    	  startActivity(intent);
    }
    
    private void initUI() {
    	mExitView = (ImageView) findViewById(R.id.main_img_exit);
    	mPlayView = (ImageView) findViewById(R.id.main_img_play);
    	mSettingView = (ImageView) findViewById(R.id.main_img_setting);
    	mLockView = (ImageView) findViewById(R.id.main_img_lock);
    	mAddView = (ImageView) findViewById(R.id.main_img_add);
    	mListView = (ListView) findViewById(R.id.main_listview);
    	mBookAdapter = new BookAdapter(mContext, mBookList);
    	mListView.setAdapter(mBookAdapter);
    	String bookString = ShareprefenceTool.getInstance().getString(BOOKS, mContext, "");
    	if (!bookString.isEmpty()) {
    		mBookList = mGson.fromJson(bookString, 
    				new TypeToken<List<BookItem>>(){}.getType()); 
    		mBookAdapter.setData(mBookList);
		}
    	
    	mExitView.setOnClickListener(this);
    	mPlayView.setOnClickListener(this);
    	mSettingView.setOnClickListener(this);
    	mLockView.setOnClickListener(this);
    	mAddView.setOnClickListener(this);
    	mListView.setOnItemClickListener(this);
    	mListView.setOnItemLongClickListener(this);
    	mBookAdapter.setOnBookItemClickListener(new OnBookItemClickListener() {
			
			@Override
			public void onPlay(int position) {
				mSelectPos = position;
				if (Build.VERSION.SDK_INT >= 23) {
					if (!Settings.canDrawOverlays(mContext)) {
		                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
		                		Uri.parse("package:" + MainActivity.this.getPackageName()));
		                startActivity(intent);
		                return;
		            } else {
		            	showToast();
		            }
//					requestPower(Manifest.permission.SYSTEM_ALERT_WINDOW);
				} else {
					showToast();
				}
			}
		});
    }
    
    private void showToast() {
    	isPlaying = true;
    	mPlayView.setImageResource(R.drawable.pause);
    	if (mServiceIntent == null) {
    		mServiceIntent = new Intent(this, ToastService.class);
		} else {
			unbindService(conn);
		}
        BookItem item = mBookList.get(mSelectPos);
    	mServiceIntent.putExtra(ToastService.BookDir, item.direction);
    	bindService(mServiceIntent, conn, Service.BIND_AUTO_CREATE);
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		BookItem item = mBookList.get(position);
		Intent intent = new Intent(MainActivity.this, WordActivity.class);
		intent.putExtra(WordActivity.BookName, item.name);
		intent.putExtra(WordActivity.BookDir, item.direction);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		showDeleteBookDialog(position);
		return true;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.main_img_exit:
			if (mServiceIntent != null) {
				unbindService(conn);
				mServiceIntent = null;
			} 
			finish();		
			break;
		case R.id.main_img_play:
			if (isPlaying) {
				sendBroadcast(true);
				mPlayView.setImageResource(R.drawable.play);
				isPlaying = false;
			} else {
				if (mServiceIntent != null) {
					sendBroadcast(false);
					mPlayView.setImageResource(R.drawable.pause);
					isPlaying = true;
				}
			}
			break;
		case R.id.main_img_setting:
			startActivity(new Intent(MainActivity.this, SettingActivity.class));
			break;
		case R.id.main_img_add:
			if (Build.VERSION.SDK_INT >= 23) {
				requestPower(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			} else {
				goToFileActivity();
			}
			break;
		case R.id.main_img_lock:
			startActivity(new Intent(MainActivity.this, LockActivity.class));
			break;
		default:
			break;
		}
	}
	
	private void goToFileActivity() {
		startActivityForResult(new Intent(MainActivity.this, FileActivity.class), 
				Request_Add);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case Request_Add:
				String dir = data.getStringExtra(FileActivity.FileDir);
				showAddBookDialog(dir);
				break;
			default:
				break;
			}
		}
	}
	
	private void saveBooks() {
		String jsonString = mGson.toJson(mBookList);
		ShareprefenceTool.getInstance().setString(BOOKS, jsonString, mContext);
	}

	private void showAddBookDialog(final String dir) {
		final CommonDialog dialog = new CommonDialog(this, R.style.MyDialogStyle);		
		dialog.addView(R.layout.layout_addbook);
		dialog.titleTextView.setText(R.string.add_book);
		final EditText nameView = (EditText) dialog.getContentView().findViewById(R.id.addbook_edit_name);
		TextView dirView = (TextView) dialog.getContentView().findViewById(R.id.addbook_tv_dir);
		dirView.setText(dir);
		dialog.ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String name = nameView.getText().toString().trim();
				if (name.isEmpty()) {
					ToastTool.showToast(mContext, R.string.please_input_bookname);
					return;
				}
				BookItem item = new BookItem(name, dir);
				mBookList.add(item);
				mBookAdapter.notifyDataSetChanged();
				saveBooks();
				dialog.dismiss();
			}
		});
		dialog.cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	private void showDeleteBookDialog(final int position) {
		final CommonDialog dialog = new CommonDialog(this, R.style.MyDialogStyle);		
		dialog.addView(R.layout.item_text);
		dialog.titleTextView.setText(R.string.tip);
		TextView content = (TextView) dialog.getContentView().findViewById(R.id.item_text);
		content.setText(getString(R.string.ensure_delete_book, mBookList.get(position).name));
		dialog.ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mBookList.remove(position);
				mBookAdapter.notifyDataSetChanged();
				saveBooks();
				dialog.dismiss();
			}
		});
		dialog.cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	private boolean requestPower(String permission) {
		// checkSelfPermission 判断是否已经申请了此权限
		if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			// 如果应用之前请求过此权限但用户拒绝了请求，shouldShowRequestPermissionRationale将返回true。
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
				showFailDialog();
			} else {
				ActivityCompat.requestPermissions(this,
						new String[] { permission }, 1);
			}
			return false;
		} else {
			if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				goToFileActivity();
			} else if (permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
				showToast();
			}
			return true;
		}
	}

	private void showFailDialog() {
		final CommonDialog dialog = new CommonDialog(this, R.style.MyDialogStyle, 0);		
		dialog.addView(R.layout.item_text);
		dialog.titleTextView.setText(R.string.tip);
		TextView content = (TextView) dialog.getContentView().findViewById(R.id.item_text);
		content.setText(R.string.request_permission_fail);
		dialog.ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults.length == 1 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (permissions.length == 1) {
					if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						goToFileActivity();
					} else if (permissions[0].equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
						showToast();
					}
				}
	        } else {
	            showFailDialog();
	        }
		}
	}

	private void sendBroadcast(boolean isPlay) {
		Intent intent=new Intent(ToastService.Action);
		intent.putExtra(ToastService.BroadCaseType, ToastService.PlayPause);
		intent.putExtra(ToastService.PlayPause, isPlay);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}
}
