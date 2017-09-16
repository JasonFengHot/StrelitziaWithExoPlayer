package tv.ismar.homepage.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/** Created by Beaver on 2016/5/5. */
public class HorizontalSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;
    private int padding;
    private ChannelRecyclerAdapter mAdapter;

    public HorizontalSpacesItemDecoration(int space, int padding, ChannelRecyclerAdapter adapter) {
        this.space = space;
        this.padding = padding;
        this.mAdapter = adapter;
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = padding;
        } else {
            outRect.left = space;
        }
        if (parent.getChildAdapterPosition(view) == mAdapter.getItemCount() - 1) {
            outRect.right = padding;
        } else {
            outRect.right = 0;
        }
        outRect.top = 0;
        outRect.bottom = 0;
    }
}
