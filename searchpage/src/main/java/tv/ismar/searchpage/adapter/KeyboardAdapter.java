package tv.ismar.searchpage.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import tv.ismar.searchpage.R;

/**
 * Created by admin on 2015/12/30.
 */
public class KeyboardAdapter extends BaseAdapter {

    private Context mContext;
    private String[] mDatas;

    public KeyboardAdapter(Context context, String[] datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.length;
    }

    @Override
    public Object getItem(int i) {
        return mDatas[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = View.inflate(mContext, R.layout.item_key, null);
        TextView key = (TextView) view.findViewById(R.id.btn_key);
        key.setText(mDatas[i]);
        return view;

    }
}
