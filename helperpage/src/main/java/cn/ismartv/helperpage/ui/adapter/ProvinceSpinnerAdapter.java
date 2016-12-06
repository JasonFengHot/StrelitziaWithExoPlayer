package cn.ismartv.helperpage.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import tv.ismar.app.db.location.ProvinceTable;


/**
 * Created by huaijie on 8/4/15.
 */
public class ProvinceSpinnerAdapter extends BaseAdapter {
    private Context mContext;
    private List<ProvinceTable> mList;


    public ProvinceSpinnerAdapter(Context context, List<ProvinceTable> list) {
        this.mContext = context;
        this.mList = list;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
//        if (convertView == null) {
//            viewHolder = new ViewHolder();
//            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_text, null);
//            viewHolder.textView = (TextView) convertView.findViewById(R.id.item_text);
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ViewHolder) convertView.getTag();
//        }
   //     viewHolder.textView.setText(mList.get(position).province_name);
        return convertView;
    }

    private class ViewHolder {
        TextView textView;
    }
}
