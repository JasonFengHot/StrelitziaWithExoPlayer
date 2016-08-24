package tv.ismar.app.widget;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by beaver on 16-8-23.
 */
public class LabelImageAdapter extends RecyclerView.Adapter<LabelImageAdapter.ViewHolder> {


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout channel_item_back;
        public TextView channel_item_text;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
