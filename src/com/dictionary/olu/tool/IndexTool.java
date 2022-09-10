package com.dictionary.olu.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dictionary.olu.WordItem;

public class IndexTool {

	private static IndexTool instance;
	
	private List<WordItem> list;
	private List<Integer> mIndexList = new ArrayList<Integer>();
	private int mCurIndex = -1;
	private WordItem mCurItem;
	private boolean isRandom = false;
	private Random random = new Random(System.currentTimeMillis());
	
	public static IndexTool getInstance() {
		if (instance == null) {
			instance = new IndexTool();
		}
		return instance;
	}
	
	public IndexTool() {
		
	}
	
	public void setData(List<WordItem> list) {
		this.list = list;
		restart();
	}
	
	public void setRandomPlay(boolean isRandom) {
		this.isRandom = isRandom;
	}
	
	public boolean isFinish() {
		return mIndexList.isEmpty();
	}
	
	public void restart() {
		mCurIndex = -1;
		mIndexList.clear();
		if (list != null && list.size() > 0) {
        	for (int i = 0; i < list.size(); i++) {
				mIndexList.add(i);
			}
		}
	}
	
	public int getCurIndex() {
		return mCurIndex;
	}
	
	public int getCount() {
		return list != null ? list.size() : 0;
	}
	
	public int getLeftCount() {
		return mIndexList.size();
	}
	
	public WordItem getCurWordItem() {
		return mCurItem;
	}
	
	public WordItem getNextWordItem() {
		if (!mIndexList.isEmpty()) {
			if (isRandom) {
				int tmpIndex = random.nextInt(mIndexList.size());
				mCurIndex = mIndexList.get(tmpIndex);
				mIndexList.remove(tmpIndex);
			} else {
				mCurIndex = mIndexList.get(0);
				mIndexList.remove(0);
			}
			return list.get(mCurIndex);
		}
		mCurIndex = -1;
		return null;
	}
	
}
