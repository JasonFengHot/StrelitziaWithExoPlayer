package tv.ismar.homepage.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import tv.ismar.homepage.R;
import tv.ismar.app.entity.ChannelEntity;

/**
 * Created by Beaver on 2016/5/5.
 */
public class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerAdapter.ChannelHolder> {

    private Context mContext;
    private List<ChannelEntity> movieList;
    private LayoutInflater mInflater;
    private OnItemActionListener mOnItemActionListener;
    private int mSelectedPosition = 0;
    private int mLastSelectedPosition = -1;
    private int mOnHoveredPosition = -1;
    private RecyclerView recyclerView;
    public View onHoveredView;

    public ChannelRecyclerAdapter(Context context, List<ChannelEntity> movieList, RecyclerView recyclerView) {
        this.mContext = context;
        this.movieList = movieList;
        this.recyclerView = recyclerView;
        mInflater = LayoutInflater.from(context);

    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setLastSelectedPosition(int position) {
        mLastSelectedPosition = position;
    }

    public int getLastSelectedPosition() {
        return mLastSelectedPosition;
    }

    public void setOnHoveredPosition(int position) {
        mOnHoveredPosition = position;
    }

    public int getOnHoveredPosition() {
        return mOnHoveredPosition;
    }

    @Override
    public ChannelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.item_channel, parent, false);
        ChannelHolder channelHolder = new ChannelHolder(v);
        return channelHolder;
    }

    @Override
    public void onBindViewHolder(final ChannelHolder holder, int position) {
        ChannelEntity channelEntity = movieList.get(position);
        holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
        holder.channel_item_text.setText(channelEntity.getName());

        holder.itemView.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
//                Log.i("LH/","itemView onHover");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                    case MotionEvent.ACTION_HOVER_MOVE:
                        recyclerView.setHovered(true);
                        recyclerView.requestFocus();
                        onHoveredView = v;
                        mOnHoveredPosition = holder.getAdapterPosition();
                        if (mSelectedPosition == holder.getAdapterPosition()) {
                            holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                            holder.channel_item_text.setTextColor(mContext.getResources().getColor(R.color._ffffff));
                        } else {
                            holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_onhover);
                            holder.channel_item_text.setTextColor(mContext.getResources().getColor(R.color._ff9c3c));

                            View viewItem = recyclerView.getLayoutManager().findViewByPosition(mSelectedPosition);
                            if (viewItem != null) {
                                LinearLayout channel_item_back = (LinearLayout) viewItem.findViewById(R.id.channel_item_back);
                                channel_item_back.setBackgroundResource(R.drawable.channel_item_focus);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        mOnHoveredPosition = -1;
                        if (mSelectedPosition == holder.getAdapterPosition()) {
                            holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_focus);
                        } else {
                            holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
                        }
                        holder.channel_item_text.setTextColor(mContext.getResources().getColor(R.color._ffffff));
                        break;
                }
                if (mOnItemActionListener != null) {
                    mOnItemActionListener.onItemHoverListener(v, event, holder.getAdapterPosition());
                }
                return false;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPosition != holder.getAdapterPosition()) {
                    mLastSelectedPosition = mSelectedPosition;
                    mSelectedPosition = holder.getAdapterPosition();

                    // 先改变上次位置背景颜色
                    View viewItem = recyclerView.getLayoutManager().findViewByPosition(mLastSelectedPosition);
                    if (viewItem != null) {
                        LinearLayout channel_item_back = (LinearLayout) viewItem.findViewById(R.id.channel_item_back);
                        channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
                    }

                    holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
                    holder.channel_item_text.setTextColor(mContext.getResources().getColor(R.color._ffffff));
                    if (mOnItemActionListener != null) {
                        mOnItemActionListener.onItemClickListener(v, holder.getAdapterPosition());
                    }
                }
            }
        });

    }

    public void changeStatus() {
        // 先改变上次位置背景颜色
        if (mLastSelectedPosition >= 0) {
            View lastItem = recyclerView.getLayoutManager().findViewByPosition(mLastSelectedPosition);
            if (lastItem != null) {
                LinearLayout channel_item_back = (LinearLayout) lastItem.findViewById(R.id.channel_item_back);
                channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
            }
        }

        View currentItem = recyclerView.getLayoutManager().findViewByPosition(mSelectedPosition);
        if (currentItem != null) {
            LinearLayout channel_item_back = (LinearLayout) currentItem.findViewById(R.id.channel_item_back);
            channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
        }

    }

    public void setOnItemActionListener(OnItemActionListener onItemActionListener) {
        this.mOnItemActionListener = onItemActionListener;
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    @Override
    public void onViewAttachedToWindow(ChannelHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder != null){
            holder.channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
        }

        View currentItem = recyclerView.getLayoutManager().findViewByPosition(mSelectedPosition);
        if (currentItem != null) {
            LinearLayout channel_item_back = (LinearLayout) currentItem.findViewById(R.id.channel_item_back);
            channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
        }
        recyclerView.requestFocus();

    }

    public void arrowScroll(int direction, boolean isFromBoard) {
        mLastSelectedPosition = mSelectedPosition;
        switch (direction) {
            case View.FOCUS_LEFT:
                if (mSelectedPosition > 0) {
                    mSelectedPosition -= 1;
                }
                break;
            case View.FOCUS_RIGHT:
                if (mSelectedPosition < getItemCount() - 1) {
                    mSelectedPosition += 1;
                }
                break;
        }
        // 先改变上次位置背景颜色
        View viewItem = recyclerView.getLayoutManager().findViewByPosition(mLastSelectedPosition);
        if (viewItem != null) {
            LinearLayout channel_item_back = (LinearLayout) viewItem.findViewById(R.id.channel_item_back);
            channel_item_back.setBackgroundResource(R.drawable.channel_item_normal);
        }

        if (isFromBoard) {
            View currentItem = recyclerView.getLayoutManager().findViewByPosition(mSelectedPosition);
            if (currentItem != null) {
                LinearLayout channel_item_back = (LinearLayout) currentItem.findViewById(R.id.channel_item_back);
                channel_item_back.setBackgroundResource(R.drawable.channel_item_focus);
            }
            if (mOnItemActionListener != null) {
                mOnItemActionListener.onItemSelectedListener(mSelectedPosition);
            }
        } else {
            View currentItem = recyclerView.getLayoutManager().findViewByPosition(mSelectedPosition);
            if (currentItem != null) {
                LinearLayout channel_item_back = (LinearLayout) currentItem.findViewById(R.id.channel_item_back);
                channel_item_back.setBackgroundResource(R.drawable.channel_item_selectd_focus);
            }
//            recyclerView.requestFocus();
            if (mOnItemActionListener != null) {
                mOnItemActionListener.onItemSelectedListener(mSelectedPosition);
            }
        }
    }

    public class ChannelHolder extends RecyclerView.ViewHolder {

        public LinearLayout channel_item_back;
        public TextView channel_item_text;

        public ChannelHolder(View itemView) {
            super(itemView);
            channel_item_back = (LinearLayout) itemView.findViewById(R.id.channel_item_back);
            channel_item_text = (TextView) itemView.findViewById(R.id.channel_item);
        }
    }

}
