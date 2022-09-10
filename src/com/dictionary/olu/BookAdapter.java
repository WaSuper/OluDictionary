package com.dictionary.olu;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BookAdapter extends BaseAdapter {

	private Context mContext;
	private List<BookItem> mBookList;
	
	private OnBookItemClickListener mListener;
	
	public BookAdapter(Context context, List<BookItem> list) {
		this.mContext = context;
		this.mBookList = list;
	}
	
	public void setData(List<BookItem> list) {
		this.mBookList = list;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mBookList != null ? mBookList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mBookList != null ? mBookList.get(position) : 0;
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
					R.layout.item_dictionary, parent, false);
			holder.mNameView = (TextView) convertView.findViewById(R.id.item_dict_tv_name);
			holder.mDirView = (TextView) convertView.findViewById(R.id.item_dict_tv_dir);
			holder.mPlayView = (ImageView) convertView.findViewById(R.id.item_dict_img_play_pause);
			holder.mPlayView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						int pos = (Integer) v.getTag();
						if (mListener != null) {
							mListener.onPlay(pos);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		BookItem item = mBookList.get(position);
		holder.mNameView.setText(item.name);
		holder.mDirView.setText(item.direction);
		holder.mPlayView.setImageResource(item.isPlaying ? 
				R.drawable.ic_pause : R.drawable.ic_play);
		holder.mPlayView.setTag(position);
		return convertView;
	}
	
	class ViewHolder {
		TextView mNameView;
		TextView mDirView;
		ImageView mPlayView;
	}
	
	public interface OnBookItemClickListener {
		void onPlay(int position);
	}
	
	public void setOnBookItemClickListener(OnBookItemClickListener listener) {
		mListener = listener;
	}

}
