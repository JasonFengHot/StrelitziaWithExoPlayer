package cn.ismartv.helperpage.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import cn.ismartv.helperpage.R;
import cn.ismartv.helperpage.ui.widget.SakuraProgressBar;
import cn.ismartv.injectdb.library.query.Select;
import tv.ismar.app.db.location.CdnTable;
import tv.ismar.app.db.location.IspTable;


/**
 * Created by huaijie on 14-10-31.
 */
public class NodeListAdapter extends CursorAdapter {

    public NodeListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public NodeListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View mView = LayoutInflater.from(context).inflate(R.layout.sakura_item_node_list, null);

        return mView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor.getCount() != 0) {

            TextView nodeNmae = (TextView) view.findViewById(R.id.node_name);
            TextView titleNumber = (TextView) view.findViewById(R.id.title_number);
            TextView message = (TextView) view.findViewById(R.id.select_prompt);
            SakuraProgressBar speedProgress = (SakuraProgressBar) view.findViewById(R.id.speed_progress);
            titleNumber.setText(String.valueOf(cursor.getPosition() + 1));
            String node = cursor.getString(cursor.getColumnIndex(CdnTable.CDN_NICK));
            int progress = cursor.getInt(cursor.getColumnIndex(CdnTable.SPEED));
            String ispId = cursor.getString(cursor.getColumnIndex(CdnTable.ISP_ID));
            IspTable ispTable = new Select().from(IspTable.class).where(IspTable.ISP_ID + " = ?", ispId).executeSingle();
            speedProgress.setProgress((int) (progress / 20.84));
            if ((progress / 20.84) < 60 || ispTable.isp_name.equals("其它"))
                message.setText(R.string.tring);
            else
                message.setText(R.string.can_select);
            nodeNmae.setText(node);
            view.setTag((cursor.getInt(cursor.getColumnIndex(CdnTable.CDN_ID))));
        }
    }
}
