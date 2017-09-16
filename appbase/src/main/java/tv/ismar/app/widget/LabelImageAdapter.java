package tv.ismar.app.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import tv.ismar.app.R;
import tv.ismar.app.network.entity.ItemEntity;

/** Created by beaver on 16-8-23. */
public class LabelImageAdapter extends RecyclerView.Adapter<LabelImageAdapter.ViewHolder> {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private LayoutInflater mInflater;
    private List<ItemEntity> itemEntityList;

    private int mSelectedPosition;
    private OnItemActionListener mOnItemActionListener;

    public LabelImageAdapter(
            Context context, RecyclerView recyclerView, List<ItemEntity> itemList) {
        mContext = context;
        mRecyclerView = recyclerView;
        itemEntityList = itemList;
        mInflater = LayoutInflater.from(context);
    }

    public void setOnItemActionListener(OnItemActionListener onItemActionListener) {
        this.mOnItemActionListener = onItemActionListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.detail_label_image_portrait, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnItemActionListener != null) {
                            mOnItemActionListener.onItemClickListener(
                                    v, holder.getAdapterPosition());
                        }
                    }
                });

        holder.itemView.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {}
                });
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public LabelImageView detail_labelImage;
        public TextView detail_labelText;

        public ViewHolder(View itemView) {
            super(itemView);
            detail_labelImage = (LabelImageView) itemView.findViewById(R.id.detail_labelImage);
            detail_labelText = (TextView) itemView.findViewById(R.id.detail_labelText);
        }
    }
}
