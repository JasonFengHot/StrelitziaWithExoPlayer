package tv.ismar.searchpage.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tv.ismar.searchpage.R;
import tv.ismar.searchpage.model.NineTKey;

/** Created by admin on 2015/12/30. */
public class T9KeyboardAdapter extends BaseAdapter {

    private Context mContext;
    private List<NineTKey> mDatas;

    public T9KeyboardAdapter(Context context, List<NineTKey> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        view = View.inflate(mContext, R.layout.item_t9_key, null);
        TextView T9_key_num = (TextView) view.findViewById(R.id.tv_num);
        TextView T9_key_letter = (TextView) view.findViewById(R.id.tv_letter);
        T9_key_num.setText(mDatas.get(i).num);
        T9_key_letter.setText(mDatas.get(i).letter);
        return view;
    }
}
