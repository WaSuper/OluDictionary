package com.dictionary.olu;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class WordAdapter extends BaseAdapter {

	private Context mContext;
	private List<WordItem> mWordList;
	
	public WordAdapter(Context context, List<WordItem> list) {
		this.mContext = context;
		this.mWordList = list;
	}
	
	public void setData(List<WordItem> list) {
		this.mWordList = list;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mWordList != null ? mWordList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mWordList != null ? mWordList.get(position) : 0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_word, parent, false);
			holder.mTextView1 = (TextView) convertView.findViewById(R.id.item_word_1);
			holder.mTextView2 = (TextView) convertView.findViewById(R.id.item_word_2);
			holder.mTextView3 = (TextView) convertView.findViewById(R.id.item_word_3);
			holder.mTextView4 = (TextView) convertView.findViewById(R.id.item_word_4);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		WordItem item = mWordList.get(position);
		holder.mTextView1.setText(item.word);
		holder.mTextView2.setText(item.accent);
		holder.mTextView3.setText(item.type);
		holder.mTextView4.setText(item.mean);
		return convertView;
	}
	
	class ViewHolder {
		TextView mTextView1;
		TextView mTextView2;
		TextView mTextView3;
		TextView mTextView4;
	}

}
