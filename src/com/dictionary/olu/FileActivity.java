package com.dictionary.olu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.dictionary.olu.tool.FileSortModel;
import com.dictionary.olu.tool.FileTools;

public class FileActivity extends Activity implements View.OnClickListener {
	
	public static final String FileType = "FileType";
	public static final String FileDir 	= "FileDir";
	
	public static final int File_All 			= 0x2001;
	public static final int File_db_and_xls		= 0x2002;
	
	private TextView mBackView;
    private ListView mFileView;
    private TextView mTitleView;
    private ImageView mCancelView;
    private FileAdapter mFileAdapter;

    private int mFileLevel = 0;
    private List<File> mRootFiles;
    private File mParentFile = null;
    private int mFirstItemPosition = 0;
    private int mFirstItemFromTop = 0;
    private List<Integer> mPositionList = new LinkedList<Integer>();
    private List<Integer> mFromTopList = new LinkedList<Integer>();
    
    private Context mContext;
    
    private int fileType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
        mContext = this;
        fileType = getIntent().getIntExtra(FileType, File_db_and_xls);
        initUI();
    }

    private void initUI() {
        mBackView = (TextView) findViewById(R.id.file_back);
        mCancelView = (ImageView) findViewById(R.id.file_exit);
        mFileView = (ListView) findViewById(R.id.file_list);
        mTitleView = (TextView) findViewById(R.id.file_title);

        mRootFiles = FileTools.initSDcardList(mContext);
        mFileAdapter = new FileAdapter(mRootFiles);
        mFileView.setAdapter(mFileAdapter);

        mBackView.setOnClickListener(this);
        mCancelView.setOnClickListener(this);
        mFileView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	goToNextLevel(position);
            }
        });
        mFileView.setOnScrollListener(new OnScrollListenerImple());  
                
    }
    
    @Override
    public void onBackPressed() {
    	if (!backToLastLevel()) {
    		setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_exit:
            	setResult(RESULT_CANCELED);
                this.finish();
                break;
            case R.id.file_back:
                backToLastLevel();
                break;
            default:
                break;
        }
    }
        
    private void goToNextLevel(int position) {
    	File file = mFileAdapter.getFileList().get(position);
    	if (file.isDirectory()) {
            mFileAdapter.setData(file);
            mTitleView.setText(file.getPath());
            mFileLevel++;
            mPositionList.add(mFirstItemPosition);
            mFromTopList.add(mFirstItemFromTop);
            mFirstItemPosition = 0;
            mFirstItemFromTop = 0;
        } else {
        	Intent intent = new Intent();
            intent.putExtra(FileDir, file.getAbsolutePath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    
    private boolean backToLastLevel() {
    	if (mFileLevel != 0) {
            if (mFileLevel == 1) {
                mFileAdapter.setRootFiles(mRootFiles);
                mTitleView.setText(getString(R.string.please_select_file));
                mFileLevel--;
                try {
                	mFirstItemPosition = mPositionList.remove(mPositionList.size() - 1);
                    mFirstItemFromTop = mFromTopList.remove(mFromTopList.size() - 1);
				} catch (Exception e) {
					mFirstItemPosition = 0;
					mFirstItemFromTop = 0;
				}                
                mFileView.setSelectionFromTop(mFirstItemPosition, mFirstItemFromTop);
            } else {
                if (mFileAdapter.back()) {
                	mTitleView.setText(mParentFile.getPath());
                	mFileLevel--;
                	try {
                    	mFirstItemPosition = mPositionList.remove(mPositionList.size() - 1);
                        mFirstItemFromTop = mFromTopList.remove(mFromTopList.size() - 1);
    				} catch (Exception e) {
    					mFirstItemPosition = 0;
    					mFirstItemFromTop = 0;
    				} 
                    mFileView.setSelectionFromTop(mFirstItemPosition, mFirstItemFromTop);
                }
            }
            return true;
        } else {
        	return false;
        }
    }

    private class OnScrollListenerImple implements OnScrollListener {
    	
		@Override
		public void onScroll(AbsListView listView, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
		}
       
		@Override
		public void onScrollStateChanged(AbsListView listview, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				mFirstItemPosition = mFileView.getFirstVisiblePosition();
				View itemView = mFileView.getChildAt(0);
				mFirstItemFromTop = (itemView == null) ? 0 : itemView.getTop();
			}
		}
 
	}
    
    class FileAdapter extends BaseAdapter {

        private List<File> mFileList = new ArrayList<File>();
        private boolean[] mSelectList;
        private boolean isSelectAll = false;
        
        private List<File> mTmpFiles = new ArrayList<File>();
        private List<File> mTmpDirs = new ArrayList<File>();

        public FileAdapter(List<File> list) {
            for (File file : list) {
                mFileList.add(file);
            }
            if (mFileList != null && mFileList.size() != 0) {
    			mSelectList = new boolean[mFileList.size()];
    			Arrays.fill(mSelectList, false);
    		} else {
    			mSelectList = null;
    		}
            isSelectAll = false;
        }

        public void setData(File parent) {
            mParentFile = parent;
            File[] list = parent.listFiles();
            mTmpDirs.clear();
            mTmpFiles.clear();
            if (list != null) {
            	for (File file : list) {
                    if (file.isDirectory()) {
                    	if (!file.getName().startsWith(".")) {
                    		mTmpDirs.add(file);
    					}                    
                    } else {
                    	switch (fileType) {
    					case File_All:
    						mTmpFiles.add(file);
    						break;
    					case File_db_and_xls:
    						String ext = FileTools.getExtension(file.getName());
                            if (ext != null && 
                            		(ext.equalsIgnoreCase("db") || ext.equalsIgnoreCase("xls"))) {
                            	mTmpFiles.add(file);
                                break;
                            }
    						break;
    					default:
    						break;
    					}                    
                    }
                }
			}            
            sortData(true);
        }
        
        public void sortData(boolean isReset) {
        	if (isReset) {
				sortByLetter();
				if (mFileList != null && mFileList.size() != 0) {
	    			mSelectList = new boolean[mFileList.size()];
	    			Arrays.fill(mSelectList, false);
	    		} else {
	    			mSelectList = null;
	    		}
				isSelectAll = false;
				notifyDataSetInvalidated();
			}
        }
        
        private void sortByLetter() {
            mFileList.clear();
        	mFileList.addAll(FileSortModel.sortFile(mTmpDirs));
            mFileList.addAll(FileSortModel.sortFile(mTmpFiles));
        }
        
        private void sortByDate() {
        	mFileList.clear();
        	mFileList.addAll(mTmpDirs);
        	mFileList.addAll(mTmpFiles);
        	Collections.sort(mFileList, new Comparator<File>() {

				@Override
				public int compare(File f0, File f1) {
					return (int)(f1.lastModified() - f0.lastModified());
				}
			});
        }

        public void setRootFiles(List<File> list) {
            mParentFile = null;
            mFileList.clear();
            for (File file : list) {
                mFileList.add(file);
            }
            notifyDataSetChanged();
        }

        public boolean back() {
            if (mParentFile != null) {
                File file = mParentFile.getParentFile();
                setData(file);
                return true;
            }
            return false;
        }

        public List<File> getFileList() {
            return mFileList;
        }
        
        /**
    	 * 全选或取消全选
    	 * 
    	 * @return 是否全选状态
    	 */
    	public boolean selectAllorNot() {
    		if (mSelectList != null) {
    			Arrays.fill(mSelectList, !isSelectAll);	
    			isSelectAll = !isSelectAll;
				notifyDataSetChanged();
				return isSelectAll;
    		} else {
    			return false;
    		}		
    	}
        
        @Override
        public int getCount() {
            return mFileList != null ? mFileList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mFileList != null ? mFileList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, null);
                holder = new ViewHolder();
                holder.mImage = (ImageView) convertView.findViewById(R.id.file_image);
                holder.mName = (TextView) convertView.findViewById(R.id.file_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            File file = mFileList.get(position);
            int drawableId = R.drawable.list_ic_folder;
            if (!file.isDirectory()) {
            	drawableId = R.drawable.list_ic_file;
			}       
            holder.mImage.setImageResource(drawableId);
            holder.mName.setText(file.getName());
            return convertView;
        }

        class ViewHolder {
            ImageView mImage;
            TextView mName;
        }

    }

}
