package tv.ismar.app.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/** Created by Beaver on 2016/5/5. */
public class HorizontalSpacesItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public HorizontalSpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(
            Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left = 0;
        } else {
            outRect.left = space;
        }
        outRect.right = 0;
        outRect.top = 0;
        outRect.bottom = 0;
    }
}
